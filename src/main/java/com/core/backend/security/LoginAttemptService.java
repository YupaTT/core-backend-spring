package com.core.backend.security;

/** Bloqueo temporal de login tras varios intentos fallidos (protección contra fuerza bruta). */
public interface LoginAttemptService {

    boolean isBlocked(String key);

    void loginFailed(String key);

    void loginSucceeded(String key);
}
