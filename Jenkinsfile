pipeline {
    agent any

    environment {
        DOCKER_IMAGE = "hello-world-backend"
        DOCKER_USER = "bhanupdas"
        DOCKER_PWD = "Midtown@12"
        DOCKERBUILD = "${env.BUILD_NUMBER}"
        DOCKERPATH = "bhanupdas/hello-world-backend"
    }
    tools {
                maven 'Maven'
            }

    stages {

        stage('Build') {
        when {
               branch "develop"
        }
            steps {
                sh "mvn clean compile"
            }
        }

        stage('Test') {
        when {
               branch "develop"
        }
            steps {
                sh "mvn test"
            }
        }

        stage('Deploy-Dev') {
        when {
               branch "develop"
        }
            steps {
                script {
                        sh "docker login -u ${DOCKER_USER} -p ${DOCKER_PWD}"
                        sh "docker build -t ${DOCKER_IMAGE}.${DOCKERBUILD} ."
                        sh "docker image tag ${DOCKER_IMAGE}.${DOCKERBUILD} ${DOCKERPATH}:${DOCKER_IMAGE}.${DOCKERBUILD}"
                        sh "docker image push ${DOCKERPATH}:${DOCKER_IMAGE}.${DOCKERBUILD}"
                        sh "docker image pull ${DOCKERPATH}:${DOCKER_IMAGE}.${DOCKERBUILD}"
                        sh "docker image tag ${DOCKER_IMAGE}.${DOCKERBUILD}:latest ${DOCKER_IMAGE}.${DOCKERBUILD}:dev"
                        def networkName = 'dev'
                        def networkExists = sh(script: "docker network inspect $networkName > /dev/null 2>&1", returnStatus: true)
                    
                    if (networkExists == 0) {
                        echo "Network '$networkName' exists."
                        sh "docker run -it --network dev -p 8050:8050 -d --env-file=dev.env ${DOCKER_IMAGE}.${DOCKERBUILD}:dev"
                    } else {
                        echo "Network '$networkName' does not exist."
                        echo "Create Network '$networkName'"
                        sh "docker network create dev"
                        sh "docker run -it --network dev -p 8050:8050 -d --env-file=dev.env ${DOCKER_IMAGE}.${DOCKERBUILD}:dev"
                    }
                    sleep(time:3,unit:'MINUTES')
                    
                    def response = sh(script: 'curl -s -o /dev/null -w "%{http_code}" http://localhost:8050/actuator/health', returnStdout: true).trim()
                    if (response != '200') {
                        echo "Smoke test failed. Errors in Service. Need to Rollback"
                        
                    } else {
                        echo "Smoke test passed."
                    }
                }
            }
        }
        
        stage('Deploy-Qa') {
        when {
               branch "develop"
        }
            steps {
                script {
                        def networkName = 'qa'
                        sh "docker image pull ${DOCKERPATH}:${DOCKER_IMAGE}.${DOCKERBUILD}"
                        sh "docker image tag ${DOCKER_IMAGE}.${DOCKERBUILD}:latest ${DOCKER_IMAGE}.${DOCKERBUILD}:qa"
                        def networkExists = sh(script: "docker network inspect $networkName > /dev/null 2>&1", returnStatus: true)
                    
                    if (networkExists == 0) {
                        echo "Network '$networkName' exists."
                        sh "docker run -it --network qa -p 8051:8050 -d --env-file=qa.env ${DOCKER_IMAGE}.${DOCKERBUILD}:qa"
                    } else {
                        echo "Network '$networkName' does not exist."
                        echo "Create Network '$networkName'"
                        sh "docker network create qa"
                        sh "docker run -it --network qa -p 8051:8050 -d --env-file=qa.env ${DOCKER_IMAGE}.${DOCKERBUILD}:qa"
                    }
                    
                    sleep(time:3,unit:'MINUTES')
                    
                    def response = sh(script: 'curl -s -o /dev/null -w "%{http_code}" http://localhost:8051/actuator/health', returnStdout: true).trim()
                    if (response != '200') {
                        echo "Smoke test failed. Errors in Service. Need to Rollback"
                        
                    } else {
                        echo "Smoke test passed."
                    }
                }
            }
        }
        
        stage('Release') {
        when {
               branch "develop"
        }
            steps {
            	script{
                sh 'mvn --batch-mode release:prepare -Dusername=BhanuPDas -Dpassword=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 release:perform'
                // Merge release branch to main branch
                    sshagent (credentials('BhanuPDas')) {
                        sh '''
                            git checkout main
                            git merge --no-ff release
                            git push origin main
                        '''
                    }
               }
            }
        }
        
        stage('Deploy-Stage') {
        when {
               branch "main"
        }
            steps {
                script {
                        sh "docker login -u ${DOCKER_USER} -p ${DOCKER_PWD}"
                        sh "docker image pull ${DOCKERPATH}:${DOCKER_IMAGE}.${DOCKERBUILD}"
                        sh "docker image tag ${DOCKER_IMAGE}.${DOCKERBUILD}:latest ${DOCKER_IMAGE}.${DOCKERBUILD}:stage"
                        def networkName = 'stage'
                        def networkExists = sh(script: "docker network inspect $networkName > /dev/null 2>&1", returnStatus: true)
                    
                    if (networkExists == 0) {
                        echo "Network '$networkName' exists."
                        sh "docker run -it --network stage -p 8052:8050 -d --env-file=stage.env ${DOCKER_IMAGE}.${DOCKERBUILD}:stage"
                    } else {
                        echo "Network '$networkName' does not exist."
                        echo "Create Network '$networkName'"
                        sh "docker network create stage"
                        sh "docker run -it --network stage -p 8052:8050 -d --env-file=stage.env ${DOCKER_IMAGE}.${DOCKERBUILD}:stage"
                    }
                    
                    sleep(time:3,unit:'MINUTES')
                    
                    def response = sh(script: 'curl -s -o /dev/null -w "%{http_code}" http://localhost:8052/actuator/health', returnStdout: true).trim()
                    if (response != '200') {
                        echo "Smoke test failed. Errors in Service. Need to Rollback"
                        
                    } else {
                        echo "Smoke test passed."
                    }
                }
            }
        }
        
        stage('Deploy-Prod') {
        when {
               branch "main"
        }
            steps {
                script {
                        sh "docker login -u ${DOCKER_USER} -p ${DOCKER_PWD}"
                        sh "docker image pull ${DOCKERPATH}:${env.DOCKER_IMAGE}.${DOCKERBUILD}"
                        sh "docker image tag ${DOCKER_IMAGE}.${DOCKERBUILD}:latest ${DOCKER_IMAGE}.${DOCKERBUILD}:prod"
                        def networkName = 'prod'
                        def networkExists = sh(script: "docker network inspect $networkName > /dev/null 2>&1", returnStatus: true)
                    
                    if (networkExists == 0) {
                        echo "Network '$networkName' exists."
                        sh "docker run -it --network prod -p 8053:8050 -d --env-file=prod.env ${DOCKER_IMAGE}.${DOCKERBUILD}:prod"
                    } else {
                        echo "Network '$networkName' does not exist."
                        echo "Create Network '$networkName'"
                        sh "docker network create prod"
                        sh "docker run -it --network prod -p 8053:8050 -d --env-file=prod.env ${DOCKER_IMAGE}.${DOCKERBUILD}:prod"
                    }
                    sleep(time:3,unit:'MINUTES')
                    
                    def response = sh(script: 'curl -s -o /dev/null -w "%{http_code}" http://localhost:8053/actuator/health', returnStdout: true).trim()
                    if (response != '200') {
                        echo "Smoke test failed. Errors in Service. Need to Rollback"
                        
                    } else {
                        echo "Smoke test passed."
                    }
                    
                }
            }
        }
    }

    post {
        always {
            echo "Pipeline has completed"
        }
    }
}
