package um.prog2.dto.autenticacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;

/**
 * DTO de solicitud para login: reemplaza uso directo de LoginVM en capa de servicio.
 * JSON esperado:
 * {
 *   "username": "juan",
 *   "password": "juan123",
 *   "rememberMe": false
 * }
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class LoginRequestDTO implements Serializable {

    @NotBlank
    @Size(min = 1, max = 50)
    private String username;

    @NotBlank
    @Size(min = 4, max = 100)
    private String password;

    private boolean rememberMe;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public boolean isRememberMe() { return rememberMe; }
    public void setRememberMe(boolean rememberMe) { this.rememberMe = rememberMe; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LoginRequestDTO)) return false;
        LoginRequestDTO that = (LoginRequestDTO) o;
        return rememberMe == that.rememberMe && Objects.equals(username, that.username) && Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() { return Objects.hash(username, password, rememberMe); }

    @Override
    public String toString() {
        return "LoginRequestDTO{" +
            "username='" + username + '\'' +
            ", rememberMe=" + rememberMe +
            '}';
    }
}
