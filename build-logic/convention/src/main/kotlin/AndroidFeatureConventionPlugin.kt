import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("deepreps.android.library")
                apply("deepreps.android.compose")
                apply("deepreps.android.hilt")
            }

            dependencies {
                add("implementation", project(":core:domain"))
                add("implementation", project(":core:ui"))
                add("implementation", project(":core:common"))

                add("implementation", libs.findLibrary("hilt-navigation-compose").get())
                add("implementation", libs.findLibrary("navigation-compose").get())
                add("implementation", libs.findBundle("lifecycle").get())
                add("implementation", libs.findLibrary("timber").get())

                add("testImplementation", libs.findBundle("testing").get())
                add("testRuntimeOnly", libs.findLibrary("junit5-engine").get())
            }
        }
    }
}

private val Project.libs
    get() = extensions.getByType(
        org.gradle.api.artifacts.VersionCatalogsExtension::class.java
    ).named("libs")
