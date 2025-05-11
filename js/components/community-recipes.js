/**
 * Mix and Munch: Community Recipes Component
 * Handles community recipes display, pagination, and recipe details modal
 */

import { formatRelativeDate, truncateText } from '../utils/formatting.js';
import { sanitizeInput } from '../utils/validation.js';

export class CommunityRecipes {
  constructor({ supabase, mealDbService, authService }) { // Updated constructor
    // Dependencies
    this.supabase = supabase; // Retain for now if needed by other methods, or remove if all DB access goes via service
    this.mealDbService = mealDbService;
    this.authService = authService;
    
    // Elements
    this.communityFeed = document.getElementById('community-feed');
    
    // State
    this.allRecipes = [];
    this.currentPage = 1;
    this.recipesPerPage = 6;
    this.totalRecipes = 0;
    
    // Initialize if dependencies are available
    if (this.mealDbService && this.communityFeed) { // Check mealDbService instead of just supabase
      this.init();
    } else {
      console.warn('CommunityRecipes: Missing dependencies (mealDbService or communityFeed element).');
      if (this.communityFeed) this.showErrorState(new Error('Service not available.'));
    }
  }
  
  /**
   * Initialize the component
   */
  init() {
    // Initial fetch of recipes
    this.fetchRecipes(1);
    
    // Add document event listeners for recipe interactions
    document.addEventListener('click', (e) => {
      if (e.target.classList.contains('view-details-btn')) {
        const recipeId = e.target.dataset.id;
        if (recipeId) {
          this.showRecipeDetails(recipeId);
        }
      }
    });
  }
  
  /**
   * Fetch community recipes with pagination
   * @param {number} page - Page number
   */
  async fetchRecipes(page = 1) {
    this.currentPage = page;
    
    // Show loading state
    this.communityFeed.innerHTML = '<div class="loading-spinner"></div><p>Loading community recipes...</p>';
    
    // If MealDbService is not available (which now handles Supabase fetching for community recipes)
    if (!this.mealDbService) {
      console.warn('CommunityRecipes: MealDbService not available. Showing sample recipes.');
      setTimeout(() => this.showSampleRecipes(), 1000);
      return;
    }
    
    try {
      // Fetch recipes using the service
      const { data, error, count } = await this.mealDbService.fetchCommunityRecipesFromSupabase(page, this.recipesPerPage);

      if (error) throw error;
      
      this.totalRecipes = count || 0;
      this.allRecipes = data || [];
      
      if (data && data.length > 0) {
        this.renderRecipes(data);
      } else {
        this.showEmptyState();
      }
    } catch (error) {
      console.error('Error fetching community recipes via service:', error);
      this.showErrorState(error);
    }
  }
  
  /**
   * Render recipes to the DOM
   * @param {Array} recipes - Array of recipe objects
   */
  renderRecipes(recipes) {
    // Generate recipe cards
    let recipesHTML = recipes.map(recipe => `
      <div class="community-recipe" data-id="${recipe.id}">
        <div class="recipe-header">
          <h3>${sanitizeInput(recipe.name)}</h3>
          <span class="recipe-author">By ${recipe.author || 'Community Member'}</span>
        </div>
        ${recipe.image_url ? `
          <div class="recipe-image-container">
            <img src="${recipe.image_url}" alt="${sanitizeInput(recipe.name)}" class="community-recipe-image" loading="lazy">
          </div>
        ` : ''}
        <div class="recipe-content">
          <p>${this.formatIngredients(recipe.ingredients)}</p>
          <div class="recipe-meta">
            <span class="recipe-category">${recipe.category || 'Recipe'}</span>
            <span class="recipe-date">${formatRelativeDate(recipe.created_at)}</span>
          </div>
          <button class="view-details-btn" data-id="${recipe.id}">View Recipe</button>
        </div>
      </div>
    `).join('');
    
    // Add pagination controls
    const totalPages = Math.ceil(this.totalRecipes / this.recipesPerPage);
    const paginationHTML = `
      <div class="pagination">
        ${this.currentPage > 1 ? `<button class="pagination-btn prev" data-page="${this.currentPage - 1}">« Previous</button>` : ''}
        <span class="pagination-info">Page ${this.currentPage} of ${totalPages}</span>
        ${this.currentPage < totalPages ? `<button class="pagination-btn next" data-page="${this.currentPage + 1}">Next »</button>` : ''}
      </div>
    `;
    
    this.communityFeed.innerHTML = recipesHTML + paginationHTML;
    
    // Add pagination event listeners
    document.querySelectorAll('.pagination-btn').forEach(btn => {
      btn.addEventListener('click', (e) => {
        const newPage = parseInt(e.target.dataset.page, 10);
        this.fetchRecipes(newPage);
        this.scrollToSection();
      });
    });
  }
  
