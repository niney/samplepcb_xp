package kr.co.samplepcb.xp.util;

import coolib.util.CommonUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CoolWebClientUtils {

    @SuppressWarnings("rawtypes")
    public static <T> Map requestForm(String url, MultiValueMap<String, String> params) {

        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 10 * 1000)).build();
        WebClient.Builder webClientBuilder = WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchangeStrategies(exchangeStrategies);

        WebClient webClient = webClientBuilder.build();


        Mono<Map> requestSpec = webClient
                .method(HttpMethod.POST)
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromFormData(params))
                .retrieve()
                .bodyToMono(String.class)
                .map(s -> {
                    try {
                        return CommonUtils.getObjectMapper().readValue(s, Map.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return Collections.emptyMap();
                    }
                });

        return requestSpec.block();

    }

    public static <T> Map requestPost(String url, String json) {

        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 10 * 1000)).build();
        WebClient.Builder webClientBuilder = WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchangeStrategies(exchangeStrategies);

        WebClient webClient = webClientBuilder.build();

        Mono<Map> requestSpec = webClient
                .method(HttpMethod.POST)
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(json))
                .retrieve()
                .bodyToMono(String.class)
                .map(s -> {
                    try {
                        return CommonUtils.getObjectMapper().readValue(s, Map.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return Collections.emptyMap();
                    }
                });

        return requestSpec.block();

    }

    public static <T> Map requestGet(String baseUrl, String path, MultiValueMap<String, String> params, URI uri) {
//        WebClient client = WebClient.create("https://api.mouser.com/api/v1/search/manufacturerlist?apiKey=a6234c5d-764b-4867-8659-e6e15b41903");
        WebClient client = WebClient.create(path);

//        uri.
        client.get().uri(uriBuilder -> {
            uriBuilder.path(path);
            params.forEach(uriBuilder::queryParam);
            return uriBuilder.build();
        });
        return null;
    }
}
