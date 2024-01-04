import { AfterViewInit, Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
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
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import * as _ from 'lodash';
import { ActivatedRoute, Router } from '@angular/router';
import { RouterUtilityService } from 'src/app/shared/services/router-utility.service';
import { TableStateService } from 'src/app/core/services/table-state.service';
import { TourService } from 'src/app/core/services/tour.service';
import { CustomValidators } from 'src/app/shared/custom-validators';
import { DataCacheService } from 'src/app/core/services/data-cache.service';
import { ComponentKeys } from 'src/app/shared/constants/component-keys';


@Component({
  selector: 'app-user-management',
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.css']
})
export class UserManagementComponent implements OnInit, AfterViewInit {

  pageTitle = "Users";
  saveStateKey: String = ComponentKeys.UserManagementList;
  dataTableDesc = "";


  @ViewChild("createEditUserRef") createEditUserRef: TemplateRef<any>;
  @ViewChild("actionRef") actionRef: TemplateRef<any>;

  userRoles = [];
  nonRemovableChips = [];
  columnNamesMap = { "email": "Email", "roles": "Roles", "status": "Status"};
  columnWidths = { "Email": 0.5, "Roles": 1, "Status": 0.25,"Actions": 0.25 }
  whiteListColumns;
  isStatePreserved = false;
  tableScrollTop = 0;
  searchTxt = "";
  tableDataLoaded = false;
  totalRows = 0;
  dialogHeader = "Add User";
  filterTypeLabels = [];
  filterTagLabels = {};
  filterTypeOptions: any = [];
  filterTagOptions: any = {};
  currentFilterType;

  headerColName;
  direction;
  errorMessage: string;
  tableErrorMessage: string = '';
  tableData = [];
  emailID: string;
  firstName: string;
  lastName: string;
  selectedRoles: string[];

  paginatorSize: number = 25;
  hasMoreDataToLoad: boolean = false;
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
  selectedRowIndex;
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
  updatedRoles = ["ROLE_USER"];

  private userForm: FormGroup;
  public userFormErrors : any = {
    emailID: '',
    firstName: '',
    lastName: ''
  }

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private routerUtilityService: RouterUtilityService,
    private logger: LoggerService,
    private utils: UtilsService,
    private errorHandling: ErrorHandlingService,
    private refactorFieldsService: RefactorFieldsService,
    private adminService: AdminService,
    private notificationObservableService: NotificationObservableService,
    public dialog: MatDialog,
    private tableStateService: TableStateService,
    private tourService: TourService,
    private dataCacheService: DataCacheService,
    public form: FormBuilder
  ) {
    this.getPreservedState();
    this.getFilters();
  }
  ngAfterViewInit(): void {
    this.tourService.setComponentReady();
  }

  ngOnInit(): void {
    this.buildForm();
    this.getRoles();
  }

  getPreservedState(){
    const state = this.tableStateService.getState(this.saveStateKey) ?? {};
    if(state){
      this.headerColName = state.headerColName ?? 'Email';
      this.direction = state.direction ?? 'asc';
      this.bucketNumber = state.bucketNumber ?? 0;
      this.totalRows = state.totalRows ?? 0;
      this.searchTxt = state?.searchTxt ?? '';

      this.tableData = state?.data ?? [];
      this.whiteListColumns = state?.whiteListColumns ?? ["Email", "Roles", "Status","Actions"];;
      this.tableScrollTop = state?.tableScrollTop;
      this.selectedRowIndex = state?.selectedRowIndex;

      if(this.tableData && this.tableData.length>0){     
        this.tableDataLoaded = true;   
        this.isStatePreserved = true;
      }else{
        this.isStatePreserved = false;
      }
    }
  }

  updateComponent() {
    if(this.isStatePreserved){
      this.tableDataLoaded = true;
      this.clearState();
    }else{
      this.tableDataLoaded = false;
      this.bucketNumber = 0;
      this.tableData = [];
      this.getUserList();
    }
  }

  getRoles(){
    const url = environment.roles.url;
    const method = environment.roles.method;
    this.adminService.executeHttpAction(url,method,{},{}).subscribe(response=>{
      try{
        if(response){
          const userRoles = response[0];
          this.processRoles(userRoles);
        }
      }catch(error){
        this.errorMessage = this.errorHandling.handleJavascriptError(error);
        this.logger.log("error", error);
      }
    })
  }

  processRoles(userRoles){
    for(let i=0;i<userRoles.length;i++){
      if(userRoles[i].isDefault){
        this.nonRemovableChips.push(userRoles[i].roleName);
      }
      this.userRoles.push(userRoles[i].roleName);
    }
  }
  
  handleHeaderColNameSelection(event){
    this.headerColName = event.headerColName;
    this.direction = event.direction;
    this.storeState();
  }

  handleWhitelistColumnsChange(event){
    this.whiteListColumns = event;
    this.storeState();
  }

  public buildForm() {
    if(this.action=='Edit'){
      this.userForm = this.form.group({
        emailID  : [this.emailID, [Validators.required,
                         Validators.email]
                   ],
      });
    }else{
      this.userForm = this.form.group({
        emailID  : ['', [Validators.required,
                         Validators.email]
                   ],
        firstName: ['', [Validators.required, Validators.pattern('^[a-zA-Z]+$')]],
        lastName : ['', [Validators.required, Validators.pattern('^[a-zA-Z]+$')]]
      });
    }
  }

  createEditUser(currentRow:any) {
    if(currentRow){
        this.dialogHeader = "Edit User Information";
        this.emailID = currentRow["Email"].valueText;
        this.selectedRoles = currentRow["Roles"].valueText;
    }
    else{
      this.action = '';
      this.dialogHeader = "Add User";
      this.emailID = null;
      this.firstName = null;
      this.lastName = null;
      this.selectedRoles = ["ROLE_USER"];
    }
    this.buildForm();
    const dialogRef = this.dialog.open(DialogBoxComponent,
      {
        width: '600px',
        data: {
          title: this.dialogHeader,
          yesButtonLabel: "Save",
          template: this.createEditUserRef,
          formGroup : this.userForm
        }
      });
    dialogRef.afterClosed().subscribe(result => {
      if (result == "yes") {
        this.resetComponent();
        if(currentRow)
        this.updateUserRoles(this.selectedRoles);
        else{
          this.addNewUser();
        }
      }
    });
  }

  addNewUser(){
    this.action = "Added";
    const url = environment.addUser.url;
    const method = environment.addUser.method;

    const payload = {
      "username": this.emailID,
      userAttributes: {
        given_name: this.firstName,
        family_name: this.lastName
      }
    }

    if(this.emailID && (this.firstName && this.lastName)){
      this.adminService.executeHttpAction(url,method,payload,{}).subscribe(response=>{
        if(response){
          this.getUserList();
          this.updateUserRoles([]);
        }
      })
    }else{
      this.openSnackBar("Error adding user successfully","Error", "error");
    }
  }

  onRolesChange(updatedRoles:any){
    this.updatedRoles = updatedRoles;
  }

  updateUserRoles(existingRoles:string[]){
     const url = environment.updateUserRole.url;
     const method = environment.updateUserRole.method;
     const newUrl = url.replace("{username}",this.emailID);

     const payload = {
       "roles": this.updatedRoles
     }

     this.adminService.executeHttpAction(newUrl,method,payload,{})
     .subscribe(response=>{
         try{
           if(response){
             this.getUserList();
             if(this.action == "Edit"){
              this.openSnackBar("Details updated successfully","check-circle");
             }
             else{
              this.openSnackBar("User added successfully!","check-circle");
             }
            this.updatedRoles = ["ROLE_USER"];
           }
         }
         catch(error){
           this.logger.log("jsError", error);
         }
     })
  }

  openSnackBar(message, iconSrc, infoCategory?) {
    this.notificationObservableService.postMessage(message, 3 * 1000, infoCategory??"success", iconSrc);
  }

  /*
   * This function gets the urlparameter and queryObj
   *based on that different apis are being hit with different queryparams
   */
   routerParam() {
    try {
      const currentQueryParams =
        this.routerUtilityService.getQueryParametersFromSnapshot(
          this.router.routerState.snapshot.root
        );
      if (currentQueryParams) {
        this.FullQueryParams = currentQueryParams;
        this.queryParamsWithoutFilter = JSON.parse(
          JSON.stringify(this.FullQueryParams)
        );
        delete this.queryParamsWithoutFilter["filter"];
        this.filterText = this.utils.processFilterObj(this.FullQueryParams);
      }
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }


  getUpdatedUrl() {
    let updatedQueryParams = {};    
      this.filterText = this.utils.arrayToObject(
      this.filters,
      "filterkey",
      "value"
    ); // <-- TO update the queryparam which is passed in the filter of the api
    this.filterText = this.utils.makeFilterObj(this.filterText);

    /**
     * To change the url
     * with the deleted filter value along with the other existing paramter(ex-->tv:true)
     */

    updatedQueryParams = {
      filter: this.filterText.filter,
    }


    /**
     * Finally after changing URL Link
     * api is again called with the updated filter
     */
    this.filterText = this.utils.processFilterObj(this.filterText);
    
    this.router.navigate([], {
      relativeTo: this.activatedRoute,
      queryParams: updatedQueryParams,
      queryParamsHandling: 'merge',
  });
  }
  deleteFilters(event?) {
    try {
      this.pageNumber = 1;
      if (!event) {
        this.filters = [];
        this.storeState();
      }else if(event.index && !this.filters[event.index].filterValue){
        this.filters.splice(event.index, 1);
        this.storeState();
      }
      else {
        if (event.clearAll) {
          this.filters = [];
        } else {
          this.filters.splice(event.index, 1);
        }
        this.storeState();
        this.getUpdatedUrl();
        this.updateComponent();
      }
    } catch (error) { }
    /* TODO: Aditya: Why are we not calling any updateCompliance function in observable to update the filters */
  }
  /*
   * this functin passes query params to filter component to show filter
   */
  getFilterArray() {
    return new Promise((resolve) => {
      const filterObjKeys = Object.keys(this.filterText);      
      const dataArray = [];
      for (let i = 0; i < filterObjKeys.length; i++) {
        let obj = {};
        const keyDisplayValue = _.find(this.filterTypeOptions, {
          optionValue: filterObjKeys[i],
        })["optionName"];
        obj = {
          keyDisplayValue,
          filterkey: filterObjKeys[i],
        };
        dataArray.push(obj);
      }
      
      const state = this.tableStateService.getState(this.saveStateKey) ?? {};
      const filters = state?.filters;
      
      if(filters){
        const dataArrayFilterKeys = dataArray.map(obj => obj.keyDisplayValue);
        filters.forEach(filter => {
          if(!dataArrayFilterKeys.includes(filter.keyDisplayValue)){
            dataArray.push({
              filterkey: filter.filterkey,
              keyDisplayValue: filter.key
            });
          }
        });
      }

      const formattedFilters = dataArray;   
      if(formattedFilters.length==0){
        resolve(true);
      }   
      for (let i = 0; i < formattedFilters.length; i++) {

        let keyDisplayValue = formattedFilters[i].keyDisplayValue;
        if(!keyDisplayValue){
          keyDisplayValue = _.find(this.filterTypeOptions, {
            optionValue: formattedFilters[i].filterKey,
          })["optionName"];
        }

        this.changeFilterType(keyDisplayValue).then(() => {
          let filterValueObj = _.find(this.filterTagOptions[keyDisplayValue], {
            id: this.filterText[formattedFilters[i].filterkey],
          });

          let filterKey = dataArray[i].filterkey;
          
          if(!this.filters.find(filter => filter.keyDisplayValue==keyDisplayValue)){
            const eachObj = {
              keyDisplayValue: keyDisplayValue,
              filterValue: filterValueObj?filterValueObj["name"]:undefined,
              key: keyDisplayValue, // <-- displayKey-- Resource Type
              value: this.filterText[filterKey], // <<-- value to be shown in the filter UI-- S2
              filterkey: filterKey?.trim(), // <<-- filter key that to be passed -- "resourceType "
              compareKey: filterKey?.toLowerCase().trim(), // <<-- key to compare whether a key is already present -- "resourcetype"
            };
            this.filters.push(eachObj);            
          }
          if(i==formattedFilters.length-1){
            this.filters = [...this.filters];
            this.storeState();
            resolve(true);
          }
          
        })
      }

    })
  }

  /**
   * This function get calls the keyword service before initializing
   * the filter array ,so that filter keynames are changed
   */

  getFilters() {
    try {
      this.filterTypeLabels.push("Status");
      this.filterTypeOptions.push({
        optionName: 'Status',
        optionValue: 'Status'
      })
      this.routerParam();
      this.getFilterArray().then(() => {
        this.updateComponent();
      }).catch(e => {
        this.logger.log("jsError: ", e);
        this.updateComponent();
      });
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  changeFilterType(value) {
    return new Promise((resolve, reject) => {
    try {
      this.currentFilterType = _.find(this.filterTypeOptions, {
        optionName: value,
      });
      this.storeState();
      if(!this.filterTagOptions[value] || !this.filterTagLabels[value]){
        if(value.toLowerCase()=="status"){
        this.filterTagLabels[value] = ["Active", "Inactive"];
        this.filterTagOptions[value] = [
          {
            id: "active",
            name: "Active"
          },
          {
            id: "inactive",
            name: "Inactive"
          }
        ]
        resolve(this.filterTagLabels[value]);
        return;
      }
      }
    } catch (error) {
      reject(false);
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
    }); 
  }

  changeFilterTags(event) {    
    this.pageNumber = 1;
    let value = event.filterValue;
    this.currentFilterType =  _.find(this.filterTypeOptions, {
        optionName: event.filterKeyDisplayValue,
      });  
    try {
      if (this.currentFilterType) {
        const filterTag = _.find(this.filterTagOptions[event.filterKeyDisplayValue], { name: value });   
        this.utils.addOrReplaceElement(
          this.filters,
          {
            keyDisplayValue: event.filterKeyDisplayValue,
            filterValue: value,
            key: this.currentFilterType.optionName,
            value: filterTag["id"],
            filterkey: this.currentFilterType.optionValue.trim(),
            compareKey: this.currentFilterType.optionValue.toLowerCase().trim(),
          },
          (el) => {
            return (
              el.compareKey ===
              this.currentFilterType.optionValue.toLowerCase().trim()
            );
          }
        );
      }
      this.storeState();
      this.getUpdatedUrl();
      this.updateComponent();
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  processData(data) {
    if(data.length==0) return;
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
          if (col.toLowerCase() == "roles") {
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
      this.tableErrorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  callNewSearch(event: any) {
  }

    
  onSelectAction(event:any){
    const action = event.action;
    const rowSelected = event.rowSelected;
    this.selectedRowIndex = event.selectedRowIndex;

    this.storeState();
    this.action = action;
    if(action == "Activate" || action == "Deactivate" || action == "Remove"){
      this.confirmAction(action,rowSelected);
    } else if(action == "Edit"){
      this.createEditUser(rowSelected);
    }
  }

  deleteUser(username:string, existingRoles:any){
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
          this.logger.log("jsError", error);
        }
    })
  }

  confirmAction(action:string,selectedRow:any){
    const username = selectedRow["Email"].valueText;
    const roles = selectedRow["Roles"].valueText;
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
          this.resetComponent();
          if(action == "Remove"){
              this.deleteUser(username, roles);
          }
          else
          this.userActivation(username,roles);
        }
      } catch (error) {
        this.errorMessage = this.errorHandling.handleJavascriptError(error);
        this.logger.log('error', error);
      }
    });
  }

  resetComponent(){
    this.tableDataLoaded = false;
    this.pageNumber = 1;
  }

  userActivation(username:string, existingRoles:any){
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

  storeState(data?){
    const state = {
        totalRows: this.totalRows,
        data: data,
        headerColName: this.headerColName,
        direction: this.direction,
        whiteListColumns: this.whiteListColumns,
        bucketNumber: this.bucketNumber,
        searchTxt: this.searchTxt,
        tableScrollTop: this.tableScrollTop,
        filters: this.filters,
        selectedRowIndex: this.selectedRowIndex
        // filterText: this.filterText
      }
    this.tableStateService.setState(this.saveStateKey, state);
  }

  clearState(){
    this.isStatePreserved = false;
  }

  getUserList(isNextPageCalled?) {
    var url = environment.listUsers.url;
    var method = environment.listUsers.method;

    let queryParams = {
      cursor: this.pageNumber,
      limit: this.paginatorSize,
      filter: undefined
    };

    if(this.filters.length && this.filters[0].key && this.filters[0].value){
      queryParams = {
          ...queryParams,
          filter: "status = \""+(this.filters[0].value=="active" ?"Enabled":"Disabled")+"\""
        };
    }

    if (this.searchTxt !== undefined && this.searchTxt !== "") {
      queryParams["searchTerm"] = this.searchTxt;
    }

    this.tableErrorMessage = '';

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
          const processedData = this.processData(updatedResponse);
          if(isNextPageCalled){
            this.onScrollDataLoader.next(processedData)
          }else{
            this.tableData = processedData;
          }
          if(tableData.length==0){
            this.hasMoreDataToLoad = false;
            if(!isNextPageCalled) this.tableErrorMessage = "noDataAvailable";
          }else{
            if(isNextPageCalled){
                this.totalRows = this.totalRows + tableData.length;
            }else{
              this.totalRows = this.totalRows + tableData.length;
            }
            this.hasMoreDataToLoad = true;
          }
          this.dataLoaded = true;
        }
      },
      (error) => {
        this.tableDataLoaded = true;
        this.tableErrorMessage = "apiResponseError";
        this.logger.log("apiResponseError: ", error);
      }
    );
    }catch(e){
      this.tableDataLoaded = true;
      this.tableErrorMessage = this.errorHandling.handleJavascriptError(e);
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
        this.pageNumber += this.paginatorSize;
        this.showLoader = true;
        this.getUserList(true);
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }
}
