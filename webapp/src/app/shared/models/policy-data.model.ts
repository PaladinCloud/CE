export interface PolicyData {
    alexaKeyword: string;
    allowList: string | null;
    assetGroup: string;
    autoFixAvailable: string;
    autoFixEnabled: string;
    category: string;
    createdDate: Date;
    elapsedTime: number;
    fixMailSubject: string | null;
    fixMessage: string | null;
    fixType: string | null;
    maxEmailNotification: number;
    modifiedDate: Date;
    policyArn: string;
    policyDesc: string;
    policyDisplayName: string;
    policyExecutable: string;
    policyFrequency: string;
    policyId: string;
    policyName: string;
    policyParams: string;
    policyRestUrl: string;
    policyType: string;
    policyUUID: string;
    resolution: string;
    resolutionUrl: string;
    severity: string;
    status: string;
    targetType: string;
    templateColumns: string | null;
    templateName: string | null;
    userId: string;
    violationMessage: string | null;
    waitingTime: number;
    warningMailSubject: string | null;
    warningMessage: string | null;
    disableDesc: string | null;
    exemptionDetails?: ExemptionDetails | null;
}

export interface ExemptionDetails {
    "expiryDate": string,
    "assetGroup": string,
    "policyId": string,
    "exceptionReason": string,
    "dataSource": string,
    "exceptionName": string,
    "createdBy"?: string,
    "createdOn"?: string
};

export interface PolicyParams {
    key: string;
    value: string;
    encrypt: boolean;
    isEdit?: boolean;
    isMandatory?: boolean;
    description?: string;
    defaultVal?: string;
    displayName?: string;
    isValueNew?: boolean;
}