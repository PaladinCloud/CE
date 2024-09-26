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

package jwt

import (
	"context"
	"testing"
)

const (
	jwksURL  = "https://cognito-idp.us-east-1.amazonaws.com/{userpool-id}/.well-known/jwks.json"
	token    = ""
	audience = "{audience}"
	issuer   = "https://cognito-idp.us-east-1.amazonaws.com/{userpool-id}"
)

func TestValidateToken(t *testing.T) {
	ctx := context.Background()

	valid, _, err := ValidateToken(ctx, token, jwksURL, audience, issuer)
	if err != nil {
		t.Errorf("Expected error to be nil")
	}

	if !valid {
		t.Errorf("Expected valid to be true")
	}
}
