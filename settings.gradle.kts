pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "DeepReps"

include(":app")

include(":feature:workout")
include(":feature:exercise-library")
include(":feature:progress")
include(":feature:profile")
include(":feature:ai-plan")
include(":feature:templates")
include(":feature:onboarding")

include(":core:domain")
include(":core:data")
include(":core:database")
include(":core:network")
include(":core:ui")
include(":core:common")

include(":benchmark")
