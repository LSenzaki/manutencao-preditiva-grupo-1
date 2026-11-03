package br.edu.biopark.manutencao.exception;

public class MlServiceIndisponivelException extends RuntimeException {
    public MlServiceIndisponivelException(String message, Throwable cause) {
        super(message, cause);
    }
}
