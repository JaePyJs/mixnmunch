# Project Plan: Mix and Munch - Local Filipino Recipe Generator

**Core Technologies:**
*   **Frontend & Backend Framework:** Next.js (using App Router or Pages Router - App Router is more modern)
*   **Database & Authentication:** Supabase (PostgreSQL, Auth, Storage for images)
*   **Styling:** Tailwind CSS (recommended for rapid, modern UI development with Next.js) or CSS Modules.
*   **API Integrations (via Next.js API routes):**
    *   Recipe Sourcing: Spoonacular, TheMealDB, Google Gemini (or other AI model API)
    *   Nutritional Info: Open Food Facts API

**Overall Architecture Vision:**

```mermaid
graph TD
    User[End User] -->|Interacts with| NextJS[Next.js Frontend (Client-Side Pages & Components)]
    NextJS -->|API Calls for Data/Auth| NextJS_API[Next.js API Routes (Server-Side Logic)]
    NextJS_API -->|Auth, DB Operations| Supabase[Supabase (Auth, Database, Storage)]
    NextJS_API -->|Fetches Recipes| ExternalAPIs[External Recipe APIs (Spoonacular, MealDB, Gemini)]
    NextJS_API -->|Fetches Nutrition| NutritionAPI[Open Food Facts API]
    Supabase -->|Stores User Data, Recipes| PostgreSQL[Supabase PostgreSQL DB]
    Supabase -->|Stores User Avatars, Recipe Images| Storage[Supabase Storage]

    subgraph "User Interface (Next.js Pages)"
        LandingPage["Landing Page (/)"]
        AppDashboard["App Dashboard (/app) - Recipe Search, Display"]
        RecipeDetailsPage["Recipe Details Page (/app/recipe/[id])"]
        UserProfilePage["User Profile & My Recipes (/app/profile)"]
        UploadRecipePage["Upload Recipe Page (/app/upload)"]
        AuthPages["Login/Signup Pages (/auth/login, /auth/signup)"]
        ChangelogPage["Changelog Page (/changelog)"]
    end

    NextJS --- LandingPage
    NextJS --- AppDashboard
    NextJS --- RecipeDetailsPage
    NextJS --- UserProfilePage
    NextJS --- UploadRecipePage
    NextJS --- AuthPages
    NextJS --- ChangelogPage
```

**Phase 1: Project Setup & Core Foundation**

1.  **Initialize Next.js Project:**
    *   Set up a new Next.js project (e.g., `npx create-next-app@latest mix-and-munch`).
    *   Choose options for TypeScript (recommended for larger projects, but can start with JS if preferred for simplicity initially), ESLint, Tailwind CSS.
