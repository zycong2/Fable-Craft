plugins {
  `java-library`
  id("io.papermc.paperweight.userdev") version "2.0.0-beta.14"
  id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "io.RPGCraft.FableCraft"
version = "1.0-SNAPSHOT"
description = "RPG inspired plugin."

java {
  toolchain.languageVersion = JavaLanguageVersion.of(21)
}

repositories {
  mavenCentral()
  maven("https://maven.citizensnpcs.co/repo")
}

dependencies {
  paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
  implementation("net.kyori:adventure-text-minimessage:4.19.0")
  compileOnly("org.projectlombok:lombok:1.18.30")
  compileOnly("net.luckperms:api:5.4")
  compileOnly("net.citizensnpcs:citizens-main:2.0.38-SNAPSHOT") {
    exclude(group = "*", module = "*")
  }
  annotationProcessor ("org.projectlombok:lombok:1.18.30")
}

tasks {
  compileJava {
    options.release = 21
  }
  javadoc {
    options.encoding = Charsets.UTF_8.name()
  }
}
