import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
    selector: 'app-error',
    templateUrl: './error.component.html',
    styleUrls: ['./error.component.css'],
})
export class ErrorComponent implements OnInit {
    githubLink = 'https://github.com/PaladinCloud/CE';
    discordLink = 'https://discord.gg/xvCFD29Jj4';
    slackLink = 'https://paladincloudworkspace.slack.com';

    constructor(private router: Router) {}

    ngOnInit(): void {}

    redirectToHomePage() {
        this.router.navigate(['/home']);
    }
}
