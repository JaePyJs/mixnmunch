module.exports = [
  {
    ignores: ['**/node_modules/**', '**/dist/**'],
    languageOptions: {
      ecmaVersion: 2022,
      sourceType: 'module',
    },
    rules: {
      'no-unused-vars': 'warn',
      'no-undef': 'error',
      'semi': ['error', 'always'],
      'quotes': ['error', 'single'],
    },
    files: ['**/*.js'],
  },
];
