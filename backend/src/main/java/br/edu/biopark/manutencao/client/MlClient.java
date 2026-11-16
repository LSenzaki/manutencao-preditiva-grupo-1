package br.edu.biopark.manutencao.client;

import br.edu.biopark.manutencao.dto.MlPredictRequest;
import br.edu.biopark.manutencao.dto.MlPredictResponse;
import br.edu.biopark.manutencao.exception.MlServiceIndisponivelException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** Executes the Python model locally in the backend container. */
@Component
public class MlClient {

    private final ObjectMapper objectMapper;
    private final String pythonCommand;
    private final Path predictScript;
    private final long timeoutMs;

    public MlClient(ObjectMapper objectMapper,
                    @Value("${ml.python-command:python}") String pythonCommand,
                    @Value("${ml.predict-script:ml/predict.py}") String predictScript,
                    @Value("${ml.timeout-ms:10000}") long timeoutMs) {
        this.objectMapper = objectMapper;
        this.pythonCommand = pythonCommand;
        this.predictScript = resolverScript(predictScript);
        this.timeoutMs = timeoutMs;
    }

    public MlPredictResponse prever(MlPredictRequest request) {
        List<String> comando = new ArrayList<>(List.of(
                pythonCommand, predictScript.toString(),
                "--tipo-produto", request.tipoProduto(),
                "--temperatura-ar-k", request.temperaturaArK().toPlainString(),
                "--temperatura-processo-k", request.temperaturaProcessoK().toPlainString(),
                "--rotacao-rpm", request.rotacaoRpm().toString(),
                "--torque-nm", request.torqueNm().toPlainString(),
                "--desgaste-ferramenta-min", request.desgasteFerramentaMin().toString()
        ));

        Process processo = null;
        try {
            processo = new ProcessBuilder(comando)
                    .directory(predictScript.getParent().toFile())
                    .start();
            Process processoAtivo = processo;
            CompletableFuture<byte[]> stdout = CompletableFuture.supplyAsync(
                    () -> lerSaida(processoAtivo.getInputStream()));
            CompletableFuture<byte[]> stderr = CompletableFuture.supplyAsync(
                    () -> lerSaida(processoAtivo.getErrorStream()));

            if (!processo.waitFor(timeoutMs, TimeUnit.MILLISECONDS)) {
                processo.destroyForcibly();
                throw new TimeoutException("O modelo excedeu " + timeoutMs + " ms");
            }

            String saida = new String(stdout.get(2, TimeUnit.SECONDS), StandardCharsets.UTF_8).trim();
            String erro = new String(stderr.get(2, TimeUnit.SECONDS), StandardCharsets.UTF_8).trim();
            if (processo.exitValue() != 0) {
                throw new IOException("predict.py encerrou com codigo " + processo.exitValue()
                        + (erro.isBlank() ? "" : ": " + limitar(erro, 2000)));
            }

            MlPredictResponse resposta = objectMapper.readValue(saida, MlPredictResponse.class);
            validarResposta(resposta);
            return resposta;
        } catch (Exception e) {
            if (processo != null && processo.isAlive()) {
                processo.destroyForcibly();
            }
            if (e instanceof MlServiceIndisponivelException indisponivel) {
                throw indisponivel;
            }
            throw new MlServiceIndisponivelException("Falha ao executar o modelo de ML: " + e.getMessage(), e);
        }
    }

    private static byte[] lerSaida(java.io.InputStream stream) {
        try (stream) {
            return stream.readAllBytes();
        } catch (IOException e) {
            throw new java.util.concurrent.CompletionException(e);
        }
    }

    private static void validarResposta(MlPredictResponse resposta) {
        if (resposta == null || resposta.probabilidadeFalha() == null
                || resposta.tipoFalhaPrevisto() == null || resposta.versaoModelo() == null
                || resposta.probabilidadeFalha().signum() < 0
                || resposta.probabilidadeFalha().compareTo(java.math.BigDecimal.ONE) > 0
                || resposta.versaoModelo().isBlank() || resposta.versaoModelo().length() > 20
                || !List.of("NONE", "TWF", "HDF", "PWF", "OSF", "RNF").contains(resposta.tipoFalhaPrevisto())) {
            throw new MlServiceIndisponivelException("O modelo retornou uma resposta invalida", null);
        }
    }

    private static String limitar(String texto, int maximo) {
        return texto.length() <= maximo ? texto : texto.substring(texto.length() - maximo);
    }

    private static Path resolverScript(String configurado) {
        Path caminho = Path.of(configurado).toAbsolutePath().normalize();
        if (java.nio.file.Files.exists(caminho)) {
            return caminho;
        }
        Path relativoAoBackend = Path.of("..", "ml", "predict.py").toAbsolutePath().normalize();
        return java.nio.file.Files.exists(relativoAoBackend) ? relativoAoBackend : caminho;
    }
}
