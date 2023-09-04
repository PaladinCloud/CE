import { FormControl, Validators } from '@angular/forms';

// setup simple regex for white listed characters
const validCharacters = /[^\s\w,.:&\/()+%'`@-]/;


// create your class that extends the angular validator class
export class CustomValidators extends Validators {
  static validateCharacters(control: FormControl) {
    if (control.value && control.value.length > 0) {
      const matches = control.value.match(validCharacters);
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
 
    if (!/^[a-zA-Z]/.test(projectId)) {
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
    const pattern = /^[a-zA-Z0-9]+$/;

    if (pattern.test(value)) {
      return null; // Validation passed
    } else {
      return { alphanumeric: true }; // Validation failed
    }
  }

  static alphanumericHyphenValidator(control){
    const value = control.value;
    const regex = /^[a-z0-9-]+$/;
    const isValid = regex.test(value);

    if (!isValid) {
      return { alphanumericHyphen: true };
    }
    return null;
  }

  static clientSecretVlidator(control){
    const value = control.value;
    const pattern = /^[a-zA-Z0-9\-_.~]{0,40}$/;

    if (!pattern.test(value)) {
      return { invalidField: true }; // Validation failed
    }

    if(value.length!=40){
      return { invalidClientSecretLength: true };
    }
    return null;
  }

  static urlValidator(control){
    const value = control.value;
    const pattern = /^(ftp|http|https):\/\/[^ "]+$/;

    if (pattern.test(value)) {
      return null; // Validation passed
    } else {
      return { invalidURL: true }; // Validation failed
    }
  }

  static checkIfMultipleOfTwentyFour(control){
    const value = control.value;
    console.log(value," value ",value%24);
    if(value%24==0){
      return null;
    }
    return { notMultipleOf24 : true};
  }

}