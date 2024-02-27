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

import { Injectable } from '@angular/core';
import each from 'lodash/each';
import findIndex from 'lodash/findIndex';
import map from 'lodash/map';
import { QUARTER } from './../constants/quarter';
import { LoggerService } from './logger.service';
import { RefactorFieldsService } from './refactor-fields.service';
import { DATA_MAPPING } from '../constants/data-mapping';
import { DatePipe } from '@angular/common';
import { find } from 'lodash';
import { IColumnNamesMap, IColumnWidthsMap, IFilterOption } from '../table/interfaces/table-props.interface';
import { JS_ERROR, REDIRECT_URL_KEY } from '../constants/global';
import { ImageCacheService } from 'src/app/core/services/image-cache.service';

@Injectable()
export class UtilsService {
  private readonly utcDateFormat = 'yyyy-MM-dd';
  constructor(
              private datePipe: DatePipe,
              private logger: LoggerService,
              private refactorFieldsService: RefactorFieldsService,
              private imageCacheService: ImageCacheService
            ) {}

  setTimeoutPromise(milliseconds) {
    const promise = new Promise((resolve: any, reject: any) => {
      setTimeout(() => {
        resolve(resolve, reject);
      }, milliseconds);
    });
    return promise;
  }

  isObjectEmpty = obj => {
    return Object.keys(obj).length === 0 && obj.constructor === Object;
  }

  debounce(func, wait, immediate) {
    let timeout;
    return function() {
      const context = this,
        args = arguments;
        const later = function() {
        timeout = null;
        if (!immediate) { func.apply(context, args); }
      };
      const callNow = immediate && !timeout;
      clearTimeout(timeout);
      timeout = setTimeout(later, wait);
      if (callNow) { func.apply(context, args); }
    };
  }

  isObject(val) {
    if (val === null) {
      return false;
    }
    return typeof val === 'function' || typeof val === 'object';
  }

  clickClearDropdown() {
    setTimeout(function() {
      const clear = document.getElementsByClassName(
        'btn btn-xs btn-link pull-right'
      );
      for (let len = 0; len < clear.length; len++) {
        const element: HTMLElement = clear[len] as HTMLElement;
        element.click();
      }
    }, 10);
  }

  uppercasefirst(value: string | null) {
    if (value === null) {
        return 'Not assigned';
    }
    value = value.toLocaleLowerCase();
    return value.charAt(0).toUpperCase() + value.slice(1);
  }

  objectToArray(object, keyLabel = 'key', valueLabel = 'value') {
    return map(object, (element, key, array) => {
      const arrayElement: any = {};
      arrayElement[keyLabel] = key;
      arrayElement[valueLabel] = element;
      return arrayElement;
    });
  }

  arrayToCommaSeparatedString(arr) {
    let str = '';
    for (let i = 0; i < arr.length; i++) {
      i === arr.length - 1 ? str = str + arr[i] : str = str + arr[i] + ',';
    }
    return str;
  }

  massageTableData(data, columnNamesMap={}) {
    /*
      * added by Trinanjan 14/02/2017
      * the funciton replaces keys of the table header data to a readable format
    */
    const refactoredService = this.refactorFieldsService;
    const newData = [];

    data.forEach((row) => {
        const keysToBeChanged = Object.keys(row);

        const newObj = {};

        keysToBeChanged.forEach((element) => {
            let elementNew;

            if (columnNamesMap[element]) {
                elementNew = columnNamesMap[element];
            } else {
                elementNew = refactoredService.getDisplayNameForAKey(element.toLowerCase()) || element;
            }

            let newDataValue = DATA_MAPPING[typeof row[element] === "string" ? row[element].toLowerCase() : row[element]];

            if (newDataValue === undefined) {
                newDataValue = row[element];
            }
            newObj[elementNew] = newDataValue;
        });

        newData.push(newObj);
    });
    return newData;
  }

  loadIcons (tableImageDataMap) {
    try {
      const icons = Object.keys(tableImageDataMap).map(key => tableImageDataMap[key].image);
      icons.forEach(icon => this.imageCacheService.loadIcon(icon));
    } catch (e) {
      this.logger.log(JS_ERROR, e);
    }
  }

