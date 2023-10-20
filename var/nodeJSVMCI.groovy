pipeline {
    agent { node { label 'AGENT-1' } } 
    options{
        ansiColor('xterm')
    }
    environment{
        packageVersion = ''
    }
    parameters {
        string(name: 'component', defaultValue: ' ', description: 'Which component?')
    }
    stages {
        // push to featire branch
        stage('Get Version'){
            steps{
                script{
                    def packageJson = readJSON(file: 'package.json')
                    packageVersion = packageJson.version
                    echo "${packageVersion}"
                }
            }
        }
        stage('Install Dependencies') {
            steps {
                sh 'npm install'
            }
        }
        stage('Unit test'){
            steps{
                echo "unit test is done here"
            }
        }
        stage('sonar-scan'){
            steps{
                // sh 'ls -ltr'
                // sh 'sonar-scanner'
                echo "Sonar scan done"
            }
        }
        stage('Build'){
            steps{
                sh 'ls -ltr'
                sh 'zip -r ${params.component}.zip ./* --exclude=.git --exclude=.zip'
            }
        }
        stage('SAST'){
            steps{
                echo "SAST DOne"
                echo "package version: $packageVersion"
            }
        }
        // install pipeline utility steps plugin
        stage('Pushing arificato to nexus'){
            steps {
                nexusArtifactUploader(
                nexusVersion: 'nexus3',
                protocol: 'http',
                nexusUrl: '34.203.210.220:8081/',
                groupId: 'com.roboshop',
                version: "$packageVersion",
                repository: "${params.component}",
                credentialsId: 'nexus-auth',
                artifacts: [
                    [artifactId: "${params.component}",
                    classifier: '',
                    file: "${params.component}.zip",
                    type: 'zip']
                ]
            )
                        }
        }
        // here i need to configure downstream job. i have to pass pkg version for deployment
        // this job will wait until downstream job is over
        stage('Deploy'){
            steps{
                script{
                    echo "Deployment"
                    def params = [
                        string(name: 'version', value: "$packageVersion")
                    ]
                    build job: "../${params.component}", wait: true, parameters: params
                }
                
            }
        }

    }
    // post{
    //     always{
    //         // need to delete workspace everytime
    //         echo "cleaning up workspace"
    //         //deleteDir()
    //     }
    // }
    post{
        always{
            echo 'cleaning up workspace'
            deleteDir()
        }
    }
}