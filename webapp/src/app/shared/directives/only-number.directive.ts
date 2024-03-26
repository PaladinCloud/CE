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

/**
 * @type Directive
 * @desc allow numbers only, this directive is used to allow input field to allow only numbers.
 * @author Nidhish Krishnan (Nidhish)
 */
import { Directive, HostListener, Input } from '@angular/core';

@Directive({
    selector: '[appOnlyNumber]',
})
export class OnlyNumberDirective {
    @Input('appOnlyNumber') shouldApply: boolean = true;

    @HostListener('paste', ['$event']) onPaste(event: ClipboardEvent) {
        if (!this.shouldApply) {
            return;
        }
        const content: string = event.clipboardData.getData('Text');
        if (isNaN(Number(content))) {
            event.preventDefault();
        }
    }

    @HostListener('keydown', ['$event']) onKeyDown(event: KeyboardEvent) {
        this.checkForNumber(event);
    }

    @HostListener('keyup', ['$event']) onKeyUp(event: KeyboardEvent) {
        this.checkForNumber(event);
    }

    private checkForNumber(event) {
        if (!this.shouldApply) {
            return;
        }
        const charCode = event.which || event.keyCode;
        const isCtrlV = (event.ctrlKey || event.metaKey) && charCode === 86; // Check for Ctrl+V (paste)
        if (
            (event.shiftKey && charCode === 187) || // Allow '+' when shift key is pressed
            (!event.shiftKey && charCode >= 48 && charCode <= 57) || // Allow numbers
            (charCode >= 96 && charCode <= 105) || // Allow numpad numbers
            charCode === 8 || // Allow backspace
            charCode === 9 || // Allow tab
            charCode === 13 || // Allow enter
            charCode === 37 || // Allow left arrow
            charCode === 39 || // Allow right arrow
            charCode === 46 || // Allow delete
            isCtrlV || // Allow Ctrl+V (paste)
            (event.ctrlKey && charCode === 65) || // Allow 'Ctrl+A' (select all)
            (event.ctrlKey && charCode === 67) || // Allow 'Ctrl+C' (copy)
            (event.ctrlKey && charCode === 88) // Allow 'Ctrl+X' (cut)
        ) {
            return;
        }
        event.preventDefault();
    }
}
