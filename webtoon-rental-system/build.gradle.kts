plugins {
    application
    java
}

group = "com.webtoon"
version = "1.0.0"

// ========================================
// Java Toolchain 설정 (최신 권장 방식)
// ========================================
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Gson for JSON serialization
    implementation("com.google.code.gson:gson:2.10.1")

    // JUnit 5 for testing
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// ========================================
// Application 설정
// ========================================
application {
    mainClass = "com.webtoon.cli.Main"
}

// ========================================
// 테스트 설정
// ========================================
tasks.named<Test>("test") {
    useJUnitPlatform()

    // 테스트 로깅 설정
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = false
    }
}

// ========================================
// JAR 빌드 설정 (Fat JAR)
// ========================================
tasks.named<Jar>("jar") {
    manifest {
        attributes(
            "Main-Class" to "com.webtoon.cli.Main",
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version
        )
    }

    // 의존성 포함 (Fat JAR)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map {
        if (it.isDirectory) it else zipTree(it)
    })
}

// ========================================
// 커스텀 태스크
// ========================================
tasks.register<JavaExec>("runApp") {
    group = "application"
    description = "Run the application"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = application.mainClass
}

// 리소스 디렉토리 생성
tasks.register("createDataDir") {
    group = "setup"
    description = "Create data directory for JSON files"
    doLast {
        val dataDir = file("src/main/resources/data")
        if (!dataDir.exists()) {
            dataDir.mkdirs()
            logger.lifecycle("Created directory: $dataDir")
        }
    }
}

// 빌드 시 자동으로 data 디렉토리 생성
tasks.named("processResources") {
    dependsOn("createDataDir")
}