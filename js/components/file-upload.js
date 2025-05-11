/**
 * Mix and Munch: File Upload Component
 * Handles image upload, drag & drop, cropping, and preview functionality
 */

import { validateImageFile } from '../utils/validation.js';

export class FileUpload {
  constructor(options = {}) {
    // Default options
    this.options = {
      maxFileSize: 5 * 1024 * 1024, // 5MB in bytes
      acceptedTypes: ['image/jpeg', 'image/png', 'image/gif'],
      enableCropping: true,
      cropperOptions: {
        aspectRatio: 16 / 9,
        viewMode: 1
      },
      ...options
    };
    
    // Elements
    this.fileInput = document.getElementById('recipe-image');
    this.uploadArea = document.getElementById('image-upload-area');
    this.previewContainer = document.getElementById('image-preview');
    this.previewImg = document.getElementById('preview-img');
    this.previewText = document.querySelector('.preview-text');
    this.chooseBtn = document.getElementById('choose-image-btn');
    this.uploadProgress = document.getElementById('image-upload-progress');
    this.progressBar = document.getElementById('image-progress-bar');
    
    // Cropper related elements
    this.cropperModal = document.getElementById('cropper-modal');
    this.cropperImage = document.getElementById('cropper-image');
    this.cropBtn = document.getElementById('crop-image-btn');
    this.cancelCropBtn = document.getElementById('cancel-crop-btn');
    
    // Cropper instance
    this.cropper = null;
    this.currentFile = null;
    
    // Custom events
    this.events = {
      fileSelected: new CustomEvent('fileSelected'),
      uploadProgress: new CustomEvent('uploadProgress'),
      uploadComplete: new CustomEvent('uploadComplete'),
      uploadError: new CustomEvent('uploadError'),
      fileCropped: new CustomEvent('fileCropped')
    };
    
    // Initialize component
    this.init();
  }
  
  /**
   * Initialize the component
   */
  init() {
    if (!this.fileInput || !this.uploadArea) {
      console.error('Required elements not found for FileUpload component');
      return;
    }
    
    this.bindEvents();
  }
  
  /**
   * Bind all event handlers
   */
  bindEvents() {
    // File input change event (regular file selection)
    if (this.fileInput) {
      this.fileInput.addEventListener('change', this.handleFileSelect.bind(this));
    }
    
    // Choose button click event
    if (this.chooseBtn) {
      this.chooseBtn.addEventListener('click', () => {
        this.fileInput.click();
      });
    }
    
    // Drag and drop events
    if (this.uploadArea) {
      this.uploadArea.addEventListener('dragover', this.handleDragOver.bind(this));
      this.uploadArea.addEventListener('dragleave', this.handleDragLeave.bind(this));
      this.uploadArea.addEventListener('drop', this.handleDrop.bind(this));
      this.uploadArea.addEventListener('click', (e) => {
        // Only trigger file input click if they clicked the area itself, not a button inside
        if (e.target === this.uploadArea || e.target === this.previewContainer || e.target === this.previewText) {
          this.fileInput.click();
        }
      });
    }
    
    // Cropper events
    if (this.cropBtn) {
      this.cropBtn.addEventListener('click', this.cropImage.bind(this));
    }
    
    if (this.cancelCropBtn) {
      this.cancelCropBtn.addEventListener('click', () => {
        this.hideCropper();
        this.resetPreview();
      });
    }
  }
  
  /**
   * Handle regular file selection via input
   * @param {Event} e - Change event
   */
  handleFileSelect(e) {
    const file = e.target.files[0];
    if (file) {
      this.processFile(file);
    }
  }
  
  /**
   * Handle drag over event
   * @param {DragEvent} e - Drag event
   */
  handleDragOver(e) {
    e.preventDefault();
    e.stopPropagation();
    this.uploadArea.classList.add('dragover');
  }
  
  /**
   * Handle drag leave event
   * @param {DragEvent} e - Drag event
   */
  handleDragLeave(e) {
    e.preventDefault();
    e.stopPropagation();
    this.uploadArea.classList.remove('dragover');
  }
  
  /**
   * Handle drop event
   * @param {DragEvent} e - Drop event
   */
  handleDrop(e) {
    e.preventDefault();
    e.stopPropagation();
    this.uploadArea.classList.remove('dragover');
    
    const file = e.dataTransfer.files[0];
    if (file) {
      this.processFile(file);
    }
  }
  
  /**
   * Process the selected/dropped file
   * @param {File} file - The file object
   */
  processFile(file) {
    this.currentFile = file;
    
    // Validate file
    if (!this.validateFile(file)) {
      return;
    }
    
    // Show preview or cropper
    const reader = new FileReader();
    reader.onload = (e) => {
      if (this.options.enableCropping && window.Cropper) {
        // Show cropper
        this.showCropper(e.target.result);
      } else {
        // Show preview directly
        this.showPreview(e.target.result);
      }
      
      this.fileInput.dispatchEvent(this.events.fileSelected);
    };
    reader.readAsDataURL(file);
  }
  
