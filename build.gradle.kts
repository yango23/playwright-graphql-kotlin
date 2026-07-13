plugins {
    // Подключаем Kotlin
    kotlin("jvm") version "1.9.20"
}

repositories {
    mavenCentral()
}

dependencies {
    // Playwright для работы с API и UI
    implementation("com.microsoft.playwright:playwright:1.44.0")
    
    // Логирование
    implementation("org.slf4j:slf4j-simple:2.0.9")
    
    // JUnit 5 для запуска тестов
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")
}

// Вот этот блок принудительно создаст задачу 'test' в Gradle:
tasks.test {
    useJUnitPlatform()
}