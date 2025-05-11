/**
 * Mix and Munch: Auth Service
 * Centralized authentication service for Supabase
 */

/**
 * Authentication service for Supabase
 */
export class AuthService {
  constructor(supabase) {
    this.supabase = supabase;
    this.currentUser = null;
    this.authStateListeners = [];

    // DOM Elements for Auth - will be queried in _getDOMElements
    this.signupForm = null;
    this.loginForm = null;
    this.logoutBtn = null;
    this.authMessageEl = null; // Renamed to avoid conflict with method
    this.userInfoEl = null;    // Renamed to avoid conflict

    // View containers
    this.landingView = null;
    this.appView = null;

    // Security-related state
    this.lastAttempt = 0;
    this.attemptCount = 0;
    this.rateLimit = {
      minDelay: 2000,      // 2 second minimum between attempts
      maxAttempts: 5,      // Max 5 attempts before lockout
      lockoutTime: 60000   // 1 minute lockout
    };
  }

  /**
   * Initialize auth state and event listeners
   * @returns {Promise<void>}
   */
  async initialize() {
    try {
      // Check for existing session
      const { data: { session } } = await this.supabase.auth.getSession();
      
      if (session) {
        this.currentUser = session.user;
        await this._handleUserSignedIn(session); // Updated to handle UI
      } else {
        this._handleUserSignedOut(); // Updated to handle UI
      }
      
      // Set up auth state change listener
      this.supabase.auth.onAuthStateChange(async (event, session) => {
        if (event === 'SIGNED_IN' && session) {
          this.currentUser = session.user;
          await this._handleUserSignedIn(session);
        } else if (event === 'SIGNED_OUT') {
          this.currentUser = null;
          this._handleUserSignedOut();
        }
        // Still notify external listeners
        this.notifyListeners(event, session);
      });
    } catch (error) {
      console.error('Auth initialization error:', error);
      this._handleUserSignedOut(); // Ensure UI is in logged out state on error
      throw error;
    }
  }

  _getDOMElements() {
    this.signupForm = document.getElementById('signup-form');
    this.loginForm = document.getElementById('login-form');
    this.logoutBtn = document.getElementById('logout-btn');
    this.authMessageEl = document.getElementById('auth-message');
    this.userInfoEl = document.getElementById('user-info'); // For username/email text
    this.userAvatarImg = document.querySelector('#user-avatar img'); // For the avatar image
    this.landingView = document.getElementById('landing-view');
    this.appView = document.getElementById('app-view');
  }

