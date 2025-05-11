/**
 * Mix and Munch: API Service
 * Centralized API service for MealDB interactions with caching and error handling
 */

// Import from env.js - this will be loaded by main.js
// MEALDB_API_KEY should be available in the global scope via env.js
// const MEALDB_API_KEY = '1'; // Replaced with global MEALDB_API_KEY

/**
 * MealDB API Service with caching and error handling
 */
export class MealDbService {
  constructor(supabase) { // Added supabase as a dependency
    this.supabase = supabase; // Store supabase instance
    this.baseUrl = 'https://www.themealdb.com/api/json/v1/';
    this.apiKey = window.MEALDB_API_KEY || '1'; // Use API key from env.js or fallback to '1'
    this.cache = new Map();
    this.cacheTTL = 30 * 60 * 1000; // 30 minutes cache TTL
  }

  /**
   * Fetch data with caching
   * @param {string} endpoint - API endpoint
   * @returns {Promise<Object>} - Response data
   */
  async fetchWithCache(endpoint) {
    const cacheKey = `mealdb-${endpoint}`;
    
    // Check cache first
    if (this.cache.has(cacheKey)) {
      const {data, timestamp} = this.cache.get(cacheKey);
      if (Date.now() - timestamp < this.cacheTTL) {
        return data;
      }
    }
    
    // If not in cache or expired, fetch fresh data
    try {
      const response = await fetch(`${this.baseUrl}${this.apiKey}/${endpoint}`);
      const data = await response.json();
      
      // Store in cache
      this.cache.set(cacheKey, {
        data,
        timestamp: Date.now()
      });
      
      return data;
    } catch (error) {
      console.error(`API fetch error: ${endpoint}`, error);
      throw new Error(`Failed to fetch from MealDB: ${error.message}`);
    }
  }

  /**
   * Get a random meal
   * @returns {Promise<Object>} - Random meal data and error status
   */
  async getRandomMeal() {
    try {
      const data = await this.fetchWithCache('random.php');
      if (data.meals && data.meals.length > 0) {
        return { meal: data.meals[0], error: null };
      }
      return { meal: null, error: 'No random meal found' };
    } catch (error) {
      console.error('Error fetching random meal:', error);
      return { meal: null, error: error.message };
    }
  }

  /**
   * Get a random Filipino dish
   * @returns {Promise<Object>} - Random Filipino meal data and error status
   */
  async getRandomFilipinoDish() {
    try {
      // List of common Filipino dishes to search for
      const filipinoDishes = [
        'adobo', 'sinigang', 'kare-kare', 'lechon', 'pancit', 
        'sisig', 'tinola', 'bulalo', 'lumpia', 'bistek', 
        'caldereta', 'arroz caldo', 'palabok', 'menudo', 'nilaga'
      ];
      
      // Get a random dish name from the list
      const dishName = filipinoDishes[Math.floor(Math.random() * filipinoDishes.length)];
      
      // Search for this dish
      const { meals } = await this.searchByName(dishName);
      
      if (meals && meals.length > 0) {
        // Get full details of a random dish from the results
        const randomIndex = Math.floor(Math.random() * meals.length);
        const { meal } = await this.getMealById(meals[randomIndex].idMeal);
        return { meal, error: null };
      }
      
      // If no dish found, fall back to regular random
      return this.getRandomMeal();
    } catch (error) {
      console.error('Error fetching Filipino dish:', error);
      return this.getRandomMeal(); // Fall back to regular random
    }
  }

  /**
   * Filter meals by ingredient
   * @param {string} ingredient - Ingredient to filter by
   * @returns {Promise<Object>} - Meals filtered by ingredient
   */
  async filterByIngredient(ingredient) {
    try {
      const data = await this.fetchWithCache(`filter.php?i=${encodeURIComponent(ingredient)}`);
      if (data.meals && data.meals.length > 0) {
        return { meals: data.meals, error: null };
      }
      return { meals: [], error: 'No meals found with this ingredient' };
    } catch (error) {
      console.error('Error filtering by ingredient:', error);
      return { meals: [], error: error.message };
    }
  }

  /**
   * Get meal details by ID
   * @param {string} id - Meal ID
   * @returns {Promise<Object>} - Meal details
   */
  async getMealById(id) {
    try {
      const data = await this.fetchWithCache(`lookup.php?i=${id}`);
      if (data.meals && data.meals.length > 0) {
        return { meal: data.meals[0], error: null };
      }
      return { meal: null, error: 'Meal not found' };
    } catch (error) {
      console.error('Error getting meal by ID:', error);
      return { meal: null, error: error.message };
    }
  }

  /**
   * Search meals by name
   * @param {string} name - Name to search for
   * @returns {Promise<Object>} - Search results
   */
  async searchByName(name) {
    try {
      const data = await this.fetchWithCache(`search.php?s=${encodeURIComponent(name)}`);
      if (data.meals && data.meals.length > 0) {
        return { meals: data.meals, error: null };
      }
      return { meals: [], error: 'No meals found with this name' };
    } catch (error) {
      console.error('Error searching by name:', error);
      return { meals: [], error: error.message };
    }
  }

