import { Injectable } from '@angular/core';
import * as _ from 'lodash';

@Injectable({
  providedIn: 'root'
})
export class MockNotificationsDataService {

  DATA = [
    {
        "eventId": "eea93f11-21ea-47c0-ac8b-21fc7aab0eda",
        "eventName": "Sticky exception testing is deleted",
        "eventCategory": "EXEMPTIONS",
        "eventCategoryName": "Sticky Exceptions",
        "eventSource": "paladinCloud",
        "eventSourceName": "PaladinCloud",
        "eventDescription": "Sticky exception testing is deleted",
        "subject": "Sticky Exception Deleted",
        "payload": {
          "exceptionName": "testing",
          "userId": "5ka454tcv99sinuhjt7a1bknoc",
          "type": "sticky",
          "action": "DELETE"
        }
      },
      {
        "eventId": "4091aba0-92b2-40d1-8b06-42f9d0a3ee77",
        "eventName": "Exemption revoked for violation with id - 0012a121ca9f07c6f93d83549ba233c8",
        "eventCategory": "EXEMPTIONS",
        "eventCategoryName": "Violation Exemption",
        "eventSource": "paladinCloud",
        "eventSourceName": "PaladinCloud",
        "eventDescription": "Exemption revoked for violation with id - 0012a121ca9f07c6f93d83549ba233c8",
        "subject": "Exemption Revoked For A Policy Violation",
        "payload": {
          "resourceId": "sg-006319fb5cf59d8b3",
          "resourceIdLink": "https://dev.paladincloud.io/pl/assets/asset-list/sg/sg-006319fb5cf59d8b3?ag\u003daws",
          "issueId": "0012a121ca9f07c6f93d83549ba233c8",
          "issueIdLink": "https://dev.paladincloud.io/pl/compliance/issue-listing/issue-details/0012a121ca9f07c6f93d83549ba233c8?ag\u003daws",
          "policyName": "Assign Mandatory Tags to the Network Security Group",
          "policyNameLink": "https://dev.paladincloud.io/pl/compliance/policy-knowledgebase-details/TaggingRule_version-1_SgTaggingRule_sg/true?ag\u003daws",
          "type": "individual",
          "action": "REVOKE"
        }
      },
      {
        "eventId": "d5ab1436-8206-4a3d-9e7b-f6a4f7ed18fe",
        "eventName": "Sticky exception testing is created",
        "eventCategory": "EXEMPTIONS",
        "eventCategoryName": "Sticky Exceptions",
        "eventSource": "paladinCloud",
        "eventSourceName": "PaladinCloud",
        "eventDescription": "Sticky exception testing is created",
        "subject": "Sticky Exception Created",
        "payload": {
          "assetGroup": "aws",
          "exceptionName": "testing",
          "exceptionReason": "testingtestingtesting",
          "expiringOn": "31/03/2023",
          "policyNames": "Restrict Internet Access to EC2 Instance with Remotely Exploitable Vulnerability (S5)",
          "userId": "5ka454tcv99sinuhjt7a1bknoc",
          "type": "sticky",
          "action": "CREATE"
        }
      },
      {
        "eventId": "758c3f19-3d67-4e13-9650-9bdd31a87886",
        "eventName": "Autofix is not applied for exempted violation of policy Delete Unused Security Groups .",
        "eventCategory": "autofix",
        "eventCategoryName": "Autofix",
        "eventSource": "paladinCloud",
        "eventSourceName": "PaladinCloud",
        "eventDescription": "Autofix is not applied for exempted violation of policy Delete Unused Security Groups .",
        "subject": "Autofix Not Applied For An Exempted Violation",
        "payload": {
          "policyName": "Delete Unused Security Groups",
          "discoveredOn": "2023-03-17 07:13:50",
          "waitingTime": "24",
          "resourceId": "sg-0c3b899bc5449337c",
          "severity": "HIGH",
          "action": "AUTOFIX_ACTION_EXEMPTED",
          "issueId": "db9a7b3e4f5fdcf29a06e18b46d8ef81",
          "policyNameLink": "https://dev.paladincloud.io/pl/compliance/policy-knowledgebase-details/Unused-Security-group_version-1_UnusedSecurityGroup_sg/true?ag\u003daws",
          "issueIdLink": "https://dev.paladincloud.io/pl/compliance/issue-listing/issue-details/db9a7b3e4f5fdcf29a06e18b46d8ef81?ag\u003daws",
          "resourceIdLink": "https://dev.paladincloud.io/pl/assets/asset-list/sg/sg-0c3b899bc5449337c?ag\u003daws"
        }
      },
      {
        "eventId": "0ec81e6b-97d5-4583-9a99-b41ea2732a6d",
        "eventName": "Violation for policy - Delete Unused Security Groups",
        "eventCategory": "violations",
        "eventCategoryName": "Violations",
        "eventSource": "paladinCloud",
        "eventSourceName": "PaladinCloud",
        "eventDescription": "Violation for policy - Delete Unused Security Groups",
        "subject": "Policy Violation Created",
        "payload": {
          "resourceId": "sg-0c3b899bc5449337c",
          "resourceIdLink": "https://dev.paladincloud.io/pl/assets/asset-list/sg/sg-0c3b899bc5449337c?ag\u003daws",
          "issueId": "db9a7b3e4f5fdcf29a06e18b46d8ef81",
          "issueIdLink": "https://dev.paladincloud.io/pl/compliance/issue-listing/issue-details/db9a7b3e4f5fdcf29a06e18b46d8ef81?ag\u003daws",
          "policyName": "Delete Unused Security Groups",
          "policyNameLink": "https://dev.paladincloud.io/pl/compliance/policy-knowledgebase-details/Unused-Security-group_version-1_UnusedSecurityGroup_sg/true?ag\u003daws",
          "description": "Unused security group found!!",
          "scanTime": "2023-04-04T06:15:51.356Z",
          "action": "create",
          sla: "24 hours",
          isAutofixAvailable: true
        }
      }
];



  constructor() { }


  getData(){
    return _.cloneDeep(this.DATA);
  }

  getDetailsById(id){
    let obj = _.find(this.DATA, {
      "eventId": id
    });

    return _.cloneDeep(obj);
  }
}
