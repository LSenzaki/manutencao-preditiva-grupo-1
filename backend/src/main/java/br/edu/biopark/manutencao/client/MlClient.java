package br.edu.biopark.manutencao.client;

import br.edu.biopark.manutencao.dto.MlPredictRequest;
import br.edu.biopark.manutencao.dto.MlPredictResponse;
import br.edu.biopark.manutencao.exception.MlServiceIndisponivelException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Comunicação síncrona Java → Python. */
@Component
public class MlClient {

    private final RestClient mlRestClient;

    public MlClient(RestClient mlRestClient) {
        this.mlRestClient = mlRestClient;
    }

    public MlPredictResponse prever(MlPredictRequest request) {
        MlPredictResponse resposta;
        try {
            resposta = mlRestClient.post()
                    .uri("/predict")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(MlPredictResponse.class);
        } catch (RestClientException e) {
            throw new MlServiceIndisponivelException("Falha ao chamar o serviço de ML: " + e.getMessage(), e);
        }
        if (resposta == null) {
            throw new MlServiceIndisponivelException("Serviço de ML retornou resposta vazia", null);
        }
        return resposta;
    }
}
