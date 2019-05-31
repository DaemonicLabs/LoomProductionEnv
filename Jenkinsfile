pipeline {
    agent any

    stages {

        stage('Build') {
            steps {
                sh "rm -rf build/libs/"
                sh "chmod +x gradlew"
                sh "./gradlew build --refresh-dependencies"
            }
        }

        stage("publish") {
            when {
                branch 'master'
            }
            steps {
                sh "./gradlew publish"
            }
        }

//        stage("gradle plugin") {
//            when {
//                branch 'master'
//            }
//            steps {
//                withCredentials([file(credentialsId: 'gradlePluginProperties', variable: 'PROPERTIES')]) {
//                    sh '''
//                    cat "$PROPERTIES" >> gradle.properties
//                    ./gradlew publishPlugins
//                    '''
//                }
//            }
//        }

        stage("incrementBuildnumber") {
            steps {
                sh "./gradlew buildNumberIncrement"
            }
        }
    }
}