  _bindAuthFormEvents() {
    if (this.signupForm) {
      this.signupForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        this.setAuthMessage('Signing up...', 'loading');
        
        const usernameInput = document.getElementById('signup-username');
        const emailInput = document.getElementById('signup-email');
        const passwordInput = document.getElementById('signup-password');

        const username = usernameInput?.value.trim();
        const email = emailInput?.value.trim();
        const password = passwordInput?.value;
        
        if (!this._validateAuthInput(username, email, password)) {
          return;
        }
        
        try {
          const { data, error } = await this.signUp(email, password, username);
          if (error) throw error;
          if (data?.user) {
            this.setAuthMessage('Signup successful! Please check your email for verification.', 'success');
            this.signupForm.reset();
          }
        } catch (error) {
          console.error('Signup error:', error);
          this.setAuthMessage(`Signup failed: ${error.message}`, 'error');
        }
      });
    }
    
    if (this.loginForm) {
      this.loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        this.setAuthMessage('Logging in...', 'loading');

        const emailInput = document.getElementById('login-email');
        const passwordInput = document.getElementById('login-password');
        
        const email = emailInput?.value.trim();
        const password = passwordInput?.value;
        
        try {
          const { data, error } = await this.signIn(email, password);
          if (error) throw error;
          if (data?.user) {
            // UI update is handled by onAuthStateChange -> _handleUserSignedIn
            this.setAuthMessage('Logged in successfully!', 'success');
            this.loginForm.reset();
          }
        } catch (error) {
          console.error('Login error:', error);
          this.setAuthMessage(`Login failed: ${error.message}`, 'error');
        }
      });
    }
    
    if (this.logoutBtn) {
      this.logoutBtn.addEventListener('click', async () => {
        try {
          this.setAuthMessage('Logging out...', 'loading');
          const { error } = await this.signOut();
          if (error) throw error;
          // UI update is handled by onAuthStateChange -> _handleUserSignedOut
          this.setAuthMessage('Logged out successfully', 'success');
        } catch (error) {
          console.error('Logout error:', error);
          this.setAuthMessage(`Logout failed: ${error.message}`, 'error');
        }
      });
    }

    const guestButton = document.getElementById('landing-btn-guest');
    if (guestButton) {
      guestButton.addEventListener('click', () => {
        this._showAppView();
        // Optionally, dispatch an event or set a "guest mode" flag
        this.notifyListeners('GUEST_MODE_ACTIVATED', null);
      });
    }
  }

  _showLandingView() {
    console.log('[AuthService] Attempting to show Landing View');
    if (!this.landingView || !this.appView) this._getDOMElements();
    if (this.landingView) {
      this.landingView.classList.remove('hidden');
      console.log('[AuthService] Landing View unhidden');
    } else {
      console.error('[AuthService] landingView element not found');
    }
    if (this.appView) {
      this.appView.classList.add('hidden');
      console.log('[AuthService] App View hidden');
    } else {
      console.error('[AuthService] appView element not found');
    }
  }

  _showAppView() {
    console.log('[AuthService] Attempting to show App View');
    if (!this.landingView || !this.appView) this._getDOMElements();
    if (this.landingView) {
      this.landingView.classList.add('hidden');
      console.log('[AuthService] Landing View hidden');
    } else {
      console.error('[AuthService] landingView element not found');
    }
    if (this.appView) {
      this.appView.classList.remove('hidden');
      console.log('[AuthService] App View unhidden');
    } else {
      console.error('[AuthService] appView element not found');
    }
  }

  async _handleUserSignedIn(session) {
    if (!session || !session.user) return;
    this.currentUser = session.user;
    const { profile } = await this.getUserProfile();
    this._showLoggedInUI(session.user, profile); // Handles auth forms and user info display
    this._showAppView(); // Show the main application view
    this.notifyListeners('SIGNED_IN', session); // Notify external listeners after UI update
  }

  _handleUserSignedOut() {
    this.currentUser = null;
    this._showLoggedOutUI(); // Handles auth forms and user info display
    this._showLandingView(); // Show the landing page view
    this.notifyListeners('SIGNED_OUT', null); // Notify external listeners after UI update
  }

  /**
   * Add a listener for auth state changes
   * @param {Function} listener - Callback function
   */
  addStateChangeListener(listener) {
    this.authStateListeners.push(listener);
  }

  /**
   * Notify all listeners of auth state changes
   * @param {string} event - Auth event name
   * @param {Object} session - Auth session
   */
  notifyListeners(event, session) {
    this.authStateListeners.forEach(listener => {
      try {
        listener(event, session);
      } catch (error) {
        console.error('Error in auth state listener:', error);
      }
    });
  }

  /**
   * Sign up a new user
   * @param {string} email - User email
   * @param {string} password - User password
   * @param {string} username - User's chosen username
   * @returns {Promise<Object>} - Result with data and error state
   */
  async signUp(email, password, username) {
    try {
      // Sign up with Supabase
      const { data, error } = await this.supabase.auth.signUp({
        email,
        password,
        options: {
          data: {
            username
          }
        }
      });
      
      if (error) throw error;
      
      // Create user profile in Supabase
      if (data.user) {
        try {
          await this.supabase.from('profiles').insert([{
            id: data.user.id,
            username,
            created_at: new Date().toISOString()
          }]);
        } catch (profileError) {
          console.error('Error creating user profile:', profileError);
          // Continue anyway, as the auth account was created
        }
        
        return { data, error: null };
      }
      // If data.user is null but no error, it might mean email confirmation is pending
      return { data, error: null };
    } catch (error) {
      console.error('Signup error:', error);
      return { data: null, error };
    }
  }

  // --- UI Helper Methods (Moved from MixAndMunchApp) ---

  _validateAuthInput(username, email, password) {
    if (username && (username.length < 3 || username.length > 20)) {
      this.setAuthMessage("Username must be between 3 and 20 characters", "error");
      return false;
    }
    if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      this.setAuthMessage("Please enter a valid email address", "error");
      return false;
    }
    if (
      !password ||
      password.length < 8 ||
      !/[A-Z]/.test(password) ||
      !/[a-z]/.test(password) ||
      !/[0-9]/.test(password)
    ) {
      this.setAuthMessage(
        "Password must be at least 8 characters with uppercase, lowercase, and numbers",
        "error"
      );
      return false;
    }
    return true;
  }

  setAuthMessage(message, type) {
    if (!this.authMessageEl) this._getDOMElements(); // Ensure DOM elements are loaded
    if (!this.authMessageEl) return;

    this.authMessageEl.textContent = message;
    this.authMessageEl.className = `auth-message ${type || ""}`;
  }

  async _showLoggedInUI(user, profileData) {
    if (!this.userAvatarImg || !this.userInfoEl || !this.logoutBtn || !this.signupForm || !this.loginForm) this._getDOMElements();
    
    let currentProfile = profileData;
    if (!currentProfile && user) {
        const { profile } = await this.getUserProfile();
        currentProfile = profile;
    }

    // Update avatar image
    if (this.userAvatarImg) {
      if (currentProfile?.avatar_url) {
        this.userAvatarImg.src = currentProfile.avatar_url;
        this.userAvatarImg.alt = currentProfile?.username || user?.user_metadata?.username || 'User';
      } else {
        // Default to initials SVG placeholder
        const initials = (user?.user_metadata?.username?.[0] || user?.email?.[0] || 'U').toUpperCase();
        this.userAvatarImg.src = `data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'%3E%3Crect width='100' height='100' fill='%23ddd'/%3E%3Ctext x='50' y='50' font-family='Arial' font-size='50' fill='%23777' text-anchor='middle' dy='.3em'%3E${initials}%3C/text%3E%3C/svg%3E`;
        this.userAvatarImg.alt = 'User Avatar';
      }
    }
    
    // Update user info text (username/email)
    if (this.userInfoEl) {
      this.userInfoEl.innerHTML = `
        <span class="username">${currentProfile?.username || user?.user_metadata?.username || 'User'}</span>
        <span class="user-email">${user?.email}</span>
      `;
      this.userInfoEl.classList.remove('hidden');
    }
    
    if (this.logoutBtn) this.logoutBtn.classList.remove('hidden');
    if (this.signupForm) this.signupForm.style.display = 'none';
    if (this.loginForm) this.loginForm.style.display = 'none';
  }

  _showLoggedOutUI() {
    if (!this.userAvatarImg || !this.userInfoEl || !this.logoutBtn || !this.signupForm || !this.loginForm) this._getDOMElements();
    
    // Set default/blank avatar
    if (this.userAvatarImg) {
      this.userAvatarImg.src = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'%3E%3Crect width='100' height='100' fill='%23eee'/%3E%3Ctext x='50' y='50' font-family='Arial' font-size='50' fill='%23aaa' text-anchor='middle' dy='.3em'%3E%3C/text%3E%3C/svg%3E"; // Blank placeholder SVG
      this.userAvatarImg.alt = "Guest Avatar";
    }

    if (this.userInfoEl) {
      this.userInfoEl.classList.add('hidden');
      this.userInfoEl.innerHTML = ''; // Clear user name/email details
    }
    if (this.logoutBtn) this.logoutBtn.classList.add('hidden');
    
    if (this.signupForm) this.signupForm.style.display = 'block';
    if (this.loginForm) this.loginForm.style.display = 'block';
  }


  /**
   * Sign in an existing user
   * @param {string} email - User email
   * @param {string} password - User password
   * @returns {Promise<Object>} - Result with data and error state
   */
  async signIn(email, password) {
    try {
      // Rate limiting
      const now = Date.now();
      
      if (now - this.lastAttempt < this.rateLimit.minDelay) {
        throw new Error('Please wait before trying again');
      }
      
      if (this.attemptCount >= this.rateLimit.maxAttempts && 
          now - this.lastAttempt < this.rateLimit.lockoutTime) {
        throw new Error('Too many attempts. Please try again later.');
      }
      
      this.lastAttempt = now;
      
      // Attempt login
      const { data, error } = await this.supabase.auth.signInWithPassword({ 
        email, 
        password 
      });
      
      if (error) throw error;
      
      if (data.user) {
        // Success - reset attempt counter
        this.attemptCount = 0;
        this.currentUser = data.user;
        return { data, error: null };
      } else {
        throw new Error('Login failed. Please check your credentials.');
      }
    } catch (error) {
      console.error('Login error:', error);
      // Increment attempt counter on failure
      this.attemptCount++;
      return { data: null, error };
    }
  }

  /**
   * Sign out the current user
   * @returns {Promise<Object>} - Result with data and error state
   */
  async signOut() {
    try {
      const { error } = await this.supabase.auth.signOut();
      
      if (error) throw error;
      
      this.currentUser = null;
      // Clear any rate limiting state
      this.attemptCount = 0;
      this.lastAttempt = 0;
      
      return { error: null };
    } catch (error) {
      console.error('Logout error:', error);
      return { error };
    }
  }

  /**
   * Get the current user's profile
   * @returns {Promise<Object>} - User profile
   */
  async getUserProfile() {
    try {
      if (!this.currentUser) return { profile: null, error: 'No user logged in' };
      
      const { data, error } = await this.supabase
        .from('profiles')
        .select('*')
        .eq('id', this.currentUser.id)
        .single();
      
      if (error) throw error;
      
      return { profile: data, error: null };
    } catch (error) {
      console.error('Error fetching user profile:', error);
      return { profile: null, error };
    }
  }
  
  /**
   * Returns the current user if logged in
   * @returns {Object|null} - Current user object or null
   */
  getCurrentUser() {
    return this.currentUser;
  }
  
  /**
   * Check if user is logged in
   * @returns {boolean} - True if user is logged in
   */
  isLoggedIn() {
    return !!this.currentUser;
  }

  /**
   * Update user's profile picture
   * @param {File} file - The image file to upload
   * @returns {Promise<Object>} - Object with data and error state
   */
  async updateProfilePicture(file) {
    if (!this.currentUser) {
      return { data: null, error: new Error('User not logged in.') };
    }
    if (!file) {
      return { data: null, error: new Error('No file provided.') };
    }

    try {
      const fileExt = file.name.split('.').pop();
      const fileName = `${this.currentUser.id}-${Date.now()}.${fileExt}`;
      const filePath = `avatars/${fileName}`;

      // Upload image to Supabase Storage in 'avatars' bucket
      const { data: uploadData, error: uploadError } = await this.supabase.storage
        .from('avatars') // Ensure 'avatars' bucket exists and has correct policies
        .upload(filePath, file, {
          cacheControl: '3600', // Cache for 1 hour
          upsert: true, // Overwrite if file with same name exists (e.g., user re-uploads)
        });

      if (uploadError) throw uploadError;

      // Get public URL for the uploaded image
      const { data: urlData } = this.supabase.storage
        .from('avatars')
        .getPublicUrl(filePath);

      if (!urlData?.publicUrl) {
        throw new Error('Could not get public URL for avatar.');
      }
      
      const avatarUrl = urlData.publicUrl;

      // Update user's profile (either user_metadata or a 'profiles' table)
      // Option 1: Update user_metadata (simpler, but less flexible for other profile fields)
      /*
      const { data: updatedUser, error: metaError } = await this.supabase.auth.updateUser({
        data: { avatar_url: avatarUrl }
      });
      if (metaError) throw metaError;
      this.currentUser = updatedUser.user; // Refresh currentUser
      */

      // Option 2: Update 'profiles' table (more robust if you have other profile fields)
      // This assumes you have a 'profiles' table with an 'avatar_url' column and RLS policies.
      const { data: profileUpdateData, error: profileUpdateError } = await this.supabase
        .from('profiles')
        .update({ avatar_url: avatarUrl, updated_at: new Date().toISOString() })
        .eq('id', this.currentUser.id)
        .select() // Important to select to get the updated profile back if needed
        .single(); // Assuming one profile per user

      if (profileUpdateError) throw profileUpdateError;
      
      // Manually trigger a UI update for the avatar if needed, or rely on onAuthStateChange if it triggers for metadata/profile updates
      // For immediate UI update:
      if (this.userAvatarImg) this.userAvatarImg.src = avatarUrl;
      if (profileUpdateData && this.userInfoEl) { // Update username display if it changed, though not expected here
         const userForUI = { ...this.currentUser, user_metadata: { ...this.currentUser.user_metadata, username: profileUpdateData.username } };
         await this._showLoggedInUI(userForUI, profileUpdateData);
      }


      return { data: { avatar_url: avatarUrl }, error: null };

    } catch (error) {
      console.error('Error updating profile picture:', error);
      return { data: null, error };
    }
  }
}

// Export factory function (Supabase instance needs to be passed from main)
export const createAuthService = (supabase) => {
  const service = new AuthService(supabase);
  // Get DOM elements and bind events after construction, ensuring DOM is ready
  // This assumes createAuthService is called after DOMContentLoaded or similar
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
      service._getDOMElements();
      service._bindAuthFormEvents();
    });
  } else {
    service._getDOMElements();
    service._bindAuthFormEvents();
  }
  return service;
};
