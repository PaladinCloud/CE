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

import { Component, OnInit, Input, TemplateRef, ViewChild, OnChanges, SimpleChanges } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { Clipboard } from '@angular/cdk/clipboard';
import { AssetGroupObservableService } from "../../../core/services/asset-group-observable.service";
import { DomainTypeObservableService } from "../../../core/services/domain-type-observable.service";
import { Subscription } from "rxjs";
import { WorkflowService } from "../../../core/services/workflow.service";
import { LoggerService } from "../../../shared/services/logger.service";
import { MatDialog } from "@angular/material/dialog";
import { DialogBoxComponent } from "src/app/shared/components/molecules/dialog-box/dialog-box.component";

@Component({
  selector: "app-policy-violation-desc",
  templateUrl: "./policy-violation-desc.component.html",
  styleUrls: ["./policy-violation-desc.component.css"],
  providers: [LoggerService],
})
export class PolicyViolationDescComponent implements OnInit, OnChanges {
  @Input() violationData;
  @Input() autofixData;
  @Input() pageLevel: number;
  @Input() breadcrumbPresent;
  urlToRedirect = "";
  private subscriptionToAssetGroup: Subscription;
  private domainSubscription: Subscription;
  selectedAssetGroup: string;
  selectedDomain: string;
  accordionData: any;
  labelData: any;
  Params: any;
  showAccordion = true;
  testData;
  public agAndDomain = {};
  violationId: any;
  vulnerabilityDetails;

  @ViewChild("vulnerabilityDialogRef") vulnerabilityDialogRef: TemplateRef<any>;
  constructor(
    private router: Router,
    private clipboard: Clipboard,
    private activatedRoute: ActivatedRoute,
    private assetGroupObservableService: AssetGroupObservableService,
    private domainObservableService: DomainTypeObservableService,
    private workflowService: WorkflowService,
    private logger: LoggerService,
    private dialog: MatDialog
  ) {
    this.subscriptionToAssetGroup = this.assetGroupObservableService
      .getAssetGroup()
      .subscribe((assetGroupName) => {
        this.selectedAssetGroup = assetGroupName;
        this.agAndDomain["ag"] = this.selectedAssetGroup;
      });
    // domain subscription
    this.domainSubscription = this.domainObservableService
      .getDomainType()
      .subscribe((domain) => {
        this.selectedDomain = domain;
        this.agAndDomain["domain"] = this.selectedDomain;
      });
    // processData for accordion
  }

  ngOnChanges(changes: SimpleChanges): void {
    try{
      if(this.violationData?.vulnerabilityDetails?.length)
        this.vulnerabilityDetails = JSON.parse(JSON.parse(this.violationData?.vulnerabilityDetails[0]?.vulnerabitityDetails));
    }catch(e){
      this.logger.log("Error parsing vulnerabilityDetails JSON object --", e);
      this.vulnerabilityDetails = [];
    }
  }

  ngOnInit() {
    this.urlToRedirect = this.router.routerState.snapshot.url;
    this.violationId = this.activatedRoute.snapshot.params.issueId;
    this.processDataForAccordion(this.violationData);
  }
  /**
   * @func processDataForAccordion
   * @param data data to process
   * @desc processes the policy violation data(mainly the accordion data)
   */


   copyToClipboard(text: string) {
    this.clipboard.copy(text);
  }

  openVulnerabilityDialog(){
    const dialogRef = this.dialog.open(DialogBoxComponent, {
      width: '80%',
      data: {
        title: "Vulnerability Information",
          template: this.vulnerabilityDialogRef
        },
      });
  }

  processDataForAccordion(data) {
    try {
      let checkifJsonString;
      this.accordionData = [];
      this.labelData = [];
      const dataToBeChecked = data.violationDetails[0];
      // check if there are nested obj inside
      Object.keys(dataToBeChecked).forEach((element) => {
        if (dataToBeChecked[element]) {
          if(element=="violation_title"){
            return;
          }
          if(element == "qualysViolationDetails"){
            let data = this.processStringToArray(dataToBeChecked[element])

            const dataToPush = {
              labelName: element.replace(/_/g, " "), // remove the '_' from the key name before pushing
              labelCount: null,
              values: null,
              isAccordion: false,
              data: data
            };

            this.labelData.push(dataToPush);
          }else if(typeof dataToBeChecked[element] == "string"){
          checkifJsonString = dataToBeChecked[element].search("{");
          // .search returns returns -1 if string doesn't exists
          if (!(checkifJsonString === -1)) {
            const arrayValues = [];
            const innerObj = JSON.parse(dataToBeChecked[element]);
            Object.keys(innerObj).forEach((elementinner) => {
              const eachObj = {
                labelName: elementinner.replace(/_/g, " "), // remove the '_' from the key name before pushing,
                labelCount: innerObj[elementinner],
              };
              arrayValues.push(eachObj);
            });

            const dataToPushForAccorDion = {
              labelName: element.replace(/_/g, " "), // remove the '_' from the key name before pushing
              labelCount: null,
              values: arrayValues,
              isAccordion: true,
            };
            this.labelData.push(dataToPushForAccorDion);
          } else {
            const dataToPush = {
              labelName: element.replace(/_/g, " "), // remove the '_' from the key name before pushing
              labelCount: dataToBeChecked[element],
              values: null,
              isAccordion: false,
            };
            this.labelData.push(dataToPush);
          }
          }
        }
      });
    } catch (e) {
      this.logger.log("error", e);
    }
  }
  processStringToArray(dataToProcess) {
    let processedData = []; // array of objects
    dataToProcess.split(",").forEach(item => {
      const splittedArr = item.split(";");
      processedData.push({
        cveNumber: splittedArr[0],
        link: splittedArr[1],
        displayName: splittedArr[2]
      });
    })
    return processedData;
  }

  /**
   * @func closeNestedAccordion
   * @desc to navigate to different link
   */

  /**
   * @func closeAccordion
   * @param event
   * @desc need to add code to hide accordion on click of outside
   */

  closeAccordion(event) {
    // code to hide the accordion
    // this.showAccordion = false
  }

  closePopup() {
    this.showAccordion = false;
  }

  /**
   * @func navigateTo
   * @param destination (link to go)
   * @param id1 ruleid or resoueceid
   * @desc to navigate to different link
   */

  navigateTo(destination, id1?, id2?) {
    this.workflowService.addRouterSnapshotToLevel(
      this.router.routerState.snapshot.root, 0, this.breadcrumbPresent
    );
    try {
      if (destination === "asset details") {
        const resourceId = encodeURIComponent(id1);
        const resourceType = encodeURIComponent(id2);
        this.workflowService.navigateTo({
          urlArray: ["../../../../", "assets", "asset-list", resourceType, resourceId],
          queryParams: {},
          relativeTo: this.activatedRoute,
          currPagetitle: this.breadcrumbPresent,
          nextPageTitle: "Asset Details"
        });
        // window.open("/pl/assets/asset-list/"+resourceType+"/"+resourceId);
      } else if (destination === "policy knowledgebase details") {
        const policyId = encodeURIComponent(id1);
        this.router.navigate(["../../../policy-knowledgebase-details", policyId, "false"], {
          relativeTo: this.activatedRoute,
          queryParamsHandling: "merge",
        });
        // window.open("/pl/compliance/policy-knowledgebase-details/"+policyId+"/false", "_blank");
      }
    } catch (e) {
      this.logger.log("error", e);
    }
  }
}
