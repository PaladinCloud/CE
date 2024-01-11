/**
 Copyright (C) 2017 T Mobile Inc - All Rights Reserve
 Purpose:
 Author :kkumar28
 Modified Date: Dec 24, 2018
 **/
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
package com.tmobile.pacman.reactors;

import com.google.gson.JsonObject;

import java.util.concurrent.Callable;

/**
 * @author kkumar28
 */
public interface Reactor extends Callable<Reaction> {

    /**
     * method to perform the action in reponse to an event
     *
     * @param event
     * @return
     * @throws ReactException
     */
    public Reaction react(JsonObject event) throws ReactException;

    /**
     * method provides opportunity to backup the old config
     *
     * @param event
     * @return
     * @throws ReactException
     */
    public Boolean backup(JsonObject event) throws ReactException;

}