  /**
   * Filter meals by multiple ingredients with scoring
   * @param {Array<string>} ingredientList - List of ingredients
   * @returns {Promise<Object>} - Scored meals by ingredients
   */
  async filterByMultipleIngredients(ingredientList) {
    try {
      if (!ingredientList || ingredientList.length === 0) {
        return { meals: [], scores: [], error: 'No ingredients provided' };
      }
      
      // Get all meals for each ingredient
      const mealPromises = ingredientList.map(ingredient => 
        this.filterByIngredient(ingredient)
      );
      
      const results = await Promise.all(mealPromises);
      
      // Extract all meals
      let allMeals = [];
      results.forEach(result => {
        if (result.meals && result.meals.length > 0) {
          allMeals = [...allMeals, ...result.meals];
        }
      });
      
      if (allMeals.length === 0) {
        return { 
          meals: [], 
          scores: [],
          error: 'No meals found with these ingredients' 
        };
      }
      
      // Score meals based on how many of the user's ingredients they match
      // This requires fetching detailed info for each meal
      const scoredMeals = [];
      const uniqueMealIds = [...new Set(allMeals.map(meal => meal.idMeal))];
      
      // Limit to 10 meals max for performance
      const mealIdsToProcess = uniqueMealIds.slice(0, 10);
      
      // Get detailed info for each meal
      const detailedMealPromises = mealIdsToProcess.map(id => 
        this.getMealById(id)
      );
      
      const detailedResults = await Promise.all(detailedMealPromises);
      
      // Score each meal based on matching ingredients
      for (const result of detailedResults) {
        if (result.meal) {
          let score = 0;
          const mealIngredients = [];
          
          // Extract all ingredients from the meal
          for (let i = 1; i <= 20; i++) {
            const ingredient = result.meal[`strIngredient${i}`];
            if (ingredient && ingredient.trim()) {
              mealIngredients.push(ingredient.toLowerCase());
            }
          }
          
          // Calculate score based on matching ingredients
          for (const userIngredient of ingredientList) {
            if (mealIngredients.some(mealIng => 
              mealIng.includes(userIngredient.toLowerCase()) || 
              userIngredient.toLowerCase().includes(mealIng)
            )) {
              score++;
            }
          }
          
          scoredMeals.push({
            meal: result.meal,
            score
          });
        }
      }
      
      // Sort by score (highest first)
      scoredMeals.sort((a, b) => b.score - a.score);
      
      // Return meals and their scores as separate arrays
      return {
        meals: scoredMeals.map(item => item.meal),
        scores: scoredMeals.map(item => item.score),
        error: null 
      };
    } catch (error) {
      console.error('Error filtering by multiple ingredients:', error);
      return { 
        meals: [], 
        scores: [],
        error: error.message 
      };
    }
  }

  /**
   * Fetch community recipes from Supabase with pagination
   * @param {number} page - Page number
   * @param {number} recipesPerPage - Number of recipes per page
   * @returns {Promise<Object>} - Object containing data, error, and count
   */
  async fetchCommunityRecipesFromSupabase(page = 1, recipesPerPage = 6) {
    if (!this.supabase) {
      return { data: null, error: new Error('Supabase client not initialized in MealDbService'), count: 0 };
    }
    try {
      // Get total count for pagination
      const { count, error: countError } = await this.supabase
        .from('recipes')
        .select('*', { count: 'exact', head: true });
        
      if (countError) throw countError;
      
      // Calculate pagination
      const from = (page - 1) * recipesPerPage;
      const to = from + recipesPerPage - 1;
      
      // Fetch paginated recipes from Supabase
      const { data, error } = await this.supabase
        .from('recipes')
        .select('*')
        .order('created_at', { ascending: false })
        .range(from, to);
      
      if (error) throw error;
      
      return { data, error: null, count: count || 0 };
    } catch (error) {
      console.error('Error fetching community recipes from Supabase:', error);
      return { data: null, error, count: 0 };
    }
  }
}

// Create and export a singleton instance
// The supabase instance will be passed by MixAndMunchApp when it instantiates MealDbService
// For now, this direct export might not work as expected if supabase is not globally available
// This will be resolved when MixAndMunchApp manages service instantiation.
// For the purpose of this refactor, we assume MixAndMunchApp passes supabase to the constructor.
// export const mealDbService = new MealDbService(); // This line will be removed or handled by the app orchestrator.
// We will export the class directly and let the app instantiate it.
// No, the plan was for js/main.js to instantiate it.
// The existing export `export const mealDbService = new MealDbService();` will cause issues
// as it doesn't receive the supabase client.
// The `js/main.js` already imports `MealDbService` class and instantiates it.
// So, I will remove the singleton export here.
