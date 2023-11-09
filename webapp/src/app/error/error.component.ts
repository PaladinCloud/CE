import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-error',
  templateUrl: './error.component.html',
  styleUrls: ['./error.component.css']
})
export class ErrorComponent implements OnInit {

  githubLink = "https://github.com/PaladinCloud/CE";
  discordLink = "https://discord.gg/xvCFD29Jj4";
  slackLink = "https://paladincloudworkspace.slack.com";

  constructor(
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(): void {
  }

  goToDashboard(){
    const queryParams = this.route.snapshot.queryParams;
    this.router.navigate(['/home'], {
      queryParams: {
          ag: queryParams.ag,
          domain: queryParams.domain,
      }
    });
  }

}
