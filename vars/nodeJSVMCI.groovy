def call(Map configMap){
    // mapNmae.get("key-name")
    echo "component is : $component"
    def component = configMap.get("component")
    pipeline {
        agent { node { label 'AGENT-1' } } 
        options{
            ansiColor('xterm')
        }
        environment{
            packageVersion = ''
        }
        // parameters {
        //     string(name: 'component', defaultValue: ' ', description: 'Which component?')
        // }
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
                    sh 'zip -r ${component}.zip ./* --exclude=.git --exclude=.zip'
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
                    nexusUrl: '184.73.110.135:8081/',
                    groupId: 'com.roboshop',
                    version: "$packageVersion",
                    repository: "${component}",
                    credentialsId: 'nexus-auth',
                    artifacts: [
                        [artifactId: "${component}",
                        classifier: '',
                        file: "${component}.zip",
                        type: 'zip']
                    ]
                )
                            }
            }
            // here i need to configure downstream job. i have to pass pkg version for deployment
            // this job will wait until downstream job is over
            // By default when a non-master branch CI is done, we can go for DEV developement
            stage('Deploy'){
                steps{
                    script{
                        echo "Deployment"
                        def params = [
                            string(name: 'version', value: "$packageVersion"),
                            // string(name: 'environment', value: "dev")
                        ]
                        build job: "../${component}-deploy", wait: true, parameters: params
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
}