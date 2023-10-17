/*******************************************************************************
 * Copyright 2023 Paladin Cloud, Inc. or its affiliates. All Rights Reserved.
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
package com.tmobile.cso.pacman.aqua.dto;

import java.util.Date;

public class Result {

    private String name;
    private String description;
    private Date publish_date;
    private Date modification_date;
    private String vendor_severity;
    private String image_name;

    public Result(String name, String description, Date publish_date, Date modification_date, String vendor_severity, String image_name) {
        this.name = name;
        this.description = description;
        this.publish_date = publish_date;
        this.modification_date = modification_date;
        this.vendor_severity = vendor_severity;
        this.image_name = image_name;
    }

    public String getImage_name() {
        return image_name;
    }

    public void setImage_name(String image_name) {
        this.image_name = image_name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getPublish_date() {
        return publish_date;
    }

    public void setPublish_date(Date publish_date) {
        this.publish_date = publish_date;
    }

    public Date getModification_date() {
        return modification_date;
    }

    public void setModification_date(Date modification_date) {
        this.modification_date = modification_date;
    }

    public String getVendor_severity() {
        return vendor_severity;
    }

    public void setVendor_severity(String vendor_severity) {
        this.vendor_severity = vendor_severity;
    }

}