2.  **Set up Supabase Project:**
    *   Create a new project on [supabase.com](https://supabase.com).
    *   Note down Project URL and `anon` key.
    *   **Database Schema Design (Initial):**
        *   `users`: (Handled by Supabase Auth, can add `profiles` table linked to `auth.users`).
        *   `profiles`: `id (UUID, FK to auth.users)`, `username`, `avatar_url`, `created_at`.
        *   `recipes`: `id (UUID, PK)`, `name`, `description`, `ingredients (JSONB or TEXT[])`, `instructions (TEXT)`, `category`, `prep_time`, `cook_time`, `servings`, `source_type (ENUM: 'user', 'chef', 'website', 'ai')`, `source_name (TEXT)`, `source_url (TEXT)`, `image_url (TEXT)`, `user_id (UUID, FK to auth.users, nullable)`, `created_at`, `updated_at`, `nutrition_info (JSONB, nullable)`.
        *   `recipe_ratings` (Optional, for later): `id`, `recipe_id`, `user_id`, `rating`, `comment`.
        *   `changelog_entries`: `id`, `version`, `date`, `title`, `description (Markdown/TEXT)`.
    *   Enable Supabase Auth (Email/Password, consider social logins later).
3.  **Integrate Supabase with Next.js:**
    *   Install Supabase client library (`npm install @supabase/supabase-js`).
    *   Set up Supabase client instance and environment variables for keys.
    *   Implement helper functions for Supabase interactions.
4.  **Basic Project Structure:**
    *   `app/` (for App Router) or `pages/` (for Pages Router).
    *   `components/`: Reusable UI components (e.g., Button, Card, Navbar, RecipeCard).
    *   `lib/` or `utils/`: Helper functions, Supabase client setup.
    *   `public/`: Static assets (images, SVGs).
    *   `styles/`: Global styles, Tailwind config.
5.  **Global Layout Component:**
    *   Create a root layout (for App Router) or `_app.js` / `_document.js` (for Pages Router).
    *   Include a basic Navbar (placeholder) and Footer.

**Phase 2: Landing Page & Static Content**

1.  **Landing Page (`app/page.js` or `pages/index.js`):**
    *   **Hero Section:**
        *   Headline: "Discover Trusted Filipino Recipes, Curated by the Experts"
        *   Paragraph: App explanation.
        *   "Get Started" / "Try Now" button (links to `/app` or signup page).
    *   **Alternating Feature Sections (3x):**
        *   Use a reusable component for these sections to alternate image/text.
        *   Content for "Trusted Filipino Chefs," "Curated by Top Food Websites," "AI-Powered Recipe Suggestions & Nutrition."
        *   Use placeholder SVGs/images.
    *   **Final CTA Section:**
        *   Headline: "Ready to Cook Like a Filipino Pro?"
        *   Supporting text.
        *   "Get Started" button.
    *   **Trust Elements:** Placeholder logos/testimonials.
2.  **Changelog Page (`app/changelog/page.js` or `pages/changelog.js`):**
    *   Fetch entries from Supabase `changelog_entries` table (or initially from a local Markdown/JSON file for simplicity).
    *   Display in chronological order.
3.  **Styling & Design:**
    *   Implement mobile-first design using Tailwind CSS.
    *   Incorporate Filipino-inspired color palette.
    *   Focus on clean, modern aesthetics.
    *   **Microanimations:** Plan for subtle hover effects, page transitions. Floating SVGs can be implemented with CSS animations or a small JS library like GSAP (if complexity is acceptable).

**Phase 3: Authentication & User Management**

1.  **Supabase Auth Setup:**
    *   Configure email/password authentication in Supabase dashboard.
    *   Set up email templates for verification, password reset.
2.  **Auth Pages/Components:**
    *   Sign-up page/modal (`app/auth/signup/page.js`).
    *   Login page/modal (`app/auth/login/page.js`).
    *   Forms for email, password (and username for signup).
    *   API routes in Next.js (`app/api/auth/...`) to handle Supabase `signUp` and `signInWithPassword`.
    *   Client-side logic to call these API routes.
3.  **Session Management:**
    *   Use Supabase's session handling.
    *   Create an Auth context/provider in Next.js to manage user state globally.
    *   Update UI based on auth state (e.g., show "Login" or "Profile/Logout" in Navbar).
4.  **Logout Functionality:**
    *   Button to call Supabase `signOut`.
5.  **Protected Routes:**
    *   Implement logic to redirect unauthenticated users from pages like `/app/profile` or `/app/upload`.
6.  **User Profile Page (`app/profile/page.js`):**
    *   Display basic user info (username, email).
    *   Placeholder for "My Recipes."
    *   Option to update profile (future enhancement).

**Phase 4: Core Recipe Functionality (App Dashboard)**

1.  **App Dashboard Page (`app/dashboard/page.js` or similar):**
    *   This will be the main screen after the landing page.
    *   **Ingredient Input:** A form field (e.g., text area, tag input) for users to list available ingredients.
    *   **Recipe Display Area:** Grid or list to show recipe cards.
    *   Filters/Sort options (future enhancement).
2.  **Recipe Generation Logic (Next.js API Route - e.g., `app/api/recipes/generate/route.js`):**
    *   Accepts user ingredients as input.
    *   **Sourcing Strategy:**
        1.  **Search User-Uploaded Recipes:** Query Supabase `recipes` table where `source_type = 'user'` matching ingredients.
        2.  **Search Curated Recipes (Chefs/Websites):**
            *   *Initial Approach:* Hardcode a small set of trusted recipes directly in the API or a JSON file.
            *   *Long-term:* Create an admin interface or script to populate these into the Supabase `recipes` table with `source_type = 'chef'` or `'website'`.
        3.  **AI Generation (Fallback):**
            *   If no matches from above, call external recipe APIs (Spoonacular, TheMealDB, Gemini).
            *   Construct prompts for AI based on user ingredients and Filipino cuisine context.
            *   **"Never show error":** If direct AI match fails, broaden search or use AI to suggest a creative dish with the ingredients.
    *   **Nutrition Info:** For each recipe (especially AI-generated or if not pre-filled), call Open Food Facts API to fetch nutritional data.
    *   **Formatting:** Standardize recipe data structure before sending to frontend.
    *   Store AI-generated recipes in Supabase `recipes` table with `source_type = 'ai'` for caching/reuse (optional).
3.  **Recipe Card Component:**
    *   Display recipe image (placeholder if none), name, source, short description, key ingredients.
    *   Link to Recipe Details Page.
4.  **Recipe Details Page (`app/recipe/[id]/page.js`):**
    *   Fetch full recipe details from Supabase (or generate if it's an on-the-fly AI recipe not yet stored).
    *   Display: Name, image, full ingredients list, step-by-step instructions, source, nutritional information, prep/cook time, servings.
    *   **Variations:** If similar recipes exist (e.g., "Adobo Chicken" vs. "Adobo Pork"), logic to link or suggest them. This might involve tagging or smart searching.

**Phase 5: User Recipe Upload & Sharing**

1.  **Upload Recipe Page/Form (`app/upload/page.js`):**
    *   Fields: Recipe Name, Description, Ingredients (textarea, structured input), Instructions (textarea), Category, Prep Time, Cook Time, Servings, Image Upload.
    *   Client-side validation.
2.  **Image Upload to Supabase Storage:**
    *   When user selects an image, upload it to a Supabase Storage bucket (e.g., `recipe-images`).
    *   Store the public URL of the image.
3.  **API Route for Saving Recipe (`app/api/recipes/upload/route.js`):**
    *   Accepts recipe data from the form.
    *   Requires authentication (get `user_id` from session).
    *   Save recipe data (including image URL) to Supabase `recipes` table with `source_type = 'user'`.
4.  **Displaying User Recipes:**
    *   User's own recipes on their Profile Page.
    *   Include user-uploaded recipes in the general recipe search/generation results.

**Phase 6: Enhancements & Polish**

1.  **Advanced Search & Filtering:**
    *   Filter recipes by category, ingredients, cooking time, source.
2.  **Ratings & Reviews:**
    *   Allow users to rate and comment on recipes.
3.  **"Floating SVGs" & Advanced Microanimations:**
    *   Implement the requested floating SVG animations (e.g., small food icons gently drifting in the background of sections). This can be done with CSS animations or JavaScript animation libraries if more complex interactions are needed.
4.  **Copywriting:**
    *   Review all text content to ensure it's persuasive, expert-sounding, and highlights trust, authenticity, and community.
5.  **Responsive Design Testing:**
    *   Thoroughly test on various mobile, tablet, and desktop screen sizes.
6.  **Performance Optimization:**
    *   Image optimization.
    *   Code splitting (Next.js handles much of this).
    *   Lazy loading for images and components.

**Changelog Management:**
*   **Initial:** A simple array of objects in a `changelog.json` file in the project, imported and rendered on the changelog page.
*   **Better:** Store changelog entries in the `changelog_entries` table in Supabase. Create a simple internal way to add new entries (could even be direct DB insertion initially, or a very basic admin form if time permits later).