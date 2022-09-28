import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable()
export class WindowExpansionService {
    status = new Subject<boolean>();

    getExpansionStatus(){
        return this.status;
    }

    setExpansionStatus(status: boolean){
        this.status.next(status);
    }
}
