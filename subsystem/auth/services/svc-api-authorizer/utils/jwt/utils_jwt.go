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
	"crypto/rsa"
	"encoding/base64"
	"encoding/binary"
	"encoding/json"
	"errors"
	"fmt"
	"github.com/golang-jwt/jwt/v5"
	"io/ioutil"
	"math/big"
	"net/http"
)

// CognitoJWK represents a JSON Web Key as provided by AWS Cognito
type CognitoJWK struct {
	Keys []CognitoKey `json:"keys"`
}

// CognitoKey represents a single key from the JSON Web Key Set
type CognitoKey struct {
	Kid string `json:"kid"`
	Alg string `json:"alg"`
	Kty string `json:"kty"`
	Use string `json:"use"`
	N   string `json:"n"`
	E   string `json:"e"`
}

// fetchCognitoJWKs fetches the JWKS from AWS Cognito
func fetchCognitoJWKs(jwksURL string) (map[string]*rsa.PublicKey, error) {
	resp, err := http.Get(jwksURL)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("failed to fetch JWKS: %s", resp.Status)
	}

	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		return nil, err
	}

	var jwks CognitoJWK
	err = json.Unmarshal(body, &jwks)
	if err != nil {
		return nil, err
	}

	keyMap := make(map[string]*rsa.PublicKey)
	for _, key := range jwks.Keys {
		if key.Kty != "RSA" {
			continue
		}

		// Decode the base64 URL-encoded modulus and exponent
		modulus, err := base64.RawURLEncoding.DecodeString(key.N)
		if err != nil {
			return nil, fmt.Errorf("failed to decode modulus: %v", err)
		}
		exponent, err := base64.RawURLEncoding.DecodeString(key.E)
		if err != nil {
			return nil, fmt.Errorf("failed to decode exponent: %v", err)
		}

		// Convert modulus and exponent to big integers
		n := new(big.Int).SetBytes(modulus)
		e := int(binary.BigEndian.Uint32(append(make([]byte, 4-len(exponent)), exponent...)))

		// Create an RSA public key
		pubKey := &rsa.PublicKey{
			N: n,
			E: e,
		}

		keyMap[key.Kid] = pubKey
	}

	return keyMap, nil
}

// ValidateToken validates a Cognito token against the provided JWKS URL and audience
func ValidateToken(ctx context.Context, tokenString, jwksURL, audience, issuer string) (bool, jwt.MapClaims, error) {
	keys, err := fetchCognitoJWKs(jwksURL)
	if err != nil {
		return false, nil, err
	}

	// Parse the JWT token
	token, err := jwt.Parse(tokenString, func(token *jwt.Token) (interface{}, error) {
		if _, ok := token.Method.(*jwt.SigningMethodRSA); !ok {
			return nil, fmt.Errorf("unexpected signing method: %v", token.Header["alg"])
		}

		// Get the kid from the token header
		kid, ok := token.Header["kid"].(string)
		if !ok {
			return nil, errors.New("kid header is missing or of wrong type")
		}

		// Look up the corresponding public key for the kid
		pubKey, found := keys[kid]
		if !found {
			return nil, fmt.Errorf("could not find public key with kid: %s", kid)
		}

		return pubKey, nil
	})

	if err != nil {
		return false, nil, err
	}

	// Validate claims
	claims, ok := token.Claims.(jwt.MapClaims)
	if !ok || !token.Valid {
		return false, nil, errors.New("invalid token")
	}

	// Check the issuer
	if claims["iss"] != issuer {
		return false, nil, fmt.Errorf("invalid issuer: %s", claims["iss"])
	}

	// Check the audience
	if claims["aud"] != audience {
		return false, nil, fmt.Errorf("invalid audience: %s", claims["aud"])
	}

	return true, claims, nil
}

func GetClaim(claims jwt.MapClaims, claimKey string) (string, error) {
	// Check if the token is valid
	if claims != nil {
		// Check if the roles claim exists and is an array of strings
		if claim, ok := claims[claimKey].(string); ok {
			if claim != "" {
				return claim, nil
			}

			return "", fmt.Errorf("claim key not found in claims")
		}

		return "", fmt.Errorf("claim key not found in claims")
	}

	return "", fmt.Errorf("invalid claims")
}
