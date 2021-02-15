package kr.co.samplepcb.xp.service.common.sub;

import coolib.common.CCObjectResult;
import kr.co.samplepcb.xp.config.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Service
public class GoogleTensorService {

    private static final Logger log = LoggerFactory.getLogger(GoogleTensorService.class);

    private final ApplicationProperties appProp;
    private final String glFlowUrl;

    public GoogleTensorService(ApplicationProperties appProp) {
        this.appProp = appProp;
        this.glFlowUrl = appProp.getGlFlow().getServerUrl();
    }


    public CCObjectResult<List<Double>> requestDoc2vector(String text) {

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.set("q", text);

        Mono<CCObjectResult<List<Double>>> requestSpec = WebClient
                .create()
                .method(HttpMethod.POST)
                .uri(glFlowUrl + "/doc2vect")
                .body(BodyInserters.fromFormData(formData))
                .retrieve().bodyToMono(new ParameterizedTypeReference<CCObjectResult<List<Double>>>() {})
                .timeout(Duration.ofSeconds(10));

        return requestSpec.block();
    }

    public CCObjectResult<List<List<Double>>> requestDocs2vectors(List<String> textList) {

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        for (String contents : textList) {
            formData.add("q[]", contents);
        }

        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 10 * 1000)).build();
        WebClient webClient = WebClient.builder().exchangeStrategies(exchangeStrategies).build();

        Mono<CCObjectResult<List<List<Double>>>> requestSpec = webClient
                .method(HttpMethod.POST)
                .uri(glFlowUrl + "/docs2vects")
                .body(BodyInserters.fromFormData(formData))
                .retrieve().bodyToMono(new ParameterizedTypeReference<CCObjectResult<List<List<Double>>>>() {});

        return requestSpec.block();
    }

    public CCObjectResult<List<Double>> requestEmbedScore(String target, List<String> targetList) {

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.set("target", target);
        for (String contents : targetList) {
            formData.add("targetList[]", contents);
        }

        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 10 * 1000)).build();
        WebClient webClient = WebClient.builder().exchangeStrategies(exchangeStrategies).build();

        Mono<CCObjectResult<List<Double>>> requestSpec = webClient
                .method(HttpMethod.POST)
                .uri(glFlowUrl + "/embedScore")
                .body(BodyInserters.fromFormData(formData))
                .retrieve().bodyToMono(new ParameterizedTypeReference<CCObjectResult<List<Double>>>() {});

        return requestSpec.block();
    }


}
