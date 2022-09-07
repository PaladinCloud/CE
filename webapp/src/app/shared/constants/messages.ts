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

export const MESSAGES = {
    'errorMessages': {
        'apiResponseError': {
            'title': 'Uh-oh!',
            'description': 'Something went wrong while we were fetching your data.',
            'image': '/assets/images/error-state.svg'
        },
        'noDataAvailable': {
            'title': '',
            'description': 'Doesn\'t look like any data is available',
            'image': '/assets/images/empty-state.svg'
        },
        'jsError': {
            'title': 'Uh-oh!',
            'description': 'Something went wrong.',
            'image': '/assets/images/error-state.svg'
        },
        'patchingMessage': {
            'title': 'Great!',
            'description': 'All assets are compliant',
            'image': '/assets/images/empty-state.svg'
        },
        'certificateMessage': {
            'title': 'Great!',
            'description': 'No certificates are expiring for you in next 45 days',
            'image': '/assets/images/empty-state.svg'
        },
        'certificateTableMessage': {
            'title': '',
            'description': 'No certificates found in this asset group',
            'image': '/assets/images/empty-state.svg'
        },
        'vulnerabilityMessage': {
            'title': 'Great!',
            'description': 'No vulnerabilities found for the assets in this asset group',
            'image': '/assets/images/empty-state.svg'
        },
        'taggingMessage': {
            'title': 'Great!',
            'description': 'All assets are tagged for this asset group',
            'image': '/assets/images/empty-state.svg'
        },
        'policyDetailsTableMessage': {
            'title': 'Great!',
            'description': 'No open policy violations found for this asset group',
            'image': '/assets/images/empty-state.svg'
        },
        'patchingDataTableMessage': {
            'title':'',
            'description': 'No instances found in this asset group',
            'image': '/assets/images/empty-state.svg'
        },
        'policyDetailsMessage': {
            'title': '',
            'description': 'None of the applications are associated with this policy',
            'image': '/assets/images/empty-state.svg'
        },
        'dataTableMessage': {
            'title': 'Ohh!',
            'description': 'No results found for this criteria',
            'image': '/assets/images/empty-state.svg'
        },
        'noDataforAssetGroup': {
            'title': 'Oops, It\'s deserted here!',
            'description': 'Doesn\'t look like any data is available for this asset group',
            'image': '/assets/images/empty-state.svg'
        },
        'policySummaryTableMessage': {
            'title': 'Ohh!',
            'description': 'None of the policies are associated in this asset group',
            'image': '/assets/images/empty-state.svg'
        },
        'taggingTargetMessage': {
            'title': '',
            'description': 'There are no assets in this asset group',
            'image': '/assets/images/empty-state.svg'
        },
        'violationMessage': {
            'title': 'Great!',
            'description': 'No Violations for this Resource ID',
            'image': '/assets/images/empty-state.svg'
        },
        'vulnerabilitiesMessage': {
            'title': 'Great!',
            'description': 'No Vulnerabilities for this Resource ID',
            'image': '/assets/images/empty-state.svg'
        },
        'awsNotificationMessage': {
            'title': '',
            'description': 'No AWS notifications found',
            'image': '/assets/images/empty-state.svg'
        },
        'noPullRequestsFound': {
            'title': '',
            'description': 'No Pull requests found for this criteria',
            'image': '/assets/images/empty-state.svg'
        },
        'noBranchesFound': {
            'title': '',
            'description': 'No Branches found for this criteria',
            'image': '/assets/images/empty-state.svg'
        },
        'noPolicyFound': {
            'title': '',
            'description': 'There are no active Policies for this Asset Type.',
            'image': '/assets/images/empty-state.svg'
        },
        'noSearchFound': {
            'title': 'No Search Found!!',
            'description': 'Retry searching something else.',
            'image': '/assets/images/empty-search.svg'
        }
    }
};
