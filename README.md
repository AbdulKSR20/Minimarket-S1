# 🛒 MiniMarket Plus - API REST Segura

Backend desarrollado con **Spring Boot** para la cadena de minimarkets "MiniMarket Plus". Este proyecto expone una API RESTful para la gestión de productos, categorías, inventario y ventas, implementando una arquitectura de seguridad robusta basada en estándares de la industria.

## 🚀 Tecnologías Utilizadas
* **Java 17**
* **Spring Boot 3** (Web, Data JPA, Security, Validation)
* **H2 Database** (Base de datos en memoria para desarrollo/pruebas)
* **JSON Web Tokens (JWT)** (Autenticación Stateless)
* **Jsoup** (Sanitización de entradas)
* **SLF4J / Logback** (Auditoría y Monitoreo)

## 🛡️ Arquitectura de Seguridad Implementada

Este proyecto fue diseñado considerando las principales amenazas de seguridad web (OWASP Top 10), implementando las siguientes defensas:

1. **Control de Acceso Basado en Roles (RBAC):**
   - Jerarquía de privilegios implementada mediante `@PreAuthorize` (GERENTE, EMPLEADO, CLIENTE).
   - Acceso granular a nivel de métodos y controladores.

2. **Autenticación Stateless con JWT:**
   - Generación, validación y firma de tokens JWT.
   - Protección contra **CSRF** por diseño, al no utilizar cookies de sesión (`SessionCreationPolicy.STATELESS`).

3. **Prevención de Inyección SQL (SQLi):**
   - Uso exclusivo de *Prepared Statements* a través de la capa de persistencia con Spring Data JPA e Hibernate.

4. **Mitigación de Cross-Site Scripting (XSS):**
   - Validación estricta de DTOs (`@NotBlank`, `@NotNull`).
   - Política de sanitización transversal en la capa de servicios utilizando **Jsoup** (`Safelist.basic()`) para remover cualquier etiqueta HTML o Script malicioso antes de la persistencia en base de datos.

5. **Auditoría, Rate Limiting y Prevención de Fuerza Bruta:**
   - Componente `SuspiciousActivityService` que monitorea el tráfico en tiempo real.
   - Detección de múltiples intentos fallidos de Login (Fuerza Bruta) bloqueando la IP y usuario.
   - Restricción de tasa de peticiones (Max 200 peticiones / 15 min por IP) para evitar ataques DoS a nivel de aplicación.
   - Registro en logs de tokens inválidos o manipulados.

6. **Configuración CORS Segura:**
   - Filtros de orígenes, métodos y cabeceras permitidas explícitamente configurados en `SecurityConfig`.
