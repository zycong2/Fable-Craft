plugins {
  `java-library`
  id("io.papermc.paperweight.userdev") version "2.0.0-beta.14"
  id("xyz.jpenilla.run-paper") version "2.3.1"
  id ("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "io.RPGCraft.FableCraft"
version = "1.2"
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
  paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
  implementation("net.kyori:adventure-text-minimessage:4.19.0")
  compileOnly("com.github.SkriptLang:Skript:2.11.1")
  compileOnly("org.projectlombok:lombok:1.18.30")
  compileOnly("me.clip:placeholderapi:2.11.6")
  implementation("com.github.MilkBowl:VaultAPI:1.7") {
    exclude(group = "org.bukkit", module = "bukkit")
  }
  compileOnly("net.luckperms:api:5.4")
  compileOnly("net.citizensnpcs:citizens-main:2.0.38-SNAPSHOT") {
    exclude(group = "*", module = "*")
  }
  compileOnly("com.comphenix.protocol:ProtocolLib:5.3.0")
  annotationProcessor ("org.projectlombok:lombok:1.18.30")

  implementation("org.reflections:reflections:0.10.2")
  implementation("net.bytebuddy:byte-buddy:1.14.9")
  implementation("org.javassist:javassist:3.29.2-GA")
}

tasks {
  compileJava {
    options.release = 21
  }
  javadoc {
    options.encoding = Charsets.UTF_8.name()
  }
}
