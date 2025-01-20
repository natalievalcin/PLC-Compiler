plugins {
    id("java")
    id("application")
    id("org.openjfx.javafxplugin") version "0.0.13" // Add the JavaFX plugin here
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    implementation("org.openjfx:javafx-controls:21.0.5")
    implementation("org.openjfx:javafx-fxml:21.0.5")

}

javafx {
    version = "21.0.5"
    modules = listOf("javafx.controls", "javafx.fxml")
}

application {
    mainClass.set("plc.project.Generator") // Replace with your main class
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.named<JavaExec>("run") {
    jvmArgs = listOf(
        "--module-path", "/Users/natalievalcin/Downloads/javafx-sdk-21.0.5/lib",
        "--add-modules", "javafx.controls,javafx.fxml"
    )
}