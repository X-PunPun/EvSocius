package pokemonifo.exception;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Validaciones de Negocio (Lanzadas desde PokemonService)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleInvalidArguments(IllegalArgumentException ex) {
        return buildResponse("Petición Inválida", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // Errores Lógicos (Listas vacías detectadas en Routes)
    @ExceptionHandler(PokemonNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(PokemonNotFoundException ex) {
        return buildResponse("No Encontrado", ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // Errores de Camel (Cuando explota la ruta por API externa)
    @ExceptionHandler(CamelExecutionException.class)
    public ResponseEntity<Map<String, String>> handleCamelException(CamelExecutionException ex) {
        Throwable cause = ex.getCause();

        // La PokeAPI devolvió un error HTTP (ej: 404 Not Found porque el tipo no existe)
        if (cause instanceof HttpOperationFailedException) {
            HttpOperationFailedException httpEx = (HttpOperationFailedException) cause;
            if (httpEx.getStatusCode() == 404) {
                return buildResponse("API Error", "El recurso no existe en la PokeAPI (Tipo inválido).", HttpStatus.NOT_FOUND);
            }
            return buildResponse("API Error", "Fallo externo con status: " + httpEx.getStatusCode(), HttpStatus.BAD_GATEWAY);
        }

        // Timeout (PokeAPI tardó mucho)
        if (cause instanceof SocketTimeoutException) {
            return buildResponse("Timeout", "La PokeAPI tardó demasiado en responder.", HttpStatus.GATEWAY_TIMEOUT);
        }

        // Excepción (PokemonNotFound) envuelta por Camel
        if (cause instanceof PokemonNotFoundException) {
            return buildResponse("No Encontrado", cause.getMessage(), HttpStatus.NOT_FOUND);
        }

        // Error genérico
        return buildResponse("Error Interno", "Error en la integración: " + cause.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<Map<String, String>> buildResponse(String error, String message, HttpStatus status) {
        Map<String, String> response = new HashMap<>();
        response.put("error", error);
        response.put("message", message);
        return new ResponseEntity<>(response, status);
    }
}