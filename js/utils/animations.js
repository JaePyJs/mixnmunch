/**
 * Mix and Munch: Animation Utilities
 * Provides reusable animation functions for micro-interactions
 */

/**
 * Creates a ripple effect on a button or interactive element
 * @param {HTMLElement} element - The element to attach the ripple to
 */
export function addRippleEffect(element) {
  if (!element) return;
  
  // Create ripple container if it doesn't exist
  if (!element.querySelector('.ripple-container')) {
    const rippleContainer = document.createElement('span');
    rippleContainer.className = 'ripple-container';
    element.appendChild(rippleContainer);
  }
  
  // Add click event listener
  element.addEventListener('click', createRippleEffect);
}

/**
 * Creates a ripple effect animation at the click point
 * @param {Event} e - The click event
 */
export function createRippleEffect(e) {
  const button = e.currentTarget;
  const rippleContainer = button.querySelector('.ripple-container');
  
  if (!rippleContainer) return;
  
  const circle = document.createElement('span');
  const diameter = Math.max(button.clientWidth, button.clientHeight);
  const radius = diameter / 2;
  
  // Get click position relative to the button
  const rect = button.getBoundingClientRect();
  circle.style.width = circle.style.height = `${diameter}px`;
  circle.style.left = `${e.clientX - rect.left - radius}px`;
  circle.style.top = `${e.clientY - rect.top - radius}px`;
  circle.classList.add('ripple');
  
  // Remove existing ripples
  const existingRipple = rippleContainer.querySelector('.ripple');
  if (existingRipple) {
    existingRipple.remove();
  }
  
  rippleContainer.appendChild(circle);
  
  // Remove the ripple after animation completes
  setTimeout(() => {
    circle.remove();
  }, 600);
}

/**
 * Creates floating SVG elements around a container
 * @param {HTMLElement} container - The container to add floating SVGs to
 * @param {number} count - Number of SVGs to create
 * @param {Array} types - Array of SVG types to use ('utensil', 'ingredient', etc)
 */
export function createFloatingSVGs(container, count = 5, types = ['utensil', 'vegetable', 'spice']) {
  if (!container) return;
  
  // Create SVG container if it doesn't exist
  let svgContainer = container.querySelector('.floating-svg-container');
  if (!svgContainer) {
    svgContainer = document.createElement('div');
    svgContainer.className = 'floating-svg-container';
    container.appendChild(svgContainer);
  }
  
  // SVG path data for various food icons
  const svgIcons = {
    utensil: 'M10,21V8H14V21H10M12,2C17.5,2 22,6.5 22,12C22,17.5 17.5,22 12,22C6.5,22 2,17.5 2,12C2,6.5 6.5,2 12,2Z',
    vegetable: 'M12,2C17.5,2 22,6.5 22,12C22,17.5 17.5,22 12,22C6.5,22 2,17.5 2,12C2,6.5 6.5,2 12,2Z',
    spice: 'M6,2C8.21,2 10,3.79 10,6V18C10,20.21 8.21,22 6,22C3.79,22 2,20.21 2,18V6C2,3.79 3.79,2 6,2M18,6V16H15V8A2,2 0 0,0 13,6H11.03C11.27,5.09 11.79,4.29 12.5,3.67V4A2,2 0 0,0 14.5,6H18M14.5,6A1,1 0 0,1 13.5,5A1,1 0 0,1 14.5,4A1,1 0 0,1 15.5,5A1,1 0 0,1 14.5,6Z',
    grain: 'M4,5A2,2 0 0,0 2,7V17A2,2 0 0,0 4,19H20A2,2 0 0,0 22,17V7A2,2 0 0,0 20,5H4Z',
    protein: 'M9.54,9.18L6.43,6.08L1.26,6.05C1.11,6.05 0.97,6.11 0.84,6.23C0.73,6.36 0.66,6.5 0.66,6.66C0.66,6.81 0.73,6.97 0.84,7.08L9.54,15.82L19.68,5.93L19.4,5.5C19.29,5.38 19.13,5.31 18.96,5.29L15.51,5.29L9.54,9.18Z'
  };
  
  // Create floating SVGs
  for (let i = 0; i < count; i++) {
    // Random SVG type from types array
    const type = types[Math.floor(Math.random() * types.length)];
    const path = svgIcons[type] || svgIcons.utensil; // Default to utensil if type not found
    
    // Create SVG element
    const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
    svg.setAttribute('viewBox', '0 0 24 24');
    svg.classList.add('floating-svg');
    
    // Create path
    const pathElement = document.createElementNS('http://www.w3.org/2000/svg', 'path');
    pathElement.setAttribute('d', path);
    
    // Random positioning and animation
    const size = Math.random() * 20 + 10; // Random size between 10px and 30px
    const left = Math.random() * 100; // Random position from 0-100%
    const duration = Math.random() * 15 + 5; // Random duration between 5-20s
    const delay = Math.random() * 5; // Random delay between 0-5s
    
    // Apply random styles
    svg.style.width = `${size}px`;
    svg.style.height = `${size}px`;
    svg.style.left = `${left}%`;
    svg.style.animationDuration = `${duration}s`;
    svg.style.animationDelay = `${delay}s`;
    svg.style.opacity = (Math.random() * 0.5 + 0.2).toString(); // Random opacity between 0.2-0.7
    
    // Random color from our palette
    const colors = ['var(--primary)', 'var(--accent)', 'var(--success)', 'var(--warning)'];
    pathElement.style.fill = colors[Math.floor(Math.random() * colors.length)];
    
    // Append elements
    svg.appendChild(pathElement);
    svgContainer.appendChild(svg);
  }
}

