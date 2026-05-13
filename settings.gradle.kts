rootProject.name = "Calculator5"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupByRegex(".*android.*")
                includeGroupByRegex(".*google.*")
                includeGroupByRegex(".*androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupByRegex(".*android.*")
                includeGroupByRegex(".*google.*")
                includeGroupByRegex(".*androidx.*")
            }
        }
        mavenCentral()
    }
}

include(":composeApp")
