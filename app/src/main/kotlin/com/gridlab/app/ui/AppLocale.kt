package com.gridlab.app.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.gridlab.app.core.preferences.AppLanguage

/** Applies the per-app locale via AppCompat (requires AppCompatActivity + AppCompat theme). */
object AppLocale {
    fun apply(language: AppLanguage?) {
        val locales = if (language == null) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(language.tag)
        }
        val current = AppCompatDelegate.getApplicationLocales()
        if (current.toLanguageTags() == locales.toLanguageTags()) {
            return
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }
}
