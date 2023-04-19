import { PropTitleCasePipe } from './prop-title-case.pipe';

describe('PropTitleCasePipe', () => {
    it('create an instance', () => {
        const pipe = new PropTitleCasePipe();
        expect(pipe).toBeTruthy();
    });

    it('does nothing for __somethingName', () => {
        const pipe = new PropTitleCasePipe();
        const result = pipe.transform('__somethingName');
        expect(result).toEqual('__somethingName');
    });

    it('converts somethingName to Something Name', () => {
        const pipe = new PropTitleCasePipe();
        const result = pipe.transform('somethingName');
        expect(result).toEqual('Something Name');
    });

    it('converts something_name to Something Name', () => {
        const pipe = new PropTitleCasePipe();
        const result = pipe.transform('something_name');
        expect(result).toEqual('Something Name');
    });
});
