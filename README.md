# MyCraftingPlugin2

A comprehensive Minecraft plugin for custom crafting and alchemy systems. This plugin allows server administrators to create custom recipes with success chances, costs, and different difficulty levels.

## Features

### Custom Crafting System
- Multiple crafting categories (Upgrading, Keys, Lootboxes)
- Up to 10 required items per recipe
- Custom success chances for crafting attempts
- Economy integration with Vault
- Cost system for crafting attempts

### Alchemy System
- Four different alchemy categories:
  - Alchemy Shop
  - Tonics Crafting
  - Potions Crafting
  - Physic Crafting
- Custom recipes for each category

### Upgrade System
- 10 quality levels (q1-q10)
- Three difficulty tiers per level:
  - Infernal
  - Hell
  - Blood
- Custom recipes for each difficulty tier

### Admin Features
- In-game recipe editor
- Layout customization
- Permission-based access control
- Paginated menu system

## Requirements

- Spigot/Paper 1.20.1
- Java 8 or higher
- MySQL Database
- Vault

## Installation

1. Download the latest release from the releases page
2. Place the JAR file in your server's `plugins` folder
3. Start/restart your server
4. Configure the plugin using the generated `config.yml`

## Configuration

### Database Setup

```yaml
database:
  host: localhost
  port: 3306
  name: minecraft_crafting
  user: your_username
  password: your_password
```

## Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/crafting` | mycraftingplugin.use | Opens the main crafting menu |
| `/editcrafting` | mycraftingplugin.editlayout | Opens the crafting menu in edit mode |
| `/alchemy` | mycraftingplugin.use | Opens the alchemy menu |
| `/edit_alchemy` | mycraftingplugin.editlayout | Opens the alchemy menu in edit mode |
| `/addrecipe` | mycraftingplugin.add | Adds a new recipe to a category |

## Permissions

- `mycraftingplugin.use` - Allows using the crafting and alchemy systems
- `mycraftingplugin.editlayout` - Allows editing layouts and categories
- `mycraftingplugin.add` - Allows adding new recipes

## Recipe Creation Guide

### Creating a New Recipe

1. Use `/editcrafting` or `/edit_alchemy` to enter edit mode
2. Navigate to the desired category
3. Click the "Add Recipe" button
4. Place required items in slots 1-10
5. Place the result item in the center slot
6. Set success chance and cost
7. Click "Save" to create the recipe

### Editing Existing Recipes

1. Enter edit mode
2. Click on the recipe you want to edit
3. Modify items, success chance, or cost
4. Click "Save" to update the recipe

### Recipe Layout Management

- Drag and drop recipes in edit mode to arrange them
- Changes are automatically saved
- Use pagination buttons to navigate through recipes

## Technical Details

### Database Structure

The plugin uses MySQL to store recipes with the following structure:

```sql
CREATE TABLE recipes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    category VARCHAR(255),
    slot INT,
    required_item_1 TEXT,
    required_item_2 TEXT,
    required_item_3 TEXT,
    required_item_4 TEXT,
    required_item_5 TEXT,
    required_item_6 TEXT,
    required_item_7 TEXT,
    required_item_8 TEXT,
    required_item_9 TEXT,
    required_item_10 TEXT,
    result_item TEXT,
    success_chance DOUBLE,
    cost DOUBLE
);
```

### Connection Pooling

The plugin uses HikariCP for efficient database connection management with the following configurations:

- Maximum pool size: 10
- Minimum idle connections: 5
- Connection timeout: 10 seconds
- Idle timeout: 5 minutes
- Maximum lifetime: 30 minutes

## Troubleshooting

### Common Issues

1. Database Connection Issues
   - Verify database credentials in config.yml
   - Check if MySQL server is running
   - Ensure database exists and user has proper permissions

2. Recipe Not Saving
   - Check database connection
   - Verify all required fields are filled
   - Check console for error messages

3. Items Not Showing in Menu
   - Verify item serialization data in database
   - Check if items are valid Minecraft items
   - Ensure proper plugin permissions

### Error Messages

- "Database connection error" - Check database configuration and connectivity
- "You don't have permission" - Verify user has required permissions
- "Invalid recipe" - Recipe data might be corrupted or missing

## Support

For support:
1. Check the issues section on GitHub
2. Create a new issue with detailed information about your problem
3. Include server version and any error messages from console

## Building from Source

```bash
git clone https://github.com/yourusername/MyCraftingPlugin2.git
cd MyCraftingPlugin2
mvn clean package
```

The compiled JAR will be in the `target` directory.

## Contributing

1. Fork the repository
2. Create a new branch for your feature
3. Commit your changes
4. Create a pull request

Please ensure your code follows the existing style and includes appropriate documentation.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
