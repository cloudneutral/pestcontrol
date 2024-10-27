package io.cockroachdb.pestcontrol.web.front;

import java.util.Optional;

import org.springframework.context.MessageSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.LocaleResolver;

import io.cockroachdb.pestcontrol.model.ClusterProperties;
import io.cockroachdb.pestcontrol.service.security.ClusterAuthenticationDetails;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class WebUtils {
    public static final String MSG_SUCCESS = "MSG_SUCCESS";

    public static final String MSG_INFO = "MSG_INFO";

    public static final String MSG_ERROR = "MSG_ERROR";

    private static MessageSource messageSource;

    private static LocaleResolver localeResolver;

    public WebUtils(final MessageSource messageSource, final LocaleResolver localeResolver) {
        WebUtils.messageSource = messageSource;
        WebUtils.localeResolver = localeResolver;
    }

    public static HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    public static String getMessage(final String code, final Object... args) {
        return messageSource.getMessage(code, args, code, localeResolver.resolveLocale(getRequest()));
    }

    public static Optional<ClusterProperties> getAuthenticatedClusterProperties() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getClass().isAssignableFrom(UsernamePasswordAuthenticationToken.class)) {
            ClusterAuthenticationDetails authenticationDetails = (ClusterAuthenticationDetails) auth.getDetails();
            ClusterProperties clusterProperties = authenticationDetails.getClusterProperties();
            return Optional.of(clusterProperties);
        }
        return Optional.empty();
    }
}
