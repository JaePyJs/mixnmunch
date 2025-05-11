const { validateEmail, validatePassword, sanitizeInput } = require('./validation');

describe('Validation Utilities', () => {
  describe('validateEmail', () => {
    test('should return true for valid emails', () => {
      expect(validateEmail('test@example.com')).toBe(true);
      expect(validateEmail('user.name+tag@example.co.uk')).toBe(true);
    });

    test('should return false for invalid emails', () => {
      expect(validateEmail('testexample.com')).toBe(false);
      expect(validateEmail('test@example')).toBe(false);
      expect(validateEmail('@example.com')).toBe(false);
      expect(validateEmail('')).toBe(false);
      expect(validateEmail(null)).toBe(false);
      expect(validateEmail(undefined)).toBe(false);
    });
  });

  describe('validatePassword', () => {
    test('should return true for valid passwords', () => {
      expect(validatePassword('Password123')).toBe(true);
      expect(validatePassword('Str0ngP@ss!')).toBe(true); // Assuming special chars are allowed by regex, though current regex doesn't explicitly require them
    });

    test('should return false for invalid passwords', () => {
      expect(validatePassword('short')).toBe(false); // Too short
      expect(validatePassword('nouppercase1')).toBe(false); // No uppercase
      expect(validatePassword('NOLOWERCASE1')).toBe(false); // No lowercase
      expect(validatePassword('NoNumber')).toBe(false); // No number
      expect(validatePassword('')).toBe(false);
      expect(validatePassword(null)).toBe(false);
      expect(validatePassword(undefined)).toBe(false);
    });
  });

  describe('sanitizeInput', () => {
    test('should return empty string for null or undefined input', () => {
      expect(sanitizeInput(null)).toBe('');
      expect(sanitizeInput(undefined)).toBe('');
    });

    test('should escape HTML special characters', () => {
      expect(sanitizeInput('<script>alert("xss")</script>')).toBe('&lt;script&gt;alert(&quot;xss&quot;)&lt;/script&gt;');
      expect(sanitizeInput('Hello & World > Test < Tag \'Single\' "Double"')).toBe('Hello &amp; World &gt; Test &lt; Tag &#039;Single&#039; &quot;Double&quot;');
    });

    test('should return the same string if no special characters are present', () => {
      expect(sanitizeInput('Just a normal string 123')).toBe('Just a normal string 123');
    });

    test('should handle empty string input', () => {
      expect(sanitizeInput('')).toBe('');
    });
  });
});
