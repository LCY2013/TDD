plugins {
    id("java")
}

group = "org.fufeng.tdd"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("jakarta.inject:jakarta.inject-api:2.0.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.3")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.3")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.9.3")
    testRuntimeOnly("org.junit.platform:junit-platform-runner:1.9.3")
    testImplementation("org.mockito:mockito-core:5.3.1")
    testImplementation("jakarta.inject:jakarta.inject-tck:2.0.1")
}

tasks.withType<Test>() {
    useJUnitPlatform()
}

tasks.test {
    useJUnitPlatform()
}
