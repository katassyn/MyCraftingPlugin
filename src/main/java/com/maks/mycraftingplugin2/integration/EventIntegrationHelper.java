package com.maks.mycraftingplugin2.integration;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.*;

/**
 * Helper class to integrate with the eventPlugin.
 * Provides methods to check event status and retrieve event information.
 * Uses the EventPluginAPI instead of reading config files.
 */
public class EventIntegrationHelper {

    private static Plugin eventPlugin = null;
    private static boolean initialized = false;
    private static Class<?> apiClass = null;

    /**
     * Initialize the event integration.
     * Call this during plugin startup.
     */
    public static void initialize() {
        eventPlugin = Bukkit.getPluginManager().getPlugin("eventPlugin");

        if (eventPlugin != null && eventPlugin.isEnabled()) {
            try {
                // Try to load the API class
                apiClass = Class.forName("org.maks.eventPlugin.api.EventPluginAPI");
                initialized = true;
                Bukkit.getLogger().info("[EventIntegration] Successfully integrated with eventPlugin API!");
            } catch (ClassNotFoundException e) {
                Bukkit.getLogger().warning("[EventIntegration] eventPlugin found but API not available!");
                Bukkit.getLogger().warning("[EventIntegration] Make sure eventPlugin is updated with API support.");
            }
        } else {
            Bukkit.getLogger().info("[EventIntegration] eventPlugin not found. Event filtering disabled.");
        }
    }

    /**
     * Check if event integration is available.
     * @return True if eventPlugin is available and configured
     */
    public static boolean isAvailable() {
        return initialized && eventPlugin != null && eventPlugin.isEnabled() && apiClass != null;
    }

    /**
     * Get all event IDs from the eventPlugin API.
     * @return List of event IDs, empty if integration not available
     */
    public static List<String> getAllEventIds() {
        if (!isAvailable()) {
            return new ArrayList<>();
        }

        try {
            @SuppressWarnings("unchecked")
            List<String> ids = (List<String>) apiClass.getMethod("getAllEventIds").invoke(null);
            return ids != null ? ids : new ArrayList<>();
        } catch (Exception e) {
            Bukkit.getLogger().warning("[EventIntegration] Error getting event IDs: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get event name by event ID.
     * @param eventId The event ID
     * @return The event name, or the eventId if name not found
     */
    public static String getEventName(String eventId) {
        if (!isAvailable() || eventId == null) {
            return eventId;
        }

        try {
            String name = (String) apiClass.getMethod("getEventName", String.class).invoke(null, eventId);
            return name != null ? name : eventId;
        } catch (Exception e) {
            Bukkit.getLogger().warning("[EventIntegration] Error getting event name: " + e.getMessage());
            return eventId;
        }
    }

    /**
     * Get event description by event ID.
     * @param eventId The event ID
     * @return The event description, or empty string if not found
     */
    public static String getEventDescription(String eventId) {
        if (!isAvailable() || eventId == null) {
            return "";
        }

        try {
            String desc = (String) apiClass.getMethod("getEventDescription", String.class).invoke(null, eventId);
            return desc != null ? desc : "";
        } catch (Exception e) {
            Bukkit.getLogger().warning("[EventIntegration] Error getting event description: " + e.getMessage());
            return "";
        }
    }

    /**
     * Check if an event is currently active.
     * @param eventId The event ID to check
     * @return True if the event is active, false otherwise
     */
    public static boolean isEventActive(String eventId) {
        if (!isAvailable() || eventId == null) {
            return false;
        }

        try {
            Boolean active = (Boolean) apiClass.getMethod("isEventActive", String.class).invoke(null, eventId);
            return active != null && active;
        } catch (Exception e) {
            Bukkit.getLogger().warning("[EventIntegration] Error checking event active status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get all events as a map with their names and active status.
     * @return Map of event ID -> Event info (name and active status)
     */
    public static Map<String, EventInfo> getAllEvents() {
        Map<String, EventInfo> events = new LinkedHashMap<>();

        if (!isAvailable()) {
            return events;
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, ?> apiEvents = (Map<String, ?>) apiClass.getMethod("getAllEvents").invoke(null);

            if (apiEvents != null) {
                for (Map.Entry<String, ?> entry : apiEvents.entrySet()) {
                    Object eventInfoObj = entry.getValue();

                    // Get data from API EventInfo object using reflection
                    String id = (String) eventInfoObj.getClass().getMethod("getId").invoke(eventInfoObj);
                    String name = (String) eventInfoObj.getClass().getMethod("getName").invoke(eventInfoObj);
                    String description = (String) eventInfoObj.getClass().getMethod("getDescription").invoke(eventInfoObj);
                    Boolean active = (Boolean) eventInfoObj.getClass().getMethod("isActive").invoke(eventInfoObj);

                    events.put(entry.getKey(), new EventInfo(id, name, description, active));
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("[EventIntegration] Error getting all events: " + e.getMessage());
            e.printStackTrace();
        }

        return events;
    }

    /**
     * Reload the event configuration.
     * Not needed when using API - API always returns current state.
     */
    public static void reload() {
        // No-op when using API - API always returns fresh data
        Bukkit.getLogger().info("[EventIntegration] Reload called - using live API data");
    }

    /**
     * Class to hold event information.
     */
    public static class EventInfo {
        private final String id;
        private final String name;
        private final String description;
        private final boolean active;

        public EventInfo(String id, String name, String description, boolean active) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.active = active;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public boolean isActive() {
            return active;
        }
    }
}
