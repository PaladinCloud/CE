/*******************************************************************************
 * Copyright 2018 T Mobile, Inc. or its affiliates. All Rights Reserved.
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
 ******************************************************************************/
/**
  Copyright (C) 2017 T Mobile Inc - All Rights Reserve
  Purpose:
  Author :kkumar
  Modified Date: Jun 14, 2017

**/
package com.tmobile.pacman.commons.policy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
// TODO: Auto-generated Javadoc

/**
 * The Interface PacmanRule.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PacmanPolicy {

	/**
	 * Key.
	 *
	 * @return the string
	 */
	String key() default "pacman-policy-";

	/**
	 * Desc.
	 *
	 * @return the string
	 */
	String desc();

	/**
	 * Severity.
	 *
	 * @return the string
	 */
	String severity();

	/**
	 * Category.
	 *
	 * @return the string
	 */
	String category();
}
