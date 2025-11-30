package com.example.capstone_design.security;

import com.example.capstone_design.repository.UserAccountRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserAccountRepository userRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String auth = request.getHeader("Authorization");

        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);

            try {
                var jws = jwtUtil.parse(token);
                String email = jws.getBody().getSubject();   // ⭐ subject = email

                System.out.println("[JWT] Token parse OK: " + email);

                var userOpt = userRepo.findByEmail(email);

                if (userOpt.isPresent()) {

                    var user = userOpt.get();

                    var authorities =
                            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));

                    var authentication =
                            new UsernamePasswordAuthenticationToken(
                                    email,
                                    null,
                                    authorities
                            );

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    System.out.println("[JWT] Authenticated user (email): " + email);
                }

            } catch (JwtException e) {
                System.out.println("[JWT] Invalid token: " + e.getMessage());
            }
        }

        chain.doFilter(request, response);
    }
}
