package ru.practicum.server.exceptions.model;

public class WrongTimeException extends RuntimeException {
    public WrongTimeException(String message) {
        super(message);
    }
}
