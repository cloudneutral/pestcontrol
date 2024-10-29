package io.cockroachdb.pestcontrol.web.front;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice(basePackageClasses = HomeController.class)
public class WebExceptionHandler extends ResponseEntityExceptionHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception exception, HttpServletRequest request)
            throws Exception {
//        if (AnnotationUtils.findAnnotation(exception.getClass(), ResponseStatus.class) != null) {
//            throw exception;
//        }
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        ;
        ResponseStatus responseStatus = AnnotationUtils.findAnnotation(exception.getClass(), ResponseStatus.class);
        if (responseStatus != null) {
            httpStatus = responseStatus.value();
        } else {
            Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
            if (status != null) {
                httpStatus = HttpStatus.valueOf(Integer.parseInt(status.toString()));
            }
        }

        if (httpStatus.is5xxServerError()) {
            logger.error("Request [" + request.getRequestURI() + "] failed with: " + httpStatus, exception);
        } else {
            logger.warn("Request [" + request.getRequestURI() + "] failed with: "
                        + httpStatus + ": " + exception);
        }

        ModelAndView mav = new ModelAndView("error");
        mav.addObject("exception", exception.toString());
        mav.addObject("message", exception.getMessage());
        mav.addObject("stackTrace", exception.getStackTrace());
        mav.addObject("url", request.getRequestURL());
        mav.addObject("timestamp", Instant.now().toString());
        mav.addObject("status", httpStatus);

        return mav;
    }
}

