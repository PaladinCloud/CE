import {
    Directive,
    ElementRef,
    EventEmitter,
    HostListener,
    Input,
    OnInit,
    Output,
    Renderer2,
} from '@angular/core';

@Directive({
    selector: '[appPropValueLink]',
})
export class PropValueLinkDirective implements OnInit {
    @Input() appPropValueLink = '';
    @Output() innerNavigate = new EventEmitter<string>();

    @HostListener('click', ['$event'])
    onClick(event: { target: HTMLElement }) {
        if (event.target.getAttribute('routerLink')) {
            this.innerNavigate.next(event.target.getAttribute('routerLink'));
        }
    }

    private readonly LINK_REGEX = new RegExp(
        /https?:\/\/(www\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_+.~#?&//=]*)/gi,
    );

    constructor(private el: ElementRef<HTMLElement>, private renderer: Renderer2) {}

    ngOnInit(): void {
        const el = this.el.nativeElement;
        const prop = 'innerHTML';
        if (this.appPropValueLink.match(this.LINK_REGEX)) {
            this.renderer.setProperty(
                el,
                prop,
                this.appPropValueLink.replace(
                    this.LINK_REGEX,
                    '<a href="$&" class="primary-400" target="_blank">$&</a>',
                ),
            );
        } else if (this.appPropValueLink.startsWith('/')) {
            this.renderer.setProperty(
                el,
                prop,
                `<a class="primary-400" routerLink="${this.appPropValueLink}">${this.appPropValueLink}</a>`,
            );
        }
    }
}
