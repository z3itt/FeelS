package com.feels.ui.theme

import android.content.Context

fun applyThemeMode(context: Context, isDarkTheme: Boolean) {
    ThemeStartupStore.write(context, isDarkTheme)
    ThemeStartupStore.applyToDelegates(isDarkTheme)
}