  /**
   * Format ingredients for display
   * @param {Array|string} ingredients - Recipe ingredients
   * @returns {string} - Formatted ingredients string
   */
  formatIngredients(ingredients) {
    if (Array.isArray(ingredients)) {
      return ingredients.slice(0, 4).join(', ') + 
        (ingredients.length > 4 ? '...' : '');
    } else if (typeof ingredients === 'string') {
      return truncateText(ingredients, 100);
    }
    return 'No ingredients listed';
  }
  
  /**
   * Show empty state when no recipes are found
   */
  showEmptyState() {
    this.communityFeed.innerHTML = `
      <div class="empty-state">
        <p>No community recipes yet. Be the first to share!</p>
        <button class="cta" id="empty-state-share-recipe-btn">Share a Recipe</button>
      </div>
    `;
    // Add event listener separately for better practice
    const shareBtn = this.communityFeed.querySelector('#empty-state-share-recipe-btn');
    if (shareBtn) {
      shareBtn.addEventListener('click', () => {
        // TODO: Ideally, this should also switch to the profile tab first
        // For now, just scroll. We'll need TabNavigation interaction later.
        const uploadSection = document.getElementById('upload-section');
        if (uploadSection) {
          // We need to ensure the profile tab is active first.
          // This requires communication with TabNavigation component.
          // Dispatching a custom event that TabNavigation listens to:
          document.dispatchEvent(new CustomEvent('navigateToTab', { detail: { tabId: 'profile', scrollToSectionId: 'upload-section' } }));
        } else {
          console.error('Upload section not found');
        }
      });
    }
  }
  
  /**
   * Show error state when recipes can't be loaded
   * @param {Error} error - Error object
   */
  showErrorState(error) {
    this.communityFeed.innerHTML = `
      <div class="error-message-container">
        <p>Error loading recipes: ${error.message}</p>
        <button onclick="document.dispatchEvent(new CustomEvent('refreshCommunityRecipes'))">Try Again</button>
      </div>
    `;
  }
  
  /**
   * Show sample recipes for demo mode
   */
  showSampleRecipes() {
    let recipesHTML = `
      <div class="community-recipe">
        <div class="recipe-header">
          <h3>Pancit Canton</h3>
          <span class="recipe-author">By Carlos Reyes</span>
        </div>
        <div class="recipe-content">
          <p>Stir-fried noodles with vegetables and meat. A Filipino classic that's perfect for gatherings.</p>
          <div class="recipe-meta">
            <span class="recipe-category">Main Dish</span>
            <span class="recipe-date">2 days ago</span>
          </div>
          <button class="view-details-btn" data-id="sample1">View Recipe</button>
        </div>
      </div>
      <div class="community-recipe">
        <div class="recipe-header">
          <h3>Lechon Kawali</h3>
          <span class="recipe-author">By Maria Santos</span>
        </div>
        <div class="recipe-content">
          <p>Crispy deep-fried pork belly with a delicious sauce. Crunchy on the outside, tender on the inside.</p>
          <div class="recipe-meta">
            <span class="recipe-category">Main Dish</span>
            <span class="recipe-date">5 days ago</span>
          </div>
          <button class="view-details-btn" data-id="sample2">View Recipe</button>
        </div>
      </div>
    `;
    
    this.communityFeed.innerHTML = recipesHTML + '<div class="pagination"><span>Demo Mode - No Pagination Available</span></div>';
  }
  
