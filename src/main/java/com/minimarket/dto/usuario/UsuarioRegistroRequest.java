package com.minimarket.dto.usuario;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsuarioRegistroRequest {

    @NotBlank(message = "El username es requerido")
    private String username;

    @NotBlank(message = "El password es requerido")
    @Size(min = 8, message = "El password debe tener al menos 8 caracteres")
    private String password;

    @NotEmpty(message = "El Rol es requerido")
    private Set<String> roles;

}
