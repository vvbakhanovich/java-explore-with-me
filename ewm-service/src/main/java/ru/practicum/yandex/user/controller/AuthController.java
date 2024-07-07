package ru.practicum.yandex.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.yandex.security.webtoken.JwtService;
import ru.practicum.yandex.user.dto.SignInRequest;
import ru.practicum.yandex.user.service.MyUserDetailsService;

@RestController
@RequestMapping("/authenticate")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    private final MyUserDetailsService userDetailsService;

    @PostMapping
    public String authenticateAndGetToken(@RequestBody SignInRequest signIn) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(signIn.getEmail(), signIn.getPassword()));
        if (authentication.isAuthenticated()) {
            return jwtService.generateToken(userDetailsService.loadUserByUsername(signIn.getEmail()));
        } else {
            throw new UsernameNotFoundException("User with email '" + signIn.getEmail() + "' not found.");
        }
    }
}
