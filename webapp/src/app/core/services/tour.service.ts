import { Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { ShepherdService } from 'angular-shepherd';
import { of, Subject } from 'rxjs';
import { catchError, delay, first, timeout } from 'rxjs/operators';
import Step from 'shepherd.js/src/types/step';

@Injectable({
    providedIn: 'root',
})
export class TourService {
    private readonly componentReadyTimeout = 2000;
    private isInitialized = false;
    private isWaitingForComponent = false;
    private componentReady$ = new Subject<void>();

    private builtInButtons: { [key in 'cancel' | 'next' | 'done']: Step.StepOptionsButton } = {
        cancel: {
            classes: 'cancel-button',
            secondary: true,
            text: 'Cancel',
            action: () => this.stop(),
        },
        next: {
            classes: 'next-button',
            text: 'Next',
        },
        done: {
            classes: 'done-button',
            text: 'Done',
            action: () => this.shepherd.complete(),
        },
    };

    private defaultStepOptions: Step.StepOptions = {
        classes: 'shepherd-theme-arrows shepherd-paladincloud-theme',
        cancelIcon: {
            enabled: true,
        },
        arrow: true,
        canClickTarget: false,
        popperOptions: {
            modifiers: [{ name: 'offset', options: { offset: [0, 12] } }],
        },
    };

    constructor(private router: Router, private shepherd: ShepherdService,
        private dialog: MatDialog) {}

    init(userRoles) {
        if (this.isInitialized) {
            return;
        }

        this.shepherd.defaultStepOptions = this.defaultStepOptions;
        this.shepherd.modal = true;
        this.shepherd.confirmCancel = false;
        this.shepherd.addSteps(this.buildSteps(userRoles));
        // TODO: hack due typings are available only since v15
        this.shepherd.tourObject['options']['keyboardNavigation'] = false;
        this.isInitialized = true;
    }

    start() {
        this.shepherd.start();
    }

    stop() {
        this.shepherd.complete();
    }

    setComponentReady() {
        this.componentReady$.next();
    }

    private buildSteps(userRoles): Step.StepOptions[] {
        let userType = "ReadOnly";
        if(userRoles.includes("ROLE_ADMIN")){
            userType = "Admin";
        }else if(userRoles.includes("ROLE_USER")){
            userType = "User";
        }

        switch(userType){
            case "User":
                return this.getReadOnlyUserSteps();
            case "Admin":
                return this.getTechnicalAdminSteps();
            default:
                return this.getReadOnlyUserSteps();
        }
    }

    private getCommonSteps(): Step.StepOptions[] {
        return [
            {
                attachTo: {
                    element: '.asset-switcher-container',
                    on: 'right',
                },
                buttons: [
                    this.builtInButtons.cancel,
                    {
                        ...this.builtInButtons.next,
                        action: async () => {
                            await this.router.navigate(
                                ['/pl', { outlets: { modal: ['change-default-asset-group'] } }],
                                { queryParamsHandling: 'merge' },
                            );

                            this.waitForComponentReady();
                        },
                    },
                ],
                id: 'intro',
                title: 'Asset Group Switcher',
                text: 'Change asset groups easily by selecting this drop-down menu',
            },
            {
                attachTo: {
                    element: '.mat-dialog-actions > app-custom-button',
                    on: 'top',
                },
                buttons: [
                    this.builtInButtons.cancel,
                    {
                        ...this.builtInButtons.next,
                        action: async() => {
                            this.dialog.closeAll();
                            await this.router.navigate(
                                [
                                    {
                                        outlets: {
                                            modal: null,
                                        },
                                    },
                                ],
                                {
                                    relativeTo: this.router.routerState.root.firstChild,
                                    queryParamsHandling: 'merge',
                                },
                            );
                            const assetSummaryRoute = 'pl/assets/asset-dashboard';
                            if (
                                this.router.isActive(assetSummaryRoute, {
                                    fragment: 'ignored',
                                    matrixParams: 'ignored',
                                    paths: 'exact',
                                    queryParams: 'ignored',
                                })
                            ) {
                                this.shepherd.next();
                                return;
                            }
                            await this.router.navigate(
                                [assetSummaryRoute],
                                { queryParamsHandling: 'merge' },
                            );
                            this.waitForComponentReady();
                        },
                    },
                ],
                id: 'asset-btn-select',
                title: 'Set Default Asset Group',
                text: 'To set your preferred default asset group, simply choose the desired group and click the button.',
            },
            {
                attachTo: {
                    element: '.date-dropdown-container app-dropdown',
                    on: 'bottom',
                },
                buttons: [
                    this.builtInButtons.cancel,
                    {
                        ...this.builtInButtons.next,
                        action: async () => {

                            const policyKnowledgeBaseRoute = 'pl/compliance/policy-knowledgebase';

                            if (
                                this.router.isActive(policyKnowledgeBaseRoute, {
                                    fragment: 'ignored',
                                    matrixParams: 'ignored',
                                    paths: 'exact',
                                    queryParams: 'ignored',
                                })
                            ) {
                                this.shepherd.next();
                                return;
                            }

                            await this.router.navigate([policyKnowledgeBaseRoute], {
                                queryParamsHandling: 'merge',
                            });

                            this.waitForComponentReady();
                        },
                    },
                ],
                id: 'overal-compliance-trend',
                title: 'Graph Data Filtering',
                text: 'Effortlessly Filter Graph Data with Date Range Selection',
            },
            {
                attachTo: {
                    element:
                        '.policy-knowledgebase-content .table-container mat-table > mat-row',
                    on: 'bottom',
                },
                buttons: [
                    this.builtInButtons.cancel,
                    {
                        ...this.builtInButtons.next,
                        action: () => {
                            this.shepherd.next();
                        },
                    },
                ],
                id: 'policy-row-select',
                title: 'Interactive Table',
                text: 'To access more detailed information, simply click on any row within the table.',
            }
        ];
    }

    private getReadOnlyUserSteps(): Step.StepOptions[] {
        const steps: Step.StepOptions[] = [
            {
                attachTo: {
                    element: '.table-header .filters-menu-btn',
                    on: 'right',
                },
                buttons: [this.builtInButtons.cancel, this.builtInButtons.done],
                id: 'policy-filters',
                title: 'Filtered View',
                text: 'Tailor your view by utilizing the page-level filter and tagging system to display specific content',
            },
        ];
        return [...this.getCommonSteps(), ...steps];
    }

    private getTechnicalAdminSteps(): Step.StepOptions[]{
        const steps: Step.StepOptions[] = [
            {
                attachTo: {
                    element: '.table-header .filters-menu-btn',
                    on: 'right',
                },
                buttons: [this.builtInButtons.cancel,
                    {
                        ...this.builtInButtons.next,
                        action: async() => {
                            const createAssetGroupRoute = 'pl/admin/asset-groups';

                            if (
                                this.router.isActive(createAssetGroupRoute, {
                                    fragment: 'ignored',
                                    matrixParams: 'ignored',
                                    paths: 'exact',
                                    queryParams: 'ignored',
                                })
                            ) {
                                this.shepherd.next();
                                return;
                            }

                            await this.router.navigate([createAssetGroupRoute], {
                                queryParamsHandling: 'merge',
                            });

                            this.waitForComponentReady();
                        },
                    },
                ],
                id: 'policy-filters',
                title: 'Filtered View',
                text: 'Tailor your view by utilizing the page-level filter and tagging system to display specific content',
            },
            {
                attachTo: {
                    element: '.asset-groups-wrapper app-custom-button',
                    on: 'left',
                },
                buttons: [this.builtInButtons.cancel,
                    {
                        ...this.builtInButtons.next,
                        action: async() => {
                            const pluginsRoute = 'pl/admin/account-management';

                            if (
                                this.router.isActive(pluginsRoute, {
                                    fragment: 'ignored',
                                    matrixParams: 'ignored',
                                    paths: 'exact',
                                    queryParams: 'ignored',
                                })
                            ) {
                                this.shepherd.next();
                                return;
                            }

                            await this.router.navigate([pluginsRoute], {
                                queryParamsHandling: 'merge',
                            });

                            this.waitForComponentReady();
                        },
                    },
                ],
                id: 'create-asset-group-btn',
                title: 'New Asset Group',
                text: 'To create a new asset group, click on this button',
            },
            {
                attachTo: {
                    element: '.account-management-page-wrapper > .page-header > .button',
                    on: 'left',
                },
                buttons: [this.builtInButtons.cancel,
                    {
                        ...this.builtInButtons.next,
                        action: async() => {
                            const policyId = "SGWithAnywhereAccess_version-1_SgWithAnywhereAccess_sg";
                            const policyDetailsRoute = 'pl/admin/policies/create-edit-policy';

                            if (
                                this.router.isActive(policyDetailsRoute, {
                                    fragment: 'ignored',
                                    matrixParams: 'ignored',
                                    paths: 'exact',
                                    queryParams: 'ignored',
                                })
                            ) {
                                this.shepherd.next();
                                return;
                            }

                            await this.router.navigate([policyDetailsRoute], {
                                queryParams: {
                                    policyId
                                },
                                queryParamsHandling: 'merge',
                            });

                            this.waitForComponentReady();
                        },
                    },
                ],
                id: 'add-plugin-button',
                title: 'Cloud Account Integration',
                text: 'Plugins lets you connect your cloud accounts to the paladin cloud',
            },
            {
                attachTo: {
                    element: '.create-edit-policy .right-wrapper .details-wrapper',
                    on: 'bottom',
                },
                buttons: [this.builtInButtons.cancel,
                    {
                        ...this.builtInButtons.next,
                        action: async() => {
                            const usersRoute = 'pl/admin/user-management';

                            if (
                                this.router.isActive(usersRoute, {
                                    fragment: 'ignored',
                                    matrixParams: 'ignored',
                                    paths: 'exact',
                                    queryParams: 'ignored',
                                })
                            ) {
                                this.shepherd.next();
                                return;
                            }

                            await this.router.navigate([usersRoute], {
                                queryParams: {policyId: undefined},
                                queryParamsHandling: 'merge',
                            });
                            this.waitForComponentReady();
                        },
                    },
                ],
                id: 'policy-details-screen',
                title: 'Policy Management',
                text: 'You have the ability to edit policy settings, save changes, and enable or disable policies.',
            },
            {
                attachTo: {
                    element: '.user-management-page-wrapper > .page-header > app-custom-button',
                    on: 'left',
                },
                buttons: [this.builtInButtons.cancel,
                    {
                        ...this.builtInButtons.next,
                        ...this.builtInButtons.done,
                    },
                ],
                id: 'add-user-button',
                title: 'New User',
                text: 'You can add new Users and assign roles to them',
            },
        ];
        return [...this.getCommonSteps(), ...steps];
    }

    private waitForComponentReady() {
        if (this.isWaitingForComponent) {
            return;
        }
        this.isWaitingForComponent = true;
        this.componentReady$
            .pipe(
                first(),
                delay(50),
                timeout(this.componentReadyTimeout),
                catchError(() => of(null)),
            )
            .subscribe(() => {
                this.isWaitingForComponent = false;
                this.shepherd.next();
            });
    }
}
