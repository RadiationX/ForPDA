package forpdateam.ru.forpda.common

import forpdateam.ru.forpda.BuildConfig

object AppBuildConfig {
    val applicationId: String = BuildConfig.APPLICATION_ID
    val debug: Boolean = BuildConfig.DEBUG
    val buildType: String = BuildConfig.BUILD_TYPE
    val flavor: String = BuildConfig.FLAVOR
    val versionCode: Int = BuildConfig.VERSION_CODE
    val versionName: String = BuildConfig.VERSION_NAME
    val buildDate: String = BuildConfig.BUILD_DATE
}