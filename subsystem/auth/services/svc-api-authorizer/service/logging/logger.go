/*
 * Copyright (c) 2024 Paladin Cloud, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package logger

import (
	"encoding/json"
	"fmt"
	"log"
)

// Logger is a struct that holds the logger instance with an optional prefix
type Logger struct {
	prefix string
}

// NewLogger creates a new instance of Logger with an optional prefix
func NewLogger(prefix ...string) *Logger {
	// Check if a prefix is provided
	var pfx string
	if len(prefix) > 0 {
		pfx = prefix[0]
	}
	return &Logger{prefix: pfx}
}

// Info logs an info message
func (l *Logger) Info(message string, data ...interface{}) {
	l.log("INFO", message, data...)
}

// Debug logs a debug message
func (l *Logger) Debug(message string, data ...interface{}) {
	l.log("DEBUG", message, data...)
}

// Error logs an error message
func (l *Logger) Error(message string, data ...interface{}) {
	l.log("ERROR", message, data...)
}

// log is a helper function to format and output the log message
func (l *Logger) log(level string, message string, data ...interface{}) {
	var output string

	if len(data) > 0 {
		switch v := data[0].(type) {
		case string:
			// If the data is a string, use it as is
			output = v
		case error:
			// If the data is an error, use the error message
			output = v.Error()
		default:
			// Otherwise, try to marshal it to JSON
			jsonBytes, err := json.Marshal(v)
			if err != nil {
				output = fmt.Sprintf("[ERROR] Failed to marshal JSON: %v", err)
			} else {
				output = string(jsonBytes)
			}
		}
	}

	// Include the prefix in the log output if it's set
	if l.prefix != "" {
		log.Printf("[%s] %s: %s - %s", level, l.prefix, message, output)
	} else {
		log.Printf("[%s] %s: %s", level, message, output)
	}
}
