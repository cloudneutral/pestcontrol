package io.cockroachdb.pestcontrol.web.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AuthenticationRequest {
    @NotNull
    @Size(max = 255)
    private String clusterId;

    @NotNull
    private final Boolean useFileCredentials = true;

    @NotNull
    @Size(max = 255)
    private String username;

    @NotNull
    @Size(max = 255)
    private String password;

    public @NotNull Boolean getUseFileCredentials() {
        return useFileCredentials;
    }

    public @NotNull @Size(max = 255) String getClusterId() {
        return clusterId;
    }

    public @NotNull @Size(max = 255) String getUsername() {
        return username;
    }

    public @NotNull @Size(max = 255) String getPassword() {
        return password;
    }
}
