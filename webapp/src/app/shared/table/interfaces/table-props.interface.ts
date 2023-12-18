export interface IFilterOption {
   optionName: string;
   optionURL: string;
   optionValue: string
}

export interface IColumnNamesMap {
   [key: string]: string;
}

export interface IColumnWidthsMap {
   [key: string]: number;
}

export type IFilterTypeLabel = string;

export interface IFilterTagOptionsMap {
   [key: string]: { name: string, id: string }[];
}

export interface IFilterTagLabelsMap {
   [key: string]: string[];
}

export interface IFilterObj {
   keyDisplayValue: string;
   filterValue: string[] | string;
   key: string;
   value: string[] | string;
   filterkey: string;
   compareKey: string;
}