/**
 * Add pulse animation to an element when clicked
 * @param {HTMLElement} element - Element to apply pulse to
 */
export function addPulseAnimation(element) {
  if (!element) return;
  
  element.classList.add('pulse-animation');
  setTimeout(() => {
    element.classList.remove('pulse-animation');
  }, 700);
}

/**
 * Create floating ingredient tag animation
 * @param {string} ingredientName - Name of ingredient to display
 * @param {HTMLElement} container - Container to add the floating tag to
 */
export function createFloatingIngredient(ingredientName, container) {
  if (!container || !ingredientName) return;
  
  // Create floating tag
  const tag = document.createElement('div');
  tag.className = 'floating-ingredient-tag';
  tag.textContent = ingredientName;
  
  // Random position within container
  const leftPos = Math.random() * 80 + 10; // Between 10-90%
  tag.style.left = `${leftPos}%`;
  
  // Append to container
  container.appendChild(tag);
  
  // Remove after animation completes
  setTimeout(() => {
    tag.remove();
  }, 3000);
}

/**
 * Add a pulse animation to an element
 * @param {HTMLElement} element - Element to animate
 * @param {number} duration - Animation duration in ms
 */
export function pulseElement(element, duration = 500) {
  if (!element) return;
  
  element.classList.add('pulse-animation');
  
  setTimeout(() => {
    element.classList.remove('pulse-animation');
  }, duration);
}

/**
 * Creates a staggered animation effect for a list of elements
 * @param {NodeList|Array} elements - Elements to animate
 * @param {string} animationClass - CSS class containing the animation
 * @param {number} delayBetween - Delay between each element animation in ms
 */
export function staggerAnimation(elements, animationClass, delayBetween = 100) {
  if (!elements || !elements.length) return;
  
  Array.from(elements).forEach((el, index) => {
    setTimeout(() => {
      el.classList.add(animationClass);
    }, index * delayBetween);
  });
}

/**
 * Adds a slide-in animation to tab content
 * @param {HTMLElement} element - The tab content element
 * @param {string} direction - Direction of animation ('left', 'right', 'up', 'down')
 */
export function animateTabTransition(element, direction = 'right') {
  if (!element) return;
  
  // Define transform values based on direction
  const transforms = {
    left: 'translateX(-20px)',
    right: 'translateX(20px)',
    up: 'translateY(-20px)',
    down: 'translateY(20px)'
  };
  
  // Set initial state
  element.style.opacity = '0';
  element.style.transform = transforms[direction];
  element.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
  
  // Force reflow
  void element.offsetWidth;
  
  // Animate to final state
  element.style.opacity = '1';
  element.style.transform = 'translate(0)';
  
  // Clean up after animation
  setTimeout(() => {
    element.style.transition = '';
  }, 300);
}

/**
 * Initialize all animation handlers across the app
 */
export function initAnimations() {
  // Add ripple effect to all buttons
  document.querySelectorAll('.btn, .cta, button:not(.no-ripple)').forEach(addRippleEffect);
  
  // Add header scroll effect
  initHeaderScrollEffect();
  
  // Initialize scroll reveal animations
  initScrollReveal();
}

/**
 * Add header shadow when scrolling
 */
function initHeaderScrollEffect() {
  const header = document.querySelector('.app-header');
  const content = document.querySelector('.app-content');
  
  if (!header || !content) return;
  
  content.addEventListener('scroll', () => {
    if (content.scrollTop > 10) {
      header.classList.add('scrolled');
    } else {
      header.classList.remove('scrolled');
    }
  });
}

/**
 * Initialize scroll reveal animations
 */
function initScrollReveal() {
  const content = document.querySelector('.app-content');
  if (!content) return;
  
  // Create an IntersectionObserver
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('fade-in');
        observer.unobserve(entry.target);
      }
    });
  }, {
    root: content,
    threshold: 0.1
  });
  
  // Observe all elements with reveal class
  document.querySelectorAll('.reveal').forEach(el => {
    observer.observe(el);
  });
}

// Initialize animations when DOM is loaded
document.addEventListener('DOMContentLoaded', initAnimations);
