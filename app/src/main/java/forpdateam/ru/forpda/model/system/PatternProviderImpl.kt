package forpdateam.ru.forpda.model.system

import android.os.SystemClock
import android.util.Log
import forpdateam.ru.forpda.entity.remote.checker.PatternsDataJson
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.PatternsStorage
import forpdateam.ru.forpda.model.data.remote.api.patterns.PatternsApi
import forpdateam.ru.forpda.model.data.storage.PatternProvider
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.radiationx.regexparser.RegexParser
import ru.radiationx.regexparser.core.RegexContext
import ru.radiationx.regexparser.extensions.toRegexParser
import java.util.regex.Pattern
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.update
import kotlin.time.Duration.Companion.minutes

class PatternProviderImpl(
    private val patternsApi: PatternsApi,
    private val patternsStorage: PatternsStorage
) : PatternProvider {

    companion object {
        private const val TAG = "PatternProviderImpl"
        private val RELOAD_TIME = 5.minutes
    }

    // for debug
    private val loadSources = setOf(LoadSource.Assets, LoadSource.Remote, LoadSource.Storage)

    private val hasNewVersion = AtomicBoolean(false)
    private val remoteFailureAt = AtomicLong(-1)

    private val loadMutex = Mutex()
    private val version = AtomicInt(-1)
    private val patternsValueMap = AtomicReference<Map<PatternKey, String>>(emptyMap())
    private val patternsMap = AtomicReference<Map<PatternKey, Pattern>>(emptyMap())

    override fun getVersion(): Int {
        init()
        return version.load()
    }

    override fun setNeedsUpdate() {
        hasNewVersion.store(true)
    }

    override fun getPattern(scope: String, key: String): Pattern {
        init()
        val key = PatternKey(scope, key)
        val patternValue = patternsValueMap.load()[key] ?: throw Exception("Not found pattern value by: s=$scope, k=$key")
        patternsMap.update { patterns ->
            val pattern = patterns[key]
            if (pattern != null) return@update patterns
            patterns + Pair(key, Pattern.compile(patternValue))
        }
        return patternsMap.load()[key] ?: throw Exception("Not found pattern by: s=$scope, k=$key")
    }

    override fun getRegexParser(scope: String, key: String): RegexParser {
        return getPattern(scope, key).toRegexParser(PatternKey(scope, key))
    }

    private fun init() {
        runBlocking {
            loadMutex.withLock {
                if (!isNeedsReinit()) {
                    return@withLock
                }

                val results = mutableListOf<PatternsDataJson>()

                if (LoadSource.Remote in loadSources) {
                    coRunCatching {
                        patternsApi.loadPatterns()
                    }.onSuccess {
                        onRemoteSuccess()
                        results.add(it)
                    }.onFailure {
                        onRemoteFailure()
                        Log.e(TAG, "load remote patterns", it)
                    }
                }

                if (LoadSource.Assets in loadSources) {
                    results.add(patternsStorage.getFromAssets())
                }

                if (LoadSource.Storage in loadSources) {
                    patternsStorage.get()?.also {
                        results.add(it)
                    }
                }

                val result = results
                    .maxByOrNull { it.version }
                    ?: throw Exception("Not found any patterns in $loadSources")

                patternsStorage.set(result)

                val resultMap = mutableMapOf<PatternKey, String>()
                result.scopes.forEach { scope ->
                    scope.patterns.forEach { pattern ->
                        resultMap[PatternKey(scope.name, pattern.key)] = pattern.value
                    }
                }
                version.store(result.version)
                patternsValueMap.store(resultMap)
                hasNewVersion.store(false)
            }
        }
    }

    private fun isNeedsReinit(): Boolean {
        if (hasNewVersion.load()) {
            return true
        }
        if (remoteFailureAt.load().let { it != -1L && (SystemClock.elapsedRealtime() - it) > RELOAD_TIME.inWholeMilliseconds }) {
            return true
        }
        if (patternsValueMap.load().isEmpty()) {
            return true
        }
        return false
    }

    private fun onRemoteSuccess() {
        remoteFailureAt.store(-1L)
    }

    private fun onRemoteFailure() {
        remoteFailureAt.update {
            if (it != -1L) {
                it
            } else {
                SystemClock.elapsedRealtime()
            }
        }
    }

    private data class PatternKey(
        val scope: String,
        val key: String,
    ) : RegexContext {
        override val message: String
            get() = "$scope:$key"
    }

    private enum class LoadSource {
        Assets,
        Remote,
        Storage
    }
}