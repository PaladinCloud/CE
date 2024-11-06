/*
 * Copyright (c) 2023 Paladin Cloud, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package main

import (
	"context"
	"partner-access-auth/service"
	"partner-access-auth/service/clients"
	logger "partner-access-auth/service/logging"

	"github.com/aws/aws-lambda-go/events"
	"github.com/aws/aws-lambda-go/lambda"
)

var log *logger.Logger

func init() {
	log = logger.NewLogger("svc-api-authorizer - main")
}

func HandleRequest(ctx context.Context, request events.APIGatewayV2HTTPRequest) (events.APIGatewayV2CustomAuthorizerSimpleResponse, error) {
	log.Info("Request received", request)
	configuration := clients.LoadConfigurationDetails(ctx)

	response, err := service.HandleLambdaRequest(ctx, request, configuration)
	if err != nil {
		return events.APIGatewayV2CustomAuthorizerSimpleResponse{}, err
	}

	return *response, nil
}

func main() {
	lambda.Start(HandleRequest)
}
