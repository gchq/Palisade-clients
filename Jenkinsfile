/**
 * This pipeline will execute a simple Maven build
 */

podTemplate(containers: [
  containerTemplate(name: 'maven', image: 'maven:3.6.1-jdk-11', ttyEnabled: true, command: 'cat')
]) {

  node(POD_LABEL) {
    stage('Build a Maven project') {
      git branch: "PAL-135-client-contents", url: 'https://github.com/gchq/Palisade-clients.git'
//      git 'https://github.com/gchq/Palisade-clients.git'
      container('maven') {
          sh 'ls && pwd'
          sh 'mvn install'
      }
    }
  }
}
