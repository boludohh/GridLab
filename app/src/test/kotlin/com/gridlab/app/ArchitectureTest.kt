package com.gridlab.app

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

/**
 * Architecture boundary tests.
 *
 * Rules mirror the package layout defined in the project architecture:
 * - feature packages must not depend on other feature packages
 * - core must not depend on feature
 * - production code outside core/ and feature/ is limited (app entry points only)
 */
class ArchitectureTest {

    @Test
    fun `feature packages should not import other feature packages`() {
        Konsist
            .scopeFromProduction()
            .files
            .withPackage("com.gridlab.app.feature..")
            .assertFalse { file ->
                file.imports.any { import ->
                    val importedFeature = import.name
                        .removePrefix("com.gridlab.app.feature.")
                        .substringBefore('.')
                    val currentFeature = file.packagee
                        ?.name
                        ?.removePrefix("com.gridlab.app.feature.")
                        ?.substringBefore('.')
                    importedFeature.isNotEmpty() &&
                        currentFeature?.isNotEmpty() == true &&
                        importedFeature != currentFeature &&
                        import.name.startsWith("com.gridlab.app.feature.")
                }
            }
    }

    @Test
    fun `core packages should not import feature packages`() {
        Konsist
            .scopeFromProduction()
            .files
            .withPackage("com.gridlab.app.core..")
            .assertFalse { file ->
                file.imports.any { import ->
                    import.name.startsWith("com.gridlab.app.feature.")
                }
            }
    }

    @Test
    fun `production classes reside only under allowed packages`() {
        val allowedPrefixes = listOf(
            "com.gridlab.app.core.",
            "com.gridlab.app.feature.",
            "com.gridlab.app.ui.",
            "com.gridlab.app.GridLabApp",
            "com.gridlab.app.MainActivity",
            "com.gridlab.app.DemoViewModel",
            "com.gridlab.app.MessageProvider",
        )

        Konsist
            .scopeFromProduction()
            .classes()
            .assertTrue { clazz ->
                val fqName = clazz.fullyQualifiedName ?: return@assertTrue false
                allowedPrefixes.any { prefix ->
                    fqName == prefix || fqName.startsWith(prefix)
                }
            }
    }
}
