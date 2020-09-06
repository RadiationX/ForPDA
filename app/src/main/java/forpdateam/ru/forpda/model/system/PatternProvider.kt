package forpdateam.ru.forpda.model.system

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import forpdateam.ru.forpda.model.data.storage.IPatternProvider
import org.json.JSONArray
import org.json.JSONObject
import java.util.regex.Pattern

class PatternProvider(
        private val context: Context,
        private val sharedPreferences: SharedPreferences
) : IPatternProvider {

    companion object {
        private const val KEY_PATTERNS = "regex_patterns"
    }

    private val patternSources = mutableMapOf<String, MutableMap<String, String>>()
    private val patterns = mutableMapOf<String, MutableMap<String, Pattern>>()

    private var currentVersion = -1

    override fun getCurrentVersion(): Int {
        if (patternSources.isEmpty()) {
            init()
        }
        return currentVersion
    }

    private fun init() {
        val time = System.currentTimeMillis()

        val assetsData = parse(getAssetsPatterns())
        val savedData = sharedPreferences.getString(KEY_PATTERNS, null)?.let {
            parse(it)
        } ?: assetsData

        Log.e("PatternProvider", "versions: assets=${assetsData.first}, saved=${savedData.first}")


        arrayOf(assetsData, savedData).maxBy { it.first }?.let {
            update(it.first, it.second)
        }
        Log.e("PatternProvider", "update time: ${System.currentTimeMillis() - time}, ${Thread.currentThread()}")
    }

    override fun getPattern(scope: String, key: String): Pattern = patternSources
            .also {
                if (it.isEmpty()) {
                    init()
                }
            }[scope]
            ?.get(key)
            ?.let { source ->
                patterns[scope]?.get(key) ?: Pattern.compile(source).also {
                    patterns[scope]?.put(key, it)
                }
            }
            ?: throw Exception("Not found pattern by: s=$scope, k=$key")

    override fun update(jsonString: String) {
        val parsed = parse(jsonString)
        Log.e("kokos", "parsed: ${parsed.first}, ${parsed.second.size}")
        update(parsed.first, parsed.second)
    }

    override fun update(version: Int, newData: Map<String, MutableMap<String, String>>) {
        Log.e("PatternProvider", "version $version, nds=${newData.size}")
        setLocalData(newData)
        save(version, patternSources)
    }

    override fun parse(jsonString: String): Pair<Int, Map<String, MutableMap<String, String>>> {
        val time = System.currentTimeMillis()
        val result = mutableMapOf<String, MutableMap<String, String>>()
        val patternsJson = JSONObject(jsonString)

        val version = patternsJson.getInt("version")

        val scopesArray = patternsJson.getJSONArray("scopes")
        for (i in 0 until scopesArray.length()) {
            val scopeItem = scopesArray.getJSONObject(i)
            val scopeName = scopeItem.getString("scope")
            val patternsMap = mutableMapOf<String, String>()
            val patternsArray = scopeItem.getJSONArray("patterns")
            for (j in 0 until patternsArray.length()) {
                val patternItem = patternsArray.getJSONObject(j)
                val patternKey = patternItem.getString("key")
                val patternValue = patternItem.getString("value")
                patternsMap[patternKey] = patternValue
            }
            result[scopeName] = patternsMap
        }

        Log.e("PatternProvider", "JSON parse time: ${System.currentTimeMillis() - time}")
        return Pair(version, result)
    }

    private fun setLocalData(newData: Map<String, MutableMap<String, String>>) {
        newData.forEach { scopeItem ->
            Log.e("kokos", "contains scope: ${!patternSources.contains(scopeItem.key)}, ${!patterns.contains(scopeItem.key)}, ${scopeItem.key}")
            if (!patternSources.contains(scopeItem.key)) {
                patternSources[scopeItem.key] = mutableMapOf()
            }
            if (!patterns.contains(scopeItem.key)) {
                patterns[scopeItem.key] = mutableMapOf()
            }
            scopeItem.value.forEach { patternItem ->
                Log.e("kokos", "check scope pattern: ${scopeItem.key}.${patternItem.key} (${patternSources[scopeItem.key]?.get(patternItem.key)?.length} vs ${patternItem.value.length} -> ${patternSources[scopeItem.key]?.get(patternItem.key) != patternItem.value})")
                if (patternSources[scopeItem.key]?.get(patternItem.key) != patternItem.value) {
                    patterns[scopeItem.key]?.remove(patternItem.key)
                    patternSources[scopeItem.key]?.remove(patternItem.key)
                    patternSources[scopeItem.key]?.put(patternItem.key, patternItem.value)
                }
            }
        }
    }

    private fun getAssetsPatterns(): String = context
            .assets
            .open("patterns.json")
            .bufferedReader()
            .use {
                it.readText()
            }

    private fun save(version: Int, source: Map<String, MutableMap<String, String>>) {
        val patternsJson = JSONObject()
        val scopesArray = JSONArray()
        source.forEach { scopeItem ->
            val jsonScopeItem = JSONObject()
            val jsonPatternsArray = JSONArray()
            scopeItem.value.forEach { patternItem ->
                jsonPatternsArray.put(JSONObject().apply {
                    put("key", patternItem.key)
                    put("value", patternItem.value)
                })
            }
            jsonScopeItem.put("scope", scopeItem.key)
            jsonScopeItem.put("patterns", jsonPatternsArray)
            scopesArray.put(jsonScopeItem)
        }

        patternsJson.put("version", version)
        patternsJson.put("scopes", scopesArray)
        val jsonString = patternsJson.toString()
        Log.e("PatternProvider", "saved length : ${jsonString.length}")
        sharedPreferences.edit().putString(KEY_PATTERNS, jsonString).apply()
        currentVersion = version
    }

}