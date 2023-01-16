export class ExceptionInput {
    common: {
        allResourceIds: Array<string>;
        allTargetTypes: Array<string>;
        resourceType: string;
        disablePolicy: Boolean;
    };
    policyId: string;
    ruleName: string;
    expiringIn: any;
    exceptionReason: string;
    exceptionEndDate: Date;
    allPolicyIds: Array<Object>;
}
