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
	"fmt"
	"github.com/aws/aws-lambda-go/events"
	"strings"
	"svc-api-authorizer/service/clients"
	"svc-api-authorizer/utils/jwt"
)

const (
	UnauthorizedMessage    = "unauthorized"
	customAccessIdClaim    = "custom:accessId"
	authorizationHeaderKey = "authorization"
	tenantIdClaim          = "tenantId"
)

func HandleLambdaRequest(request events.APIGatewayV2HTTPRequest, config *clients.Configuration) (*events.APIGatewayV2CustomAuthorizerSimpleResponse, error) {
	// Extract the JWT token from the Authorization header
	authorizationHeader := ExtractAuthorizationHeader(request)
	if authorizationHeader == "" {
		return nil, fmt.Errorf("missing authorization header")
	}

	// Split "Bearer <token>"
	token := strings.TrimPrefix(authorizationHeader, "Bearer ")
	if token == "" {
		return nil, fmt.Errorf("missing Bearer token")
	}

	isValid, claims, err := jwt.ValidateToken(token, config.JwksURL, config.Audience, config.Issuer)
	if err != nil {
		return nil, fmt.Errorf("error validating token %w", err)
	}

	if !isValid {
		return nil, fmt.Errorf(UnauthorizedMessage)
	}

	// to hide the tenantId from the client, we will use the accessId claim
	tenantId, err := jwt.GetClaim(claims, customAccessIdClaim)
	if tenantId == "" {
		return nil, fmt.Errorf("missing claim [%s]", customAccessIdClaim)
	}

	return CreateAllowAllPolicy(tenantId), nil
}

// ExtractAuthorizationHeader retrieves the Authorization header in a case-insensitive manner.
func ExtractAuthorizationHeader(request events.APIGatewayV2HTTPRequest) string {
	// Convert all header keys to lowercase and check for "authorization"
	for key, value := range request.Headers {
		if strings.ToLower(key) == authorizationHeaderKey {
			return value
		}
	}

	return ""
}

func CreateAllowAllPolicy(tenantId string) *events.APIGatewayV2CustomAuthorizerSimpleResponse {
	allowPolicyDocument := events.APIGatewayV2CustomAuthorizerSimpleResponse{
		IsAuthorized: true,
		Context: map[string]interface{}{
			tenantIdClaim: tenantId,
		},
	}

	return &allowPolicyDocument
}
