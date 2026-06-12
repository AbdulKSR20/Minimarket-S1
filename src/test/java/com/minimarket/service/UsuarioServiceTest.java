package com.minimarket.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.service.impl.UsuarioServiceImpl;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    @Test
    void testGuardarUsuarioExitoso() {
        Usuario usuario = new Usuario();
        usuario.setUsername("abdul_saroukhan");
        usuario.setPassword("1234");
        
        Rol rol = new Rol();
        rol.setId(1L);
        rol.setNombre("CLIENTE");
        usuario.setRoles(Set.of(rol));

        when(passwordEncoder.encode("1234")).thenReturn("encoded1234");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Usuario resultado = usuarioService.save(usuario);

        assertNotNull(resultado);
        assertEquals("abdul_saroukhan", resultado.getUsername());
        assertEquals("encoded1234", resultado.getPassword());
        verify(usuarioRepository, times(1)).save(usuario);

    }

    @Test
    public void testGuardarUsuario_FallaPorUsernameIncompleto() {
        Usuario usuario = new Usuario();
        usuario.setUsername("");
        usuario.setPassword("1234");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.save(usuario);
        });

        assertEquals("Datos obligatorios del usuario incompletos", exception.getMessage());
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    public void testGuardarUsuario_FallaPorPasswordNulo() {
        Usuario usuario = new Usuario();
        usuario.setUsername("cliente");
        usuario.setPassword(null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.save(usuario);
        });

        assertEquals("Datos obligatorios del usuario incompletos", exception.getMessage());
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    public void testBuscarUsuarioPorUsername_Exitoso() {
        Usuario usuario = new Usuario();
        usuario.setUsername("abdul");
        when(usuarioRepository.findByUsername("abdul")).thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = usuarioService.findByUsername("abdul");

        assertNotNull(resultado);
        assertEquals("abdul", resultado.get().getUsername());
        verify(usuarioRepository, times(1)).findByUsername("abdul");
    }
}
