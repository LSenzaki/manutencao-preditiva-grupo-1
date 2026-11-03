package br.edu.biopark.manutencao.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class MlClientConfig {

    @Bean
    RestClient mlRestClient(RestClient.Builder builder,
                            @Value("${ml.service.url}") String baseUrl,
                            @Value("${ml.service.timeout-ms:3000}") int timeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);
        return builder.baseUrl(baseUrl).requestFactory(factory).build();
    }
}
