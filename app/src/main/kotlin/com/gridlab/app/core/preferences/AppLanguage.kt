package com.gridlab.app.core.preferences

/**
 * In-app UI language. Only English and Spanish for the first release.
 */
enum class AppLanguage(val tag: String) {
    English("en"),
    Spanish("es"),
    ;

    companion object {
        fun fromTag(tag: String?): AppLanguage? = when (tag?.lowercase()) {
            English.tag -> English
            Spanish.tag -> Spanish
            else -> null
        }
    }
}
