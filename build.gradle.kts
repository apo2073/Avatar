plugins {
    kotlin("jvm") version "2.0.21"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "kr.apo2073"
version = "1"

val relocate = (findProperty("relocate") as? String)?.toBoolean() ?: true

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    implementation("io.github.monun:tap-api:4.9.8")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.github.apo2073:ApoLib:1.0.4")
}

val targetJavaVersion = 17
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.build {
    dependsOn("shadowJar")
}
/*tasks.jar {
    destinationDirectory=file("C:\\Users\\이태수\\Desktop\\server\\plugins")
}*/

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.shadowJar {
    archiveFileName.set("Avatar.jar")
    archiveBaseName.set("Avatar.jar")
    archiveClassifier.set("")
    dependencies {
        include(dependency("com.github.apo2073:ApoLib:1.0.4"))
    }
}