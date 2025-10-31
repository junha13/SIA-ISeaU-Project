pluginManagement {
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
    resolutionStrategy {
        // FCM 플러그인 버전 명시
        eachPlugin {
            if (requested.id.id == "com.google.gms.google-services") {
                useVersion("4.4.0")
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        maven { url = uri("https://maven.google.com") }
        mavenCentral()
    }
}

rootProject.name = "ISeaUApp"
include(":app")
include(":wear")