package com.heartrisk.service;

/** Thrown when a patient or assessment id does not exist; the REST layer maps it to HTTP 404. */
public class NotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     *
     * @param message what could not be found
     */
    public NotFoundException(String message) {
        super(message);
    }
}
