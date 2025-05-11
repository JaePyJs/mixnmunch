/**
 * Mix and Munch: Recipe Generator Component
 * Handles ingredient input, recipe search and recipe display functionality
 * Updated for mobile-first UI with tabbed interface
 */

import { MealDbService } from '../services/api-service.js'; // Import class
import { showErrorMessage, clearErrorMessage } from '../utils/validation.js';
import { truncateText, formatRelativeDate } from '../utils/formatting.js';

export class RecipeGenerator {
  constructor(dependencies = {}) { // Accept dependencies
    this.mealDbService = dependencies.mealDbService; // Store injected service instance
    // DOM element references - mobile-first UI elements
    this.mobileIngredientInput = document.getElementById('mobile-ingredient-input');
    this.mobileAddIngredientBtn = document.getElementById('mobile-add-ingredient');
    this.mobileIngredientList = document.getElementById('mobile-ingredient-list');
    this.mobileGenerateRecipeBtn = document.getElementById('mobile-generate-recipe');
    this.mobileRecipeResult = document.getElementById('mobile-recipe-result');
    
    // For backwards compatibility, also reference the old elements if they exist
    this.ingredientInput = document.getElementById('ingredient-input') || this.mobileIngredientInput;
    this.addIngredientBtn = document.getElementById('add-ingredient') || this.mobileAddIngredientBtn;
    this.ingredientList = document.getElementById('ingredient-list') || this.mobileIngredientList;
    this.generateRecipeBtn = document.getElementById('generate-recipe') || this.mobileGenerateRecipeBtn;
    this.recipeResult = document.getElementById('recipe-result') || this.mobileRecipeResult;
    
    // State
    this.ingredients = [];
    
    // Bind event handlers
    this.bindEvents();
  }
  
