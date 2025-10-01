package com.concours.config;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig {

    @Bean
    public TomcatServletWebServerFactory tomcatFactory() {
        return new TomcatServletWebServerFactory() {
            @Override
            protected void customizeConnector(Connector connector) {
                super.customizeConnector(connector);

                // Configuration simplifiée - laissez Spring gérer les limites
                // Ces valeurs doivent être cohérentes avec application.properties
                connector.setMaxPostSize(500 * 1024 * 1024); // 500MB
                connector.setMaxSavePostSize(500 * 1024 * 1024); // 500MB

                // ⚡ SUPPRIMEZ ces lignes qui causent des conflits :
                // connector.setProperty("maxParameterCount", "10000");
                // connector.setProperty("maxFileSize", String.valueOf(100 * 1024 * 1024));
                // connector.setProperty("maxRequestSize", String.valueOf(500 * 1024 * 1024));
                // connector.setProperty("fileCountMax", "1000"); // Cette ligne cause l'erreur
            }
        };
    }
}