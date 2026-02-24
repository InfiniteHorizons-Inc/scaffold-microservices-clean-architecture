plugins {
    idea
    jacoco
    `maven-publish`
    `java-gradle-plugin`
    `java-library`
    alias(libs.plugins.plugin.publish)
    alias(libs.plugins.spotless)
    alias(libs.plugins.dependency.check)
    alias(libs.plugins.nexus.publish)

}

group = "co.infinitehorizons.scaffold.microservices"
version = System.getProperty("version")
description = "Plugin to generate microservice ecosystems with Clean Architecture."

val pluginName = "Infinite Horizons Inc Scaffolding Plugin"
val repoUrl = "https://github.com/InfiniteHorizons-Inc/scaffold-microservices-clean-architecture"
val issueTrackerUrl = "$repoUrl/issues"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

dependencies {
    implementation(gradleApi())

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    // Testing dependencies
    testImplementation(libs.junit.api)
    testImplementation(libs.junit.engine)
    testImplementation(libs.junit.platform.launcher)
}

gradlePlugin {

    website = "https://github.com/InfiniteHorizons-Inc/scaffold-microservices-clean-architecture"
    vcsUrl = "https://github.com/InfiniteHorizons-Inc/scaffold-microservices-clean-architecture"

    plugins {
        register("scaffoldMicroServices") {
            id = "co.infinitehorizons.scaffold.microservices"
            displayName = pluginName
            implementationClass = "co.infinitehorizons.scaffold.microservices.ScaffoldMicroservicesPlugin"
            description = project.description
            tags.addAll("scaffold", "microservices", "clean architecture", "gradle", "plugin")
        }
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
}

publishing {
    publications {
        afterEvaluate {
            named<MavenPublication>("pluginMaven") {
                artifactId = base.archivesName.get()
                pom {
                    name = pluginName
                    description = project.description
                    inceptionYear = "2025"
                    url = repoUrl
                    developers {
                        developer {
                            name = "Maicol Montoya"
                            id = "Maicol-19ty"
                            email = "maicolmontoya323@gmail.com"
                            url = "https://github.com/Maicol-19ty"
                            organization = "Infinite Horizons Inc."
                            organizationUrl = "https://github.com/InfiniteHorizons-Inc"
                            roles = listOf("Author", "Maintainer")
                        }
                    }
                    licenses {
                        license {
                            name = "The Apache License, Version 2.0"
                            url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                            distribution = "repo"
                        }
                    }
                }
            }
        }
    }
}