/*
 * Copyright 2019 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


podTemplate(containers: [
        containerTemplate(name: 'maven', image: 'maven:3.6.1-jdk-11', ttyEnabled: true, command: 'cat')
]) {

    node(POD_LABEL) {
        stage('Bootstrap') {
            sh 'printenv'
            sh "echo ${env.GIT_BRANCH}"
            sh "echo ${env.GIT_AUTHOR_NAME}"
            sh "echo ${env.GIT_LOCAL_BRANCH}"
            sh "echo ${env.BRANCH_NAME}"
//            sh "echo GIT_BRANCH_LOCAL=\\\"${env.GIT_BRANCH}\\\" | sed -e 's|origin/||g' | tee version.properties"
        }

        stage('Build a Maven project') {
            git branch: "${env.BRANCH_NAME}", url: 'https://github.com/gchq/Palisade-clients.git'

//            git 'https://github.com/gchq/Palisade-clients.git'
            container('maven') {
                sh 'ls && pwd'
                configFileProvider(
                        [configFile(fileId: '450d38e2-db65-4601-8be0-8621455e93b5', variable: 'MAVEN_SETTINGS')]) {
                    sh 'mvn -s $MAVEN_SETTINGS deploy'
                }
            }
        }
    }
}


