# MyCraftingPlugin

A Minecraft plugin that introduces an advanced custom crafting system with GUI-based recipe management. Players can access crafting categories, each with unique recipes, and server admins can dynamically add or edit recipes through an in-game editor. All recipe data is stored in a configurable database.

---

## Features

### **/crafting Command**
- Opens a crafting GUI with three main categories:
  1. **Upgrade**
     - Contains subcategories from `Q1` to `Q10`.
     - Each subcategory offers crafting recipes for three difficulty levels:
       - **Infernal**
       - **Hell**
       - **Blood**
  2. **Keys**
     - A category for crafting key-related items.
  3. **Lootboxes**
     - Craft custom lootboxes with specific items.

### **/editcrafting Command**
- Opens the same GUI but with admin privileges to add or edit recipes.
- Features for recipe creation:
  - **Define input items**: Place items in the specified slots in the GUI.
  - **Set output item**: Define the resulting item for the recipe.
  - **Customize recipe properties**:
    - **Chance**: Set the success chance for crafting (in percentage).
    - **Cost**: Define the crafting cost in `$` (requires Vault plugin). 
      - Supports shorthand notations:
        - **K**: Thousands (e.g., `1K` = 1,000).
        - **KK**: Millions (e.g., `1KK` = 1,000,000).
        - **KKK**: Billions (e.g., `1KKK` = 1,000,000,000).
- All recipes are saved in a database for persistence.

---

## Configuration

### **config.yml**
Set up the MySQL database connection:
```yaml
database:
  host: host
  port: port
  name: database name
  username: username
  password: password
