package com.gridlab.app.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.gridlab.app.core.preferences.AppLanguage

/** Applies or clears the per-app locale via AppCompat. */
object AppLocale {
    fun apply(language: AppLanguage?) {
        val locales = if (language == null) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(language.tag)
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }

    /** Effective app language from AppCompat locales, if one of the supported tags. */
    fun currentAppLanguageOrNull(): AppLanguage? {
        val tags = AppCompatDelegate.getApplicationLocales()
        if (tags.isEmpty) return null
        return AppLanguage.fromTag(tags[0]?.language)
    }
}
