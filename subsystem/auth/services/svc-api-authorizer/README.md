# svc-service-access

## Description

This is the part of API Gateway Auth to access SaaS microservices. 
It is a Lambda function that is triggered by API Gateway. 
It is responsible to validate the JWT token and authorize the request to the SaaS microservices. 
It is also responsible to add the user information to the request context so that the SaaS microservices can use it to authorize the request.

## Getting Started

### Dependencies

* Go 1.2 or higher

### Installing

* run `make package` from root directory to build and package the lambda function
