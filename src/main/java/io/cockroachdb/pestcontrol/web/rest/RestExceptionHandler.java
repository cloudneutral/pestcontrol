package io.cockroachdb.pestcontrol.web.rest;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Objects;

import org.springframework.beans.TypeMismatchException;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice(basePackageClasses = IndexRestController.class)
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    protected ResponseEntity<Object> wrap(Problem problem) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(problem.getStatus(),
                problem.getDetail());
        problemDetail.setTitle(problem.getTitle());
        if (problem.getStatus().is5xxServerError()) {
            logger.error("Server error processing request: %s".formatted(problem.getDetail()));
        } else {
            logger.warn("Client error processing request: %s".formatted(problem.getDetail()));
        }
        return ResponseEntity
                .status(problem.getStatus())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problemDetail);
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleAny(Throwable ex) {
        if (ex instanceof UndeclaredThrowableException) {
            ex = ((UndeclaredThrowableException) ex).getUndeclaredThrowable();
        }

        ResponseStatus responseStatus = AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class);
        if (responseStatus != null) {
            if (responseStatus.code().is5xxServerError()) {
                logger.error("Exception processing request", ex);
            }
            return wrap(Problem.create()
                    .withTitle(ex.getLocalizedMessage())
                    .withDetail(Objects.toString(ex))
                    .withStatus(responseStatus.value()));
        } else {
            logger.error("Exception processing request", ex);
            return wrap(Problem.create()
                    .withDetail(Objects.toString(ex))
                    .withTitle(ex.getLocalizedMessage())
                    .withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Problem problem = Problem.create()
                .withDetail(Objects.toString(ex))
                .withStatus(HttpStatus.BAD_REQUEST)
                .withProperties(map -> {
                    for (FieldError error : ex.getBindingResult().getFieldErrors()) {
                        map.put(error.getField(), error.getDefaultMessage());
                    }
                    for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
                        map.put(error.getObjectName(), error.getDefaultMessage());
                    }
                });

        return handleExceptionInternal(ex, problem, headers, problem.getStatus(), request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode status, WebRequest request) {
        if (status.is5xxServerError()) {
            logger.error("", ex);
        } else {
            logger.warn("", ex);
        }
        return super.handleExceptionInternal(ex, body, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
                                                        HttpStatusCode status, WebRequest request) {
        String error =
                ex.getValue() + " value for " + ex.getPropertyName() + " should be of type " + ex.getRequiredType();
        return wrap(Problem.create()
                .withDetail(Objects.toString(ex))
                .withStatus(HttpStatus.BAD_REQUEST)
                .withProperties(error));
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(MissingServletRequestPartException ex,
                                                                     HttpHeaders headers,
                                                                     HttpStatusCode status,
                                                                     WebRequest request) {
        String error = ex.getRequestPartName() + " part is missing";
        return wrap(Problem.create()
                .withDetail(Objects.toString(ex))
                .withStatus(HttpStatus.BAD_REQUEST)
                .withProperties(error));
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatusCode status,
            WebRequest request) {
        String error = ex.getParameterName() + " parameter is missing";
        return wrap(Problem.create()
                .withDetail(Objects.toString(ex))
                .withStatus(HttpStatus.BAD_REQUEST)
                .withProperties(error));
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex,
                                                                   HttpHeaders headers, HttpStatusCode status,
                                                                   WebRequest request) {
        String error = "No handler found for " + ex.getHttpMethod() + " " + ex.getRequestURL();
        return wrap(Problem.create()
                .withDetail(Objects.toString(ex))
                .withStatus(HttpStatus.NOT_FOUND)
                .withProperties(error));
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatusCode status,
            WebRequest request) {

        StringBuilder sb = new StringBuilder();
        sb.append(ex.getMethod());
        sb.append(" method is not supported for this request. Supported methods are ");

        if (ex.getSupportedHttpMethods() != null) {
            ex.getSupportedHttpMethods().forEach(t -> sb.append(t).append(" "));
        }

        return wrap(Problem.create()
                .withDetail(Objects.toString(ex))
                .withStatus(HttpStatus.METHOD_NOT_ALLOWED)
                .withProperties(sb.toString()));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
                                                                     HttpHeaders headers, HttpStatusCode status,
                                                                     WebRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append(ex.getContentType());
        sb.append(" media type is not supported. Supported media types are ");

        if (ex.getSupportedMediaTypes() != null) {
            ex.getSupportedMediaTypes().forEach(t -> sb.append(t).append(" "));
        }

        return wrap(Problem.create()
                .withDetail(Objects.toString(ex))
                .withStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .withProperties(sb.toString()));
    }

    @ExceptionHandler({DataAccessException.class})
    public ResponseEntity<Object> handleDataAccessException(DataAccessException ex) {
        return wrap(Problem.create()
                .withTitle(ex.getLocalizedMessage())
                .withDetail(Objects.toString(ex.getCause()))
                .withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
    }
}

