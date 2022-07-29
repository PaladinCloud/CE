# Installation FAQs

### Installation is failed. What should I do now?

Installation could fail due to various reasons. If an error occurs then detailed messages will be stored in
log/error.log. You can check the log file and identify the issue.

If Installation fails during any of the terraform states, you can resume the installation by running the redeploy. If
installation fails before any of the terraform stages, please re-run the install command.

Please verify the following steps before you proceed further if any error occurs.

1. Is your installer machine has at least 8 GB of ram?
   To install Paladin Cloud the installer machine should have at least 8GB of ram. We recommend using a **t2.large**
   instance at least

2. Is the maven build failing?
   It could be possible to fail the maven build if you run the installation from the home directory of the user. So we
   recommend installation from the /opt/ directory.

3. Are you getting Can't connect to MySQL server on 'paladincloud-data.xxxxx.rds.amazonaws.com:3306'?
   The installer machine should be under the same VPC or there should be a VPC peering to connect to the resources
   created from the installer machine. This is required as the installation script needs to access MySQL to import
   initial data from the SQL file

4. Is your installer machine has enough disk space?
   To be on the safer side please ensure that at least 20GB of disk space is there so that docker build can create the
   images.

5. Is the Amazon region has the capacity to create ~200 more CloudWatch rules?
   As part of Paladin Cloud installation, 200+ CloudWatch rules will be created. Normally AWS has the limitation of 300
   rules per region. So please ensure that there is room for rules creation. You can contact support to get an increased
   limit. We are working on improving this process.

### Batch jobs stuck in runnable state and not moving to running state. Why?

There can be various reasons why batch jobs remain in the runnable state and do not advance. One reason could be the bad
network configuration. For batch jobs to run the instances should have external network connectivity. Since the
resources have no public IP address, they must have NAT gateway/instance attached to it.
Please see more details about this
here, https://docs.aws.amazon.com/batch/latest/userguide/troubleshooting.html#job_stuck_in_runnable

### I have created an internet-facing(public) ALB but still the application is not loading Or seems to be very slow. Why?

If you create the ALB as internet-facing then it should have subnet(s) with an internet gateway attached to it.
Otherwise, communication between VPC and the internet may not happen. So please ensure that the internet gateway is
correctly configured to the subnet. You can check this by going to the Load balancer and editing the subnet. There you
will be able to see the warning if there is any.

### I have created an internet-facing(public) ALB but APIs are failing. Why?

If you make an ALB internet-facing and the internet gateway is correctly configured with subnets then ever after APIs
are failing then that might be because of security group inbound rules. You should either enable access from anywhere Or
identify the container IPs and add every one of them to the security group. This is required as all API services except
config service communicate with config service initially to get the configuration properties. So other APIs from their
containers should be able to connect to the config service which can happen only if those container IPs are enabled in
the security group.

### I am disconnected from the installer machine before the install/destroy command gets completed. What should I do now?

It is always recommended to run the install or destroy command behind Linux
screen(https://linuxize.com/post/how-to-use-linux-screen/).
After running the install/destroy command if you get disconnected from the installer machine then the process will be
running in the background. So wait for at least 30 minutes and then again try to run the command again. If you get a
warning message saying "Another process running...", try to check any process with the name **terraform** is running or
not. If there is any such process then wait till that completes. If there is no such process then please delete the lock
file from installer/data/terraform/.terraform.lock.info, and try to run the command again

### Is it required for the installer machine to be running all the time?

No, the installer machine does not require to be running always. You can stop the instance once you have done the
installation. If there is any newer version update occurs, you can start the machine, pull the latest Paladin Cloud code
and run redeploy command. Then after that, you can stop the instance.

### My installer machine got terminated accidentally. How can I redeploy if the latest version gets released?

Your installer machine got terminated? do not worry we will be saving the required state files in S3. What you have to
do is to follow the below steps

1. Start a new instance under the same VPC
2. Get the latest stable release for Paladin Cloud repo in /opt directory
3. In S3 you can see the Paladin Cloud bucket and there is a zip file with the name
   paladincloud-terraform-installer-backup.zip. Download the file and extract them inside the installer directory to
   replace the /installer/data directory.
4. Edit the local.py file to have all configurations
5. Try to run the install command followed by redeploy command

### Destroy command threw timeout error. What should I do now?

If destroy command does not get executed successfully and terminated with a timeout error then the destruction might be
happening at the AWS. So wait for 30-60 minutes and run destroy command again.