package ru.aosivt.rasterparquet.errors;

public class ReflectException extends Error {
    public ReflectException(Throwable cause) {
        super(cause);
    }

    public ReflectException(String message) {
        super(message);
    }
}
