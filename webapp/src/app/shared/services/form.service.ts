import { FormGroup } from '@angular/forms';
import { Injectable } from '@angular/core';

@Injectable()
export class FormService {

  // get all values of the formGroup, loop over them
  // then mark each field as touched
  public markFormGroupTouched(formGroup: FormGroup) {
    Object.values(formGroup.controls).forEach(control => {
      control.markAsTouched();

      if (control['controls']) {
          control['controls'].forEach(c => this.markFormGroupTouched(c));
      }
    });
  }

  // return list of error messages
  public validationMessages() {
    const messages = {
      required: 'This field is required',
      email: 'This email address is invalid',
      pattern: 'This character is not allowed',
      digit: 'This field must contain only digits',
      exactDigits: 'This field must contain exact 12 digits',
      groupNameAlreadyExists: 'Group Name already exists',
      invalidType: 'Type cannot be System or User',
      invalidJson: 'Invalid Json format',
      invalidProjectIdLength: 'This field must be between 6 and 30 characters long',
      invalidClientSecretLength: 'This field must be 40 characters long',
      alphanumeric: 'This field must contain only letters and digits',
      alphanumericHyphen: 'This field must contain only letters, digits and hyphen',
      endsWithHyphen: 'This field must not end with a hyphen',
      startsWithLetter: 'This field must start with a letter',
      invalidField: 'This field contains invalid characters',
      invalidURL: 'Please enter a valid Url',
      maxlength: 'This field is exceeding maximum characters',
      minlength: 'This field must contain atleast 6 characters',
      max: 'This field should not exceed maximum value',
      notMultipleOf24: 'This field should contain only multiples of 24',
      noStartingNumberOrSpecialCharacter: 'This field cannot start with a number or any special character',
      alphabetsOnly: 'This field must contain only alphabets',
      invalid_characters: (matches: any[]) => {

        let matchedCharacters = matches;

        matchedCharacters = matchedCharacters.reduce((characterString, character, index) => {
          let string = characterString;
          string += character;

          if (matchedCharacters.length !== index + 1) {
            string += ', ';
          }

          return string;
        }, '');

        return `These characters are not allowed: ${matchedCharacters}`;
      },
    };

    return messages;
  }

  // Validate form instance
  // check_dirty true will only emit errors if the field is touched
  // check_dirty false will check all fields independent of
  // being touched or not. Use this as the last check before submitting
  public validateForm(formToValidate: FormGroup, formErrors: any, checkDirty?: boolean) {
    const form = formToValidate;
    for (const field in formErrors) {
      if (field) {
        formErrors[field] = '';
        const control = form.get(field);

        const messages = this.validationMessages();
        if (control && !control.valid) {
          if (!checkDirty || (control.dirty || control.touched)) {
            for (const key in control.errors) {
              if (key && key !== 'invalid_characters') {
                formErrors[field] = formErrors[field] || messages[key];
              } else {
                formErrors[field] = formErrors[field] || messages[key](control.errors[key]);
              }
            }
          }
        }
      }
    }

    return formErrors;
  }
}
