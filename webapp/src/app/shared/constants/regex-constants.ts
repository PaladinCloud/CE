// Only alphanumeric characters
export const ONLY_ALPHANUMERIC: RegExp = /^[a-zA-Z0-9]+$/;

// URL pattern
export const URL_PATTERN: RegExp = /^(ftp|http|https):\/\/[^ "]+$/;

// Client secret pattern (alphanumeric, hyphen, underscore, dot, tilde, max length 40)
export const CLIENT_SECRET_PATTERN: RegExp = /^[a-zA-Z0-9\-_.~]{0,40}$/;

// Lowercase alphanumeric and hyphen only
export const LOWERCASE_ALPHANUMERIC_HYPHEN: RegExp = /^[a-z0-9-]+$/;

// Starts with an alphabet
export const STARTS_WITH_ALPHABET: RegExp = /^[a-zA-Z]/;

// Starts with an alphabet and allows any characters after that
export const STARTS_WITH_ALPHABET_ALLOWS_ANY: RegExp = /^[a-zA-Z].*$/;

// Only alphabetic characters
export const ONLY_ALPHABETS: RegExp = /^[a-zA-Z]+$/;

// Email pattern
export const EMAIL_PATTERN: RegExp = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;

// Starts with a digit
export const STARTS_WITH_DIGIT: RegExp = /^\d/;

// Only digits
export const ONLY_DIGITS: RegExp = /^\d+$/;

// Checks if not a space, word character, comma, period, colon, ampersand, forward slash, plus sign, percentage sign, single quote, backtick, at symbol, or hyphen.
export const NOT_SPECIAL_CHARACTERS = /[^\s\w,.:&\/()+%'`@-]/;