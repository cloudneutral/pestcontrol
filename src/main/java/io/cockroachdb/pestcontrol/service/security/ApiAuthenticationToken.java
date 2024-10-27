package io.cockroachdb.pestcontrol.service.security;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class ApiAuthenticationToken extends AbstractAuthenticationToken {
    private final String clusterId;

    public ApiAuthenticationToken(String clusterId,
                                  Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.clusterId = clusterId;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return clusterId;
    }
}
