/**
 * Mix and Munch: Main Application Entry Point
 * Orchestrates the application using modular components
 */

// Import environment variables (these will come from the existing env.js)
import '../env.js';

// Import Services
import { MealDbService } from './services/api-service.js'; // Changed to import class
import { createAuthService } from './services/auth-service.js';

// Import Components
import { TabNavigation } from './components/tab-navigation.js'; // Added TabNavigation
import { createRecipeGenerator } from './components/recipe-generator.js';
import { createFileUpload } from './components/file-upload.js';
import { createCommunityRecipes } from './components/community-recipes.js';

// Import Utils
import { validateField, validateImageFile, showErrorMessage, clearErrorMessage, sanitizeInput } from './utils/validation.js';
import { formatRelativeDate } from './utils/formatting.js';

// Global error handling
window.addEventListener('error', function(e) {
  console.error('Global error caught:', e.error);
  // Optionally send to error reporting service
  showGlobalErrorNotification(e.message);
});

// Show a user-friendly error notification
function showGlobalErrorNotification(message) {
  const container = document.createElement('div');
  container.className = 'global-error-notification';
  container.innerHTML = `
    <div class="notification-content">
      <button class="notification-close">&times;</button>
      <p>Something went wrong. Please try again later.</p>
      <p class="error-details">${sanitizeInput(message)}</p>
    </div>
  `;
  
  document.body.appendChild(container);
  
  // Auto-remove after 8 seconds
  setTimeout(() => {
    if (document.body.contains(container)) {
      document.body.removeChild(container);
    }
  }, 8000);
  
  // Close button
  const closeBtn = container.querySelector('.notification-close');
  if (closeBtn) {
    closeBtn.addEventListener('click', () => {
      if (document.body.contains(container)) {
        document.body.removeChild(container);
      }
    });
  }
}

// Application class
class MixAndMunchApp {
  constructor() {
    // Supabase initialization
    this.supabase = null;
    this.authService = null;
    this.mealDbService = null; // Added mealDbService property
    
    // Components
    this.tabNavigation = null; // Added tabNavigation property
    this.recipeGenerator = null;
    this.fileUpload = null;
    this.communityRecipes = null;
    
    // DOM Elements for Auth are now managed by AuthService
    
    // DOM Elements for Recipe Upload
    this.uploadForm = document.getElementById('upload-form');
    this.uploadMessage = document.getElementById('upload-message');
    this.uploadProgress = document.getElementById('upload-progress');
    this.progressBar = document.getElementById('progress-bar');

    // DOM Elements for Profile Picture Upload
    this.profileUploadMessage = document.getElementById('profile-upload-message');
    // The FileUpload component itself will handle its internal choose button and input.
    // We need a reference to the FileUpload instance for profile pictures.
    this.profilePictureUpload = null;
    
    // Initialization
    // this.mealDbService will be instantiated after Supabase is initialized
    this.initSupabase();
    this.initComponents();
    this.bindEvents();
  }
  
  /**
   * Initialize Supabase client and auth
   */
  initSupabase() {
    try {
      // Check if Supabase is available (from CDN in index.html)
      if (window.supabase && window.SUPABASE_URL && window.SUPABASE_ANON_KEY) {
        this.supabase = window.supabase.createClient(window.SUPABASE_URL, window.SUPABASE_ANON_KEY);
        console.log('Supabase client initialized successfully');
        
        // Initialize services that depend on Supabase
        this.authService = createAuthService(this.supabase);
        this.authService.initialize().catch(console.error);
        this.mealDbService = new MealDbService(this.supabase); // Pass supabase client

      } else {
        console.warn('Supabase initialization failed: Missing dependencies or configuration');
        // Add visual indication for users
        document.body.classList.add('supabase-unavailable');
      }
    } catch (error) {
      console.error('Error initializing Supabase client:', error);
      document.body.classList.add('supabase-unavailable');
    }
  }
  
