FROM jenkins/jenkins:lts
LABEL maintainer="Xavier Thery <xthery@egencia.com>"

COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt
RUN echo 2.0 > /usr/share/jenkins/ref/jenkins.install.UpgradeWizard.state

USER root
RUN apt-get update && apt-get install lsb-release -y
RUN export CLOUD_SDK_REPO="cloud-sdk-$(lsb_release -c -s)" && \
    echo "deb http://packages.cloud.google.com/apt $CLOUD_SDK_REPO main" | tee -a /etc/apt/sources.list.d/google-cloud-sdk.list && \
    curl https://packages.cloud.google.com/apt/doc/apt-key.gpg | apt-key add - && \
    apt-get update -y && apt-get install google-cloud-sdk -y

RUN curl -fsSL https://get.docker.com | bash -
# Very bad.. but container will be used locally
# Need to find a way to authorize container to use /var/run/docker.sock with docker-compose
# https://github.com/jenkinsci/docker/issues/263
RUN usermod -aG root jenkins
USER jenkins