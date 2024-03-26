// Only alphanumeric characters
export const ONLY_ALPHANUMERIC: Readonly<RegExp> = /^[a-zA-Z0-9]+$/;

// URL pattern
export const URL_PATTERN: Readonly<RegExp> = /^(ftp|http|https):\/\/[^ "]+$/;

// Client secret pattern (alphanumeric, hyphen, underscore, dot, tilde, max length 40)
export const CLIENT_SECRET_PATTERN: Readonly<RegExp> = /^[a-zA-Z0-9\-_.~]{0,40}$/;

// Lowercase alphanumeric and hyphen only
export const LOWERCASE_ALPHANUMERIC_HYPHEN: Readonly<RegExp> = /^[a-z0-9-]+$/;

// Starts with an alphabet
export const STARTS_WITH_ALPHABET: Readonly<RegExp> = /^[a-zA-Z]/;

// Starts with an alphabet and allows any characters after that
export const STARTS_WITH_ALPHABET_ALLOWS_ANY: Readonly<RegExp> = /^[a-zA-Z].*$/;

// Only alphabetic characters
export const ONLY_ALPHABETS: Readonly<RegExp> = /^[a-zA-Z]+$/;

// Email pattern
export const EMAIL_PATTERN: Readonly<RegExp> = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;

// Starts with a digit
export const STARTS_WITH_DIGIT: Readonly<RegExp> = /^\d/;

// Only digits
export const ONLY_DIGITS: Readonly<RegExp> = /^\d+$/;

// Checks if not a space, word character, comma, period, colon, ampersand, forward slash, plus sign, percentage sign, single quote, backtick, at symbol, or hyphen.
export const NOT_SPECIAL_CHARACTERS: Readonly<RegExp> = /[^\s\w,.:&\/()+%'`@-]/;
