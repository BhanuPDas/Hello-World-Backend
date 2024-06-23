pipeline {
    agent any

    environment {
        GIT_CREDENTIALS = credentials('BhanuPDas')
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
    
        stage('Checkout') {
        when {
            expression {
               env.BRANCH_NAME == 'develop' || env.BRANCH_NAME == 'main'
            }
        }
        steps {
                git branch: '${env.BRANCH_NAME}', credentialsId: 'BhanuPDas', url: 'https://github.com/BhanuPDas/Hello-World-Backend.git'
            }
        }

        stage('Build') {
        when {
            expression {
               env.BRANCH_NAME == 'develop'
            }
        }
            steps {
                sh 'mvn clean compile'
            }
        }

        stage('Test') {
        when {
            expression {
               env.BRANCH_NAME == 'develop'
            }
        }
            steps {
                sh 'mvn test'
            }
        }

        stage('Deploy-Dev') {
        when {
            expression {
               env.BRANCH_NAME == 'develop'
            }
        }
            steps {
                script {
                        sh "docker login -u ${env.DOCKER_USER} -p ${env.DOCKER_PWD}"
                        sh "docker build -t ${env.DOCKER_IMAGE}:${DOCKERBUILD} ."
                        sh "docker image tag ${env.DOCKER_IMAGE}:${DOCKERBUILD} ${DOCKERPATH}/${env.DOCKER_IMAGE}:${DOCKERBUILD}"
                        sh "docker image push ${DOCKERPATH}/${env.DOCKER_IMAGE}:${DOCKERBUILD}"
                        def networkName = 'dev'
                        def networkExists = sh(script: "docker network inspect $networkName > /dev/null 2>&1", returnStatus: true)
                    
                    if (networkExists == 0) {
                        echo "Network '$networkName' exists."
                        sh "docker run -it --network dev -p 8050:8050 -d --env-file=dev.env ${env.DOCKER_IMAGE}.dev:${DOCKERBUILD}"
                    } else {
                        echo "Network '$networkName' does not exist."
                        echo "Create Network '$networkName'"
                        sh "docker network create dev"
                        sh "docker run -it --network dev -p 8050:8050 -d --env-file=dev.env ${env.DOCKER_IMAGE}.dev:${DOCKERBUILD}"
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
            expression {
               env.BRANCH_NAME == 'develop'
            }
        }
            steps {
                script {
                        def networkName = 'qa'
                        sh "docker image pull ${DOCKERPATH}/${env.DOCKER_IMAGE}:${DOCKERBUILD}"
                        def networkExists = sh(script: "docker network inspect $networkName > /dev/null 2>&1", returnStatus: true)
                    
                    if (networkExists == 0) {
                        echo "Network '$networkName' exists."
                        sh "docker run -it --network qa -p 8051:8050 -d --env-file=qa.env ${env.DOCKER_IMAGE}.qa:${DOCKERBUILD}"
                    } else {
                        echo "Network '$networkName' does not exist."
                        echo "Create Network '$networkName'"
                        sh "docker network create qa"
                        sh "docker run -it --network qa -p 8051:8050 -d --env-file=qa.env ${env.DOCKER_IMAGE}.qa:${DOCKERBUILD}"
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
            expression {
               env.BRANCH_NAME == 'develop'
            }
        }
            steps {
            	script{
                sh 'mvn --batch-mode release:prepare release:perform'
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
            expression {
               env.BRANCH_NAME == 'main'
            }
        }
            steps {
                script {
                        sh "docker login -u ${env.DOCKER_USER} -p ${env.DOCKER_PWD}"
                        sh "docker image pull ${DOCKERPATH}/${env.DOCKER_IMAGE}:${DOCKERBUILD}"
                        def networkName = 'stage'
                        def networkExists = sh(script: "docker network inspect $networkName > /dev/null 2>&1", returnStatus: true)
                    
                    if (networkExists == 0) {
                        echo "Network '$networkName' exists."
                        sh "docker run -it --network stage -p 8052:8050 -d --env-file=stage.env ${env.DOCKER_IMAGE}.stage:${DOCKERBUILD}"
                    } else {
                        echo "Network '$networkName' does not exist."
                        echo "Create Network '$networkName'"
                        sh "docker network create stage"
                        sh "docker run -it --network stage -p 8052:8050 -d --env-file=stage.env ${env.DOCKER_IMAGE}.stage:${DOCKERBUILD}"
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
            expression {
               env.BRANCH_NAME == 'main'
            }
             cron('0 21 * * *')
        }
            steps {
                script {
                        sh "docker login -u ${env.DOCKER_USER} -p ${env.DOCKER_PWD}"
                        sh "docker image pull ${DOCKERPATH}/${env.DOCKER_IMAGE}:${DOCKERBUILD}"
                        def networkName = 'prod'
                        def networkExists = sh(script: "docker network inspect $networkName > /dev/null 2>&1", returnStatus: true)
                    
                    if (networkExists == 0) {
                        echo "Network '$networkName' exists."
                        sh "docker run -it --network prod -p 8053:8050 -d --env-file=prod.env ${env.DOCKER_IMAGE}.prod:${DOCKERBUILD}"
                    } else {
                        echo "Network '$networkName' does not exist."
                        echo "Create Network '$networkName'"
                        sh "docker network create prod"
                        sh "docker run -it --network prod -p 8053:8050 -d --env-file=prod.env ${env.DOCKER_IMAGE}.prod:${DOCKERBUILD}"
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
