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

package service

import (
	"context"
	"github.com/aws/aws-lambda-go/events"
	"partner-access-auth/service/clients"
	"testing"
)

func TestHandleLambdaRequest(t *testing.T) {
	t.Log("TestHandleLambdaRequest")
	request := events.APIGatewayV2HTTPRequest{
		Headers: map[string]string{
			"authorization": "Bearer {TOKEN}",
		},
		RouteKey: "GET /api/v2/plugins",
	}

	configuration := &clients.Configuration{
		Region:   "us-east-1",
		JwksURL:  "https://cognito-idp.us-east-1.amazonaws.com/{userpool-id}/.well-known/jwks.json",
		Audience: "6j05hol2qp4eqbqrlblsdc427m",
		Issuer:   "https://cognito-idp.us-east-1.amazonaws.com/{userpool-id}",
	}

	ctx := context.Background()
	response, err := HandleLambdaRequest(ctx, request, configuration)
	if err != nil {
		t.Errorf("Expected response to be not nil")
	}

	if !response.IsAuthorized {
		t.Errorf("Expected response status code to be Allow")
	}
}
