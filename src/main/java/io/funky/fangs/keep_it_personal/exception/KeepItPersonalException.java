package io.funky.fangs.keep_it_personal.exception;

import jakarta.annotation.Nonnull;

import java.util.Arrays;

public class KeepItPersonalException extends RuntimeException {
    public KeepItPersonalException() {
        super();
    }

    public KeepItPersonalException(String message) {
        super(message);
    }

    public KeepItPersonalException(Throwable throwable) {
        super(throwable);
    }

    public KeepItPersonalException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public KeepItPersonalException(String messageTemplate, @Nonnull Object... parameters) {
        super(extractMessage(messageTemplate, parameters), extractThrowable(parameters));
    }

    private static String extractMessage(@Nonnull String messageTemplate, @Nonnull Object... parameters) {
        final var throwable = extractThrowable(parameters);
        return messageTemplate.formatted(
                throwable == null
                    ? Arrays.copyOfRange(parameters, 0, parameters.length - 1)
                    : parameters
        );
    }

    private static Throwable extractThrowable(Object... parameters) {
        final var parameter = parameters[parameters.length - 1];
        return parameter instanceof Throwable throwable
                ? throwable
                : null;
    }
}
