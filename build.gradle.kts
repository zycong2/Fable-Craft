plugins {
  `java-library`
  kotlin("jvm") version "1.9.23"
  id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
  id("xyz.jpenilla.run-paper") version "2.3.1"
  id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "io.RPGCraft.FableCraft"
version = "1.3-FINAL"
description = "RPG inspired plugin."

java {
  toolchain.languageVersion = JavaLanguageVersion.of(21)
}

repositories {
  mavenCentral()
  maven("https://maven.citizensnpcs.co/repo")
  maven("https://repo.dmulloy2.net/repository/public/")
  maven("https://repo.skriptlang.org/releases")
  maven("https://repo.extendedclip.com/releases/")
  maven("https://jitpack.io")
}

dependencies {

  paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")
  implementation("net.bytebuddy:byte-buddy:1.14.9")
  implementation("org.javassist:javassist:3.29.2-GA")
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
  }
}

tasks {
  shadowJar {
    archiveClassifier.set("")
  }
  build {
    dependsOn(shadowJar)
  }
  javadoc {
    options.encoding = Charsets.UTF_8.name()
  }
}
