#!/bin/sh

# Render provides DB_URL in the format: postgres://user:password@host:port/database
# Java JDBC requires: jdbc:postgresql://host:port/database?user=user&password=password
# OR if we pass credentials separately, just: jdbc:postgresql://host:port/database

# This script transforms the URL for the JDBC driver

if [ -n "$DB_URL" ]; then
    echo "Configuring Database URL from DB_URL..."
    
    # 1. Replace 'postgres://' or 'postgresql://' with 'jdbc:postgresql://'
    # 2. Check if user info is present and handle it (JDBC driver often prefers it stripped from URL if user/pass are separate, 
    #    but newer drivers handle it. Safe bet: use the provided separate variables or let Spring handling it)
    
    # Simple strategy: Just prepend 'jdbc:' and change scheme.
    # Because Render usually ALSO provides DB_USERNAME and DB_PASSWORD if we ask for it, 
    # OR we can just rely on the complete URL.
    
    # However, standard Render environment variable DB_URL includes the user/pass.
    # The PostgreSQL JDBC driver (newer versions) supports parsing user/pass from the URL if the scheme is correct.
    
    # Transform: postgres://... -> jdbc:postgresql://...
    CLEAN_URL=$(echo "$DB_URL" | sed -e 's/^postgres:/jdbc:postgresql:/' -e 's/^postgresql:/jdbc:postgresql:/')
    
    export SPRING_DATASOURCE_URL="$CLEAN_URL"
    echo "Set SPRING_DATASOURCE_URL to JDBC format."
fi

exec "$@"
