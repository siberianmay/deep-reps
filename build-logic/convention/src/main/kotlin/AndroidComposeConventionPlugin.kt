import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            val composeBom = dependencies.platform(
                libs.findLibrary("compose-bom").get()
            )

            dependencies {
                add("implementation", composeBom)
                add("implementation", libs.findBundle("compose").get())
                add("implementation", libs.findLibrary("activity-compose").get())
                add("debugImplementation", libs.findBundle("compose-debug").get())
                add("androidTestImplementation", composeBom)
                add("androidTestImplementation", libs.findLibrary("compose-ui-test").get())
            }
        }
    }
}

private val Project.libs
    get() = extensions.getByType(
        org.gradle.api.artifacts.VersionCatalogsExtension::class.java
    ).named("libs")
