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
package com.tmobile.pacman.api.compliance.domain;

import java.util.Date;
import java.util.Map;
/**
 * The Class CompliantTrendRequest.
 */
public class CompliantTrendRequest {

    /** The ag. */
    private String ag;

    /** The from. */
    private Date from;

    private Date to;

    /** The filters. */
    private Map<String, String> filters;

    /**
     * Gets the filters.
     *
     * @return the filters
     */
    public Map<String, String> getFilters() {
        return filters;
    }

    /**
     * Sets the filters.
     *
     * @param filters the filters
     */
    public void setFilters(Map<String, String> filters) {
        this.filters = filters;
    }

    /**
     * Gets the ag.
     *
     * @return the ag
     */
    public String getAg() {
        return ag;
    }

    /**
     * Sets the ag.
     *
     * @param ag the new ag
     */
    public void setAg(String ag) {
        this.ag = ag;
    }

    /**
     * Gets the from.
     *
     * @return the from
     */
    public Date getFrom() {
        return from;
    }

    /**
     * Sets the from.
     *
     * @param from the new from
     */
    public void setFrom(Date from) {
        this.from = from;
    }
    public Date getTo() {
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }

}