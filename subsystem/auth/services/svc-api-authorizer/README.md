# svc-api-authorizer

## Description

The `svc-api-authorizer` is a critical component of the API Gateway authentication system for accessing SaaS microservices. This Lambda function, triggered by API Gateway, performs the following key tasks:

1. Validates JWT tokens to authenticate incoming requests.
2. Enriches the request context with the tenant ID for authorized requests.
3. Generates appropriate IAM policies based on token validity.

## Features

- JWT token validation
- Tenant ID extraction and injection into the allow policy
- Automatic 401 Unauthorized response for invalid tokens
- Seamless integration with API Gateway

## Context Enrichment

For valid tokens, the authorizer always injects the tenant ID into the allow policy. This enriched context is crucial for maintaining proper multi-tenant data isolation in downstream services.

For all invalid tokens, the authorizer automatically sends a 401 Unauthorized response.

## Getting Started

### Prerequisites

- Go 1.20 or higher
- AWS CLI configured with appropriate permissions
- AWS SAM CLI (for local testing and deployment)

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/your-org/svc-api-authorizer.git
   cd svc-api-authorizer
   ```

2. Build and package the Lambda function:
   ```bash
   make package
   ```