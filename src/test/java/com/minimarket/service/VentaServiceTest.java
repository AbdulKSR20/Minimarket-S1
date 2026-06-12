package com.minimarket.service;

import com.minimarket.entity.*;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.repository.VentaRepository;
import com.minimarket.service.impl.VentaServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VentaServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private VentaServiceImpl ventaService;

    @BeforeEach
    public void setup() {
        Rol clienteRol = new Rol();
        clienteRol.setId(1L);
        clienteRol.setNombre("CLIENTE");

        Usuario usuarioValido = new Usuario();
        usuarioValido.setId(1L);
        usuarioValido.setRoles(Set.of(clienteRol));
    }

    @Test
    public void testCrearVenta_Exitoso() {
        Rol clienteRol = new Rol();
        clienteRol.setId(1L);
        clienteRol.setNombre("CLIENTE");

        Usuario usuarioValido = new Usuario();
        usuarioValido.setId(1L);
        usuarioValido.setRoles(Set.of(clienteRol));

        Producto producto = new Producto();
        producto.setId(10L);
        producto.setNombre("Leche");
        producto.setPrecio(1500.0);
        producto.setStock(20);

        Venta venta = new Venta();
        venta.setUsuario(usuarioValido);

        DetalleVenta detalle = new DetalleVenta();
        detalle.setProducto(producto);
        detalle.setCantidad(2);
        venta.setDetalles(List.of(detalle));

        when(usuarioRepository.existsById(1L)).thenReturn(true);
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(i -> i.getArgument(0));

        Venta ventaProcesada = ventaService.save(venta);

        assertNotNull(ventaProcesada);
        assertEquals(1500.0, ventaProcesada.getDetalles().get(0).getPrecio());
        assertEquals(18, producto.getStock()); // Verificamos cálculo interno de stock
        verify(ventaRepository, Mockito.times(1)).save(any(Venta.class));
    }

    @Test
    public void testRegistrarVenta_FallaPorStockInsuficiente() {
        Rol clienteRol = new Rol();
        clienteRol.setId(1L);
        clienteRol.setNombre("CLIENTE");

        Usuario usuarioValido = new Usuario();
        usuarioValido.setId(1L);
        usuarioValido.setRoles(Set.of(clienteRol));

        Producto productoSinStock = new Producto();
        productoSinStock.setId(20L);
        productoSinStock.setNombre("Bebida");
        productoSinStock.setStock(1); // Stock muy bajo

        Venta venta = new Venta();
        venta.setUsuario(usuarioValido);

        DetalleVenta detalle = new DetalleVenta();
        detalle.setProducto(productoSinStock);
        detalle.setCantidad(5); // Pide más de lo que hay
        venta.setDetalles(List.of(detalle));

        when(usuarioRepository.existsById(1L)).thenReturn(true);
        when(productoRepository.findById(20L)).thenReturn(Optional.of(productoSinStock));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            ventaService.save(venta);
        });

        assertTrue(exception.getMessage().contains("Stock insuficiente"));
        verify(ventaRepository, Mockito.times(0)).save(any(Venta.class));
    }

    @Test
    public void testRegistrarVenta_FallaPorUsuarioSinRoles() {
        Usuario usuarioSinRol = new Usuario();
        usuarioSinRol.setId(2L);
        usuarioSinRol.setRoles(new HashSet<>()); // Sin roles

        Venta venta = new Venta();
        venta.setUsuario(usuarioSinRol);

        when(usuarioRepository.existsById(2L)).thenReturn(true);

        Exception exception = assertThrows(SecurityException.class, () -> {
            ventaService.save(venta);
        });

        assertEquals("El usuario no posee roles válidos para operar", exception.getMessage());
        verify(productoRepository, Mockito.times(0)).findById(any());
        verify(ventaRepository, Mockito.times(0)).save(any(Venta.class));
    }
}
