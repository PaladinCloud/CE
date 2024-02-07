import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'resultCount'
})
export class ResultCountPipe implements PipeTransform {

  transform (totalRecordsAfterFilter: number, dataSource: any): string {
    const resultCount = totalRecordsAfterFilter === 0 ?
      dataSource?.matTableDataSource.data?.length :
      totalRecordsAfterFilter;
    const formattedResultCount = new Intl.NumberFormat().format(resultCount);
    return formattedResultCount;
  }

}
