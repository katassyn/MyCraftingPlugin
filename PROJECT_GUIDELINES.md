# Minecraft Plugin Requirements Checklist

This document contains specific requirements and guidelines for our Minecraft plugin development. Use it as a quick reference when implementing features.

## Player-Facing Content

- [ ] Use only English text for all in-game messages to players
- [ ] Use consistent color codes for different message types:
  - Error messages: RED
  - Warnings: YELLOW
  - Success messages: GREEN
  - Information: AQUA
- [ ] Format all messages with proper spacing and punctuation
- [ ] Make sure all GUI item names and descriptions are in English

## Data Storage

- [ ] Use database storage instead of YAML files for player data
- [ ] Always use HikariCP for database connections for better performance
- [ ] Store items in database using proper serialization/deserialization methods
- [ ] When storing ItemStacks in database, save all properties:
  - Item ID
  - Display name
  - Lore
  - Enchantments
  - Item flags
  - Custom NBT data

## Configuration

- [ ] Place debugging flags in config.yml file, not hardcoded in classes
- [ ] Use a standardized debug section in config:
```yaml
debug:
  enabled: true
  level: 1  # 0=off, 1=basic, 2=verbose
```
- [ ] Add detailed comments to all configuration options
- [ ] Provide sensible default values for all configuration options

## Performance

- [ ] Run database operations asynchronously to prevent main thread blocking
- [ ] Use caching for frequently accessed data
- [ ] Unregister listeners when they're no longer needed
- [ ] Clean up resources properly when plugin disables
- [ ] Use task scheduling appropriately (sync vs. async)
- [ ] Profile and optimize high-frequency code paths
- [ ] Clean up timers and tasks on plugin disable
- [ ] Delay layout saving operations by 1 tick to prevent race conditions
- [ ] Batch database operations where possible
- [ ] Use UUIDs consistently for player identification
- [ ] Cache frequently accessed data for performance

## Menu Hierarchy and Navigation

- [ ] Implement consistent navigation paths through menus
- [ ] Maintain breadcrumb trail of previous menus (like your category tracking)
- [ ] Store last visited category/page for seamless navigation

## GUI Design

- [ ] Use consistent design patterns across all plugin GUIs
- [ ] Include back buttons in multi-page GUIs
- [ ] Show tooltips for items with additional information
- [ ] Implement confirmation screens for destructive actions
- [ ] Use visual separators (glass panes or other decorative blocks) to divide different sections of complex GUIs
- [ ] Standard GUI size should be 27 slots (3 rows) for simple menus and 54 slots (6 rows) for more complex menus
- [ ] Place main option items in a consistent pattern (slots 10, 13, 16 for 3-option menus)
- [ ] Fill all empty slots with WHITE_STAINED_GLASS_PANE items with empty display names (" ")
- [ ] Use consistent color coding for difficulty levels:
  - BLUE for "Infernal" (easiest)
  - DARK_BLUE for "Hell" (medium)
  - GOLD for "Bloodshed" (hardest)
- [ ] Ensure all GUI items have proper display names and lore
- [ ] Maintain consistent item positioning across similar GUIs to avoid confusing players 
- [ ] Implement consistent color coding for different function types
- [ ] Design GUI layouts with golden ratio principles for visual balance
- [ ] Scale particle effects appropriately to avoid overwhelming players
- [ ] Maintain a limited, coherent color palette (3-5 colors) for each plugin's visual identity
- [ ] Provide contextual help in GUIs with hover tooltips

## User Interaction

- [ ] Use chat for numerical/text input when GUI limitations make it necessary
- [ ] Implement a chat state system to track what input is expected from users
- [ ] Store GUI state when switching to chat input, to restore it when returning
- [ ] Provide clear instructions when requesting chat input

## Commands

- [ ] Provide tab completion for all commands
- [ ] Include help text for all commands
- [ ] Use consistent command naming patterns
- [ ] Implement proper permission checking

## Permissions

- [ ] Use hierarchical permission structure
- [ ] Document all permissions in README
- [ ] Set sensible defaults for permissions
- [ ] Test permissions with different permission plugins
- [ ] All commands should have permissions needed, DO NOT set to default any - only chat display commands can be used w/o permssions

## Error Handling

- [ ] Log all errors with appropriate severity levels
- [ ] Provide user-friendly error messages to players
- [ ] Implement graceful fallbacks for missing data
- [ ] Add detailed debugging information in logs



