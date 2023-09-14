from __future__ import print_function

import json
import boto3
import os


batch = boto3.client('batch')
jobQueue = os.environ.get('JOB_QUEUE') #JOB_QUEUE:pacman-batch-jobs
jobDefinition = os.environ.get('JOB_DEFINITION') #JOB_DEFINITION:pacman-rule-engine-job:15

def lambda_handler(event, context):

    print("check whether it is SQS type or not")
    ##Check whether it is of SQS type. SQS event will have Parent Key as 'Records'
    print(event)
    if('Records' in event):
        print('I am sqs event')
        process_sqs_message(event,context)
    else:
        print("I am custom event")
        # Log the received event
        print("Received event: " + json.dumps(event, indent=2))
        if jobQueue is None or jobDefinition is None:
            print("job definition or environment not found")
            return
        return processJsonInput(event,context)

## funcation name: submit_to_batch
## Author : kkumar
## submit a job with Batch
##
def submit_to_batch(jobQueue,jobName,jobDefinition,containerOverrides,parameters):

    try:
        # Submit a Batch Job
        response = batch.submit_job(jobQueue=jobQueue, jobName=jobName, jobDefinition=jobDefinition,
                                    containerOverrides=containerOverrides, parameters=parameters)
        # Log response from AWS Batch
        print("Response: " + json.dumps(response, indent=2))
        # Return the jobId
        jobId = response['jobId']
        return {
            'jobId': jobId
        }
    except Exception as e:
        print(e)
        message = 'Error submitting Batch Job'
        print(message)
        raise Exception(message)


## Seperate Function to handle SQS event for Triggering Shipper Job
## Author : Anandhanatarajan Kuppusamy

def process_sqs_message(event,context):
    for record in event['Records']:
        print ("Inside process_sqs_message")
        print(record)
        # Convert JSON String to Python
        payload = json.loads(record['body'])
        #payload=json.dumps(record["body"])
        print("printing job payload")
        print(payload)
        processJsonInput(payload,context)

## funcation name: processJsonInput
## Author : kkumar
## process json input from CW and submit a job with Batch
##
def processJsonInput(event,context):
    print("inside processJsonInput")
    print(event)
    jobName =  event['jobName'] + '-job'
    executableName = event['jobUuid']+"."+"jar"
    print(jobName)
    print(executableName)
    environmentVariables = event['environmentVariables']
    print(environmentVariables)

    if event.get('environmentVariables'):
        containerOverrides = {"environment":event['environmentVariables']}
    else:
        containerOverrides = {}
    #remove the environment variables now.
    del event['environmentVariables']
    parameters = {"executableName":executableName,
                  "params":json.dumps(event),
                  "jvmMemParams":os.getenv('JVM_HEAP_SIZE',"-Xms1024m -Xmx4g"),
                  "ruleEngineExecutableName":"policy-engine.jar",
                  "entryPoint":"com.tmobile.pacman.executor.JobExecutor"}

    return submit_to_batch(jobQueue,jobName,jobDefinition,containerOverrides,parameters)

## funcation name: submit_to_batch
## Author : kkumar
## submit a job with Batch
##
def submit_to_batch(jobQueue,jobName,jobDefinition,containerOverrides,parameters):

    try:
        # Submit a Batch Job
        print("Submiting Job ")
        response = batch.submit_job(jobQueue=jobQueue, jobName=jobName, jobDefinition=jobDefinition,
                                    containerOverrides=containerOverrides, parameters=parameters)
        # Log response from AWS Batch
        print("Response: " + json.dumps(response, indent=2))
        # Return the jobId
        jobId = response['jobId']
        return {
            'jobId': jobId
        }
    except Exception as e:
        print(e)
        message = 'Error submitting Batch Job'
        print(message)
        raise Exception(message)
