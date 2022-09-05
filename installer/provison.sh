#!/bin/bash

# ---------------------------------
# -----Amazon Linux-----
# ---------------------------------

## Install git
sudo yum -y install git

## Install python3
sudo yum -y update
sudo amazon-linux-extras install epel -y
sudo yum install python3-pip -y

## Install Terraform
sudo yum install -y yum-utils
sudo yum-config-manager --add-repo https://rpm.releases.hashicorp.com/AmazonLinux/hashicorp.repo
sudo yum -y install terraform

## INSTALL NODEJS and dependencies
sudo yum install curl -y
sudo yum -y update
curl -sL https://rpm.nodesource.com/setup_14.x | sudo -E bash -
sudo yum install -y nodejs
sudo npm install -g yarn
sudo npm install -g @angular/cli@1.6.8 
sudo npm install -g bower

## Install openjdk 
sudo yum -y install java-1.8.0-openjdk

## iNSTALL MAVEN
sudo yum install -y maven

## Install docker
sudo yum -y install docker
sudo systemctl start docker

# Install mysql
sudo yum -y install mysql

#installing python packages
sudo pip3 install -r requirements.txt

# echo alias cdd=\"cd $(pwd)\" >> ~/.bashrc
# echo alias cdt=\"cd $(pwd)/data/terraform\" >> ~/.bashrc
# echo alias cdl=\"cd $(pwd)/log\" >> ~/.bashrc
# source ~/.bashrc

# ## Install virtualenv
# mkdirs ~/envs/
# virtualenv ~/envs/pacbot_env --python=python3
# source ~/envs/pacbot_env/bin/activate
# echo source ~/envs/pacbot_env/bin/activate >> ~/.bashrc
# pip install -r requirements.txt


# ---------------------------------
# -----Ubuntu-----
# ---------------------------------

# #install openjdk
# sudo apt -y update
# sudo add-apt-repository ppa:openjdk-r/ppa
# sudo apt-get update
# sudo apt install -y openjdk-8-jdk
# sudo  update-java-alternatives --set openjdk-8-jdk

# #install maven
# sudo apt install -y maven

# #install docker
# sudo apt install -y docker
# sudo apt install -y docker.io

# # install python
# sudo apt install -y python3
# sudo apt install -y python3-pip

# # install mysql
# sudo apt install -y mysql-client

# ## Install Terraform
#wget -O- https://apt.releases.hashicorp.com/gpg | gpg --dearmor | sudo tee /usr/share/keyrings/hashicorp-archive-keyring.gpg
#echo "deb [signed-by=/usr/share/keyrings/hashicorp-archive-keyring.gpg] https://apt.releases.hashicorp.com $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/hashicorp.list
#sudo apt update && sudo apt install terraform

# # INSTALL NODEJS and dependencies
# sudo apt install -y curl
# curl -sL https://deb.nodesource.com/setup_14.x | sudo bash -
# sudo apt -y update
# sudo apt install -y nodejs
# sudo npm install -g yarn
# sudo npm install -g @angular/cli -y
# sudo npm install -g bower

# #installing python packages
# sudo pip3 install -r requirements.txt

# # echo alias cdd=\"cd $(pwd)\" >> ~/.bashrc
# # echo alias cdt=\"cd $(pwd)/data/terraform\" >> ~/.bashrc
# # echo alias cdl=\"cd $(pwd)/log\" >> ~/.bashrc
# # source ~/.bashrc

# ## Install virtualenv
# # mkdir ~/envs/
# # python3 -m venv ~/envs/pacbot_env
# # source ~/envs/pacbot_env/bin/activate
# # echo source ~/envs/pacbot_env/bin/activate >> ~/.bashrc
# # pip install -r requirements.txt

