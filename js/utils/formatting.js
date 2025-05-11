/**
 * Mix and Munch: Formatting Utilities
 * Centralized formatting functions for dates, strings, and other display elements
 */

/**
 * Formats a date string in a user-friendly way (relative time)
 * @param {string} dateString - ISO date string to format
 * @returns {string} - Formatted date string
 */
export function formatRelativeDate(dateString) {
  if (!dateString) return 'Unknown date';
  
  const date = new Date(dateString);
  const now = new Date();
  const diffTime = Math.abs(now - date);
  const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));
  
  if (isNaN(date.getTime())) return 'Invalid date';
  
  if (diffDays === 0) {
    return 'Today';
  } else if (diffDays === 1) {
    return 'Yesterday';
  } else if (diffDays < 7) {
    return `${diffDays} days ago`;
  } else if (diffDays < 30) {
    return `${Math.floor(diffDays / 7)} weeks ago`;
  } else {
    return date.toLocaleDateString();
  }
}

/**
 * Formats ingredient amounts and measurements consistently
 * @param {string} amount - The ingredient amount
 * @param {string} measurement - The unit of measurement
 * @param {string} ingredient - The ingredient name
 * @returns {string} - Formatted ingredient string
 */
export function formatIngredient(amount, measurement, ingredient) {
  if (!ingredient) return '';
  
  let formattedAmount = amount ? amount : '';
  const formattedMeasurement = measurement ? measurement : '';
  const formattedIngredient = ingredient.trim();
  
  // Format fractions nicely
  if (formattedAmount.includes('/')) {
    const [whole, fraction] = formattedAmount.split(' ');
    if (fraction) {
      formattedAmount = `${whole} ${fraction}`;
    }
  }
  
  return [formattedAmount, formattedMeasurement, formattedIngredient]
    .filter(Boolean)
    .join(' ');
}

/**
 * Formats recipe difficulty level for display
 * @param {number|string} difficulty - Difficulty rating (1-5)
 * @returns {string} - Formatted difficulty string with emoji
 */
export function formatDifficulty(difficulty) {
  const difficultyNum = parseInt(difficulty, 10);
  
  if (isNaN(difficultyNum) || difficultyNum < 1) {
    return 'Easy 😊';
  }
  
  switch (difficultyNum) {
    case 1: return 'Very Easy 😊';
    case 2: return 'Easy 👌';
    case 3: return 'Medium 🧑‍🍳';
    case 4: return 'Challenging 👨‍🍳';
    case 5: return 'Difficult 🔥';
    default: return 'Expert Level 🏆';
  }
}

/**
 * Formats cooking time in a user-friendly way
 * @param {number} minutes - Cooking time in minutes
 * @returns {string} - Formatted time string
 */
export function formatCookingTime(minutes) {
  if (!minutes || isNaN(parseInt(minutes, 10))) {
    return 'Time not specified';
  }
  
  const mins = parseInt(minutes, 10);
  
  if (mins < 60) {
    return `${mins} min`;
  } else {
    const hours = Math.floor(mins / 60);
    const remainingMins = mins % 60;
    
    if (remainingMins === 0) {
      return `${hours} hr`;
    } else {
      return `${hours} hr ${remainingMins} min`;
    }
  }
}

/**
 * Creates a truncated text with ellipsis if needed
 * @param {string} text - Original text 
 * @param {number} maxLength - Maximum length before truncation
 * @returns {string} - Truncated text with ellipsis if needed
 */
export function truncateText(text, maxLength = 100) {
  if (!text || text.length <= maxLength) {
    return text;
  }
  
  return text.substring(0, maxLength) + '...';
}
