plugins {
    id("java-library")
    id("org.allaymc.gradle.plugin") version "0.2.1"
}

group = "org.nivaris"
description = "Party plugin for AllayMC"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    flatDir {
        dirs("../proxythread-allay/build/libs")
    }
}

allay {
    api = "0.28.0"

    plugin {
        entrance = ".party.Main"
        authors += "NivarisMC"
        website = "https://github.com/nivarismc"
    }
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
    compileOnly(files("../proxythread-allay/build/libs/proxythread-allay-1.0.0.jar"))
}
