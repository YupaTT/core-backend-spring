# Instrucciones para implementar un backend compatible con el login de este frontend (Spring Boot)

## Endpoints requeridos por el frontend

El backend debe exponer los siguientes endpoints para que el frontend funcione correctamente:

### 1. POST `/auth`
- **Descripción:** Autenticación de usuario (login).
- **Request:**
    - JSON con los campos:
        - `usuario`: string
        - `contrasena`: string (en base64)
- **Response (éxito):**
    - JSON con los campos:
        - `status`: "success"
        - `message`: string
        - `datos`: objeto con la información del usuario, incluyendo:
            - `id`, `usuario`, `access_token`, `ciudadania_digital`, `correoElectronico`, `estado`, `idRol`, `rol`, `roles`, `persona`, `sucursal`, `exp`
- **Response (error):**
    - JSON con los campos:
        - `status`: "error"
        - `message`: string

### 2. GET `/usuarios/cuenta/perfil`
- **Descripción:** Devuelve el perfil del usuario autenticado.
- **Requiere:** Header `Authorization: Bearer <access_token>`
- **Response:**
    - Igual que el campo `datos` de la respuesta de login.

### 3. POST `/api/auth/set-cookie`
- **Descripción:** (Opcional, si el backend gestiona cookies) Establece la cookie de sesión.
- **Request:**
    - JSON con los campos:
        - `token`: string
        - `maxAge`: número (segundos)
        - `exp`: número (timestamp de expiración)
- **Response:**
    - JSON con `status` y `message`.

### 4. POST `/api/auth/refresh-token`
- **Descripción:** Renueva el token de autenticación.
- **Request:**
    - JSON con los campos:
        - `token`: string (token actual)
- **Response:**
    - JSON con los campos:
        - `status`: "success" o "error"
        - `newExp`: número (nuevo timestamp de expiración)

### 5. POST `/api/auth/clear-cookie`
- **Descripción:** (Opcional) Elimina la cookie de sesión.
- **Request:**
    - Sin body o body vacío.
- **Response:**
    - JSON con `status` y `message`.

### 6. GET `/api/auth/verify`
- **Descripción:** Verifica si el usuario está autenticado.
- **Response:**
    - JSON con el campo `isAuthenticated`: boolean.

---

## Requisitos generales para los endpoints
- Las respuestas deben tener el formato JSON especificado arriba.
- El campo `access_token` debe ser un JWT válido.
- El campo `sucursal` en la respuesta de login/perfil debe tener la estructura:
  ```json
  {
    "id": "string",
    "nombre": "string",
    "sigla": "string",
    "descripcion": "string"
  }
  ```
- El backend debe validar el token JWT en los endpoints protegidos.
- Los endpoints `/api/auth/set-cookie`, `/api/auth/refresh-token`, `/api/auth/clear-cookie`, `/api/auth/verify` pueden ser gestionados por el backend o por un middleware (por ejemplo, en Node.js), pero deben estar disponibles para el frontend.

---

# (El resto del documento contiene ejemplos y recomendaciones de implementación)

## Descripción general del flujo de login

El frontend espera autenticarse mediante un endpoint `/auth` que reciba usuario y contraseña, y devuelva un objeto JSON con los datos del usuario y un token de acceso. El token se usará en futuras peticiones autenticadas.

---

## 1. Estructura esperada del endpoint de login

### Endpoint
- **POST** `/auth`

### Request Body
```json
{
  "usuario": "usuario",
  "contrasena": "cGFzc3dvcmQ="
}
```
- La contraseña llega en base64 (puede estar codificada con encodeURI antes de base64).

### Response Body (éxito)
```json
{
  "status": "success",
  "message": "Login correcto",
  "datos": {
    "id": "string",
    "usuario": "string",
    "access_token": "string",
    "ciudadania_digital": true,
    "correoElectronico": "string",
    "estado": "string",
    "idRol": "string",
    "rol": "string",
    "roles": [
      {
        "idRol": "string",
        "rol": "string",
        "nombre": "string",
        "descripcion": "string",
        "modulos": []
      }
    ],
    "persona": {
      "nombres": "string",
      "primerApellido": "string",
      "segundoApellido": "string",
      "tipoDocumento": "string",
      "nroDocumento": "string",
      "fechaNacimiento": "string"
    },
    "sucursal": {
      "id": "string",
      "nombre": "string",
      "sigla": "string",
      "descripcion": "string",
    },
    "exp": 1234567890
  }
}
```

### Response Body (error)
```json
{
  "status": "error",
  "message": "Usuario o contraseña incorrectos"
}
```

---

## 2. Ejemplo de implementación en Spring Boot

### Dependencias recomendadas
- Spring Web
- Spring Security
- JWT (por ejemplo, `io.jsonwebtoken:jjwt`)

### DTO de request
```java
public class LoginRequest {
    private String usuario;
    private String contrasena;
    // getters y setters
}
```

### Controlador de autenticación
```java
@RestController
@RequestMapping("/auth")
public class AuthController {

    @PostMapping
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // Decodificar la contraseña de base64
        String password = new String(Base64.getDecoder().decode(request.getContrasena()), StandardCharsets.UTF_8);

        // Validar usuario y contraseña (ejemplo simple)
        User user = userService.findByUsername(request.getUsuario());
        if (user == null || !userService.checkPassword(user, password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("status", "error", "message", "Usuario o contraseña incorrectos"));
        }

        // Generar token JWT
        String token = jwtService.generateToken(user);

        // Construir respuesta
        Map<String, Object> datos = userToDatos(user, token);
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Login correcto",
            "datos", datos
        ));
    }

    // Métodos auxiliares para mapear el usuario al formato esperado...
}
```

### Ejemplo de generación de JWT
```java
public String generateToken(User user) {
    return Jwts.builder()
        .setSubject(user.getUsername())
        .claim("id", user.getId())
        .claim("rol", user.getRol())
        // ...otros claims
        .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 día
        .signWith(SignatureAlgorithm.HS256, secretKey)
        .compact();
}
```

### Estructura del objeto `datos` (respuesta)
- El objeto debe tener todos los campos que espera el frontend (ver ejemplo arriba).
- El campo `access_token` debe ser un JWT válido.
- Los campos `roles`, `persona`, `entidad` deben estar presentes aunque sean vacíos.

---

## 3. Seguridad y recomendaciones
- El token JWT debe usarse como Bearer Token en futuras peticiones protegidas.
- Usa filtros de Spring Security para validar el JWT en endpoints protegidos.
- El endpoint `/auth` debe ser público, pero los demás deben requerir autenticación.

---

## 4. Otros endpoints útiles
- `GET /usuarios/cuenta/perfil` — Devuelve el perfil del usuario autenticado (requiere JWT en Authorization header).
- `GET /logout` — (opcional, puede ser solo frontend)

---

## 5. Resumen del flujo de login
1. El usuario envía usuario y contraseña (base64) a `/auth`.
2. El backend valida y responde con el objeto `datos` y un JWT.
3. El frontend almacena el token y lo usa en futuras peticiones.

---

## 6. Notas adicionales
- Si usas cookies para el token, asegúrate de que sean HttpOnly y Secure.
- El frontend espera la estructura de respuesta exactamente como se muestra.
- Si necesitas soporte para Ciudadanía Digital, deberás implementar endpoints adicionales según la documentación del frontend.

---

¿Dudas? Puedes consultar el código fuente del frontend para ver ejemplos de la estructura de datos esperada o pedir ayuda para la configuración de Spring Security y JWT.
