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
        VERSION = "1.2.3-DEVOXXBE"
    }

    stages {
        stage('Build flight-service') {
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
        stage('Build docker image') {
            steps {
                sh """
                    docker build -t ${IMAGE} --build-arg JAR_FILE=target/${IMAGE}-${VERSION}.jar .
                    docker tag ${IMAGE} ${GCR_HOSTNAME}/${GCR_PROJECT}/${IMAGE}:${VERSION}
                    docker tag ${GCR_HOSTNAME}/${GCR_PROJECT}/${IMAGE}:${VERSION} ${GCR_HOSTNAME}/${GCR_PROJECT}/${IMAGE}
                """
            }
        }
        stage('Test image') {
            steps {
                    script {
                            docker.image("${IMAGE}").withRun() { c ->
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
        stage('Push to GCR') {
            steps {
                withDockerRegistry([ url: "https://${GCR_HOSTNAME}", credentialsId: "gcr:${GCR_PROJECT}"]) {
                    sh """
                        docker push ${GCR_HOSTNAME}/${GCR_PROJECT}/${IMAGE}:${VERSION}
                        docker push ${GCR_HOSTNAME}/${GCR_PROJECT}/${IMAGE}
                    """
                }
            }
        }
        stage('Deploy with Cloud Run') {
            steps {
                withCredentials([file(credentialsId: 'GCP_KEY_SA', variable: 'GC_KEY')]) {
                    sh "gcloud auth activate-service-account --key-file=${GC_KEY}"
                }
            }
        }
    }

    post {
        always {
            junit '**/newman/report.xml'
        }
    }
}