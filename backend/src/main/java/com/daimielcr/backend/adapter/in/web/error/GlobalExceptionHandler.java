package com.daimielcr.backend.adapter.in.web.error;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.daimielcr.backend.domain.exceptions.InsufficientSeatsException;
import com.daimielcr.backend.domain.exceptions.InvalidTripException;
import com.daimielcr.backend.domain.exceptions.TripNotAvailableException;
import com.daimielcr.backend.domain.exceptions.TripNotFoundException;
import com.daimielcr.backend.domain.exceptions.UnauthorizedTripActionException;
import com.daimielcr.backend.domain.exceptions.UserNotFoundException;
import com.daimielcr.backend.domain.exceptions.UserPhoneNotVerifiedException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiErrorResponse> handleValidationError(
                        MethodArgumentNotValidException exception,
                        HttpServletRequest request) {
                Map<String, String> fieldErrors = new LinkedHashMap<>();

                for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
                        fieldErrors.putIfAbsent(
                                        fieldError.getField(),
                                        fieldError.getDefaultMessage());
                }

                return buildResponse(
                                HttpStatus.BAD_REQUEST,
                                "VALIDATION_ERROR",
                                "Hay campos inválidos en la petición",
                                request,
                                fieldErrors);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ApiErrorResponse> handleUnreadableRequest(
                        HttpMessageNotReadableException exception,
                        HttpServletRequest request) {
                return buildResponse(
                                HttpStatus.BAD_REQUEST,
                                "INVALID_REQUEST_BODY",
                                "El cuerpo de la petición tiene un formato inválido",
                                request);
        }

        @ExceptionHandler(MissingRequestHeaderException.class)
        public ResponseEntity<ApiErrorResponse> handleMissingHeader(
                        MissingRequestHeaderException exception,
                        HttpServletRequest request) {
                return buildResponse(
                                HttpStatus.BAD_REQUEST,
                                "MISSING_HEADER",
                                "Falta la cabecera obligatoria: %s"
                                                .formatted(exception.getHeaderName()),
                                request);
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
                        MethodArgumentTypeMismatchException exception,
                        HttpServletRequest request) {
                return buildResponse(
                                HttpStatus.BAD_REQUEST,
                                "INVALID_PARAMETER",
                                "Uno de los parámetros de la petición tiene un formato inválido",
                                request);
        }

        @ExceptionHandler(UserNotFoundException.class)
        public ResponseEntity<ApiErrorResponse> handleUserNotFound(
                        UserNotFoundException exception,
                        HttpServletRequest request) {
                return buildResponse(
                                HttpStatus.NOT_FOUND,
                                "USER_NOT_FOUND",
                                exception.getMessage(),
                                request);
        }

        @ExceptionHandler(UserPhoneNotVerifiedException.class)
        public ResponseEntity<ApiErrorResponse> handlePhoneNotVerified(
                        UserPhoneNotVerifiedException exception,
                        HttpServletRequest request) {
                return buildResponse(
                                HttpStatus.FORBIDDEN,
                                "PHONE_NOT_VERIFIED",
                                exception.getMessage(),
                                request);
        }

        @ExceptionHandler(UnauthorizedTripActionException.class)
        public ResponseEntity<ApiErrorResponse> handleUnauthorizedTripAction(
                        UnauthorizedTripActionException exception,
                        HttpServletRequest request) {
                return buildResponse(
                                HttpStatus.FORBIDDEN,
                                "FORBIDDEN",
                                exception.getMessage(),
                                request);
        }

        @ExceptionHandler(InvalidTripException.class)
        public ResponseEntity<ApiErrorResponse> handleInvalidTrip(
                        InvalidTripException exception,
                        HttpServletRequest request) {
                return buildResponse(
                                HttpStatus.BAD_REQUEST,
                                "INVALID_TRIP",
                                exception.getMessage(),
                                request);
        }

        @ExceptionHandler({
                        InsufficientSeatsException.class,
                        TripNotAvailableException.class
        })
        public ResponseEntity<ApiErrorResponse> handleTripConflict(
                        RuntimeException exception,
                        HttpServletRequest request) {
                return buildResponse(
                                HttpStatus.CONFLICT,
                                "TRIP_CONFLICT",
                                exception.getMessage(),
                                request);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiErrorResponse> handleUnexpectedError(
                        Exception exception,
                        HttpServletRequest request) {
                return buildResponse(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "INTERNAL_ERROR",
                                "Ha ocurrido un error inesperado",
                                request);
        }

        @ExceptionHandler(TripNotFoundException.class)
        public ResponseEntity<ApiErrorResponse> handleTripNotFound(TripNotFoundException exception,
                        HttpServletRequest request) {
                return buildResponse(
                                HttpStatus.NOT_FOUND,
                                "TRIP_NOT_FOUND",
                                exception.getMessage(),
                                request);
        }

        @ExceptionHandler(MissingServletRequestParameterException.class)
        public ResponseEntity<ApiErrorResponse> handleMissingRequestParameter(
                        MissingServletRequestParameterException exception,
                        HttpServletRequest request) {
                return buildResponse(
                                HttpStatus.BAD_REQUEST,
                                "MISSING_PARAMETER",
                                "Falta el parámetro obligatorio: %s"
                                                .formatted(exception.getParameterName()),
                                request);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
                        IllegalArgumentException exception,
                        HttpServletRequest request) {
                return buildResponse(
                                HttpStatus.BAD_REQUEST,
                                "INVALID_SEARCH_QUERY",
                                exception.getMessage(),
                                request);
        }

        private ResponseEntity<ApiErrorResponse> buildResponse(
                        HttpStatus status,
                        String code,
                        String message,
                        HttpServletRequest request) {
                return buildResponse(status, code, message, request, Map.of());
        }

        private ResponseEntity<ApiErrorResponse> buildResponse(
                        HttpStatus status,
                        String code,
                        String message,
                        HttpServletRequest request,
                        Map<String, String> fieldErrors) {
                ApiErrorResponse response = new ApiErrorResponse(
                                Instant.now(),
                                status.value(),
                                code,
                                message,
                                request.getRequestURI(),
                                fieldErrors);

                return ResponseEntity
                                .status(status)
                                .body(response);
        }
}