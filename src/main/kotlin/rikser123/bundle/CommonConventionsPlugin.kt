package rikser123.bundle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.*

class CommonConventionsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.run {
            pluginManager.apply("java")
            pluginManager.apply("checkstyle")
            pluginManager.apply("maven-publish")
            pluginManager.apply("org.springframework.boot")
            pluginManager.apply("io.spring.dependency-management")

            group = "rikser123"

            // Java toolchain
            extensions.configure<JavaPluginExtension> {
                toolchain {
                    languageVersion.set(JavaLanguageVersion.of(21))
                }
            }

            // Репозитории
            repositories {
                mavenCentral()
                gradlePluginPortal()
                maven {
                    name = "GitHubPackagesBundle"
                    url = uri("https://maven.pkg.github.com/rikser123/rikser-bundle")
                    credentials {
                        username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                        password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
                    }
                }
            }

            extensions.configure<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension> {
                imports {
                    mavenBom("org.springframework.cloud:spring-cloud-dependencies:2024.0.1")
                }
            }

            val sourceSets = extensions.getByType(org.gradle.api.tasks.SourceSetContainer::class)
            val integrationTest = sourceSets.create("integrationTest")

            integrationTest.compileClasspath += sourceSets["main"].output + configurations["testRuntimeClasspath"]
            integrationTest.runtimeClasspath += sourceSets["main"].output + integrationTest.compileClasspath

            integrationTest.resources {
                setSrcDirs(listOf("src/integrationTest/resources"))
            }

            val integrationTestImplementation by configurations.getting {
                extendsFrom(configurations["testImplementation"])
                extendsFrom(configurations["implementation"])
            }
            val integrationTestRuntimeOnly by configurations.getting {
                extendsFrom(configurations["testRuntimeOnly"])
                extendsFrom(configurations["runtimeOnly"])
            }

            val mockitoAgent = configurations.create("mockitoAgent")

            dependencies {
                add("implementation", "rikser123:bundle:0.0.97")
            }

            tasks.withType<Test>().configureEach {
                useJUnitPlatform()
                jvmArgs("-javaagent:${mockitoAgent.asPath}")
            }

            tasks.register<Test>("integrationTest") {
                description = "Runs integration tests."
                group = "verification"

                testClassesDirs = integrationTest.output.classesDirs
                classpath = integrationTest.runtimeClasspath

                useJUnitPlatform()

                testLogging {
                    events("passed", "skipped", "failed")
                }
            }
        }
    }
}