/**
 * Mix and Munch: Validation Utilities
 * Centralized validation functions for form fields, inputs and file uploads
 */

/**
 * Validates a form field and shows error if invalid
 * @param {HTMLElement} field - The field to validate
 * @param {string} errorMessage - Error message to display
 * @returns {boolean} - True if valid, false otherwise
 */
export function validateField(field, errorMessage) {
  if (!field) return true; // Skip if field doesn't exist
  
  const value = field.value.trim();
  const fieldContainer = field.closest('.form-group');
  
  if (!value) {
    // Show error
    field.classList.add('error-input');
    
    // Remove any existing error message
    removeExistingErrorMessage(fieldContainer);
    
    // Add error message
    const errorElement = document.createElement('p');
    errorElement.className = 'field-error';
    errorElement.textContent = errorMessage;
    fieldContainer.appendChild(errorElement);
    
    return false;
  } else {
    // Clear error
    field.classList.remove('error-input');
    removeExistingErrorMessage(fieldContainer);
    return true;
  }
}

/**
 * Validates an image file for type and size restrictions
 * @param {File} file - The file to validate
 * @param {Function} showError - Function to show error messages
 * @param {Function} resetPreview - Function to reset preview on error
 * @returns {boolean} - True if valid, false otherwise
 */
export function validateImageFile(file, showError, resetPreview) {
  // Check file type
  const validTypes = ['image/jpeg', 'image/png', 'image/gif'];
  if (!validTypes.includes(file.type)) {
    showError('Invalid file type. Please upload JPG, PNG, or GIF.');
    resetPreview();
    return false;
  }
  
  // Check file size (5MB max)
  const maxSize = 5 * 1024 * 1024; // 5MB in bytes
  if (file.size > maxSize) {
    showError('Image too large. Maximum size is 5MB.');
    resetPreview();
    return false;
  }
  
  return true;
}

/**
 * Validates an email address format
 * @param {string} email - Email to validate
 * @returns {boolean} - True if valid, false otherwise
 */
export function validateEmail(email) {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
}

/**
 * Validates password strength
 * @param {string} password - Password to validate
 * @returns {boolean} - True if strong enough, false otherwise
 */
export function validatePassword(password) {
  // At least 8 chars, with uppercase, lowercase and number
  const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/;
  return passwordRegex.test(password);
}

/**
 * Removes any existing error message in a container
 * @param {HTMLElement} container - Container to check for error messages
 */
export function removeExistingErrorMessage(container) {
  const existingError = container.querySelector('.field-error');
  if (existingError) {
    container.removeChild(existingError);
  }
}

/**
 * Shows an error message for an input field
 * @param {HTMLElement} input - Input element 
 * @param {string} message - Error message to display
 */
export function showErrorMessage(input, message) {
  input.classList.add('error-input');
  
  // Find or create error message element
  const formGroup = input.closest('.form-group') || input.parentNode;
  let errorMsg = formGroup.querySelector('.error-message');
  
  if (!errorMsg) {
    errorMsg = document.createElement('div');
    errorMsg.className = 'error-message';
    formGroup.appendChild(errorMsg);
  }
  
  errorMsg.textContent = message;
  errorMsg.style.display = 'block';
}

/**
 * Clears error message for an input field
 * @param {HTMLElement} input - Input element
 */
export function clearErrorMessage(input) {
  input.classList.remove('error-input');
  
  const formGroup = input.closest('.form-group') || input.parentNode;
  const errorMsg = formGroup.querySelector('.error-message');
  
  if (errorMsg) {
    errorMsg.textContent = '';
    errorMsg.style.display = 'none';
  }
}

/**
 * Sanitizes user input to prevent XSS
 * @param {string} input - Text to sanitize
 * @returns {string} - Sanitized text
 */
export function sanitizeInput(input) {
  if (!input) return '';
  
  return String(input)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}
