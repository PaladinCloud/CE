import { AbstractControl, FormControl, ValidatorFn, Validators } from '@angular/forms';
import { CLIENT_SECRET_PATTERN, LOWERCASE_ALPHANUMERIC_HYPHEN, NOT_SPECIAL_CHARACTERS, ONLY_ALPHABETS, ONLY_ALPHANUMERIC, STARTS_WITH_ALPHABET, STARTS_WITH_ALPHABET_ALLOWS_ANY, URL_PATTERN } from './constants/regex-constants';

// create your class that extends the angular validator class
export class CustomValidators extends Validators {
  static validateCharacters(control: FormControl) {
    if (control.value && control.value.length > 0) {
      const matches = control.value.match(NOT_SPECIAL_CHARACTERS);
      return matches 
      && matches.length ? { 'not_allowed_characters': matches } : null;
    } else {
      return null;
    }
  }

    static exactDigits(control: FormControl) {
        const value = control.value;
        if (value !== null && value.toString().length == 12) {
                 return null;
        }
        return { exactDigits: { value: 'The input should contain exact 12 digits.' } };
    };

    static validateJson(control) {
      try {
        JSON.parse(control.value);
        return null; // Return null if it's a valid JSON
      } catch (error) {
        return { invalidJson: true }; // Return an error object if it's an invalid JSON
      }
    }

    static validateProjectId(control) {
    const projectId = control.value;
 
      if (!STARTS_WITH_ALPHABET.test(projectId)) {
      return { startsWithLetter: true };
    }

    if(projectId.length<6 || projectId.length>30){
      return { invalidProjectIdLength: true }
    }

    if (projectId.endsWith('-')) {
      return { endsWithHyphen: true };
    }
    return null;
  }

  static alphanumericValidator(control){
    const value = control.value;

    if (ONLY_ALPHANUMERIC.test(value)) {
      return null; // Validation passed
    } else {
      return { alphanumeric: true }; // Validation failed
    }
  }

  static alphanumericHyphenValidator(control){
    const value = control.value;
    const isValid = LOWERCASE_ALPHANUMERIC_HYPHEN.test(value);

    if (!isValid) {
      return { alphanumericHyphen: true };
    }
    return null;
  }

  static clientSecretVlidator(control){
    const value = control.value;

    if (!CLIENT_SECRET_PATTERN.test(value)) {
      return { invalidField: true }; // Validation failed
    }

    if(value.length!=40){
      return { invalidClientSecretLength: true };
    }
    return null;
  }

  static urlValidator(control){
    const value = control.value;

    if (URL_PATTERN.test(value)) {
      return null; // Validation passed
    } else {
      return { invalidURL: true }; // Validation failed
    }
  }

  static checkIfMultipleOfTwentyFour(control){
    const value = control.value;
    if(value%24==0){
      return null;
    }
    return { notMultipleOf24 : true};
  }

  static onlyAlphabets(control: AbstractControl) : { [key: string]: any } | null {
    const value:string = control.value;
    
    if (!value) {
      return null;
    }
    return ONLY_ALPHABETS.test(value) ? null : { alphabetsOnly: true };
  }

  static noStartingNumberOrSpecialCharacter(control: AbstractControl): { [key: string]: any } | null {
    const value: string = control.value;
    if (!value) {
      return null;
    }

    return STARTS_WITH_ALPHABET_ALLOWS_ANY.test(value) ? null : { noStartingNumberOrSpecialCharacter: true };
  }

  static minLengthTrimValidator(minLength: number): ValidatorFn {
    return (control: AbstractControl): { [key: string]: any } | null => {
      const value: string = control.value;
  
      if (!value) {
        return null; // If the value is empty, consider it as valid
      }
  
      const trimmedValue = value.trim();
  
      return trimmedValue.length >= minLength ? null : { 'minlength': true };
    };
  }

}