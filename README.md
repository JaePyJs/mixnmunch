# Mix and Munch: Filipino Recipe Generator

Mix and Munch is a modern, mobile-first web application that helps users discover and create authentic Filipino dishes based on the ingredients they have at home. The app combines trusted local sources, community sharing, and AI-powered suggestions to ensure you always find a recipe to try.

## Overview

Mix and Munch is designed for food lovers, home cooks, and anyone interested in Filipino cuisine. Whether you want to cook with what you have, explore new dishes, or share your own recipes, Mix and Munch provides a seamless and engaging experience.

### Key Features

- **Ingredient-Based Recipe Generation:** Enter the ingredients you have, and Mix and Munch will suggest Filipino recipes from a curated database and AI-generated options.
- **Never a Dead End:** If no perfect match is found, the app generates creative recipe suggestions so you always have something to cook.
- **Source Attribution:** Every recipe clearly shows its origin—celebrity chefs, reputable food creators, trusted websites, or AI.
- **Community Sharing:** Users can create, upload, and share their own recipes with the community.
- **User Authentication:** Secure sign up, login, and profile management powered by Supabase Auth.
- **Mobile-First Design:** Fully responsive UI for a great experience on any device.
- **Nutritional Facts:** Recipes include nutritional information sourced from Open Food Facts and other APIs.

## Tech Stack

- **Frontend:** HTML, CSS, JavaScript, [Next.js](https://nextjs.org/) (App Router)
- **Backend:** Next.js API routes
- **Database:** [Supabase](https://supabase.com/) (PostgreSQL)
- **Authentication:** Supabase Auth
- **Styling:** Tailwind CSS
- **APIs:**
  - [MealDB API](https://www.themealdb.com/) for recipe data
  - [Open Food Facts](https://world.openfoodfacts.org/) for nutrition
  - Spoonacular API (planned)
  - Google/Gemini API (planned for AI recipe generation)

## Project Structure

- `index.html`, `landing.html`: Entry points for the static prototype
- `js/`: JavaScript modules (app logic, services, UI components)
- `assets/`: SVGs and images
- `css/`: Stylesheets (Tailwind, custom animations)
- `nextjs-app/`: Next.js implementation (in progress)
- `memory-bank/`: Project context, plans, and logs

## Getting Started

### Static Prototype

1. Clone this repository.
2. Open `index.html` in your browser.
3. To enable full functionality, set up API keys in `env.js`.

### Next.js App (in `nextjs-app/`)

1. Navigate to `nextjs-app/`.
2. Install dependencies:

   ```sh
   npm install
   ```

3. Start the development server:

   ```sh
   npm run dev
   ```

4. Open [http://localhost:3000](http://localhost:3000) in your browser.

## Development Status

- The static prototype is functional for demo purposes.
- The Next.js version is under active development (see `PROJECT_PLAN.md` and `REFACTORING_PLAN.md`).
- See `CHANGELOG.txt` for updates and known issues.

## Contributing

Contributions are welcome! Please see the project plans and logs in the `memory-bank/` directory for context. For major changes, open an issue first to discuss what you would like to change.

## License

This project is for demonstration and academic purposes only. Not for commercial use.