  /**
   * Initialize application components
   */
  initComponents() {
    // Initialize Tab Navigation
    this.tabNavigation = new TabNavigation();
    this.tabNavigation.init(); // Initialize TabNavigation

    // Initialize Recipe Generator
    this.recipeGenerator = createRecipeGenerator({ mealDbService: this.mealDbService }); // Pass mealDbService if needed
    
    // Initialize File Upload
    this.fileUpload = createFileUpload({
      enableCropping: true,
      cropperOptions: {
        aspectRatio: 16 / 9,
        viewMode: 1
      }
    });
    
    // Initialize Community Recipes
    this.communityRecipes = createCommunityRecipes({
      supabase: this.supabase,
      mealDbService: this.mealDbService,
      authService: this.authService
    }); // Pass services

    // Initialize FileUpload for Profile Picture
    this.profilePictureUpload = createFileUpload({
      fileInputId: 'profile-image-upload-input', // Specific ID for profile file input
      uploadAreaId: 'profile-picture-section', // Main container for profile pic section can act as area
      previewImgId: 'profile-tab-avatar-img', // The image tag to show preview
      chooseBtnId: 'profile-choose-image-btn',
      cropperModalId: 'profile-cropper-modal',
      cropperImageId: 'profile-cropper-image',
      cropBtnId: 'profile-crop-image-btn',
      cancelCropBtnId: 'profile-cancel-crop-btn',
      enableCropping: true,
      cropperOptions: {
        aspectRatio: 1 / 1, // Square for avatars
        viewMode: 1,
        movable: true,
        zoomable: true,
        rotatable: true,
        scalable: true,
      },
      maxFileSize: 2 * 1024 * 1024, // 2MB for profile pictures
      acceptedTypes: ['image/jpeg', 'image/png', 'image/gif']
    });
  }
  
  /**
   * Bind global event handlers
   */
  bindEvents() {
    // Auth events are now handled by AuthService internally.
    // MixAndMunchApp can still listen to auth state changes if needed for other components
    // or global UI adjustments.
    if (this.authService) {
      this.authService.addStateChangeListener((event, session) => {
        // console.log('Auth state changed in App:', event, session); // For debugging
        if (event === 'SIGNED_IN' && session) {
          document.body.classList.add('user-logged-in');
          // Refresh community recipes or other user-specific content
          document.dispatchEvent(new CustomEvent('refreshCommunityRecipes'));
        } else if (event === 'SIGNED_OUT') {
          document.body.classList.remove('user-logged-in');
        }
      });
    }
    
    // Recipe upload events
    if (this.uploadForm) {
      this.bindUploadEvents(); // For regular recipe image uploads
    }
    this.bindProfilePictureUploadEvents(); // For profile picture uploads
    
    // Navigation events
    this.bindNavigationEvents();
    
    // Global events
    document.addEventListener('refreshCommunityRecipes', () => {
      if (this.communityRecipes) {
        this.communityRecipes.fetchRecipes(1);
      }
    });
    
    // Removed DOMContentLoaded from here, will be at the end of the file
  }
  
  // Removed bindAuthEvents method as its logic is now in AuthService
  
