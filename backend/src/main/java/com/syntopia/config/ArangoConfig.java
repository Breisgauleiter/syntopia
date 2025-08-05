package com.syntopia.config;

import com.arangodb.ArangoDB;
import com.arangodb.springframework.annotation.EnableArangoRepositories;
import com.arangodb.springframework.config.ArangoConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ArangoDB Configuration with Spring Data Integration
 * 
 * Configures the connection to ArangoDB instance with TAO (Objects, Associations, Search) architecture.
 * This configuration uses Spring Data ArangoDB for better integration and repository support.
 */
@Configuration
@EnableArangoRepositories(basePackages = "com.syntopia.repository")
public class ArangoConfig implements ArangoConfiguration {

    @Value("${arangodb.host:localhost}")
    private String host;

    @Value("${arangodb.port:8529}")
    private int port;

    @Value("${arangodb.database:syntopia}")
    private String database;

    @Value("${arangodb.username:root}")
    private String username;

    @Value("${arangodb.password:syntopia123}")
    private String password;

    @Value("${arangodb.timeout:30000}")
    private int timeout;

    @Value("${arangodb.useSsl:false}")
    private boolean useSsl;

    @Override
    @Bean
    public ArangoDB.Builder arango() {
        return new ArangoDB.Builder()
                .host(host, port)
                .user(username)
                .password(password)
                .timeout(timeout)
                .useSsl(useSsl);
    }

    @Override
    public String database() {
        return database;
    }
}
