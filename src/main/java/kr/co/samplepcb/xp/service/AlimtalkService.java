package kr.co.samplepcb.xp.service;

import coolib.common.CCObjectResult;
import coolib.common.CCResult;
import kr.co.samplepcb.xp.config.ApplicationProperties;
import kr.co.samplepcb.xp.pojo.Alimtalk;
import kr.co.samplepcb.xp.pojo.AlimtalkResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AlimtalkService {

    private final ApplicationProperties applicationProperties;
    private final WebClient webClient;

    public AlimtalkService(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        webClient = WebClient.builder()
                .baseUrl(this.applicationProperties.getAuth().getAlimtalkIWinvUrl() + "/v2/send/")
                .defaultHeader("AUTH", applicationProperties.getAuth().getAlimtalkIWinvToken())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public CCResult sendAlimtalk(Alimtalk alimtalk) {
        alimtalk.setReSend("Y");
        alimtalk.setResendCallback(applicationProperties.getAuth().getAlimtalkIWinvResendCallback());
        alimtalk.setResendType("Y");

        AlimtalkResponse response = webClient.post()
                .body(BodyInserters.fromValue(alimtalk))
                .retrieve()
                .bodyToMono(AlimtalkResponse.class)
                .block();

        return CCObjectResult.setSimpleData(response);
    }
}
