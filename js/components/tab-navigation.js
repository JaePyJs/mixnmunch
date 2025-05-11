/**
 * Mix and Munch: Tab Navigation Component
 * Handles tab switching and animations for mobile-first design
 */

export class TabNavigation {
  constructor() {
    // Tab elements
    this.tabItems = document.querySelectorAll('.tab-item');
    this.tabContents = document.querySelectorAll('.tab-content');
    
    // Current active tab
    this.activeTab = 'home'; // Default tab
    
    // Bind events
    this.bindEvents();
    
    // Initialize
    this.init();
  }
  
  /**
   * Initialize component
   */
  init() {
    // Check if there's a saved tab state in localStorage
    const savedTab = localStorage.getItem('activeTab');
    if (savedTab) {
      this.activeTab = savedTab;
    }
    
    // Show default tab on load
    this.switchTab(this.activeTab, false);
    
    // Add ripple effect containers to all buttons
    document.querySelectorAll('.btn, .cta, button:not(.no-ripple)').forEach(button => {
      if (!button.querySelector('.ripple-container')) {
        const rippleContainer = document.createElement('span');
        rippleContainer.className = 'ripple-container';
        button.appendChild(rippleContainer);
        
        button.addEventListener('click', this.createRippleEffect);
      }
    });
    
    // Handle "Start Cooking" button click to navigate to generate tab
    const startCookingBtn = document.getElementById('start-cooking-btn');
    if (startCookingBtn) {
      startCookingBtn.addEventListener('click', () => {
        this.switchTab('generate');
        
        // Find the generate tab nav item and mark it as active
        this.tabItems.forEach(tab => {
          if (tab.getAttribute('data-tab') === 'generate') {
            tab.click();
          }
        });
      });
    }
    
    // Add header scroll effect
    this.initHeaderScrollEffect();
    
    // Add floating SVGs to welcome banner
    this.initFloatingElements();
    
    // Apply staggered animations to category cards
    this.animateCategoryCards();
  }
  
  /**
   * Bind event listeners
   */
  bindEvents() {
    // Tab click events
    this.tabItems.forEach(tab => {
      tab.addEventListener('click', (e) => {
        e.preventDefault();
        const tabId = tab.getAttribute('data-tab');
        if (tabId) {
          this.switchTab(tabId);
          
          // Update active tab in the navigation
          this.tabItems.forEach(t => t.classList.remove('active'));
          tab.classList.add('active');
        }
      });
    });
    
    // Register swipe events for tabs
    this.initSwipeDetection();

    // Listen for custom event to navigate to a tab and optionally scroll
    document.addEventListener('navigateToTab', (e) => {
      const { tabId, scrollToSectionId } = e.detail;
      if (tabId) {
        this.switchTab(tabId, true); // Switch with animation

        // Ensure the correct nav item is marked active
        this.tabItems.forEach(tab => {
          if (tab.getAttribute('data-tab') === tabId) {
            tab.classList.add('active');
          } else {
            tab.classList.remove('active');
          }
        });

        if (scrollToSectionId) {
          // Use a timeout to allow tab switch animation to complete before scrolling
          setTimeout(() => {
            const section = document.getElementById(scrollToSectionId);
            if (section) {
              section.scrollIntoView({ behavior: 'smooth', block: 'start' });
            }
          }, 350); // Adjust timeout if needed based on animation duration
        }
      }
    });
  }
  
  /**
   * Switch to specified tab with animations
   * @param {string} tabId - ID of tab to switch to
   * @param {boolean} animate - Whether to animate the transition
   */
  switchTab(tabId, animate = true) {
    // Update active tab
    this.activeTab = tabId;
    
    // Update tab buttons in navigation
    this.tabItems.forEach(tab => {
      const targetTab = tab.getAttribute('data-tab');
      if (targetTab === tabId) {
        tab.classList.add('active');
      } else {
        tab.classList.remove('active');
      }
    });
    
    // Get the tab content elements
    const tabContentId = `tab-${tabId}`;
    
    // Update tab content with animations
    if (animate) {
      // First mark all tabs as exiting except the active one
      this.tabContents.forEach(content => {
        if (content.classList.contains('active')) {
          content.classList.add('exiting');
          content.classList.remove('active');
        }
      });
      
      // After exit animation, show the new tab with enter animation
      setTimeout(() => {
        this.tabContents.forEach(content => {
          // Remove all animation classes
          content.classList.remove('exiting');
          content.classList.remove('entering');
          
          if (content.id === tabContentId) {
            // Show the target tab with enter animation
            content.style.display = 'block';
            content.classList.add('entering');
            
            // Trigger reflow to ensure animation plays
            void content.offsetWidth;
            
            // Add active class to start animation
            content.classList.add('active');
            content.classList.remove('entering');
          } else {
            // Hide other tabs
            content.style.display = 'none';
          }
        });
      }, 300); // Match this with CSS transition duration
    } else {
      // No animation, just show/hide
      this.tabContents.forEach(content => {
        if (content.id === tabContentId) {
          content.style.display = 'block';
          content.classList.add('active');
        } else {
          content.style.display = 'none';
          content.classList.remove('active');
        }
      });
    }
    
    // Save tab state to localStorage
    localStorage.setItem('activeTab', tabId);
    
    // Dispatch custom event for tab change
    const tabChangeEvent = new CustomEvent('tabChanged', { 
      detail: { tabId: tabId }
    });
    document.dispatchEvent(tabChangeEvent);
  }
  
