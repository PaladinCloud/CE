import { SeverityPipe } from './severity.pipe';

describe('SeverityPipe', () => {
    it('create an instance', () => {
        const pipe = new SeverityPipe();
        expect(pipe).toBeTruthy();
    });
});
