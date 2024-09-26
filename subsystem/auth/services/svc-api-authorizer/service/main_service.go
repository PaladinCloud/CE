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
	logger "partner-access-auth/service/logging"
	"partner-access-auth/utils/jwt"
	"strings"
)

var log *logger.Logger

func init() {
	log = logger.NewLogger("svc-api-authorizer - main_service")
}

func HandleLambdaRequest(ctx context.Context, request events.APIGatewayV2HTTPRequest, config *clients.Configuration) (events.APIGatewayV2CustomAuthorizerSimpleResponse, error) {
	// Extract the JWT token from the Authorization header
	authorizationHeader := ExtractAuthorizationHeader(request)
	if authorizationHeader == "" {
		log.Error("Authorization header is missing")
		return CreateDenyAllPolicy(), nil
	}

	// Split "Bearer <token>"
	token := strings.TrimPrefix(authorizationHeader, "Bearer ")
	if token == "" {
		log.Error("Missing access token\n")
		return CreateDenyAllPolicy(), nil
	}

	isValid, claims, err := jwt.ValidateToken(ctx, token, config.JwksURL, config.Audience, config.Issuer)
	if err != nil {
		log.Error("Error getting authorization:", err)
		return CreateDenyAllPolicy(), nil
	}

	if !isValid {
		log.Error("User is not authorized")
		return CreateDenyAllPolicy(), nil
	}

	// to hide the tenantId from the client, we will use the accessId claim
	tenantId, err := jwt.GetClaim(claims, "custom:accessId")
	if tenantId == "" {
		log.Error("Missing accessId")
		return CreateDenyAllPolicy(), nil
	}

	log.Info("User is authorized")
	return CreateAllowAllPolicy(tenantId), nil
}

// ExtractAuthorizationHeader retrieves the Authorization header in a case-insensitive manner.
func ExtractAuthorizationHeader(request events.APIGatewayV2HTTPRequest) string {
	// Convert all header keys to lowercase and check for "authorization"
	for key, value := range request.Headers {
		if strings.ToLower(key) == "authorization" {
			return value
		}
	}

	return ""
}

func CreateAllowAllPolicy(tenantId string) events.APIGatewayV2CustomAuthorizerSimpleResponse {
	allowPolicyDocument := events.APIGatewayV2CustomAuthorizerSimpleResponse{
		IsAuthorized: true,
		Context: map[string]interface{}{
			"tenantId": tenantId,
		},
	}

	log.Info("Allowing access", allowPolicyDocument)
	return allowPolicyDocument
}

func CreateDenyAllPolicy() events.APIGatewayV2CustomAuthorizerSimpleResponse {
	denyPolicyDocument := events.APIGatewayV2CustomAuthorizerSimpleResponse{
		IsAuthorized: false,
	}

	log.Info("Denying access", denyPolicyDocument)
	return denyPolicyDocument
}
