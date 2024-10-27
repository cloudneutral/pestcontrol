package io.cockroachdb.pestcontrol.service.security;

import java.io.IOException;
import java.io.PrintWriter;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ApiAuthenticationFilter extends OncePerRequestFilter {
    private static final String AUTH_TOKEN_HEADER_NAME = "x-cluster-id";

    private final ApiAuthenticationService authenticationService;

    public ApiAuthenticationFilter(ApiAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = request.getHeader(AUTH_TOKEN_HEADER_NAME);
        if (token == null) {
            this.logger.trace("Did not process authentication request since failed to find "
                              + AUTH_TOKEN_HEADER_NAME + " authorization header");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Authentication authentication = authenticationService.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception exp) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            PrintWriter writer = response.getWriter();
            writer.print(exp.getMessage());
            writer.flush();
            writer.close();
        }

        filterChain.doFilter(request, response);
    }
}
