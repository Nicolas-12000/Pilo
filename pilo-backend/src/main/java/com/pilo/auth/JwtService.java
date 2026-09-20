package com.pilo.auth;

import com.pilo.users.Role;
import com.pilo.users.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

	private final JwtProperties properties;
	private final SecretKey key;
	private final SecretKey previousKey;

	public JwtService(JwtProperties properties) {
		this.properties = properties;
		this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
		this.previousKey = properties.hasPreviousSecret()
				? Keys.hmacShaKeyFor(properties.previousSecret().getBytes(StandardCharsets.UTF_8))
				: null;
	}

	public String createAccessToken(User user) {
		Instant now = Instant.now();
		Instant expiresAt = now.plus(properties.expiration());
		return Jwts.builder()
				.issuer(properties.issuer())
				.audience()
				.add(properties.audience())
				.and()
				.subject(user.getId().toString())
				.claim("email", user.getEmail())
				.claim("role", user.getRole().name())
				.claim("tv", user.getTokenVersion())
				.issuedAt(Date.from(now))
				.expiration(Date.from(expiresAt))
				.signWith(key)
				.compact();
	}

	public JwtPrincipal parse(String token) {
		try {
			return parseWith(key, token);
		} catch (ExpiredJwtException expired) {
			throw expired;
		} catch (JwtException | IllegalArgumentException primary) {
			if (previousKey == null) {
				throw primary;
			}
			return parseWith(previousKey, token);
		}
	}

	private JwtPrincipal parseWith(SecretKey verifyKey, String token) {
		Claims claims = Jwts.parser()
				.verifyWith(verifyKey)
				.requireIssuer(properties.issuer())
				.requireAudience(properties.audience())
				.build()
				.parseSignedClaims(token)
				.getPayload();
		Integer tokenVersion = claims.get("tv", Integer.class);
		return new JwtPrincipal(
				UUID.fromString(claims.getSubject()),
				claims.get("email", String.class),
				Role.valueOf(claims.get("role", String.class)),
				tokenVersion == null ? 0 : tokenVersion);
	}
}
