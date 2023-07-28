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
        private String token = "";
        private String samplepcbSiteToken;
        private String alimtalkIWinvUrl;
        private String alimtalkIWinvToken;
        private String alimtalkIWinvResendCallback;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getSamplepcbSiteToken() {
            return samplepcbSiteToken;
        }

        public void setSamplepcbSiteToken(String samplepcbSiteToken) {
            this.samplepcbSiteToken = samplepcbSiteToken;
        }

        public String getAlimtalkIWinvUrl() {
            return alimtalkIWinvUrl;
        }

        public void setAlimtalkIWinvUrl(String alimtalkIWinvUrl) {
            this.alimtalkIWinvUrl = alimtalkIWinvUrl;
        }

        public String getAlimtalkIWinvToken() {
            return alimtalkIWinvToken;
        }

        public void setAlimtalkIWinvToken(String alimtalkIWinvToken) {
            this.alimtalkIWinvToken = alimtalkIWinvToken;
        }

        public String getAlimtalkIWinvResendCallback() {
            return alimtalkIWinvResendCallback;
        }

        public void setAlimtalkIWinvResendCallback(String alimtalkIWinvResendCallback) {
            this.alimtalkIWinvResendCallback = alimtalkIWinvResendCallback;
        }
    }

}
