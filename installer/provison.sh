#!/bin/bash

# ---------------------------------
# -----Amazon Linux-----
# ---------------------------------

## Update and Install git
sudo yum update -y
sudo yum install git -y

## Install python3
sudo yum install python3 -y

## Install Terraform
sudo yum install unzip -y
wget https://releases.hashicorp.com/terraform/1.5.2/terraform_1.5.2_linux_amd64.zip
unzip terraform_1.5.2_linux_amd64.zip
sudo mv terraform /usr/bin

## INSTALL NODEJS and dependencies
curl -fsSL https://rpm.nodesource.com/setup_16.x | sudo bash -
sudo yum install -y nodejs
sudo npm install -g yarn
sudo npm install -g @angular/cli
sudo npm install -g bower

## Remove openjdk 17 and Install openjdk 8
sudo yum remove java-17-amazon-corretto-headless -y
sudo yum install java-1.8.0-openjdk-devel -y

# Download and extract Maven 3.6.3 binary distribution
wget https://mirrors.estointernet.in/apache/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.tar.gz --no-check-certificate
tar -xvf apache-maven-3.6.3-bin.tar.gz

# Move Maven to /opt directory
sudo mv apache-maven-3.6.3 /opt/

# Add Maven to PATH
echo 'export M2_HOME="/opt/apache-maven-3.6.3"' >> ~/.bashrc
echo 'export PATH="$PATH:$M2_HOME/bin"' >> ~/.bashrc

# Reload environment variables
source ~/.bashrc

# Create symlink for mvn executable
sudo rm /usr/bin/mvn
sudo ln -s /opt/apache-maven-3.6.3/bin/mvn /usr/bin/mvn

## Install docker
sudo yum install docker -y
sudo systemctl start docker

## Remove conflicting packages
sudo yum remove mariadb-connector-c-config -y

## Install MySQL
sudo rpm -Uvh https://dev.mysql.com/get/mysql57-community-release-el7-11.noarch.rpm
sudo rpm --import https://repo.mysql.com/RPM-GPG-KEY-mysql-2022
sudo yum install mysql-community-server -y

## Clean package cache
sudo yum clean packages -y

## Enable and start MySQL service
sudo systemctl enable mysqld
sudo systemctl start mysqld

# Installing python packages
pip3 install -r requirements.txt

pip3 install --upgrade setuptools
pip3 install --upgrade distlib

pip3 install --user boto3
