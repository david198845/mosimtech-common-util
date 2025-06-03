pipeline {
    agent any

    // JDK wird über die Systemumgebung verwendet

    environment {
        // Umgebungsvariablen für das Projekt
        KOTLIN_VERSION = '1.9.0'
        GRADLE_USER_HOME = "${env.WORKSPACE}/.gradle"
    }

    stages {
        stage('Checkout') {
            steps {
                // Code aus dem Repository auschecken
                checkout scm

                // Ausführungsrechte für das Gradle-Wrapper-Skript hinzufügen
                sh 'chmod +x ./gradlew'
            }
        }

        stage('Clean') {
            steps {
                // Projektbereinigung
                sh './gradlew clean'
            }
        }

        stage('Compile') {
            steps {
                // Kompilierung des Kotlin-Codes
                sh './gradlew compileKotlin'
            }
        }

        stage('Test') {
            steps {
                // Unit-Tests ausführen
                sh './gradlew test'
            }
            post {
                always {
                    // Testergebnisse veröffentlichen
                    junit '**/build/test-results/test/*.xml'
                }
            }
        }
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('Sonar') {
                    sh "./gradlew sonar"
                }
            }
        }


        stage('Build') {
            steps {
                // Haupt-Build-Prozess
                sh './gradlew build -x test'
            }
        }

        stage('Package') {
            steps {
                // JAR- oder andere Artefakte erstellen
                sh './gradlew jar'

                // Alternativ für eine Anwendung mit Fat-JAR
                // sh './gradlew shadowJar'
            }
            post {
                success {
                    // Build-Artefakte archivieren
                    archiveArtifacts artifacts: '**/build/libs/*.jar', fingerprint: true
                }
            }
        }

        stage('Publish') {
            when {
                branch 'main'  // Nur auf dem Hauptzweig ausführen
            }
            steps {
                // Optional: Veröffentlichung der Artefakte in einem Repository
                sh './gradlew publish'
            }
        }
    }

    post {
        always {
            // Arbeitsbereich bereinigen
            cleanWs()
        }
        success {
            // Benachrichtigung bei Erfolg
            echo 'Build erfolgreich abgeschlossen!'
            // Optional: E-Mail- oder Slack-Benachrichtigung
            // mail to: 'team@example.com', subject: 'Build erfolgreich', body: 'Der Build wurde erfolgreich abgeschlossen.'
        }
        failure {
            // Benachrichtigung bei Fehlern
            echo 'Build fehlgeschlagen!'
            // Optional: E-Mail- oder Slack-Benachrichtigung
            // mail to: 'team@example.com', subject: 'Build fehlgeschlagen', body: 'Der Build ist fehlgeschlagen. Bitte überprüfen Sie die Logs.'
        }
    }
}
