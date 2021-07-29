package kr.co.samplepcb.xp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

    private final GlFlow glFlow = new GlFlow();
    private final MlServer mlServer = new MlServer();
    private final CorsConfiguration cors = new CorsConfiguration();
    private final Auth auth = new Auth();

    public GlFlow getGlFlow() {
        return glFlow;
    }

    public MlServer getMlServer() {
        return mlServer;
    }

    public CorsConfiguration getCors() {
        return cors;
    }

    public Auth getAuth() {
        return auth;
    }

    public static class GlFlow {
        private String serverUrl;

        public String getServerUrl() {
            return serverUrl;
        }

        public void setServerUrl(String serverUrl) {
            this.serverUrl = serverUrl;
        }
    }

    public static class MlServer {
        private String serverUrl;

        public String getServerUrl() {
            return serverUrl;
        }

        public void setServerUrl(String serverUrl) {
            this.serverUrl = serverUrl;
        }
    }

    public static class Auth {
        private String token = "6fb1af98cc1411ebb8bc0242ac130003";

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

}
