package com.devshawn.kafka.dsf.exception;

public class KafkaExecutionException extends RuntimeException {

    private final String exceptionMessage;

    public KafkaExecutionException(String message, String exceptionMessage) {
        super(message);
        this.exceptionMessage = exceptionMessage;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }
}
