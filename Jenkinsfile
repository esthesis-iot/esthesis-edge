pipeline {
    agent {
        kubernetes {
            yaml '''
              apiVersion: v1
              kind: Pod
              metadata:
                name: esthesis-edge
                namespace: jenkins
              spec:
                tolerations:
                - key: "jenkins"
                  operator: "Equal"
                  value: "agent"
                  effect: "NoSchedule"
                nodeSelector:
                  jenkins-agent: "true"
                priorityClassName: jenkins-low-priority
                securityContext:
                  runAsUser: 0
                  runAsGroup: 0
                  fsGroup: 0
                containers:
                - name: esthesis-edge-builder
                  image: eddevopsd2/ubuntu-dind:docker24-mvn3.9.6-jdk21-node18.16-go1.23-buildx-helm
                  volumeMounts:
                  - name: maven
                    mountPath: /root/.m2/
                    subPath: esthesis
                  - name: sonar-scanner
                    mountPath: /root/sonar-scanner
                  - name: docker
                    mountPath: /root/.docker
                  tty: true
                  securityContext:
                    privileged: true
                    runAsUser: 0
                imagePullSecrets:
                - name: regcred
                volumes:
                - name: maven
                  persistentVolumeClaim:
                    claimName: maven-nfs-pvc
                - name: sonar-scanner
                  persistentVolumeClaim:
                    claimName: sonar-scanner-nfs-pvc
                - name: docker
                  persistentVolumeClaim:
                    claimName: docker-nfs-pvc
            '''
            workspaceVolume persistentVolumeClaimWorkspaceVolume(claimName: 'workspace-nfs-pvc', readOnly: false)
        }
    }
    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 3, unit: 'HOURS')
    }
    stages {
    	stage ('Dockerd') {
            steps {
                container (name: 'esthesis-edge-builder') {
                    sh 'dockerd -H tcp://0.0.0.0:2375 -H unix:///var/run/docker.sock &> dockerd-logfile &'
                }
            }
        }
        stage ('Clone Common and Bom Repositories') {
            steps {
                container (name: 'esthesis-edge-builder') {
                    withCredentials([usernamePassword(credentialsId: 'Jenkins-Github-token',
                    usernameVariable: 'Username',
                    passwordVariable: 'Password')]){
                        sh '''
                            git config --global user.email "devops-d2@eurodyn.com"
                            git config --global user.name "$Username"
                            git clone https://$Password@github.com/esthesis-iot/esthesis-bom
                            git clone https://$Password@github.com/esthesis-iot/esthesis-common
                        '''
                    }
                }
            }
        }
        stage('Build Bom') {
            steps {
                container (name: 'esthesis-edge-builder') {
                    sh '''
                        cd esthesis-bom
                        mvn clean install
                    '''
                }
            }
        }
        stage('Build Common') {
            steps {
                container (name: 'esthesis-edge-builder') {
                    sh '''
                        cd esthesis-common
                        mvn clean install
                    '''
                }
            }
        }
        stage('Build Server') {
            steps {
                container (name: 'esthesis-edge-builder') {
                    sh '''

                        mvn -f esthesis-edge-backend/pom.xml clean install
                    '''
                }
            }
        }
        stage('Sonar Analysis') {
            steps {
                container (name: 'esthesis-edge-builder') {
                    withSonarQubeEnv('sonar') {
                        sh '''
                            /root/sonar-scanner/sonar-scanner/bin/sonar-scanner -Dsonar.projectVersion="$(mvn -f esthesis-edge-backend/pom.xml help:evaluate -Dexpression=project.version -q -DforceStdout)" -Dsonar.host.url=${SONAR_HOST_URL} -Dsonar.token=${SONAR_GLOBAL_KEY} -Dsonar.working.directory="/tmp"
                        '''
                    }
                }
            }
        }
        stage('Produce bom.xml for backend') {
            steps{
                container (name: 'esthesis-edge-builder') {
                    sh 'mvn -f esthesis-edge-backend/pom.xml org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom'
                }
            }
        }
        stage('Post Dependency-Track Analysis for server') {
            steps {
                container (name: 'esthesis-edge-builder') {
                    sh '''
                        DT_BRANCH=$(echo "${BRANCH_NAME:-unknown}" | tr "/ " "__")
                
                        curl -sS -X POST "${DEPENDENCY_TRACK_URL}" \
                          -H "X-Api-Key: ${DEPENDENCY_TRACK_API_KEY}" \
                          -F "autoCreate=true" \
                          -F "parentName=esthesis-edge" \
                          -F "parentVersion=parent" \
                          -F "projectName=esthesis-edge-backend" \
                          -F "projectVersion=${DT_BRANCH}" \
                          -F "bom=@esthesis-edge-backend/target/bom.xml"
                      '''
                }
            }
        }
    }
}
