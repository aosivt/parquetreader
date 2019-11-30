package ru.aosivt.rasterparquet.errors;

public class CountParameterQuery extends Error {
    public CountParameterQuery(Throwable cause) {
        super(cause);
    }

    public CountParameterQuery(String message) {
        super(message);
    }
}
