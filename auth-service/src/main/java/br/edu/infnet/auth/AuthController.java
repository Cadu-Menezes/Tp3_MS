package br.edu.infnet.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final SecretKey chave;
    private final long validadeAccessMillis;
    private final long validadeRefreshMillis;

    public AuthController(
            @Value("${jwt.secret}") String segredo,
            @Value("${jwt.access-expiration-ms}") long validadeAccessMillis,
            @Value("${jwt.refresh-expiration-ms}") long validadeRefreshMillis) {
        this.chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
        this.validadeAccessMillis = validadeAccessMillis;
        this.validadeRefreshMillis = validadeRefreshMillis;
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest request) {
        if (!"usuario".equals(request.username()) || !"123456".equals(request.password())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario ou senha invalidos");
        }

        return new TokenResponse(
                gerarToken(request.username(), "access", validadeAccessMillis),
                gerarToken(request.username(), "refresh", validadeRefreshMillis));
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@RequestBody RefreshRequest request) {
        try {
            Claims claims = Jwts.parser().verifyWith(chave).build()
                    .parseSignedClaims(request.refreshToken()).getPayload();

            if (!"refresh".equals(claims.get("tipo", String.class))) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token de refresh invalido");
            }

            return new TokenResponse(
                    gerarToken(claims.getSubject(), "access", validadeAccessMillis),
                    request.refreshToken());
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token de refresh invalido");
        }
    }

    private String gerarToken(String usuario, String tipo, long validade) {
        Date agora = new Date();
        return Jwts.builder()
                .subject(usuario)
                .claim("tipo", tipo)
                .issuedAt(agora)
                .expiration(new Date(agora.getTime() + validade))
                .signWith(chave)
                .compact();
    }

    public record LoginRequest(String username, String password) {
    }

    public record RefreshRequest(String refreshToken) {
    }

    public record TokenResponse(String accessToken, String refreshToken) {
    }
}
