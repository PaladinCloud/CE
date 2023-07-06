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
  Author :santoshi
  Modified Date: Dec 9, 2017

 **/
package com.tmobile.pacman.api.compliance.domain;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * The DTO Class ResponseWithOrder for API response.
 */
public class ResponseWithOrder {

    /** The response. */
    List<LinkedHashMap<String, Object>> response;

    /** The total. */
    long total;



    /**
     * Instantiates a new response with order.
     */
    public ResponseWithOrder() {
        super();
    }

    /**
     * Instantiates a new response with order.
     *
     * @param response the response
     * @param total the total
     */
    public ResponseWithOrder(List<LinkedHashMap<String, Object>> response,
            long total) {
        super();
        this.response = response;
        this.total = total;
    }

    /**
     * Gets the response.
     *
     * @return the response
     */
    public List<LinkedHashMap<String, Object>> getResponse() {
        return response;
    }

    /**
     * Sets the response.
     *
     * @param response the response
     */
    public void setResponse(List<LinkedHashMap<String, Object>> response) {
        this.response = response;
    }

    /**
     * Gets the total.
     *
     * @return the total
     */
    public long getTotal() {
        return total;
    }

    /**
     * Sets the total.
     *
     * @param total the new total
     */
    public void setTotal(long total) {
        this.total = total;
    }

}
