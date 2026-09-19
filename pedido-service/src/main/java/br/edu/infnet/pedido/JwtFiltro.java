package br.edu.infnet.pedido;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class JwtFiltro extends OncePerRequestFilter {
    private final SecretKey chave;

    public JwtFiltro(@Value("${jwt.secret}") String segredo) {
        this.chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String autorizacao = request.getHeader("Authorization");

        if (autorizacao == null || !autorizacao.startsWith("Bearer ")) {
            responderNaoAutorizado(response, "Token nao informado");
            return;
        }

        try {
            String token = autorizacao.substring(7);
            var claims = Jwts.parser().verifyWith(chave).build()
                    .parseSignedClaims(token).getPayload();

            if (!"access".equals(claims.get("tipo", String.class))) {
                responderNaoAutorizado(response, "Token de acesso invalido");
                return;
            }

            request.setAttribute("usuario", claims.getSubject());
            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            responderNaoAutorizado(response, "Token invalido ou expirado");
        }
    }

    private void responderNaoAutorizado(HttpServletResponse response, String mensagem) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"erro\":\"" + mensagem + "\"}");
    }
}
