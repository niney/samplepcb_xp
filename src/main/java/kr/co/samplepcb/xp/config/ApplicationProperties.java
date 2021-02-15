package kr.co.samplepcb.xp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

    private final GlFlow glFlow = new GlFlow();
    private final CorsConfiguration cors = new CorsConfiguration();

    public GlFlow getGlFlow() {
        return glFlow;
    }

    public CorsConfiguration getCors() {
        return cors;
    }

    public class GlFlow {
        private String serverUrl;

        public String getServerUrl() {
            return serverUrl;
        }

        public void setServerUrl(String serverUrl) {
            this.serverUrl = serverUrl;
        }
    }

}
