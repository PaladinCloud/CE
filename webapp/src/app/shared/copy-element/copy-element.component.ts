import { Component, OnInit, Input, ChangeDetectionStrategy } from '@angular/core';
import { CopyElementService } from '../services/copy-element.service';

@Component({
    selector: 'app-copy-element',
    templateUrl: './copy-element.component.html',
    styleUrls: ['./copy-element.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CopyElementComponent implements OnInit {
    constructor(private copyElService: CopyElementService) {}

    ngOnInit() {}

    @Input() CopyElement: string;
    @Input() iconSize: string;
    @Input() hidePadding = false;

    async copyTextToClipboard(event: MouseEvent) {
        event.stopPropagation();

        let copyMsg = 'Copying failed! Please try later';
        let copyType = 'Error';
        let copyIcon = 'Error.svg';
        let copyTimeDuration = 2;

        try {
            if (this.CopyElement) {
                await navigator.clipboard.writeText(this.CopyElement);
                copyMsg = 'Element has been copied';
                copyType = 'Info';
                copyIcon = 'Info.svg';
            } else {
                copyMsg = 'No Data Available';
            }
        } catch (err) {
            copyTimeDuration = 3;
        }
        this.copyElService.textCopyMessage(copyMsg, copyTimeDuration, copyType, copyIcon);
    }
}