  /**
   * Validate file type and size
   * @param {File} file - The file to validate
   * @returns {boolean} - Is the file valid
   */
  validateFile(file) {
    // Check file type
    if (!this.options.acceptedTypes.includes(file.type)) {
      this.showError('Invalid file type. Please upload JPG, PNG, or GIF.');
      this.resetPreview();
      return false;
    }
    
    // Check file size
    if (file.size > this.options.maxFileSize) {
      this.showError('Image too large. Maximum size is 5MB.');
      this.resetPreview();
      return false;
    }
    
    return true;
  }
  
  /**
   * Show the image cropper
   * @param {string} src - Data URL of the image
   */
  showCropper(src) {
    // Show cropper modal
    this.cropperModal.classList.remove('hidden');
    
    // Set cropper image source
    this.cropperImage.src = src;
    
    // Initialize cropper
    if (window.Cropper) {
      // Destroy previous cropper if it exists
      if (this.cropper) {
        this.cropper.destroy();
      }
      
      // Create new cropper
      this.cropper = new Cropper(this.cropperImage, this.options.cropperOptions);
    } else {
      console.error('Cropper.js is not available. Please include it in your page.');
      this.hideCropper();
      this.showPreview(src);
    }
  }
  
  /**
   * Hide the image cropper
   */
  hideCropper() {
    this.cropperModal.classList.add('hidden');
    
    // Destroy cropper instance
    if (this.cropper) {
      this.cropper.destroy();
      this.cropper = null;
    }
  }
  
  /**
   * Crop the image
   */
  cropImage() {
    if (!this.cropper) return;
    
    // Get cropped canvas
    const canvas = this.cropper.getCroppedCanvas({
      width: 800,    // max width
      height: 450,   // max height
      imageSmoothingEnabled: true,
      imageSmoothingQuality: 'high'
    });
    
    // Convert to data URL
    const croppedImageUrl = canvas.toDataURL(this.currentFile.type);
    
    // Show preview
    this.showPreview(croppedImageUrl);
    
    // Hide cropper
    this.hideCropper();
    
    // Convert data URL to Blob
    canvas.toBlob((blob) => {
      // Create new File object from Blob
      this.currentFile = new File([blob], this.currentFile.name, {
        type: this.currentFile.type,
        lastModified: Date.now()
      });
      
      // Dispatch event
      this.fileInput.dispatchEvent(this.events.fileCropped);
    }, this.currentFile.type);
  }
  
  /**
   * Show image preview
   * @param {string} src - Data URL of the image
   */
  showPreview(src) {
    if (this.previewImg) {
      this.previewImg.src = src;
      this.previewImg.classList.remove('hidden');
    }
    
    if (this.previewText) {
      this.previewText.classList.add('hidden');
    }
  }
  
  /**
   * Reset image preview
   */
  resetPreview() {
    if (this.previewImg) {
      this.previewImg.src = '#';
      this.previewImg.classList.add('hidden');
    }
    
    if (this.previewText) {
      this.previewText.classList.remove('hidden');
    }
    
    if (this.fileInput) {
      this.fileInput.value = '';
    }
    
    this.currentFile = null;
  }
  
  /**
   * Show upload progress
   * @param {number} percent - Progress percentage (0-100)
   */
  showProgress(percent) {
    if (!this.uploadProgress || !this.progressBar) return;
    
    this.uploadProgress.classList.remove('hidden');
    this.progressBar.style.width = `${percent}%`;
    
    // Dispatch event
    this.events.uploadProgress.detail = { percent };
    this.fileInput.dispatchEvent(this.events.uploadProgress);
  }
  
  /**
   * Hide upload progress
   */
  hideProgress() {
    if (!this.uploadProgress) return;
    
    // Fade out transition
    setTimeout(() => {
      this.uploadProgress.classList.add('hidden');
      if (this.progressBar) this.progressBar.style.width = '0%';
    }, 1000);
    
    // Dispatch event
    this.fileInput.dispatchEvent(this.events.uploadComplete);
  }
  
  /**
   * Show error message
   * @param {string} message - Error message
   */
  showError(message) {
    // Trigger error event with message
    this.events.uploadError.detail = { message };
    this.fileInput.dispatchEvent(this.events.uploadError);
  }
  
  /**
   * Get the current file
   * @returns {File|null} - The current file or null
   */
  getFile() {
    return this.currentFile;
  }
}

// Export factory function
export const createFileUpload = (options) => {
  return new FileUpload(options);
};
