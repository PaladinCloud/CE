/*
 *Copyright 2018 T Mobile, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); You may not use
 * this file except in compliance with the License. A copy of the License is located at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * or in the "license" file accompanying this file. This file is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express or
 * implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { Component, OnInit } from '@angular/core';
import { AdalService } from './core/services/adal.service';
import { CONFIGURATIONS } from './../config/configurations';


@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'app';

  constructor(private adalService: AdalService) {
    if (CONFIGURATIONS.optional.auth.AUTH_TYPE === 'azuresso') {
      adalService.init(CONFIGURATIONS.optional.auth.adConfig);
    }
  }

  ngOnInit() {
    if (CONFIGURATIONS.optional.auth.AUTH_TYPE === 'azuresso') {
      this.adalService.handleWindowCallback();
    }
  }
}
