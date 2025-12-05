package com.ecosimulator.ui;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Icon Manager for loading and caching icons used throughout the application
 * Provides icons for predators, prey, mutations, scavengers, terrain, etc.
 */
public class IconManager {
    
    private static final Logger LOGGER = Logger.getLogger(IconManager.class.getName());
    
    // Icon paths
    private static final String ICONS_PATH = "/icons/";
    
    // Icon names mapping to files
    public static final String PREDATOR = "predator";
    public static final String PREY = "prey";
    public static final String FEMALE_PREDATOR = "femalepredator";
    public static final String FEMALE_PREY = "femaleprey";
    public static final String MUTATION = "mutation";
    public static final String SCAVENGER = "scavenger";
    public static final String TERRAIN = "terrain";
    
    // Cache for loaded images
    private static final Map<String, Image> imageCache = new HashMap<>();
    
    // Default icon sizes
    public static final int SMALL_ICON_SIZE = 16;
    public static final int MEDIUM_ICON_SIZE = 24;
    public static final int LARGE_ICON_SIZE = 32;
    public static final int CELL_ICON_SIZE = 18;
    
    private IconManager() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Initialize and preload all icons into cache
     */
    public static void preloadIcons() {
        String[] iconNames = {PREDATOR, PREY, FEMALE_PREDATOR, FEMALE_PREY, MUTATION, SCAVENGER, TERRAIN};
        for (String iconName : iconNames) {
            loadIcon(iconName);
        }
        LOGGER.info("Icons preloaded successfully");
    }
    
    /**
     * Load an icon by name
     * @param iconName the name of the icon
     * @return the loaded Image, or null if not found
     */
    public static Image loadIcon(String iconName) {
        if (imageCache.containsKey(iconName)) {
            return imageCache.get(iconName);
        }
        
        Image image = loadIconFromResources(iconName);
        if (image != null) {
            imageCache.put(iconName, image);
        }
        return image;
    }
    
    /**
     * Load icon from resources folder
     * @param iconName the name of the icon
     * @return the loaded Image or null
     */
    private static Image loadIconFromResources(String iconName) {
        // Try different extensions
        String[] extensions = {".png", ".webp", ".jpg", ".jpeg", ".gif"};
        
        for (String ext : extensions) {
            String path = ICONS_PATH + iconName + ext;
            try {
                InputStream is = IconManager.class.getResourceAsStream(path);
                if (is != null) {
                    Image image = new Image(is);
                    LOGGER.fine("Loaded icon: " + path);
                    return image;
                }
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "Could not load icon: " + path, e);
            }
        }
        
        LOGGER.warning("Icon not found: " + iconName);
        return null;
    }
    
    /**
     * Get an ImageView for a specific icon
     * @param iconName the name of the icon
     * @param size the size of the icon (width and height)
     * @return an ImageView with the icon, or null if not found
     */
    public static ImageView getIconView(String iconName, int size) {
        Image image = loadIcon(iconName);
        if (image != null) {
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(size);
            imageView.setFitHeight(size);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            return imageView;
        }
        return null;
    }
    
    /**
     * Get a small icon view (16x16)
     * @param iconName the name of the icon
     * @return an ImageView with the small icon
     */
    public static ImageView getSmallIcon(String iconName) {
        return getIconView(iconName, SMALL_ICON_SIZE);
    }
    
    /**
     * Get a medium icon view (24x24)
     * @param iconName the name of the icon
     * @return an ImageView with the medium icon
     */
    public static ImageView getMediumIcon(String iconName) {
        return getIconView(iconName, MEDIUM_ICON_SIZE);
    }
    
    /**
     * Get a large icon view (32x32)
     * @param iconName the name of the icon
     * @return an ImageView with the large icon
     */
    public static ImageView getLargeIcon(String iconName) {
        return getIconView(iconName, LARGE_ICON_SIZE);
    }
    
    /**
     * Get an icon for a cell in the grid
     * @param iconName the name of the icon
     * @return an ImageView sized for grid cells
     */
    public static ImageView getCellIcon(String iconName) {
        return getIconView(iconName, CELL_ICON_SIZE);
    }
    
    /**
     * Get the predator icon view
     * @param size the size of the icon
     * @param isFemale whether to use the female variant
     * @return an ImageView with the predator icon
     */
    public static ImageView getPredatorIcon(int size, boolean isFemale) {
        String iconName = isFemale ? FEMALE_PREDATOR : PREDATOR;
        return getIconView(iconName, size);
    }
    
    /**
     * Get the prey icon view
     * @param size the size of the icon
     * @param isFemale whether to use the female variant
     * @return an ImageView with the prey icon
     */
    public static ImageView getPreyIcon(int size, boolean isFemale) {
        String iconName = isFemale ? FEMALE_PREY : PREY;
        return getIconView(iconName, size);
    }
    
    /**
     * Get the mutation icon view
     * @param size the size of the icon
     * @return an ImageView with the mutation icon
     */
    public static ImageView getMutationIcon(int size) {
        return getIconView(MUTATION, size);
    }
    
    /**
     * Get the scavenger (third species) icon view
     * @param size the size of the icon
     * @return an ImageView with the scavenger icon
     */
    public static ImageView getScavengerIcon(int size) {
        return getIconView(SCAVENGER, size);
    }
    
    /**
     * Get the terrain icon view
     * @param size the size of the icon
     * @return an ImageView with the terrain icon
     */
    public static ImageView getTerrainIcon(int size) {
        return getIconView(TERRAIN, size);
    }
    
    /**
     * Check if an icon is available
     * @param iconName the name of the icon
     * @return true if the icon exists
     */
    public static boolean isIconAvailable(String iconName) {
        if (imageCache.containsKey(iconName)) {
            return true;
        }
        return loadIcon(iconName) != null;
    }
    
    /**
     * Clear the icon cache
     */
    public static void clearCache() {
        imageCache.clear();
        LOGGER.info("Icon cache cleared");
    }
    
    /**
     * Get the number of cached icons
     * @return the cache size
     */
    public static int getCacheSize() {
        return imageCache.size();
    }
}
