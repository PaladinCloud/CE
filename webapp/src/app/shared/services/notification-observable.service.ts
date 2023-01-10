import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class NotificationObservableService {

  private subject = new Subject;

    postMessage (msg: String, duration , category? , image?) {
        if (msg) {
            const obj = {
                'msg': msg,
                'duration': duration,
                'category' : category,
                'image': image
            };
            this.subject.next(obj);
        }
    }

    getMessage(): Observable<any> {
        return this.subject.asObservable();
    }
}
