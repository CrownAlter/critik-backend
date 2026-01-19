package com.application.critik.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String defaultUrl;

    @Value("${spring.datasource.username}")
    private String defaultUsername;

    @Value("${spring.datasource.password}")
    private String defaultPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        String dbUrl = System.getenv("DB_URL");
        String username = System.getenv("DB_USERNAME");
        String password = System.getenv("DB_PASSWORD");

        HikariConfig config = new HikariConfig();

        if (StringUtils.hasText(dbUrl)) {
            // Handle Render's postgresql:// URL format
            if (dbUrl.startsWith("postgresql://")) {
                try {
                    URI uri = new URI(dbUrl);
                    String host = uri.getHost();
                    int port = uri.getPort();
                    String path = uri.getPath(); // /dbname

                    // Parse user info if present in URL (Render usually provides it separately but
                    // sometimes in URL)
                    if (uri.getUserInfo() != null) {
                        String[] auth = uri.getUserInfo().split(":");
                        username = auth[0];
                        password = auth.length > 1 ? auth[1] : "";
                    }

                    // Construct JDBC URL: jdbc:postgresql://host:port/dbname
                    String jdbcUrl = String.format("jdbc:postgresql://%s:%d%s", host, port, path);
                    config.setJdbcUrl(jdbcUrl);
                } catch (URISyntaxException e) {
                    throw new RuntimeException("Invalid DB_URL: " + dbUrl, e);
                }
            } else {
                // Assume it's already a JDBC URL
                config.setJdbcUrl(dbUrl);
            }
        } else {
            // Fallback to application.properties
            config.setJdbcUrl(defaultUrl);
        }

        // Set credentials (env vars take precedence over URL parsed info if strictly
        // provided, or properties)
        if (StringUtils.hasText(username)) {
            config.setUsername(username);
        } else {
            config.setUsername(defaultUsername);
        }

        if (StringUtils.hasText(password)) {
            config.setPassword(password);
        } else {
            config.setPassword(defaultPassword);
        }

        config.setDriverClassName("org.postgresql.Driver");

        // Set other HikariCP settings from properties or defaults can be added here if
        // needed
        // For now, we trust default Hikari defaults or spring.datasource.hikari.*
        // properties
        // Note: Spring Boot's auto-configuration of Hikari properties might be bypassed
        // if we manually create DataSource,
        // so we should ideally bind properties. But for solving the connection URL
        // issue, this is sufficient.

        return new HikariDataSource(config);
    }
}
