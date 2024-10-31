package io.cockroachdb.pestcontrol.service.security;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.cockroachdb.pestcontrol.config.ApplicationProperties;
import io.cockroachdb.pestcontrol.schema.ClusterProperties;
import io.cockroachdb.pestcontrol.service.ClusterManager;

@Component
public class ClusterAuthenticationProvider implements AuthenticationProvider {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        final ClusterAuthenticationDetails authenticationDetails
                = (ClusterAuthenticationDetails) authentication.getDetails();

        if (authentication.getCredentials() == null) {
            throw new BadCredentialsException("Missing credentials");
        }

        String username = authentication.getPrincipal().toString();
        String password = authentication.getCredentials().toString();
        String clusterId = authenticationDetails.getClusterId();
        Boolean useFileCredentials = authenticationDetails.getUseFileCredentials();

        final ClusterProperties properties = applicationProperties.getClusterPropertiesById(clusterId);
        authenticationDetails.setClusterProperties(properties);

        // Fallback to static config
        if (useFileCredentials) {
            username = properties.getDataSourceProperties().getUsername();
            password = properties.getDataSourceProperties().getPassword();
        }

        try {
            // Login to obtain session token
            clusterManager.login(clusterId, username, password);

            // Create user details
            final UserDetails userDetails = createUserDetails(username, password);

            // For whatever reason, the principal needs to be of type 'User' or the thymeleaf taglib won't work
            final UserDetails principal = User.withUserDetails(userDetails).build();

            return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        } catch (Exception exception) {
            logger.warn("Authentication failed: " + exception.getMessage());
            throw new AuthenticationServiceException("", exception);
        }
    }

    private UserDetails createUserDetails(String username, String password) {
        return new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_ADMIN")
                );
            }

            @Override
            public String getPassword() {
                return password;
            }

            @Override
            public String getUsername() {
                return username;
            }
        };
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}

