import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Subject } from 'rxjs';
import { AdminService } from 'src/app/pacman-features/services/all-admin.service';
import { DialogBoxComponent } from 'src/app/shared/components/molecules/dialog-box/dialog-box.component';
import { ErrorHandlingService } from 'src/app/shared/services/error-handling.service';
import { LoggerService } from 'src/app/shared/services/logger.service';
import { NotificationObservableService } from 'src/app/shared/services/notification-observable.service';
import { RefactorFieldsService } from 'src/app/shared/services/refactor-fields.service';
import { UtilsService } from 'src/app/shared/services/utils.service';
import { environment } from 'src/environments/environment';
import { UntypedFormControl, Validators } from '@angular/forms';


@Component({
  selector: 'app-user-management',
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.css']
})
export class UserManagementComponent implements OnInit {

  pageTitle = "User Management";
  dataTableDesc = "";

  emailControl = new UntypedFormControl('', [
                  Validators.required,
                  Validators.email
               ]);

  @ViewChild("createEditUserRef") createEditUserRef: TemplateRef<any>;
  @ViewChild("actionRef") actionRef: TemplateRef<any>;

  sampleRoles = ["PaladinCloud-ReadOnly","TenantAdmin","PaladinCloud-TechnicalAdmin"];
  columnNamesMap = { "email": "Email ID", "roles": "Role", "status": "Status"};
  columnWidths = { "Email ID": 0.5, "Role": 1, "Status": 0.25,"Actions": 0.25 }
  whiteListColumns = ["Email ID", "Role", "Status","Actions"];
  isStatePreserved = false;
  tableScrollTop = 0;
  searchTxt = "";
  tableDataLoaded = false;
  totalRows = 0;
  dialogHeader = "Add User";

  headerColName;
  direction;
  errorMessage: string;
  tableData = [];
  emailID: string;
  selectedRoles: string[];

  paginatorSize: number = 25;
  isLastPage: boolean;
  isFirstPage: boolean;
  totalPages: number;
  pageNumber: number = 1;

  outerArr: any = [];
  dataLoaded: boolean = false;
  allColumns: any = [];
  currentBucket: any = [];
  bucketNumber: number = 0;
  firstPaginator: number = 1;
  lastPaginator: number;
  currentPointer: number = 0;
  seekdata: boolean = false;
  showLoader: boolean = true;
  filters: any = [];
  searchCriteria: any;
  filterText: any = {};
  errorValue: number = 0;
  showGenericMessage: boolean = false;
  urlID: String = "";
  public labels: any;
  FullQueryParams: any;
  queryParamsWithoutFilter: any;
  urlToRedirect: any = "";
  private pageLevel = 0;
  public backButtonRequired;
  mandatory: any;
  onScrollDataLoader: Subject<any> = new Subject<any>();
  action: any;
  updatedRoles: any;

  constructor(
    private logger: LoggerService,
    private utils: UtilsService,
    private errorHandling: ErrorHandlingService,
    private refactorFieldsService: RefactorFieldsService,
    private adminService: AdminService,
    private notificationObservableService: NotificationObservableService,
    public dialog: MatDialog
  ) { }

  ngOnInit(): void {
    this.updateComponent();
  }

  updateComponent(){
    this.getUserList();
  }

  handleHeaderColNameSelection(event){
    this.headerColName = event.headerColName;
    this.direction = event.direction;
  }

  createEditUser(currentRow:any) {
    if(currentRow){
        this.dialogHeader = "Edit User Information";
        this.emailID = currentRow["Email ID"].valueText;
        this.selectedRoles = currentRow["Role"].valueText;
    }
    else{
      this.dialogHeader = "Add User";
      this.emailID = null;
      this.selectedRoles = [];
    }
    const dialogRef = this.dialog.open(DialogBoxComponent,
      {
        width: '600px',
        data: {
          title: this.dialogHeader,
          yesButtonLabel: "Save",
          template: this.createEditUserRef
        }
      });
    dialogRef.afterClosed().subscribe(result => {
      if (result == "yes") {
        if(currentRow)
        this.updateUserRoles();
        else{
          this.addNewUser();
        }
      }
    });
  }

