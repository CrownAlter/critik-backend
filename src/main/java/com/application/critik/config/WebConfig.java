package com.application.critik.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration for the Critik application.
 * 
 * Configures:
 * 1. Static resource handling for uploaded artwork images
 * 2. CORS settings for cross-origin requests (development/frontend integration)
 * 
 * The upload directory is configurable via application.properties:
 * file.upload-dir=uploads (default)
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * Configure static resource handler for uploaded files.
     * Maps /uploads/** URLs to the file system directory where images are stored.
     * 
     * Example: GET /uploads/uuid_filename.jpg serves from {uploadDir}/uuid_filename.jpg
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }

    /**
     * Configure CORS mappings for cross-origin requests.
     * Allows frontend applications (e.g., Angular, React) to access the API.
     * 
     * PRODUCTION: Update allowedOrigins to your production frontend domain.
     * Consider using environment variables for different environments.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Apply CORS to all endpoints
                .allowedOrigins("http://localhost:4200") // Allow Angular dev server
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
