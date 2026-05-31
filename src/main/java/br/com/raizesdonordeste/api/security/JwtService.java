package br.com.raizesdonordeste.api.security;

import br.com.raizesdonordeste.api.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private static final String SECRET_KEY = "minha-chave-secreta-super-segura-para-jwt-raizes-nordeste";

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    public String gerarToken(Usuario usuario) {

        long agora = System.currentTimeMillis();
        long expiracao = agora + 1000 * 60 * 60 * 24;

        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("id", usuario.getId())
                .claim("nome", usuario.getNome())
                .claim("role", usuario.getRole().name())
                .issuedAt(new Date(agora))
                .expiration(new Date(expiracao))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims extrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extrairEmail(String token) {
        return extrairClaims(token).getSubject();
    }

    public boolean tokenValido(String token) {
        Date expiracao = extrairClaims(token).getExpiration();
        return expiracao.after(new Date());
    }
}