  /**
   * Set up event listeners
   */
  bindEvents() {
    // For mobile UI
    if (this.mobileAddIngredientBtn) {
      this.mobileAddIngredientBtn.addEventListener('click', () => this.addIngredient());
    }
    
    if (this.mobileIngredientInput) {
      this.mobileIngredientInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
          this.addIngredient();
          e.preventDefault();
        }
      });
    }
    
    if (this.mobileGenerateRecipeBtn) {
      this.mobileGenerateRecipeBtn.addEventListener('click', () => this.generateRecipe());
    }
    
    // For backwards compatibility with old UI
    if (this.addIngredientBtn && this.addIngredientBtn !== this.mobileAddIngredientBtn) {
      this.addIngredientBtn.addEventListener('click', () => this.addIngredient());
    }
    
    if (this.ingredientInput && this.ingredientInput !== this.mobileIngredientInput) {
      this.ingredientInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
          this.addIngredient();
          e.preventDefault();
        }
      });
    }
    
    if (this.generateRecipeBtn && this.generateRecipeBtn !== this.mobileGenerateRecipeBtn) {
      this.generateRecipeBtn.addEventListener('click', () => this.generateRecipe());
    }
    
    // Register for tab change events
    document.addEventListener('tabChanged', (e) => {
      if (e.detail.tabId === 'generate') {
        // Focus the input field when the generate tab becomes active
        if (this.mobileIngredientInput) {
          setTimeout(() => this.mobileIngredientInput.focus(), 300);
        }
      }
    });
  }
  
  /**
   * Add an ingredient to the list
   * @returns {void}
   */
  addIngredient() {
    // Use mobile input if available, otherwise fallback
    const input = this.mobileIngredientInput || this.ingredientInput;
    const value = input.value.trim();
    
    // Basic form validation
    if (!value) {
      showErrorMessage(input, 'Please enter an ingredient');
      return;
    }
    
    // Check for duplicates
    if (this.ingredients.includes(value.toLowerCase())) {
      showErrorMessage(input, 'This ingredient is already in your list');
      return;
    }
    
    // Validate: only letters, spaces, and basic punctuation allowed
    if (!/^[a-zA-Z\s.,()-]+$/.test(value)) {
      showErrorMessage(input, 'Please use only letters, spaces, and basic punctuation');
      return;
    }
    
    // Success case
    this.ingredients.push(value.toLowerCase());
    input.value = '';
    this.renderIngredients();
    clearErrorMessage(input);
    
    // Add a micro-animation class to the ingredients container for feedback
    const container = document.querySelector('.ingredients-container');
    if (container) {
      container.classList.add('pulse-animation');
      setTimeout(() => container.classList.remove('pulse-animation'), 300);
    }
  }
  
  /**
   * Render the ingredient list in the UI
   * @returns {void}
   */
  renderIngredients() {
    // Render in the mobile UI
    if (this.mobileIngredientList) {
      this.mobileIngredientList.innerHTML = '';
      this.ingredients.forEach((ing, idx) => {
        const li = document.createElement('li');
        li.className = 'ingredient-tag';
        li.textContent = ing;
        
        const removeBtn = document.createElement('button');
        removeBtn.className = 'remove-ingredient';
        removeBtn.innerHTML = '<i class="fas fa-times"></i>';
        removeBtn.title = 'Remove';
        removeBtn.setAttribute('aria-label', `Remove ${ing}`);
        removeBtn.onclick = () => {
          this.ingredients.splice(idx, 1);
          this.renderIngredients();
        };
        
        li.appendChild(removeBtn);
        this.mobileIngredientList.appendChild(li);
      });
    }
  }
  
  /**
   * Generate a recipe based on the ingredients
   * @returns {Promise<void>}
   */
  async generateRecipe() {
    // Use the mobile result container
    const resultContainer = this.mobileRecipeResult;
    
    // Show loading state with more detailed information
    resultContainer.innerHTML = `
      <div class="recipe-loading">
        <div class="loading-spinner"></div>
        <p>Finding the perfect Filipino recipe based on your ingredients...</p>
        <p class="search-status">Searching for recipes with multiple ingredients...</p>
      </div>
    `;
    
    try {  
      const updateStatus = (message) => {
        const statusElement = resultContainer.querySelector('.search-status');
        if (statusElement) {
          statusElement.textContent = message;
        }
      };
      
      if (this.ingredients.length === 0) {
        // Fallback: Get a Filipino random meal recipe if no ingredients provided
        updateStatus('No ingredients provided. Finding a delicious Filipino recipe for you...');
        if (!this.mealDbService) {
          this.displayRecipe(null, 'Error: MealDB Service not available.');
          return;
        }
        const { meal, error } = await this.mealDbService.getRandomFilipinoDish();
        
        if (meal) {
          this.displayRecipe(meal, 'MealDB API (Random Filipino Dish)');
        } else {
          throw new Error(error || 'No random recipe found');
        }
        return;
      }
      
      // ENHANCEMENT: Using multiple ingredient matching
      updateStatus(`Searching for recipes with your ${this.ingredients.length} ingredients...`);
      if (!this.mealDbService) {
        this.displayRecipe(null, 'Error: MealDB Service not available.');
        return;
      }
      const { meals: multiMeals, scores, error: multiError } =
        await this.mealDbService.filterByMultipleIngredients(this.ingredients);
      
      if (multiMeals && multiMeals.length > 0) {
        updateStatus('Success! Found recipes matching multiple ingredients.');
        
        // Display the highest scoring meal
        const bestMeal = multiMeals[0];
        const matchScore = scores[0];
        const ingredientCount = this.ingredients.length;
        const matchPercentage = Math.round((matchScore / ingredientCount) * 100);
        
        this.displayRecipe(bestMeal, `MealDB API (Matched ${matchPercentage}% of your ingredients)`);
        return;
      }
      
      // If multiple ingredient search fails, try with main ingredient
      updateStatus('Trying with your main ingredient...');
      const mainIngredient = this.ingredients[0];
      
      // Filter by ingredient
      if (!this.mealDbService) {
        this.displayRecipe(null, 'Error: MealDB Service not available.');
        return;
      }
      const { meals: filteredMeals, error: filterError } = await this.mealDbService.filterByIngredient(mainIngredient);
      
      if (filteredMeals && filteredMeals.length > 0) {
        updateStatus(`Found recipes with ${mainIngredient}!`);
        // Get detailed info for the first matching meal
        const { meal, error: mealError } = await this.mealDbService.getMealById(filteredMeals[0].idMeal);
        
        if (meal) {
          this.displayRecipe(meal, `MealDB API (Based on ${mainIngredient})`);
          return;
        }
      }
      
      // If no results with filter, try a search by name
      updateStatus(`Searching for recipes similar to ${mainIngredient}...`);
      if (!this.mealDbService) {
        this.displayRecipe(null, 'Error: MealDB Service not available.');
        return;
      }
      const { meals: searchMeals, error: searchError } = await this.mealDbService.searchByName(mainIngredient);
      
      if (searchMeals && searchMeals.length > 0) {
        updateStatus('Found recipes with a similar name!');
        // Display the first recipe that matches
        this.displayRecipe(searchMeals[0], 'MealDB API');
        return;
      }
      
      // If we get here, no recipes were found - generate an AI recipe
      updateStatus('Creating a custom Filipino recipe with your ingredients...');
      throw new Error('No recipes found with your ingredients');
      
    } catch (error) {
      console.error('Error in recipe generation:', error);
      // Fallback to AI-generated recipe with user's ingredients
      const statusElement = this.recipeResult.querySelector('.search-status');
      if (statusElement) {
        statusElement.textContent = 'Creating a unique Filipino recipe just for you...';
      }
      this.generateAIRecipe();
    }
  }
  
  /**
   * Displays a recipe in the UI
   * @param {Object} meal - Recipe data from MealDB
   * @param {string} source - Where the recipe came from
   * @returns {void}
   */
  displayRecipe(meal, source) {
    if (!meal || !this.mobileRecipeResult) return;
    
    // Extract ingredients and measurements
    const ingredients = [];
    for (let i = 1; i <= 20; i++) {
      const ingredient = meal[`strIngredient${i}`];
      const measure = meal[`strMeasure${i}`];
      
      if (ingredient && ingredient.trim()) {
        ingredients.push({
          name: ingredient,
          measure: measure || 'to taste'
        });
      }
    }
    
    // Create ingredients list HTML
    const ingredientsList = ingredients.map(ing => 
      `<li>${ing.measure} ${ing.name}</li>`
    ).join('');
    
    // Process instructions (break into paragraphs)
    let instructions = meal.strInstructions || 'No instructions available.';
    let instructionsHtml = '';
    
    if (instructions) {
      // Convert newlines to paragraphs
      instructionsHtml = instructions
        .split(/\r?\n/)
        .filter(line => line.trim() !== '')
        .map(line => `<p>${line}</p>`)
        .join('');
    }
    
    // Format recipe data for display
    this.mobileRecipeResult.innerHTML = `
      <div class="recipe-card">
        <h3 class="recipe-title">${meal.strMeal}</h3>
        <div class="recipe-meta">
          <span class="recipe-category">${meal.strCategory || 'Main Dish'}</span>
          <span class="recipe-source">Source: ${source}</span>
        </div>
        
        ${meal.strMealThumb ? `
        <div class="recipe-image">
          <img src="${meal.strMealThumb}" alt="${meal.strMeal}" loading="lazy">
        </div>
        ` : ''}
        
        <div class="recipe-detail-content">
          <div class="recipe-ingredients">
            <h3>Ingredients</h3>
            <ul class="ingredients-list">${ingredientsList}</ul>
          </div>
          
          <div class="recipe-instructions">
            <h3>Instructions</h3>
            <div class="instructions-content">${instructionsHtml}</div>
          </div>
        </div>
        
        <div class="recipe-actions">
          <button class="print-recipe-btn" onclick="window.print()">Print Recipe</button>
        </div>
      </div>
    `;
  }
  
  /**
   * Generate an AI recipe when API doesn't find anything
   * @returns {void}
   */
  generateAIRecipe() {
    // Filipino recipe templates logic moved from main.js
    const filipinoTemplates = [
      {
        name: 'Adobo',
        base: 'soy sauce, vinegar, garlic, bay leaves, peppercorns',
        protein: 'chicken, pork',
        vegetables: 'potatoes, bell peppers',
        cookingMethod: 'simmering',
        difficulty: 2,
        region: 'Nationwide',
        description: 'A savory, tangy Filipino dish featuring meat braised in soy sauce, vinegar, and spices. It\'s sometimes referred to as the unofficial national dish of the Philippines.',
        instructions: [
          'In a large pot, combine your choice of protein with vinegar, soy sauce, crushed garlic, bay leaves, and peppercorns.',
          'Bring to a boil, then reduce heat and simmer for 30 minutes.',
          'If using vegetables, add them 10 minutes before the dish is done.',
          'For best flavor, let the adobo rest overnight and reheat before serving.',
          'Serve hot with steamed white rice.'
        ]
      },
      {
        name: 'Sinigang',
        base: 'tamarind, tomatoes, onions',
        protein: 'pork ribs, fish, shrimp',
        vegetables: 'string beans, radish, eggplant, bok choy',
        cookingMethod: 'boiling',
        difficulty: 1,
        region: 'Luzon',
        description: 'A sour soup featuring meat or seafood and vegetables in a tamarind-based broth. Known for its refreshing tanginess that Filipinos love.',
        instructions: [
          'In a large pot, sauté onions and tomatoes until soft.',
          'Add your choice of protein and cook until browned.',
          'Pour in water and bring to a boil, then add tamarind (fresh, paste, or powder).',
          'Simmer until meat is tender, about 1 hour for pork (less for seafood).',
          'Add vegetables starting with the hardest (like radish) and ending with the leafy ones.',
          'Season with fish sauce to taste and serve hot with rice.'
        ]
      },
      {
        name: 'Kare-Kare',
        base: 'peanut sauce, annatto seeds, bagoong (shrimp paste)',
        protein: 'oxtail, tripe, pork hocks',
        vegetables: 'eggplant, banana blossom, string beans, bok choy',
        cookingMethod: 'stewing',
        difficulty: 3,
        region: 'Pampanga',
        description: 'A rich Filipino stew with a thick savory peanut sauce. Traditionally served with bagoong (fermented shrimp paste) on the side.',
        instructions: [
          'Simmer your choice of meat until tender (can take 2-3 hours for oxtail).',
          'In a separate pan, prepare the sauce by toasting ground rice and peanuts, then blending with water.',
          'Sauté garlic and onions in a pot, then add the sauce mixture and annatto for color.',
          'Add the tenderized meat and simmer for 15 minutes.',
          'Add vegetables and cook until just tender.',
          'Serve hot with bagoong (shrimp paste) on the side and rice.'
        ]
      },
      {
        name: 'Pancit',
        base: 'noodles, soy sauce, broth',
        protein: 'chicken, pork, shrimp',
        vegetables: 'carrots, snow peas, cabbage, bell peppers',
        cookingMethod: 'stir-frying',
        difficulty: 1,
        region: 'Nationwide',
        description: 'A popular Filipino noodle dish influenced by Chinese cuisine. There are numerous varieties, such as Pancit Canton (with wheat noodles) and Pancit Bihon (with rice noodles).',
        instructions: [
          'Prepare your choice of noodles according to package directions (typically soaked or partially cooked).',
          'In a wok or large pan, sauté garlic and onions until fragrant.',
          'Add your protein and cook until nearly done.',
          'Add vegetables, starting with the hardest ones first.',
          'Add the noodles and toss with soy sauce and broth.',
          'Stir-fry until the liquid is absorbed and everything is well combined.',
          'Garnish with green onions, calamansi or lemon, and serve immediately.'
        ]
      },
      {
        name: 'Tinola',
        base: 'ginger, onion, fish sauce',
        protein: 'chicken',
        vegetables: 'green papaya, moringa leaves (malunggay), chili leaves',
        cookingMethod: 'simmering',
        difficulty: 1,
        region: 'Nationwide',
        description: 'A light and healthy Filipino chicken soup cooked with plenty of ginger, giving it a distinct aroma and flavor.',
        instructions: [
          'Sauté ginger, onion, and garlic until fragrant.',
          'Add chicken pieces and cook until browned.',
          'Pour in water or chicken broth and bring to a boil.',
          'Reduce heat and simmer until chicken is tender, about 30-40 minutes.',
          'Add green papaya and continue cooking until papaya is tender.',
          'Add moringa leaves (malunggay) or chili leaves and cook just until wilted.',
          'Season with fish sauce to taste and serve hot with rice.'
        ]
      },
      {
        name: 'Bicol Express',
        base: 'coconut milk, shrimp paste, chili peppers',
        protein: 'pork belly',
        cookingMethod: 'simmering',
        difficulty: 2,
        region: 'Bicol Region',
        description: 'A spicy Filipino dish named after the passenger train service from Manila to the Bicol region. Known for its creamy coconut milk and hot chili peppers.',
        instructions: [
          'Sauté garlic, onions, and ginger until fragrant.',
          'Add pork belly and cook until browned.',
          'Add shrimp paste and chili peppers, adjusting the amount based on your spice preference.',
          'Pour in coconut milk and bring to a simmer.',
          'Cook uncovered on low heat until sauce thickens and pork is tender, about 30-40 minutes.',
          'If the sauce becomes too thick, add a little water.',
          'Serve hot with plenty of rice to balance the spiciness.'
        ]
      },
      {
        name: 'Pinakbet',
        base: 'bagoong (fermented fish paste)',
        vegetables: 'bitter melon, eggplant, okra, squash, string beans, tomatoes',
        protein: 'pork belly (optional)',
        cookingMethod: 'stir-frying and simmering',
        difficulty: 2,
        region: 'Ilocos Region',
        description: 'A colorful vegetable dish that showcases the bounty of Filipino farms. The bitter melon gives this dish its distinctive flavor.',
        instructions: [
          'If using pork, sauté until browned and rendered.',
          'Add garlic, onions, and tomatoes and cook until softened.',
          'Add bagoong (fermented fish paste) and stir to combine.',
          'Add harder vegetables first (squash, string beans), followed by softer ones (eggplant, okra, bitter melon).',
          'Cover and simmer until vegetables are tender but not mushy.',
          'Be careful not to overcook - vegetables should retain their shape and some bite.',
          'Serve with rice and additional bagoong on the side if desired.'
        ]
      },
      {
        name: 'Bulalo',
        base: 'beef bone marrow, onions, fish sauce',
        protein: 'beef shank',
        vegetables: 'cabbage, string beans, corn, potatoes, pechay (bok choy)',
        cookingMethod: 'boiling',
        difficulty: 2,
        region: 'Southern Luzon',
        description: 'A hearty beef soup made with beef shanks and bone marrow. Popular in the cooler highlands of Batangas and Tagaytay.',
        instructions: [
          'In a large pot, bring beef shanks and bone marrow to a boil, then reduce heat.',
          'Simmer for 2-3 hours or until beef is tender, skimming any scum that rises to the top.',
          'Add corn cobs and potatoes and cook until tender.',
          'Add the remaining vegetables and simmer until just cooked.',
          'Season with fish sauce and black pepper to taste.',
          'Serve hot in bowls with the bone marrow as the centerpiece.',
          'Provide small spoons for scooping out the prized bone marrow.'
        ]
      }
    ];
    
    // Choose template based on ingredients
    let template = filipinoTemplates[0]; // Default to Adobo
    let bestMatchCount = 0;
    
    for (const t of filipinoTemplates) {
      let matchCount = 0;
      const allIngredients = [
        ...(t.base ? t.base.split(', ') : []),
        ...(t.protein ? t.protein.split(', ') : []),
        ...(t.vegetables ? t.vegetables.split(', ') : [])
      ];
      
      for (const ing of this.ingredients) {
        if (allIngredients.some(templateIng => 
          templateIng.includes(ing) || ing.includes(templateIng)
        )) {
          matchCount++;
        }
      }
      
      if (matchCount > bestMatchCount) {
        bestMatchCount = matchCount;
        template = t;
      }
    }
    
    // Generate recipe name
    let recipeName = `Filipino ${template.name}`;
    
    // Try to add user ingredients to the name
    const unusedIngredientsForName = this.ingredients.filter(ing => 
      !template.base?.includes(ing) && 
      !template.protein?.includes(ing) && 
      !template.vegetables?.includes(ing)
    );
    
    if (unusedIngredientsForName.length > 0) {
      // Use up to 2 unused ingredients in the name
      const nameIngredients = unusedIngredientsForName.slice(0, 2).join(' and ');
      recipeName = `${nameIngredients.charAt(0).toUpperCase() + nameIngredients.slice(1)} ${template.name}`;
    }
    
    // Generate ingredients list
    const baseIngredients = template.base ? template.base.split(', ') : [];
    const proteins = template.protein ? template.protein.split(', ') : [];
    const veggies = template.vegetables ? template.vegetables.split(', ') : [];
    
    // Try to incorporate user ingredients
    let html = '<ul class="ingredients-list">';
    
    // Add user ingredients first if they match template categories
    for (const userIng of this.ingredients) {
      let added = false;
      
      // Check if it's in the base ingredients
      if (baseIngredients.some(base => base.includes(userIng) || userIng.includes(base))) {
        html += `<li>${userIng}</li>`;
        added = true;
      }
      
      // Check if it's a protein
      if (!added && proteins.some(p => p.includes(userIng) || userIng.includes(p))) {
        html += `<li>${userIng}</li>`;
        added = true;
      }
      
      // Check if it's a vegetable
      if (!added && veggies.some(v => v.includes(userIng) || userIng.includes(v))) {
        html += `<li>${userIng}</li>`;
        added = true;
      }
      
      // If it doesn't match any category, add it anyway
      if (!added) {
        html += `<li>${userIng}</li>`;
      }
    }
    
    // Fill in any missing proteins
    const userHasProtein = this.ingredients.some(ing => 
      proteins.some(p => p.includes(ing) || ing.includes(p))
    );
    
    if (!userHasProtein && proteins.length > 0) {
      const unusedProtein = proteins.find(p => 
        !this.ingredients.some(ing => ing.includes(p) || p.includes(ing))
      );
      
      if (unusedProtein) {
        html += `<li>${unusedProtein} (or use your preferred protein)</li>`;
      }
    }
    
    // Fill in any missing base ingredients
    for (const base of baseIngredients) {
      if (!this.ingredients.some(ing => ing.includes(base) || base.includes(ing))) {
        html += `<li>${base}</li>`;
      }
    }
    
    // Fill in some vegetables if user didn't provide many
    if (this.ingredients.length < 3) {
      const unusedVeggies = veggies.filter(v => 
        !this.ingredients.some(ing => ing.includes(v) || v.includes(ing))
      );
      html += unusedVeggies.map(v => `<li>${v}</li>`).join('');
    }
    
    html += '</ul>';
    
    // Generate instructions
    const instructionsHtml = template.instructions.map(step => 
      `<p>${step}</p>`
    ).join('');
    
    // Calculate nutrition information
    // Base values that will be adjusted based on ingredients
    let nutrition = {
      calories: 350,
      protein: 20,
      carbs: 30,
      fat: 15
    };
    
    // Adjust based on ingredients
    if (this.ingredients.some(ing => ['pork', 'beef', 'fatty'].some(term => ing.includes(term)))) {
      nutrition.calories += 150;
      nutrition.protein += 10;
      nutrition.fat += 12;
    }
    
    if (this.ingredients.some(ing => ['chicken', 'turkey', 'lean'].some(term => ing.includes(term)))) {
      nutrition.calories += 100;
      nutrition.protein += 15;
      nutrition.fat += 5;
    }
    
    if (this.ingredients.some(ing => ['fish', 'seafood', 'shrimp'].some(term => ing.includes(term)))) {
      nutrition.calories += 80;
      nutrition.protein += 18;
      nutrition.fat += 3;
    }
    
    if (this.ingredients.some(ing => ['rice', 'noodles', 'potato', 'starch'].some(term => ing.includes(term)))) {
      nutrition.calories += 120;
      nutrition.carbs += 25;
    }
    
    if (this.ingredients.some(ing => ['vegetable', 'broccoli', 'spinach', 'greens'].some(term => ing.includes(term)))) {
      nutrition.calories += 40;
      nutrition.carbs += 8;
      nutrition.fiber = 5;
    }
    
    // Format and display the AI-generated recipe
    this.mobileRecipeResult.innerHTML = `
      <div class="recipe-card ai-recipe">
        <h3 class="recipe-title">${recipeName}</h3>
        <div class="recipe-meta">
          <span class="recipe-category">Main Dish</span>
          <span class="recipe-source">Source: AI Recipe Generator</span>
          <span class="recipe-region">Region: ${template.region}</span>
          <span class="recipe-difficulty">Difficulty: ${formatDifficulty(template.difficulty)}</span>
        </div>
        
        <div class="recipe-description">
          <p>${template.description}</p>
          <p class="ai-disclaimer">This recipe was generated based on your ingredients: ${this.ingredients.join(', ')}</p>
        </div>
        
        <div class="recipe-nutrition">
          <h4>Estimated Nutrition (per serving)</h4>
          <div class="nutrition-grid">
            <div class="nutrition-item">
              <span class="nutrition-value">${nutrition.calories}</span>
              <span class="nutrition-label">Calories</span>
            </div>
            <div class="nutrition-item">
              <span class="nutrition-value">${nutrition.protein}g</span>
              <span class="nutrition-label">Protein</span>
            </div>
            <div class="nutrition-item">
              <span class="nutrition-value">${nutrition.carbs}g</span>
              <span class="nutrition-label">Carbs</span>
            </div>
            <div class="nutrition-item">
              <span class="nutrition-value">${nutrition.fat}g</span>
              <span class="nutrition-label">Fat</span>
            </div>
          </div>
        </div>
        
        <div class="recipe-detail-content">
          <div class="recipe-ingredients">
            <h3>Ingredients</h3>
            ${html}
          </div>
          
          <div class="recipe-instructions">
            <h3>Instructions</h3>
            <div class="instructions-content">
              ${instructionsHtml}
            </div>
          </div>
        </div>
        
        <div class="recipe-tips">
          <h4>Filipino Cooking Tips</h4>
          <p>Try adding calamansi, banana ketchup, or fish sauce for a more authentic Filipino flavor profile!</p>
        </div>
        
        <div class="recipe-actions">
          <button class="print-recipe-btn" onclick="window.print()">Print Recipe</button>
        </div>
      </div>
    `;
  }
  
  /**
   * Helper function to format difficulty level with emoji
   * @param {number} difficulty - Difficulty level (1-5)
   * @returns {string} - Formatted string
   */
  formatDifficulty(difficulty) {
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
}

// Export a factory function
export const createRecipeGenerator = () => {
  return new RecipeGenerator();
};

// Initialize the recipe generator when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
  new RecipeGenerator();
});
