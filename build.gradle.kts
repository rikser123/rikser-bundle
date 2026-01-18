import org.gradle.api.publish.PublishingExtension

plugins {
	`java-library`
	id("org.springframework.boot") version "3.5.5"
	id("io.spring.dependency-management") version "1.1.7"
	`maven-publish`
}

configure<PublishingExtension> {
	publications {
		register<MavenPublication>("gpr") {
			from(components["java"])
		}
	}
	repositories {
		maven {
			name = "GitHubPackages"
			url = uri("https://maven.pkg.github.com/rikser123/rikser-bundle")
			credentials {
				username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
				password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
			}
		}
	}
}

group = "rikser123"
version = "0.0.20"
description = "Bundle"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

val mockitoAgent = configurations.create("mockitoAgent")

dependencies {
	api("org.springframework.boot:spring-boot-starter-data-jpa")
	api("org.springframework.boot:spring-boot-starter-data-rest")
	api("org.springframework.boot:spring-boot-starter-web")
	api("org.springframework.boot:spring-boot-starter-validation:3.5.6")
	api("org.mapstruct:mapstruct:1.5.5.Final")
	api("org.liquibase:liquibase-core")
	api("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")
	api("io.jsonwebtoken:jjwt:0.13.0")
	api("org.springframework.boot:spring-boot-starter-security")
	api("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")

	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("org.postgresql:postgresql")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	mockitoAgent("org.mockito:mockito-core") { isTransitive = false }
	annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
	testCompileOnly("org.projectlombok:lombok:1.18.30")
	testAnnotationProcessor("org.projectlombok:lombok:1.18.30")
}

tasks.withType<Test> {
	useJUnitPlatform()
	jvmArgs("-javaagent:${mockitoAgent.asPath}")
}


