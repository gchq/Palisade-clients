/**
 * This pipeline will execute a simple Maven build
 */

podTemplate(containers: [
  containerTemplate(name: 'maven', image: 'maven:3.6.1-jdk-11', ttyEnabled: true, command: 'cat')
  ]) {

  node(POD_LABEL) {
    stage('Build a Maven project') {
//      withCredentials([usernamePassword(credentialsId: 'git-pass-credentials-ID', password: 'GIT_PASSWORD', username: 'GIT_USERNAME')]) {
//        git clone -u ${GIT_USERNAME}:${GIT_PASSWORD} 'https://github.com/gchq/Palisade-clients.git'
        //    sh("git tag -a some_tag -m 'Jenkins'")
//    sh('git push https://${GIT_USERNAME}:${GIT_PASSWORD}@<REPO> --tags')
}

      git 'https://github.com/gchq/Palisade-clients.git'
      container('maven') {
          sh 'mvn install'
      }
    }
  }
}
