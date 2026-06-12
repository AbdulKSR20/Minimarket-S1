package com.minimarket.service.impl;

import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Venta;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.repository.VentaRepository;
import com.minimarket.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VentaServiceImpl implements VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Override
    public List<Venta> findAll() {
        return ventaRepository.findAll();
    }

    @Override
    public Venta findById(Long id) {
        return ventaRepository.findById(id).orElse(null);
    }

    @Override
    public Venta save(Venta venta) {

        if (venta.getUsuario() == null || venta.getUsuario().getId() == null ||
                !usuarioRepository.existsById(venta.getUsuario().getId())) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }

        if (venta.getUsuario().getRoles() == null || venta.getUsuario().getRoles().isEmpty()) {
            throw new SecurityException("El usuario no posee roles válidos para operar");
        }

        double totalCalculado = 0.0;

        if (venta.getDetalles() != null) {
            for (DetalleVenta detalle : venta.getDetalles()) {
                Producto producto = productoRepository.findById(detalle.getProducto().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

                if (producto.getStock() < detalle.getCantidad()) {
                    throw new IllegalStateException("Stock insuficiente para el producto: " + producto.getNombre());
                }

                producto.setStock(producto.getStock() - detalle.getCantidad());
                productoRepository.save(producto);

                double subtotal = producto.getPrecio() * detalle.getCantidad();
                detalle.setPrecio(producto.getPrecio());
                totalCalculado += subtotal;
            }
        }
        return ventaRepository.save(venta);
    }

    @Override
    public List<Venta> findByUsuarioId(Long usuarioId) {
        return ventaRepository.findByUsuarioId(usuarioId);
    }
}
