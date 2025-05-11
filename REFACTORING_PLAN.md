# Refactoring Plan for Mix and Munch Application

**Date:** 2025-05-11

**Overall Goal:** Transition from the current mixed state (monolithic root `main.js` + emerging modules) to a fully modular architecture orchestrated by `MixAndMunchApp` (presumably in `js/main.js`), significantly reducing the complexity and responsibilities of the root `main.js`.

## Proposed Refactoring Plan:

1.  **Establish `js/main.js` as the Single JavaScript Entry Point:**
    *   **Action:** Modify `index.html` to remove the script tag for the root `main.js` (e.g., `<script src="main.js"></script>`).
    *   **Action:** Ensure `index.html` loads `js/main.js` as the primary module script (e.g., `<script type="module" src="js/main.js"></script>`). This script will be responsible for initializing the `MixAndMunchApp`.
    *   **Action:** Remove the inline authentication script block (lines 34-43) from `index.html`. All auth logic will be handled within the JavaScript modules.
    *   **Rationale:** Centralizes application startup, clarifies the loading sequence, and removes scattered JavaScript logic from the HTML.

2.  **Empower `MixAndMunchApp` (in `js/main.js`) as the Orchestrator:**
    *   **Action:** If the `MixAndMunchApp` class in `js/main.js` doesn't exist or is incomplete, it will be created/fleshed out.
    *   **Action:** This class will be responsible for:
        *   Initializing and holding instances of all core services (`AuthService`, `MealDbService`).
        *   Initializing and managing all primary UI components (`TabNavigation`, `RecipeGenerator`, `CommunityRecipes`, `FileUpload`, a new `ProfileComponent` if needed).
        *   Handling the main application setup sequence (e.g., check auth status, load initial data, render initial UI).
    *   **Rationale:** Provides a clear central point for application control and state management, promoting better organization.

3.  **Consolidate Authentication Logic into `AuthService`:**
    *   **Action:** All Supabase authentication calls (signup, login, logout, session management, profile fetching/creation) currently in the root `main.js` (approx. lines 25-315) will be moved into `js/services/auth-service.js`.
    *   **Action:** The `AuthService` will be responsible for initializing the Supabase client using configuration from `env.js`.
    *   **Action:** UI interactions related to auth (displaying forms, messages, user info) will be handled by the relevant component (e.g., a `ProfileComponent` or directly within the "Profile" tab logic managed by `MixAndMunchApp` or `TabNavigation`).
    *   **Rationale:** Centralizes all authentication concerns, making it easier to manage, update, and secure. Removes redundant auth logic.

4.  **Centralize API Interactions in `MealDbService`:**
    *   **Action:** All interactions with external recipe APIs (like TheMealDB), currently handled by the `mealDbService` object and related functions in the root `main.js` (approx. lines 634-842), will be moved into `js/services/api-service.js` (which contains `MealDbService`).
    *   **Rationale:** Creates a dedicated service for external data fetching, improving separation of concerns.

5.  **Refine and Integrate UI Components:**
    *   **Action:** Ensure `RecipeGenerator` (`js/components/recipe-generator.js`), `CommunityRecipes` (`js/components/community-recipes.js`), and `FileUpload` (`js/components/file-upload.js`) are fully self-contained. They should manage their own specific DOM elements and interactions.
    *   **Action:** These components will use `AuthService` (for user context if needed) and `MealDbService` (or other data services) to fetch and submit data, rather than containing direct API call logic.
    *   **Action:** Logic related to these features in the root `main.js` (e.g., recipe upload lines 317-589, AI recipe generation lines 1831-2139) will be removed or fully migrated to the respective components.
    *   **Rationale:** Promotes reusability and testability of UI components.

6.  **Systematically Reduce and Eliminate Root `main.js` Content:**
    *   **Action:** Gradually move all remaining functional logic (DOM manipulation, event handling, utility functions not already in `js/utils/`) from the root `main.js` into appropriate services, components, or utility modules.
    *   **Goal:** The root `main.js` file should ideally become empty or be deleted if `js/main.js` completely takes over as the entry point.
    *   **Rationale:** Achieves the primary goal of eliminating the monolithic script.

7.  **Ensure Consistent and Secure Configuration:**
    *   **Action:** All API keys, Supabase URLs, and other environment-specific configurations must be loaded exclusively from `env.js` and accessed only by the necessary services (e.g., `AuthService` for Supabase keys, `MealDbService` for its API key).
    *   **Rationale:** Centralizes configuration, improves security by not hardcoding keys in multiple places, and makes it easier to manage different environments.

## Visualizing the New Architecture:

```mermaid
graph TD
    A[index.html] -->|loads only| B(js/main.js);
    B -->|initializes & orchestrates| C{MixAndMunchApp};

    C -->|uses| D[AuthService];
    C -->|uses| E[MealDbService];
    C -->|manages| F[TabNavigation Component];
    C -->|manages| G[RecipeGenerator Component];
    C -->|manages| H[CommunityRecipes Component];
    C -->|manages| I[FileUpload Component];
    C -->|manages| J[ProfileComponent (handles auth UI & recipe uploads)];

    D -->|interacts with| K[Supabase Auth];
    D -->|manages| L[User Profile Data (Supabase DB)];
    E -->|interacts with| P[External Recipe APIs (e.g., MealDB)];
    H -->|uses| E;
    H -->|interacts with| M[Community Recipes (Supabase DB)];
    I -->|interacts with| N[Recipe Images (Supabase Storage)];

    subgraph CoreApplication
        C
    end

    subgraph Services
        direction LR
        D
        E
    end

    subgraph UIComponents
        direction LR
        F
        G
        H
        I
        J
    end

    subgraph ExternalSystems
        direction LR
        K
        L
        M
        N
        P
    end

    classDef app fill:#f9f,stroke:#333,stroke-width:2px;
    classDef service fill:#9cf,stroke:#333,stroke-width:2px;
    classDef component fill:#9f9,stroke:#333,stroke-width:2px;
    classDef external fill:#ccc,stroke:#333,stroke-width:2px;

    class C app;
    class D,E service;
    class F,G,H,I,J component;
    class K,L,M,N,P external;