  /**
   * Bind recipe upload events
   */
  bindUploadEvents() {
    this.uploadForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      
      // Clear previous messages
      this.clearUploadMessages();
      
      // Check user authentication
      if (!this.authService?.isLoggedIn()) {
        this.showUploadMessage('Please log in to upload recipes.', 'error');
        document.querySelector('#auth-section').scrollIntoView({ behavior: 'smooth' });
        return;
      }
      
      // Validate form
      if (!this.validateRecipeForm()) {
        return;
      }
      
      // Show upload progress
      this.showUploadProgress(10);
      
      try {
        const currentUser = this.authService.getCurrentUser();
        
        // Get form values
        const name = document.getElementById('recipe-name').value.trim();
        const category = document.getElementById('recipe-category').value;
        const ingredients = document.getElementById('recipe-ingredients').value.trim().split(',');
        const instructions = document.getElementById('recipe-instructions').value.trim();
        const origin = document.getElementById('recipe-origin')?.value.trim();
        
        // Prepare recipe data
        const recipeData = {
          name,
          category,
          ingredients,
          instructions,
          origin: origin || null,
          author: currentUser?.user_metadata?.username || currentUser?.email,
          user_id: currentUser?.id,
          created_at: new Date().toISOString(),
          image_url: null // Will be updated if image upload succeeds
        };
        
        this.showUploadProgress(30);
        
        // Handle image upload if a file was selected
        const imageFile = this.fileUpload.getFile();
        if (imageFile) {
          try {
            this.showUploadProgress(40);
            
            // Generate unique file path
            const filePath = `${currentUser.id}/${Date.now()}-${imageFile.name}`;
            
            // Upload image to Supabase Storage
            const { data: uploadData, error: uploadError } = await this.supabase.storage
              .from('recipes')
              .upload(filePath, imageFile, {
                cacheControl: '3600',
                upsert: false
              });
              
            this.showUploadProgress(60);
              
            if (uploadError) throw uploadError;
            
            // Get public URL
            const { data: urlData } = await this.supabase.storage
              .from('recipes')
              .getPublicUrl(filePath);
              
            if (urlData?.publicUrl) {
              recipeData.image_url = urlData.publicUrl;
            }
          } catch (imageError) {
            console.error('Image upload error:', imageError);
            this.showUploadMessage(`Image upload failed: ${imageError.message}. Recipe will be saved without an image.`, 'warning');
          }
        }
        
        this.showUploadProgress(80);
        
        // Insert recipe into database
        const { data, error } = await this.supabase
          .from('recipes')
          .insert([recipeData]);
        
        if (error) throw error;
        
        // Show success message
        this.showUploadProgress(100);
        this.showUploadMessage('Recipe uploaded successfully!', 'success');
        
        // Reset form and refresh community recipes
        setTimeout(() => {
          this.uploadForm.reset();
          this.fileUpload.resetPreview();
          document.dispatchEvent(new CustomEvent('refreshCommunityRecipes'));
          this.hideUploadProgress();
        }, 2000);
        
      } catch (error) {
        console.error('Recipe upload error:', error);
        this.showUploadMessage(`Upload failed: ${error.message}`, 'error');
        this.hideUploadProgress();
      }
    });
    
    // Add validation for form fields
    const recipeName = document.getElementById('recipe-name');
    const recipeCategory = document.getElementById('recipe-category');
    const recipeIngredients = document.getElementById('recipe-ingredients');
    const recipeInstructions = document.getElementById('recipe-instructions');
    
    if (recipeName) recipeName.addEventListener('blur', () => validateField(recipeName, 'Please enter a recipe name'));
    if (recipeCategory) recipeCategory.addEventListener('change', () => validateField(recipeCategory, 'Please select a category'));
    if (recipeIngredients) recipeIngredients.addEventListener('blur', () => validateField(recipeIngredients, 'Please enter ingredients'));
    if (recipeInstructions) recipeInstructions.addEventListener('blur', () => validateField(recipeInstructions, 'Please enter cooking instructions'));
    
    // Listen for file upload events
    const recipeImage = document.getElementById('recipe-image');
    if (recipeImage) {
      recipeImage.addEventListener('uploadError', (e) => {
        this.showUploadMessage(e.detail.message, 'error');
      });
    }
  }

  bindProfilePictureUploadEvents() {
    const profileImageInput = document.getElementById('profile-image-upload-input');
    if (profileImageInput && this.profilePictureUpload && this.authService) {
      // Listen for the 'fileCropped' or 'fileSelected' (if cropping disabled) event from FileUpload
      profileImageInput.addEventListener('fileCropped', async () => {
        const file = this.profilePictureUpload.getFile();
        if (file) {
          this.showProfileUploadMessage('Uploading new profile picture...', 'loading');
          try {
            const { error } = await this.authService.updateProfilePicture(file);
            if (error) {
              throw error;
            }
            this.showProfileUploadMessage('Profile picture updated!', 'success');
            // AuthService's onAuthStateChange should refresh the avatar via _handleUserSignedIn
            // Or we can trigger a manual refresh if needed:
            // const user = this.authService.getCurrentUser();
            // if (user) this.authService._handleUserSignedIn({ user });
          } catch (error) {
            console.error('Profile picture update error:', error);
            this.showProfileUploadMessage(`Update failed: ${error.message}`, 'error');
          }
        }
      });

      profileImageInput.addEventListener('uploadError', (e) => {
         this.showProfileUploadMessage(e.detail.message, 'error');
      });
    }
  }
  
  showProfileUploadMessage(message, type) {
    if (!this.profileUploadMessage) {
        this.profileUploadMessage = document.getElementById('profile-upload-message');
    }
    if (this.profileUploadMessage) {
        this.profileUploadMessage.textContent = message;
        this.profileUploadMessage.className = `upload-message ${type || ''}`;
        this.profileUploadMessage.classList.remove('hidden');
        setTimeout(() => {
            this.profileUploadMessage.classList.add('hidden');
            this.profileUploadMessage.textContent = '';
        }, 5000);
    }
  }
  
  /**
   * Bind navigation-related events
   */
  bindNavigationEvents() {
    // Set up all navigation links
    const navLinks = document.querySelectorAll('.nav-link');
    navLinks.forEach(link => {
      link.addEventListener('click', (e) => {
        const href = link.getAttribute('href');
        if (href !== '#') {
          e.preventDefault();
          const targetId = href.substring(1); // Remove the # symbol
          this.scrollToElement(targetId);
        }
      });
    });
    
    // Global scroll function for elements added via JavaScript
    window.scrollToFeatures = () => {
      this.scrollToElement('features-section');
    };
  }
  
  // --- Auth Helper Methods (showLoggedIn, showLoggedOut, setAuthMessage, validateAuthInput) removed ---
  // This logic is now handled by AuthService.
  
  // --- Recipe Upload Helper Methods ---
  
  /**
   * Validate recipe upload form
   * @returns {boolean} - Is form valid
   */
  validateRecipeForm() {
    let isValid = true;
    
    const recipeName = document.getElementById('recipe-name');
    const recipeCategory = document.getElementById('recipe-category');
    const recipeIngredients = document.getElementById('recipe-ingredients');
    const recipeInstructions = document.getElementById('recipe-instructions');
    
    if (!validateField(recipeName, 'Please enter a recipe name')) isValid = false;
    if (!validateField(recipeCategory, 'Please select a category')) isValid = false;
    if (!validateField(recipeIngredients, 'Please enter ingredients')) isValid = false;
    if (!validateField(recipeInstructions, 'Please enter cooking instructions')) isValid = false;
    
    return isValid;
  }
  
  /**
   * Show upload message
   * @param {string} message - Message text
   * @param {string} type - Message type (success, error, warning)
   */
  showUploadMessage(message, type) {
    if (!this.uploadMessage) return;
    
    this.uploadMessage.textContent = message;
    this.uploadMessage.className = `upload-message ${type || ''}`;
    this.uploadMessage.classList.remove('hidden');
  }
  
  /**
   * Clear upload messages
   */
  clearUploadMessages() {
    if (this.uploadMessage) {
      this.uploadMessage.textContent = '';
      this.uploadMessage.className = 'upload-message';
    }
  }
  
  /**
   * Show upload progress
   * @param {number} percent - Progress percentage
   */
  showUploadProgress(percent) {
    if (!this.uploadProgress || !this.progressBar) return;
    
    this.uploadProgress.classList.remove('hidden');
    this.progressBar.style.width = `${percent}%`;
  }
  
  /**
   * Hide upload progress
   */
  hideUploadProgress() {
    if (!this.uploadProgress) return;
    
    // Fade out transition
    setTimeout(() => {
      this.uploadProgress.classList.add('hidden');
      if (this.progressBar) this.progressBar.style.width = '0%';
    }, 1000);
  }
  
  // --- Navigation Helper Methods ---
  
  /**
   * Scroll to element by ID
   * @param {string} elementId - Element ID to scroll to
   */
  scrollToElement(elementId) {
    const element = document.getElementById(elementId);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
    }
  }
}

// Initialize the application once the DOM is fully loaded
document.addEventListener('DOMContentLoaded', () => {
  new MixAndMunchApp();
  console.log('Mix and Munch application initialized.');
});

// Initialize the application (already done by DOMContentLoaded listener)
// const app = new MixAndMunchApp(); // Removed duplicate instantiation

// Export for debugging if needed
// window.app = app; // Removed, app instance is created within DOMContentLoaded