  processTableData (data, tableImageDataMap = {}, transformCallback?) {
    this.loadIcons(tableImageDataMap);
    try {
      const processedData = [];
      
      for (const row of data) {
        const innerArr = {};
        for (const col in row) {
          const cellData = row[col];
          const tableImageData = tableImageDataMap[typeof cellData === "string" ? cellData.toLowerCase() : cellData];
          
          let cellObj = {
              text: tableImageData?.imageOnly ? "" : cellData,
              titleText: cellData,
              valueText: cellData,
              hasPostImage: false,
              imgSrc: tableImageData?.image || "",
              postImgSrc: "",
              isChip: "",
              isMenuBtn: false,
              properties: "",
              isLink: false,
              imageTitleText: "",
              isDate: false
          };
          if(this.isDateStringValid(cellData)){
            cellObj = {
              ...cellObj,
              isDate: true
            };
          }
          if(transformCallback){
            cellObj = transformCallback(row, col, cellObj);
          }
          innerArr[col] = cellObj;
        };
        processedData.push(innerArr);
      }
      return processedData;
    } catch (error) {
      this.logger.log("error", error);
    }
  }

  // get map of column display name and api name and updates the column widths map
  getColumnNamesMapAndColumnWidthsMap(filterTypeLabels, filterTypeOptions, columnWidths: IColumnWidthsMap, columnNamesMap:IColumnNamesMap, excludedColumnNames)
  : [IColumnNamesMap, IColumnWidthsMap]
  {
    excludedColumnNames = new Set(excludedColumnNames);
    filterTypeLabels.forEach(label => {
        if (!excludedColumnNames.has(label)) {
            if (!Object.keys(columnWidths).includes(label)) {
                columnWidths[label] = 0.7;
            }

            if(!Object.values(columnNamesMap).includes(label)){
              const apiColName = filterTypeOptions
                .find(option => option.optionName === label)?.optionValue;

              if (apiColName) {
                  columnNamesMap[apiColName.replace(".keyword", "")] = label;
              }
            }
        }
    });
    return [{...columnNamesMap}, {...columnWidths}];
  }

  getFilterKeyDisplayValue(formattedFilterItem, filterTypeOptions: IFilterOption[]){
    let keyDisplayValue = formattedFilterItem.keyDisplayValue;
    if(!keyDisplayValue){
      const filterOption = find(filterTypeOptions, {
        optionValue: formattedFilterItem.filterkey,
      });
      keyDisplayValue = filterOption?.optionName;
    }
    return keyDisplayValue;
  }

  addOrReplaceElement(array, toAddElement, comparator) {
    const i = findIndex(array, (element, index, _array) => {
      return comparator(element, index, _array);
    });

    if (i >= 0) {
      array.splice(i, 1, toAddElement);
    } else {
      array.push(toAddElement);
    }
  }

  getParamsFromUrlSnippet(urlSnippet) {
    if(urlSnippet===undefined){
      return;
    }
    const split = urlSnippet.split('?');
    const url = split[0];
    const params = {};
    if(split.length>1){
      const inputParams = split[1].split('&');
      each(inputParams, arg => {
        const key = arg.substring(0, arg.indexOf('='));
        const value = arg.substring(arg.indexOf('=') + 1, arg.length);
        params[key] = value;
      });
    }

    return {
      url: url,
      params: params
    };
  }

  arrayToObject(array, keyLabel = 'key', valueLabel = 'value') {
    const object = {};
    each(array, (element, index, list) => {
      object[element[keyLabel]] = element[valueLabel];
    });
    return object;
  }

  /**
  * Funciton added by trinanjan on 30.01.2018
  * This function process the queryparams from router snapshot and
  * passes the required formate obj for filter parameter
  */

  processFilterObj(data) {
    let object = {};

    if (data.filter !== '' && data.filter !== undefined  ) {
      const eachFilterObj = data.filter.split('**');
      each(eachFilterObj, (element, index) => {
        const eachFilterParam = element.split('=');
        const key = eachFilterParam[0];
        const value = eachFilterParam[1];
        object[key] = decodeURIComponent(value);
      });
    } else {
      object = {};
    }
    return object;
  }

  /**
  * Funciton added by trinanjan on 31.01.2018
  * This function process filter parameter to be passes to required format
  * Example Input --> {'tagged':'true','targetType':'ec2'}
  * Example Output --> {'filter': 'tagged=true*targetType=ec2'}
  */

