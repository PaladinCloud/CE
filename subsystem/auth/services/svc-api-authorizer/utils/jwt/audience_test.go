/*
 * Copyright (c) 2026 Paladin Cloud, Inc. or its affiliates. All Rights Reserved.
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

import "testing"

func TestIsAllowedAudience(t *testing.T) {
	cases := []struct {
		name     string
		aud      interface{}
		allowed  string
		expected bool
	}{
		{"single match", "legacy", "legacy", true},
		{"single mismatch", "web", "legacy", false},
		{"list first", "legacy", "legacy,web", true},
		{"list second", "web", "legacy,web", true},
		{"list with spaces", "web", " legacy , web ", true},
		{"list mismatch", "other", "legacy,web", false},
		{"empty allowed", "legacy", "", false},
		{"empty aud", "", "legacy,web", false},
		{"aud array match", []interface{}{"x", "web"}, "legacy,web", true},
		{"aud array mismatch", []interface{}{"x", "y"}, "legacy,web", false},
		{"missing aud", nil, "legacy,web", false},
	}

	for _, c := range cases {
		if got := isAllowedAudience(c.aud, c.allowed); got != c.expected {
			t.Errorf("%s: isAllowedAudience(%v, %q) = %v, want %v", c.name, c.aud, c.allowed, got, c.expected)
		}
	}
}
