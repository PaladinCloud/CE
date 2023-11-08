/*******************************************************************************
 *  Copyright 2023 Paladin Cloud, Inc. or its affiliates. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License.  You may obtain a copy
 *  of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 ******************************************************************************/
package com.tmobile.pacman.api.admin.factory;

import com.tmobile.pacman.api.admin.exceptions.PluginNotFoundException;
import com.tmobile.pacman.api.admin.repository.service.plugins.PluginsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PluginFactory {
    private final List<PluginsService> services;

    public PluginFactory(List<PluginsService> services) {
        this.services = services;
    }

    /**
     * Retrieves PluginsService based on a type.
     *
     * @param type The plugin type to search for.
     * @return The matching PluginsService.
     * @throws PluginNotFoundException if no matching PluginsService is found.
     */
    public PluginsService getService(String type) throws PluginNotFoundException {
        return services.stream()
                .filter(service -> service.getClass().getSimpleName().toLowerCase().startsWith(type))
                .findFirst()
                .orElseThrow(() -> new PluginNotFoundException("PluginsService not found for plugin type: " + type));
    }
}
