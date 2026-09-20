package com.pilo.auth;

import com.pilo.users.User;
import com.pilo.users.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final UserRepository userRepository;

	public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
		this.jwtService = jwtService;
		this.userRepository = userRepository;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return request.getRequestURI().startsWith("/internal/");
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header != null && header.startsWith("Bearer ")) {
			String token = header.substring(7);
			try {
				JwtPrincipal principal = jwtService.parse(token);
				User user = userRepository.findById(principal.userId()).orElse(null);
				if (user != null
						&& user.isEnabled()
						&& user.getTokenVersion() == principal.tokenVersion()) {
					UsernamePasswordAuthenticationToken authentication =
							new UsernamePasswordAuthenticationToken(
									user.getId().toString(),
									null,
									List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
					SecurityContextHolder.getContext().setAuthentication(authentication);
				} else {
					SecurityContextHolder.clearContext();
				}
			} catch (JwtException | IllegalArgumentException ignored) {
				SecurityContextHolder.clearContext();
			}
		}
		filterChain.doFilter(request, response);
	}
}
