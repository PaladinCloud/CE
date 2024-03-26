export interface IFilterOption {
    optionName: string;
    optionURL: string;
    optionValue: string;
}

export interface IColumnNamesMap {
    [key: string]: string;
}

export interface IColumnWidthsMap {
    [key: string]: number;
}

export type IFilterTypeLabel = string;

export interface IFilterTagOptionsMap {
    [key: string]: { name: string; id: string }[];
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

export interface ICellObj {
    text: string;
    titleText: string;
    valueText: string;
    hasPostImage: boolean;
    imgSrc: string;
    postImgSrc: string;
    isChip: boolean;
    chipList?: string[];
    chipBackgroundColor?: string;
    chipTextColor?: string;
    isMenuBtn: boolean;
    properties?: string;
    isLink: boolean;
    imageTitleText?: string;
    isDate: boolean;
    menuItems?: string[];
}

export interface IRowObj {
    [key: string]: ICellObj;
}
