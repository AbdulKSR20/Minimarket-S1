package com.minimarket.dto.usuario;

import java.util.Set;
import java.util.stream.Collectors;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UsuarioResponse {
    private Long id;
    private String username;
    private Set<String> roles;

    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getRoles().stream().map(Rol::getNombre).collect(Collectors.toSet()));
    }
}
