# Local Jenkins pipeline setup

## Launch Jenkins container

```
$ PROJECT_VOLUME=${YOUR_CICD_PROJECT_DIRECTORY} docker-compose up -d
```

_replace `${YOUR_CICD_PROJECT_DIRECTORY}` by your local workspace path._

### Retrieve Jenkins initial administrator password

```
$ docker logs jenkins_jenkins_1 -f
*************************************************************
*************************************************************
*************************************************************

Jenkins initial setup is required. An admin user has been created and a password generated.
Please use the following password to proceed to installation:

03804dd61f2c47acfed171de15d6993c

This may also be found at: /var/jenkins_home/secrets/initialAdminPassword

*************************************************************
*************************************************************
```

### Initialize Jenkins

* Go to `localhost:8090` and copy-paste above password when asked
* Install suggested plugins
* Create  admin user

## Setup pipeline job

First, add label 'jenkins' to master node. It will be used later on in Jenkinsfile. 
Then create pipeline job for flight-service:
* Pipeline script from SCM
* Choose GIT
* URL: file:///home/project

#### Add a post-commit hook

* Create user for git hooks
* Add build-trigger in flight-service pipeline: 
  * Check trigger builds remotely
  * Create a random token
* In .git directory create following file:
```
$ cat .git/hooks/post-commit 
#!/bin/sh
curl -u YOUR_GITHOOKS_USERNAME:YOUR_PASSWORD http://localhost:8090/job/flight-service-pipeline/build?token=YOUR_TOKEN     
```

_replace `YOUR_GITHOOKS_USERNAME`, `YOUR_PASSWORD` and `YOUR_TOKEN`._

#### Create credentials to be used in Jenkinsfile

* Add system credentials for GCR as `Google service account from private key` with name `devoxxbe-2019` with you own google account json key file
* Add system credentials for Cloud Run as `Secret file` with id `GCP_KEY_SA` (can be the same json file as above)
> _roles needed by your google service account are:  Service Account User, Cloud Run Admin, Storage Admin, Storage Object Viewer_
* Add system credentials for service basic-auth as `username/password` with id `SPRING_SECURITY_USER_PASSWORD` (username is paul, password can be randomly generated)



