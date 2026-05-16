package forpdateam.ru.forpda.common.apptheme

import kotlinx.coroutines.flow.Flow

interface AppThemeController {
    fun init()

    fun observeTheme(): Flow<AppTheme>
    fun getTheme(): AppTheme

    fun observeMode(): Flow<AppThemeMode>
    fun getMode(): AppThemeMode
    fun setMode(mode: AppThemeMode)
}