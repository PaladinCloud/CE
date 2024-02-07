import { Injectable } from '@angular/core';
import { SvgIconRegistryService } from 'angular-svg-icon';
import { imagesToPreLoad } from 'src/app/shared/table/images-to-preload';

@Injectable({
  providedIn: 'root'
})
export class ImageCacheService {
  constructor(
    private iconReg: SvgIconRegistryService
  ) { }

  async preLoadImages (): Promise<void> {
    const promises = imagesToPreLoad.map(async (icon) => {
      const url = '/assets/icons/' + icon + '.svg';
      this.iconReg.loadSvg(url, icon).toPromise();
    });
    await Promise.all(promises);
  }
}