  makeFilterObj(data) {
    try {
      let object = {};
      const localArray = [];
      if (Object.keys(data).length === 0 && data.constructor === Object) {
        return object;
      } else {
        const localObjKeys = Object.keys(data);
        each(localObjKeys, (element, index) => {
          if (typeof data[element] !== 'undefined') {
            const localValue = encodeURIComponent(data[element].toString());
            const localKeys = element.toString();
            const localObj = localKeys + '=' + localValue;
            localArray.push(localObj);
          }
        });
        object = { filter: localArray.join('**') };
        return object;
      }
    } catch (error) {
      this.logger.log('error', 'js error - ' + error);
    }
  }

  isDateStringValid(dateString) {
    const datePattern1 = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}\+\d{4}$/;
    const datePattern2 = /^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/;
    const datePattern3 = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$/;
    return datePattern1.test(dateString) || datePattern2.test(dateString) || datePattern3.test(dateString);
  }

  calculateDate(_JSDate) {
        if (!_JSDate) {
            return 'No Data';
        }
        const date = new Date(_JSDate);
        const year = date.getFullYear().toString();
        const month = date.getMonth() + 1;
        let monthString;
        if (month < 10) {
            monthString = '0' + month.toString();
        } else {
            monthString = month.toString();
        }
        const day = date.getDate();
        let dayString;
        if (day < 10) {
            dayString = '0' + day.toString();
        } else {
            dayString = day.toString();
        }
        return monthString + '-' + dayString + '-' + year ;
    }

    calculateDateAndTime(_JSDate,requiredTime = false) {

      const monthsList = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sept', 'Oct', 'Nov', 'Dec'];
      const date = new Date(_JSDate);
      const year = date.getFullYear();
      const month = date.getMonth();
      const day = date.getDate();
      const monthValue = monthsList[month];
      let hours = date.getHours();
      let hrs = ''+hours;
      if(hours<10)
      hrs = '0' + hours;
      let minutes = date.getMinutes();
      let mins = ''+minutes;
      if(minutes<10)
      mins = '0' + minutes;
      const ampm = (hours >= 12 && hours<24)  ? 'PM' : 'AM';
        if ((ampm === 'PM' && hours != 12)|| (ampm=='AM' && hours==24)) {
          hours = hours - 12;
        }
      if (requiredTime) {
        return monthValue + ' ' + day + ',' + ' ' + year + ' '+ hrs + ':' + mins + ' ' + ampm;
      }
      return monthValue + ' ' + day + ',' + ' ' + year + ' ';
    }

    getNumberOfWeeks = (year, quarter) => {
      const currentQuarter = QUARTER.quarterObj[quarter];
      const fromDate = new Date(`${year}-${currentQuarter.fromMonth}-${currentQuarter.fromDay}`);
      let weeks = 14;
      if (+quarter === 1) {
        // if year is leap year and first quarter starts with Sunday.
        if (this.isLeapYear(fromDate.getFullYear()) && fromDate.getDay() === 0) {
          weeks = 13;
        // if first quarter starts with Sunday or Monday.
        } else if (fromDate.getDay() === 0 || fromDate.getDay() === 1) {
          weeks = 13;
        }
      // if second quarter starts with Sunday.
      } else if (+quarter === 2 && fromDate.getDay() === 0) {
        weeks = 13;
      }

      return weeks;
    };

    private isLeapYear(year: number) {
        return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
    }

    checkIfAPIReturnedDataIsEmpty(data) {
    // There can be multiple scenarios:
    /*
    - data can be an empty object
    - data can be an empty array
    - data can be undefined
    */

    let isEmpty = false;

        if (data) {
            if (Array.isArray(data) && data.length === 0) {
                isEmpty = true;
            } else if (Object.keys(data).length === 0 && data.constructor === Object) {
                isEmpty = true;
            }
        } else {
            isEmpty = true;
        }
        return isEmpty;
    }

    strToBool(str) {
        // will match one and only one of the string 'true','1', or 'on' rerardless
        // of capitalization and regardless off surrounding white-space.
        //
        const regex = /^\s*(true|1|on)\s*$/i;

        return regex.test(str);
    }

    extractNumbersFromString(str) {
      const numb = str.match(/\d/g);
      const number = numb.join('');

      return number;
    }

    capitalizeFirstLetter(string): any {
      return string.charAt(0).toUpperCase() + string.slice(1);
    }

    findValueInArray(array, valueToBeFound) {

      return array.findIndex(eachValue => {
        return (eachValue && eachValue.toLowerCase()) === (valueToBeFound && valueToBeFound.toLowerCase());
      });
    }

    getContextUrlExceptDomain(url) {
      const parser = this.parseUrl(url);

      const pathname = parser.pathname || '';
      const query = parser.search || '';
      const fragments = parser.hash || '';

      return pathname + query + fragments;
    }

    parseUrl(url) {

      let parser;

      if (url && url !== '') {
        parser = document.createElement('a');
        parser.href = url;

        /*
        parser.protocol; // => "http:"
        parser.hostname; // => "example.com"
        parser.port;     // => "3000"
        parser.pathname; // => "/pathname/"
        parser.search;   // => "?search=test"
        parser.hash;     // => "#hash"
        parser.host;     // => "example.com:3000"
        */
      }

      return parser;

    }

    getDateAndTime(dateWithoutTime, assumeUTCEndOfDay = true) {
      const date = new Date(dateWithoutTime);

      const hours = assumeUTCEndOfDay ? 23 : 0;
      const minutes = assumeUTCEndOfDay ? 59 : 0;
      const seconds = 0;

      const date_utc = this.convertDateToUTCEndOfDay(date, hours, minutes, seconds);

      const date_currentTimeZone = new Date(date_utc);

      return date_currentTimeZone;
    }

    convertDateToUTCEndOfDay(date, hours = 23, minutes = 59, seconds = 0) {
      return Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), hours, minutes, seconds);
    }

    getUTCDate(date) {
      return this.datePipe.transform(new Date(date).toUTCString(), this.utcDateFormat);
    }

    generateIntervals(min, max, numOfIntervals, isInclusive=false) : {lowerBound: number, upperBound: number}[]{
      if (numOfIntervals <= 0) {
        return []; // No intervals to generate
      }
    
      if (numOfIntervals >= max - min + 1) {
        return [{ lowerBound: min, upperBound: max }]; // Single interval covering the entire range
      }
    
      const intervalSize = Math.floor((max - min + 1) / numOfIntervals); // Calculate the interval size
    
      const intervals = [];
    
      for (let i = 0; i < numOfIntervals; i++) {
        const lowerBound = min + i * intervalSize;
        const upperBound = i === numOfIntervals - 1 ? max : min + (i + 1) * intervalSize - 1;
    
        if (isInclusive) {
          intervals.push({ lowerBound: lowerBound-1, upperBound: upperBound })
        } else {
          intervals.push({ lowerBound, upperBound });
        }
      }
    
      return intervals;
    }

    getDifferenceBetweenDateByDays(date1: Date, date2: Date) : number {
      const oneDay = 24 * 60 * 60 * 1000; // One day in milliseconds
      const diffDays = Math.round(Math.abs((date1.getTime() - date2.getTime()) / oneDay));      
      return diffDays;
    }

    mapToObject(map: Map<string, string> | undefined): { [key: string]: string } | undefined {
      return map
        ? Object.fromEntries(map)
        : undefined;
    }

    mapValuesToArray(map):string[] {
      if(map) return Array.from(map.values());
      else return undefined;
    }
    
    storeRedirectUrl(url?){
      localStorage.setItem(REDIRECT_URL_KEY, url ?? location.href);
    }
  
    getAscendingOrder(orderMap){
      return Object.keys(orderMap).sort((a, b) => orderMap[b] - orderMap[a]);
    }
  
    getDescendingOrder (orderMap) {
      return this.getAscendingOrder(orderMap).reverse();
    }
  
  getFormattedDate (dateString: string, isEndDate: boolean = false, offsetToAppend="+0000"): string {
    const localDate = new Date(dateString);

    if (isEndDate) {
      localDate.setHours(23, 59, 59);
    } else {
      localDate.setMinutes(localDate.getMinutes() + localDate.getTimezoneOffset());
    }
    localDate.setMinutes(localDate.getMinutes() + localDate.getTimezoneOffset());

    const datePipe = new DatePipe('en-US');

    let formattedDate = datePipe.transform(localDate, 'yyyy-MM-dd HH:mm:ss');
    if (offsetToAppend === "z") {
      formattedDate = datePipe.transform(localDate, 'yyyy-MM-ddTHH:mm:ss');
    }

    return formattedDate + offsetToAppend;
  }
}