  /**
   * Initialize swipe detection for tab navigation
   */
  initSwipeDetection() {
    let startX;
    let endX;
    const minSwipeDistance = 50; // Minimum distance to trigger swipe
    
    // Only apply swipe detection to app-content area
    const contentArea = document.querySelector('.app-content');
    if (!contentArea) return;
    
    contentArea.addEventListener('touchstart', (e) => {
      startX = e.touches[0].clientX;
    }, { passive: true });
    
    contentArea.addEventListener('touchend', (e) => {
      if (!startX) return;
      
      endX = e.changedTouches[0].clientX;
      const distance = endX - startX;
      
      if (Math.abs(distance) < minSwipeDistance) return;
      
      // Get tab indices for calculation
      const tabIds = Array.from(this.tabItems).map(tab => tab.getAttribute('data-tab'));
      const currentIndex = tabIds.indexOf(this.activeTab);
      
      // Swipe right to go to previous tab
      if (distance > 0 && currentIndex > 0) {
        this.switchTab(tabIds[currentIndex - 1]);
        this.tabItems[currentIndex - 1].classList.add('active');
        this.tabItems[currentIndex].classList.remove('active');
      }
      // Swipe left to go to next tab
      else if (distance < 0 && currentIndex < tabIds.length - 1) {
        this.switchTab(tabIds[currentIndex + 1]);
        this.tabItems[currentIndex + 1].classList.add('active');
        this.tabItems[currentIndex].classList.remove('active');
      }
      
      startX = null;
    }, { passive: true });
  }
  
  /**
   * Initialize header scroll effect for a cleaner mobile experience
   */
  initHeaderScrollEffect() {
    const header = document.querySelector('.app-header');
    if (!header) return;
    
    let lastScrollTop = 0;
    
    window.addEventListener('scroll', () => {
      const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
      
      if (scrollTop > 50) {
        // Scrolling down, add compact class
        header.classList.add('compact');
      } else {
        // At top, remove compact class
        header.classList.remove('compact');
      }
      
      // Update last scroll position
      lastScrollTop = scrollTop;
    });
  }
  
  /**
   * Initialize floating SVGs and other decorative elements
   */
  initFloatingElements() {
    // Import the animation utilities
    import('../utils/animations.js').then(module => {
      const { createFloatingSVGs } = module;
      
      // Add floating SVGs to welcome banner
      const welcomeBanner = document.querySelector('.welcome-banner');
      if (welcomeBanner) {
        createFloatingSVGs(welcomeBanner, 6, ['utensil', 'vegetable', 'spice']);
      }
      
      // Add floating SVGs to generate tab
      const generateTab = document.getElementById('tab-generate');
      if (generateTab) {
        createFloatingSVGs(generateTab, 4, ['grain', 'protein', 'vegetable']);
      }
    }).catch(err => console.error('Failed to load animations:', err));
  }
  
  /**
   * Animate category cards with staggered effect
   */
  animateCategoryCards() {
    const categoryCards = document.querySelectorAll('.category-card');
    categoryCards.forEach((card, index) => {
      card.style.animationDelay = `${index * 0.1}s`;
      card.classList.add('slide-up');
    });
    
    const recipeCards = document.querySelectorAll('.recipe-card-horizontal');
    recipeCards.forEach((card, index) => {
      card.style.animationDelay = `${index * 0.15 + 0.3}s`;
      card.classList.add('slide-up');
    });
  }
  
  /**
   * Create ripple effect on button click
   * @param {Event} e - Click event
   */
  createRippleEffect(e) {
    const button = e.currentTarget;
    const rippleContainer = button.querySelector('.ripple-container');
    if (!rippleContainer) return;
    
    const circle = document.createElement('span');
    const diameter = Math.max(button.clientWidth, button.clientHeight);
    const radius = diameter / 2;
    
    // Get position relative to button
    const rect = button.getBoundingClientRect();
    
    circle.style.width = circle.style.height = `${diameter}px`;
    circle.style.left = `${e.clientX - rect.left - radius}px`;
    circle.style.top = `${e.clientY - rect.top - radius}px`;
    circle.classList.add('ripple');
    
    // Remove existing ripples
    const ripple = rippleContainer.querySelector('.ripple');
    if (ripple) {
      ripple.remove();
    }
    
    rippleContainer.appendChild(circle);
    
    // Clean up ripple after animation
    setTimeout(() => {
      circle.remove();
    }, 600);
  }
}

// Initialize tab navigation when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
  new TabNavigation();
});

// Export for potential re-use
export function createTabNavigation() {
  return new TabNavigation();
}
