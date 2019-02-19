package com.bol.katalog;

class KatalogMojoException extends RuntimeException {
    KatalogMojoException() {
    }

    KatalogMojoException(String message) {
        super(message);
    }

    KatalogMojoException(String message, Throwable cause) {
        super(message, cause);
    }
}
