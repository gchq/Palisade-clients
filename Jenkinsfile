pipeline {
  agent {
     label "maven"
  }
  stages {
    stage('Build') {
      steps {
        sh 'mvn -version'
        sh 'mvn install'
      }
    }
  }
}
