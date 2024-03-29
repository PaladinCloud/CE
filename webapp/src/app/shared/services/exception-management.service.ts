import { Injectable } from '@angular/core';
import { Exception } from '../models/exception.model';
import { ExceptionInput } from '../models/exception-input.model';
import { DatePipe } from '@angular/common';
@Injectable()
export class ExceptionManagementService {
    private readonly shortDateFormat = 'yyyy-MM-dd';

    constructor(private datePipe: DatePipe) {}

    createDataToAddOrModifyException(input: ExceptionInput) {
        // input should be an object with below values:
        const inputToExtendException = new Exception();

        const today = new Date();
        const resetSelectedDateToExpiry = new Date();
        const minDate = new Date();

        let policy;
        if (input.policyId && input.ruleName) {
            policy = {
                id: input.policyId || '',
                name: input.ruleName || '',
            };
        }

        const numberOfDaysToBeAdded = today.getDate() + 1;
        minDate.setDate(numberOfDaysToBeAdded);
        resetSelectedDateToExpiry.setDate(numberOfDaysToBeAdded);
        // Set the min date to expiring in date
        inputToExtendException.minDate = minDate;
        inputToExtendException.selectedDate = this.datePipe.transform(
            input.exceptionEndDate ? input.exceptionEndDate : resetSelectedDateToExpiry,
            this.shortDateFormat,
        );

        // Get year, month and date string, for calendard pre selected date
        inputToExtendException.disablePolicy = input.common.disablePolicy;
        inputToExtendException.exceptionReason = input.exceptionReason || '';
        inputToExtendException.targetTypeExemptionMap = [{}];

        inputToExtendException.targetTypeExemptionMap[0].policyIds = policy ? [policy] : [];
        inputToExtendException.targetTypeExemptionMap[0].targetType = input.common.resourceType;
        inputToExtendException.targetTypeExemptionMap[0].resourceIds = input.common.allResourceIds;

        inputToExtendException.allResourceIds = input.common.allResourceIds;
        inputToExtendException.allTargetTypes = input.common.allTargetTypes || [];
        inputToExtendException.allPolicyIds = policy ? [policy] : [];

        return inputToExtendException;
    }
}
