pipeline {
    agent any
      tools {
        jdk 'JDK25'
      }
    // JDK wird über die Systemumgebung verwendet

    environment {
        // Umgebungsvariablen für das Projekt
        KOTLIN_VERSION = '2.2.20'
        GRADLE_USER_HOME = "${env.WORKSPACE}/.gradle"
    }
        tools {
            // Java 21 Tool (Name anpassen an Ihre Jenkins Tool-Konfiguration)
            jdk 'JDK21'
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

        stage('Build') {
            steps {
                // Haupt-Build-Prozess
                sh './gradlew build -x test'
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

                stage('Publish to Nexus') {
                    steps {
                        script {
                            // Artefakte in Nexus veröffentlichen
                            sh './gradlew publish'
                        }
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
            script {
                emailext(
                    to: 'momasoft.notification@gmail.com',
                    subject: "[Jenkins] ${env.BRANCH_NAME} branch Build Failure: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    body: """<p><b>Build Failure</b></p>
                            <p>Job: ${env.JOB_NAME}<br>
                            Build Number: ${env.BUILD_NUMBER}<br>
                            Branch: ${env.BRANCH_NAME}<br>
                            Status: FAILURE</p>
                            <p>Console Output: <a href="${env.BUILD_URL}console">${env.BUILD_URL}console</a></p>
                            <p>Please check the Jenkins console for details and take appropriate action.</p>""",
                    mimeType: 'text/html'
                )
            }

            // Optional: E-Mail- oder Slack-Benachrichtigung
            // mail to: 'team@example.com', subject: 'Build fehlgeschlagen', body: 'Der Build ist fehlgeschlagen. Bitte überprüfen Sie die Logs.'
        }
    }
}
