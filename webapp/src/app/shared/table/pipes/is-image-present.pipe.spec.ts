import { IsImagePresentPipe } from './is-image-present.pipe';

describe('IsImagePresentPipe', () => {
  it('create an instance', () => {
    const pipe = new IsImagePresentPipe();
    expect(pipe).toBeTruthy();
  });
});
