import { Injectable } from '@angular/core';
import { SvgIconRegistryService } from 'angular-svg-icon';
import { imagesToPreLoad } from 'src/app/shared/table/images-to-preload';

@Injectable({
    providedIn: 'root',
})
export class ImageCacheService {
    constructor(private iconReg: SvgIconRegistryService) {}

    preLoadImages() {
        imagesToPreLoad.map((icon) => {
            this.loadIcon(icon);
        });
    }

    loadIcon(icon, path = '/assets/icons', extension = 'svg') {
        this.iconReg.getSvgByName(icon).subscribe(
            () => {},
            (error) => {
                const url = `${path}/${icon}.${extension}`;
                this.iconReg.loadSvg(url, icon);
            },
        );
    }
}
