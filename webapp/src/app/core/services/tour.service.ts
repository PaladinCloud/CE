import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { ShepherdService } from 'angular-shepherd';
import Step from 'shepherd.js/src/types/step';

@Injectable({
    providedIn: 'root',
})
export class TourService {
    private isInitialized = false;

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
        scrollTo: true,
        cancelIcon: {
            enabled: true,
        },
        arrow: true,
        canClickTarget: false,
    };

    constructor(private router: Router, private shepherd: ShepherdService) {}

    init() {
        if (this.isInitialized) {
            return;
        }

        this.shepherd.defaultStepOptions = this.defaultStepOptions;
        this.shepherd.modal = true;
        this.shepherd.confirmCancel = false;
        this.shepherd.addSteps(this.buildSteps());
        this.isInitialized = true;
    }

    start() {
        this.shepherd.start();
    }

    stop() {
        this.shepherd.complete();
    }

    private buildSteps(): Step.StepOptions[] {
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
                            setTimeout(() => {
                                this.shepherd.next();
                            }, 100);
                        },
                    },
                ],
                id: 'intro',
                title: 'Welcome to Paladin Cloud',
                text: 'Change Asset Groups anytime by selecting the header drop-down',
            },
            {
                attachTo: {
                    element: '.side-btn-wrap',
                    on: 'left-start',
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
                id: 'asset-btn-select',
                text: 'Choose any Asset Group and set it to be your default',
            },
            {
                attachTo: {
                    element: '.asset-tile-content > div:not(.currently-selected)',
                    on: 'right',
                },
                buttons: [
                    this.builtInButtons.cancel,
                    {
                        ...this.builtInButtons.next,
                        action: async () => {
                            await this.router.navigate(
                                ['/pl', { outlets: { modal: ['overall-compliance-trend'] } }],
                                {
                                    queryParamsHandling: 'merge',
                                },
                            );

                            setTimeout(() => this.shepherd.next(), 100);
                        },
                    },
                ],
                id: 'asset-group-select',
                text: 'Select any Asset Group to temporary switch views without changing your default Asset Group',
            },
            {
                attachTo: {
                    element: '.multiline-brush-zoom-container > svg .brush',
                    on: 'bottom',
                },
                buttons: [
                    this.builtInButtons.cancel,
                    {
                        ...this.builtInButtons.next,
                        action: async () => {
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

                            await this.router.navigate(['pl/compliance/policy-knowledgebase'], {
                                queryParamsHandling: 'merge',
                            });

                            setTimeout(() => this.shepherd.next(), 200);
                        },
                    },
                ],
                id: 'overal-compliance-trend',
                text: 'Select a date range to quickly filter what data is shown',
            },
            {
                attachTo: {
                    element:
                        '.policy-knowledgebase-content .table-container tbody > tr > .mat-column-Policy > div',
                    on: 'right',
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
                text: 'Select a row in a table to view all details of that row',
            },
            {
                attachTo: {
                    element: '.table-header .filters-menu-btn',
                    on: 'right',
                },
                buttons: [this.builtInButtons.cancel, this.builtInButtons.done],
                id: 'policy-filters',
                text: 'Use the page-level filter and tagging system to show only what you want to see',
            },
        ];
    }
}
