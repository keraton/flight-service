pipeline {

    agent {
        label 'jenkins'
    }

    options {
        timestamps()
    }

    environment {
        GCR_HOSTNAME = 'eu.gcr.io'
        GCR_PROJECT = 'devoxxbe-2019'
        IMAGE = readMavenPom().getArtifactId()
        VERSION = readMavenPom().getVersion()
        COMMITID = sh(returnStdout: true, script: 'git rev-parse --short HEAD')
    }

    stages {
        stage('Build') {
            agent {
                docker {
                    image 'openjdk:8-jdk-alpine'
                    args '-v $HOME/.m2:/root/.m2'
                    reuseNode true
                }
            }
            steps {
                withMaven() {
                    sh "./mvnw versions:set -DnewVersion=${VERSION}"
                    sh './mvnw clean package'
                }
            }
        }
        stage('Package') {
            steps {
                sh """
                    docker build -t ${IMAGE} --build-arg JAR_FILE=target/${IMAGE}-${VERSION}.jar .
                    docker tag ${IMAGE} ${GCR_HOSTNAME}/${GCR_PROJECT}/${IMAGE}:${VERSION}
                    docker tag ${GCR_HOSTNAME}/${GCR_PROJECT}/${IMAGE}:${VERSION} ${GCR_HOSTNAME}/${GCR_PROJECT}/${IMAGE}
                """
            }
        }
        stage('Acceptance') {
            steps {
                    script {
                            docker.image("${IMAGE}").withRun('-e "SPRING_PROFILES_ACTIVE=dev"') { c ->
                                docker.image('node:10.16.3-alpine').inside("--network container:${c.id}") {
                                    dir("acceptance") {
                                        sh 'while ! nc -z localhost 8080 ; do sleep 2 ; done'
                                        sh 'npm ci'
                                        sh 'npm run local'
                                        publishHTML target: [
                                            allowMissing: false,
                                            alwaysLinkToLastBuild: false,
                                            keepAll: true,
                                            reportDir: 'newman',
                                            reportFiles: 'report.html',
                                            reportName: 'HTML Report'
                                        ]
                                    }
                                }
                            }
                    }
            }
        }
        stage('Deploy') {
            steps {
                echo 'deploying application to prod...'
            }
        }
    }

    post {
        always {
            junit '**/newman/report.xml'
        }
    }
}