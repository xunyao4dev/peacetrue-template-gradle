plugins {
    id("maven-publish")
    id("signing")
    id("idea")
    id("java-library")
    id("io.spring.dependency-management") version "1.0.6.RELEASE"
//    id("org.springframework.boot") version "2.1.8.RELEASE"
}

allprojects {

    group = "com.github.peacetrue.template"
    version = "1.0-SNAPSHOT"
    description = "gradle模板"

    apply(plugin = "java")
    apply(plugin = "java-library")

    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    apply(plugin = "idea")
    idea {
        module {
            inheritOutputDirs = false
            outputDir = tasks.compileJava.get().destinationDir
            testOutputDir = tasks.compileTestJava.get().destinationDir
            isDownloadSources = true
            isDownloadJavadoc = false
        }
    }

    repositories {
        mavenCentral()
        jcenter()
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
    }

    apply(plugin = "io.spring.dependency-management")
    dependencyManagement {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:2.1.8.RELEASE")
            mavenBom("com.github.peacetrue:peacetrue-dependencies:1.0.4-SNAPSHOT")
        }
    }

    dependencies {
        compileOnly("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")
        implementation("com.google.code.findbugs:jsr305")

        testImplementation("junit:junit")
        testCompileOnly("org.projectlombok:lombok")
        testAnnotationProcessor("org.projectlombok:lombok")
        testImplementation("ch.qos.logback:logback-classic")
    }



    tasks.register("antoraCopySourceToExample") {
        doLast {
            val targetFolder = "${rootDir.path}/docs/antora/modules/ROOT/examples/${project.name}"
            delete {
                delete(targetFolder)
            }
            copy {
                from(sourceSets.main.get().allJava)
                into(targetFolder)
            }
            copy {
                from(sourceSets.test.get().allJava)
                into(targetFolder)
            }
        }
    }


    if (name.endsWith("sample") || name.endsWith("ui")) return@allprojects

    tasks.register<Jar>("sourcesJar") {
        from(sourceSets.main.get().allJava)
        archiveClassifier.set("sources")
    }

    tasks.register<Jar>("javadocJar") {
        from(tasks.javadoc)
        archiveClassifier.set("javadoc")
    }


    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                artifactId = project.name
                from(components["java"])
                artifact(tasks["sourcesJar"])
                artifact(tasks["javadocJar"])
                versionMapping {
                    usage("java-api") {
                        fromResolutionOf("runtimeClasspath")
                    }
                    usage("java-runtime") {
                        fromResolutionResult()
                    }
                }

                pom {
                    name.set(project.name)
                    description.set(project.description)
                    url.set("https://github.com/peacetrue/${project.name}")
//                    properties.set(mapOf(
//                            "myProp" to "value",
//                            "prop.with.dots" to "anotherValue"
//                    ))
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("xiayouxue")
                            name.set("xiayouxue")
                            email.set("xiayouxue@hotmail.com")
                        }
                    }
                    scm {
                        connection.set("https://github.com/peacetrue/${project.name}.git")
                        developerConnection.set("https://github.com/peacetrue/${project.name}.git")
                        url.set("https://github.com/peacetrue/${project.name}")
                    }
                }
            }
        }

        repositories {
            maven {
                val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
                url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            }
        }
    }

    signing {
        sign(publishing.publications["mavenJava"])
    }

    tasks.javadoc {
        if (JavaVersion.current().isJava9Compatible) {
            (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
        }
    }
}







