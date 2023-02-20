import { AfterViewInit,  Component, Inject,  OnInit, TemplateRef } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

interface DialogData {
  title: string;
  template: TemplateRef<any>;
  noButtonLabel: string;
  yesButtonLabel: string;
}

@Component({
  selector: 'app-dialog-box',
  templateUrl: './dialog-box.component.html',
  styleUrls: ['./dialog-box.component.css']
})
export class DialogBoxComponent implements OnInit, AfterViewInit {

  constructor(
    public dialogRef: MatDialogRef<DialogBoxComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DialogData,
  ) { }
  
  ngAfterViewInit(): void {
    this.dialogRef.componentInstance.data.template = this.data.template;
  }

  onNoClick(): void {
    this.dialogRef.close("no");
  }

  onYesClick(): void {
    this.dialogRef.close("yes");
  }

  ngOnInit(): void {
  }

}
