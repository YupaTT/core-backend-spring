package com.core.backend.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final String CLAIM_UID = "uid";
    private static final String CLAIM_AUTHORITIES = "auth";
    private static final String CLAIM_TYPE = "typ";
    private static final String TYPE_ACCESS = "access";

    private final SecurityProperties.Jwt props;
    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    public JwtService(SecurityProperties properties) {
        this.props = properties.jwt();
        this.algorithm = Algorithm.HMAC512(props.secret().getBytes(StandardCharsets.UTF_8));
        this.verifier = JWT.require(algorithm)
                .withIssuer(props.issuer())
                .withClaim(CLAIM_TYPE, TYPE_ACCESS)
                .acceptLeeway(5)
                .build();
    }

    public IssuedToken issueAccessToken(UUID userId, String usuario, Collection<String> authorities) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(props.accessTokenTtl());
        String token = JWT.create()
                .withIssuer(props.issuer())
                .withSubject(usuario)
                .withJWTId(UUID.randomUUID().toString())
                .withIssuedAt(now)
                .withExpiresAt(expiresAt)
                .withClaim(CLAIM_TYPE, TYPE_ACCESS)
                .withClaim(CLAIM_UID, userId.toString())
                .withClaim(CLAIM_AUTHORITIES, List.copyOf(authorities))
                .sign(algorithm);
        return new IssuedToken(token, expiresAt);
    }

    /** @throws JWTVerificationException si la firma, el emisor, el tipo o la expiración no son válidos */
    public VerifiedToken verifyAccessToken(String token) {
        DecodedJWT jwt = verifier.verify(token);
        AuthenticatedUser user = new AuthenticatedUser(
                UUID.fromString(jwt.getClaim(CLAIM_UID).asString()),
                jwt.getSubject(),
                jwt.getId(),
                jwt.getExpiresAtAsInstant());
        List<String> authorities = jwt.getClaim(CLAIM_AUTHORITIES).asList(String.class);
        return new VerifiedToken(user, authorities == null ? List.of() : authorities);
    }

    public record IssuedToken(String value, Instant expiresAt) {}

    public record VerifiedToken(AuthenticatedUser user, List<String> authorities) {}
}