  /**
   * Show detailed view of a recipe
   * @param {string} recipeId - ID of the recipe to show
   */
  showRecipeDetails(recipeId) {
    let recipe;
    
    // Get recipe from stored data
    if (recipeId.startsWith('sample')) {
      recipe = this.getSampleRecipe(recipeId);
    } else {
      recipe = this.allRecipes.find(r => r.id === recipeId);
    }
    
    if (!recipe) {
      console.error('Recipe not found:', recipeId);
      return;
    }
    
    // Create modal container if it doesn't exist
    let modalContainer = document.getElementById('recipe-modal-container');
    if (!modalContainer) {
      modalContainer = document.createElement('div');
      modalContainer.id = 'recipe-modal-container';
      modalContainer.className = 'modal-container';
      document.body.appendChild(modalContainer);
    }
    
    // Format ingredients
    const ingredients = Array.isArray(recipe.ingredients) 
      ? recipe.ingredients 
      : typeof recipe.ingredients === 'string'
        ? recipe.ingredients.split(',')
        : [];
    
    const ingredientsList = ingredients.map(ing => 
      `<li>${sanitizeInput(ing.trim())}</li>`
    ).join('');
    
    // Format instructions
    const instructions = recipe.instructions || 'No instructions available.';
    const instructionsHtml = typeof instructions === 'string'
      ? instructions
          .split(/\\r?\\n/)
          .filter(line => line.trim() !== '')
          .map(line => `<p>${sanitizeInput(line)}</p>`)
          .join('')
      : '<p>No instructions available.</p>';
    
    // Build modal HTML
    modalContainer.innerHTML = `
      <div class="recipe-modal">
        <button class="modal-close-btn">&times;</button>
        <h2 class="modal-title">${sanitizeInput(recipe.name)}</h2>
        
        <div class="recipe-modal-meta">
          <span class="recipe-author">By ${sanitizeInput(recipe.author || 'Community Member')}</span>
          <span class="recipe-category">${sanitizeInput(recipe.category || 'Recipe')}</span>
          <span class="recipe-date">${formatRelativeDate(recipe.created_at)}</span>
        </div>
        
        ${recipe.image_url ? `
          <div class="recipe-image-large">
            <img src="${recipe.image_url}" alt="${sanitizeInput(recipe.name)}" loading="lazy">
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
    
    // Prevent body scrolling when modal is open
    document.body.style.overflow = 'hidden';
    
    // Add close button event listener
    const closeBtn = modalContainer.querySelector('.modal-close-btn');
    if (closeBtn) {
      closeBtn.addEventListener('click', () => {
        document.body.removeChild(modalContainer);
        document.body.style.overflow = 'auto';
      });
    }
    
    // Close on click outside modal
    modalContainer.addEventListener('click', (e) => {
      if (e.target === modalContainer) {
        document.body.removeChild(modalContainer);
        document.body.style.overflow = 'auto';
      }
    });
  }
  
  /**
   * Get a sample recipe for demo mode
   * @param {string} sampleId - ID of the sample recipe
   * @returns {Object} - Sample recipe object
   */
  getSampleRecipe(sampleId) {
    const sampleRecipes = {
      sample1: {
        id: 'sample1',
        name: 'Pancit Canton',
        author: 'Carlos Reyes',
        category: 'Main Dish',
        created_at: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000).toISOString(),
        ingredients: [
          '500g pancit canton noodles',
          '300g chicken breast, sliced',
          '200g shrimp, peeled and deveined',
          '2 carrots, julienned',
          '1 bell pepper, sliced',
          '1 cup cabbage, shredded',
          '1/4 cup soy sauce',
          '2 tbsp oyster sauce',
          '3 cloves garlic, minced',
          '1 onion, sliced',
          '3 tbsp cooking oil',
          'Green onions for garnish',
          'Calamansi or lemon wedges'
        ],
        instructions: 'Heat oil in a large wok or pan over medium heat. Sauté garlic and onions until fragrant.\n\nAdd chicken and cook until no longer pink. Add shrimp and cook until pink.\n\nAdd carrots, bell pepper and cabbage. Stir-fry for 2-3 minutes.\n\nSoak noodles in warm water for a few minutes as directed on the package, then drain.\n\nAdd noodles to the wok. Pour in soy sauce and oyster sauce. Mix well to coat the noodles and ingredients.\n\nStir-fry for an additional 3-5 minutes until noodles are cooked through.\n\nGarnish with green onions and serve with calamansi or lemon wedges.'
      },
      sample2: {
        id: 'sample2',
        name: 'Lechon Kawali',
        author: 'Maria Santos',
        category: 'Main Dish',
        created_at: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000).toISOString(),
        ingredients: [
          '1 kg pork belly, whole slab',
          '2 tbsp salt',
          '1 tsp black peppercorns',
          '5 cloves garlic, crushed',
          '3 bay leaves',
          '1 onion, quartered',
          'Water for boiling',
          'Oil for deep frying',
          'Lechon sauce or liver sauce for serving',
          'Vinegar-soy dipping sauce'
        ],
        instructions: 'In a large pot, combine pork belly, salt, peppercorns, garlic, bay leaves, and onion. Cover with water.\n\nBring to a boil, then lower heat and simmer for 1-1.5 hours until meat is tender but not falling apart.\n\nRemove pork and let it cool completely. Refrigerate overnight to dry out the skin.\n\nPrick the skin all over with a fork (this helps create a crispier skin).\n\nHeat oil in a deep pan for frying.\n\nDeep-fry the pork belly, skin side down first, until the skin is crispy and golden brown. Flip and continue frying until all sides are crispy.\n\nDrain on paper towels. Let rest for a few minutes.\n\nChop into serving pieces and serve with lechon sauce or a mixture of soy sauce, vinegar, and chilis.'
      }
    };
    
    return sampleRecipes[sampleId];
  }
  
  /**
   * Scroll to the community section
   */
  scrollToSection() {
    const section = document.getElementById('community-section');
    if (section) {
      section.scrollIntoView({ behavior: 'smooth' });
    }
  }
}

// Export factory function
export const createCommunityRecipes = (dependencies) => { // dependencies: { supabase, mealDbService, authService }
  return new CommunityRecipes(dependencies);
};