  addNewUser(){
    this.action = "";
    const url = environment.addUser.url;
    const method = environment.addUser.method;

    const payload = {
      "username": this.emailID
    }

    this.adminService.executeHttpAction(url,method,payload,{}).subscribe(response=>{
      if(response){
        this.updateUserRoles();
      }
      
    })
  }

  onRolesChange(updatedRoles:any){
    this.updatedRoles = updatedRoles;
  }

  updateUserRoles(){
     const url = environment.updateUserRole.url;
     const method = environment.updateUserRole.method;
     const newUrl = url.replace("{username}",this.emailID);

     const payload = {
       "userName": this.emailID,
       "email": this.emailID,
       "roles": this.updatedRoles
     }

     this.adminService.executeHttpAction(newUrl,method,payload,{})
     .subscribe(response=>{
         try{
           if(response){
             this.getUserList();
             if(this.action == "Edit")
             this.openSnackBar("Details updated successfully","check-circle");
             else
             this.openSnackBar("User added successfully!","check-circle");

           }
         }
         catch(error){
           console.log(error);
         }
     })
  }

  openSnackBar(message, iconSrc) {
    this.notificationObservableService.postMessage(message, 3 * 1000, "success", iconSrc);
  }


  processData(data) {
    try {
      var innerArr = {};
      var totalVariablesObj = {};
      var cellObj = {};
      let processedData = [];
      var getData = data;
      const keynames = Object.keys(getData[0]);

      for (var row = 0; row < getData.length; row++) {
        innerArr = {};
        keynames.forEach(col => {
          cellObj = {
            text: getData[row][col], // text to be shown in table cell
            titleText: getData[row][col], // text to show on hover
            valueText: getData[row][col],
            hasPostImage: false,
            imgSrc: "",  // if imageSrc is not empty and text is also not empty then this image comes before text otherwise if imageSrc is not empty and text is empty then only this image is rendered,
            postImgSrc: "",
            isChip: "",
            isMenuBtn: false,
            properties: "",
            link: false
            // chipVariant: "", // this value exists if isChip is true,
          // menuItems: [], // add this if isMenuBtn
          }
          if (col.toLowerCase() == "role") {
            cellObj = {
              ...cellObj,
              chipList: getData[row][col],
              isChip: true,
              chipBackgroundColor: "#F2F3F5",
              chipTextColor: "#73777D"
            };
          } 
          else if(col.toLowerCase() == "status"){
            let chipBackgroundColor:string,chipTextColor:string,chipText:string;
            if(getData[row]["Status"].toLowerCase() === "active"){
              chipBackgroundColor = "#E6F5EC";
              chipTextColor = "#00923f";
            }else{
              chipBackgroundColor = "#FFF1EF";
              chipTextColor = "#D95140";
            }
            cellObj = {
              ...cellObj,
              chipList: [getData[row][col]],
              isChip: true,
              chipBackgroundColor: chipBackgroundColor,
              chipTextColor: chipTextColor
            };
          }
          else if (col.toLowerCase() == "actions") {
            let dropDownItems: Array<String> = ["Edit","Remove"];
            if (getData[row].Status.toLowerCase() === "active") {
              dropDownItems.push("Deactivate");
            } else {
              dropDownItems.push("Activate");
            }
            cellObj = {
              ...cellObj,
              isMenuBtn: true,
              menuItems: dropDownItems,
            };
          } 
          innerArr[col] = cellObj;
          totalVariablesObj[col] = "";
        });
        processedData.push(innerArr);
      }
      if (processedData.length > getData.length) {
        var halfLength = processedData.length / 2;
        processedData = processedData.splice(halfLength);
      }
      return processedData;
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  callNewSearch(event: any) {
  }

  onSelectAction(event:any){
    const action = event.action;
    const rowSelected = event.rowSelected;
    this.action = action;
    if(action == "Activate" || action == "Deactivate" || action == "Remove"){
      this.confirmAction(action,rowSelected);
    } else if(action == "Edit"){
      this.createEditUser(rowSelected);
    }
  }

  deleteUser(username:string){
    const url = environment.deleteUser.url;
    const method = environment.deleteUser.method;
    const newUrl = url.replace('{username}',username);

    
    this.adminService.executeHttpAction(newUrl,method,{},{})
      .subscribe(response=>{
        try{
          if(response){
            this.getUserList();
            this.openSnackBar("Removed user successfully","check-circle");
          }
        }
        catch(error){
          console.log(error);
        }
    })
  }

  confirmAction(action:string,selectedRow:any){
    const username = selectedRow["Email ID"].valueText;
    this.emailID = username;
    const dialogRef = this.dialog.open(DialogBoxComponent,
    {
      width: '600px',
      data: {
        title: null,
        yesButtonLabel: action,
        noButtonLabel: "Cancel",
        template: this.actionRef
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      try {
        if (result == "yes") {
          if(action == "Remove"){
              this.deleteUser(username);
          }
          else
          this.userActivation(username);
        }
      } catch (error) {
        this.errorMessage = this.errorHandling.handleJavascriptError(error);
        this.logger.log('error', error);
      }
    });
  }

  userActivation(username:string){
      const url = environment.editUserStatus.url;
      const method = environment.editUserStatus.method;
      let replacedUrl = url.replace('{username}', username);

      this.adminService.executeHttpAction(replacedUrl, method, {}, {}).subscribe(
        (response) => {
          try{
            if(response[0].message == "success"){
            this.getUserList();
            this.openSnackBar("User status "+this.action.toLowerCase()+"d successfully","check-circle");
            }
          }
          catch (error) {
          this.errorMessage = this.errorHandling.handleJavascriptError(error);
          this.logger.log('error', error);
        }
      }
      )
  }

  getUserList(isNextPageCalled?) {
    var url = environment.listUsers.url;
    var method = environment.listUsers.method;

    var queryParams = {
      cursor: this.pageNumber,
      limit: this.paginatorSize,
    };

    if (this.searchTxt !== undefined && this.searchTxt !== "") {
      queryParams["searchTerm"] = this.searchTxt;
    }

    this.errorMessage = '';

    try{
      this.adminService.executeHttpAction(url, method, {}, queryParams).subscribe(
      (response) => {
        this.showLoader = false;
        if (response.length > 0) {
          this.errorValue = 1;
          this.searchCriteria = undefined;
          const tableData = response[0];
          this.tableDataLoaded = true;
          const updatedResponse = this.massageData(tableData);
          const processedData = this.processData(updatedResponse)
          if(isNextPageCalled){
            this.onScrollDataLoader.next(processedData)
          }else{
            this.tableData = processedData;
            if(this.tableData?.length==0){
              this.errorMessage = "noDataAvailable";
            }
          }
          this.totalRows = tableData.length;
          this.dataLoaded = true;
        }
      },
      (error) => {
        this.showGenericMessage = true;
        this.errorValue = -1;
        this.outerArr = [];
        this.dataLoaded = true;
        this.seekdata = true;
        this.errorMessage = "apiResponseError";
        this.showLoader = false;
      }
    );
    }catch(e){
      this.logger.log("error: ", e);
    }
  }
 
  massageData(data){
    const refactoredService = this.refactorFieldsService;
    const columnNamesMap = this.columnNamesMap;
    const newData = [];
    data.map(function (row) {
      const KeysTobeChanged = Object.keys(row); 
           
      let newObj = {};
      KeysTobeChanged.forEach((element) => {
        let elementnew;
        if(columnNamesMap[element]) {
          elementnew = columnNamesMap[element];
          newObj = Object.assign(newObj, { [elementnew]: row[element] });
        }
        else {
        elementnew =
          refactoredService.getDisplayNameForAKey(
            element.toLocaleLowerCase()
          ) || element;
          newObj = Object.assign(newObj, { [elementnew]: row[element] });
        }
      });
      newObj["Actions"] = "";
      newData.push(newObj);
    });
    return newData;
  }

  nextPage(e) {
    try {
        this.pageNumber++;
        this.showLoader = true;
        this.getUserList(true);
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }
}
