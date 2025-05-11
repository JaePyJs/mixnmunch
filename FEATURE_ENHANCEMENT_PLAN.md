# Feature Enhancement Plan - Phase 1

**Date:** 2025-05-11

**Overall Goal for New Features:** Address user feedback regarding profile pictures, landing page experience, and UI modernization, preparing the app for a mobile (Android) context.

**Phase 1 Goals:**

1.  Integrate the landing page experience into `index.html`, making it the default view for logged-out users.
2.  Implement a blank/default avatar for guest (logged-out) users in the main app header.
3.  Allow logged-in users to upload a profile picture.
4.  Store and retrieve the user's profile picture URL (e.g., in Supabase user metadata or a `profiles` table).
5.  Display the user's custom avatar in the header.
6.  Begin modernizing the integrated landing view with initial UI/UX improvements and microanimations, suitable for a mobile Android app.

---

## Detailed Steps for Phase 1:

1.  **Modify `index.html` Structure for Landing/App Views:**
    *   **Action:** Read `index.html`.
    *   **Action:** Identify the main application container (likely `<div class="mobile-container">`).
    *   **Action:** Create a new top-level `div` (e.g., `<div id="landing-view" class="hidden">...</div>`) that will contain the landing page content.
    *   **Action:** The existing `<div class="mobile-container">` will become the "app view" and should also start hidden or be controlled by JavaScript to be shown after login.
    *   **Rationale:** Allows `MixAndMunchApp` to toggle visibility between landing and main app views based on auth state.

2.  **Transfer Content from `landing.html` to `index.html`:**
    *   **Action:** Read `landing.html`.
    *   **Action:** Copy the relevant HTML structure, content, and any specific CSS class names for the landing page sections (hero, features, CTA, etc.) into the new `<div id="landing-view">` in `index.html`.
    *   **Action:** Ensure any CSS specific to `landing.html` is merged into `css/mobile-app.css` or linked appropriately in `index.html`.
    *   **Rationale:** Consolidates all UI into a single entry point.

3.  **Update `AuthService` and `MixAndMunchApp` for View Toggling:**
    *   **Action (AuthService - `js/services/auth-service.js`):**
        *   Modify `_handleUserSignedIn` to hide `#landing-view` and show the main app view (e.g., `#mobile-container`).
        *   Modify `_handleUserSignedOut` to show `#landing-view` and hide the main app view.
    *   **Action (MixAndMunchApp - `js/main.js`):**
        *   Ensure it correctly initializes and uses `AuthService`. The auth state change listener in `MixAndMunchApp` will primarily handle app-level class changes (like `user-logged-in` on `body`) while `AuthService` handles the direct view toggling.
    *   **Rationale:** Centralizes control of what view is displayed based on authentication status.

4.  **Implement Default/Blank Avatar for Guests:**
    *   **Action (AuthService - `js/services/auth-service.js`):**
        *   In `_showLoggedOutUI`, ensure the user avatar element in the header (e.g., `#user-avatar img` in `index.html`) is set to a default placeholder/blank image or styled to appear as a generic icon (e.g., by removing `src` or setting it to a placeholder).
    *   **Rationale:** Fulfills the requirement for guest user avatar display.

5.  **Profile Picture Upload Functionality:**
    *   **Action (HTML - `index.html`):**
        *   In the "Profile" tab/section (within the main app view), add a file input and button for "Change Profile Picture." This can reuse or adapt the structure from the existing recipe image upload.
    *   **Action (FileUpload - `js/components/file-upload.js`):**
        *   A new instance of `FileUpload` will be created by `MixAndMunchApp` (or a dedicated `ProfileComponent`) specifically for profile pictures. Consider `cropperOptions` like `aspectRatio: 1/1`.
    *   **Action (AuthService - `js/services/auth-service.js`):**
        *   Add a new method `async updateProfilePicture(file)`. This method will:
            *   Upload the file to a specific Supabase Storage bucket (e.g., `avatars`). Path should be user-specific, e.g., `avatars/${userId}/${fileName}`.
            *   Get the public URL.
            *   Update the user's record (either `auth.user.user_metadata.avatar_url` or a `profiles` table column `avatar_url`) with this new URL.
    *   **Action (MixAndMunchApp/ProfileComponent - `js/main.js` or new Profile Component):**
        *   Bind an event to the new "Change Profile Picture" button/area.
        *   On file selection/cropping (via the dedicated `FileUpload` instance), call `authService.updateProfilePicture(file)`.
        *   On success, refresh the displayed avatar (likely by re-fetching user profile or directly updating the image src).
    *   **Rationale:** Enables users to personalize their profiles.

6.  **Display Custom User Avatar:**
    *   **Action (AuthService - `js/services/auth-service.js`):**
        *   Modify `_showLoggedInUI` to fetch/use the `avatar_url` from the user's profile data (obtained via `getUserProfile`).
        *   If an `avatar_url` exists, set the `src` of the header avatar image element to this URL. Otherwise, display a default (e.g., initials-based or generic icon, potentially generated from username/email).
    *   **Rationale:** Shows the user's chosen picture.

7.  **Initial Landing View UI/UX Modernization (within `index.html` and `css/mobile-app.css`):**
    *   **Action:** Review the transferred landing page content within `#landing-view`.
    *   **Action:** Apply modern styling: update typography, spacing, color palette (respecting "Filipino-inspired" theme), and imagery.
    *   **Action:** Identify 2-3 key areas for initial microanimations (e.g., hero text entrance, feature card hover/reveal, button interactions) using CSS transitions/animations.
    *   **Action:** Ensure clear calls to action (e.g., "Sign Up," "Log In," "Explore as Guest").
    *   **Rationale:** Addresses the request for a more "lively," "super modernized" landing experience, keeping mobile Android context in mind.
---