package dev.sosnovsky.task.management.system.service;

import dev.sosnovsky.task.management.system.dto.AccessTokenRequest;
import dev.sosnovsky.task.management.system.dto.LoginRequest;
import dev.sosnovsky.task.management.system.dto.TokensResponse;
import dev.sosnovsky.task.management.system.exception.LoginOrPasswordException;
import dev.sosnovsky.task.management.system.jwt.JwtTokenUtils;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtTokenUtils jwtTokenUtils;

    @Override
    public TokensResponse login(LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(), loginRequest.getPassword()));
        } catch (BadCredentialsException e) {
            throw new LoginOrPasswordException("Неправильный логин или пароль");
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
        String accessToken = jwtTokenUtils.generateAccessToken(userDetails);
        String refreshToken = jwtTokenUtils.generateRefreshToken(userDetails);
        return new TokensResponse(accessToken, refreshToken);
    }

    @Override
    public TokensResponse getNewTokensByRefresh(AccessTokenRequest accessTokenRequest) {
        String refreshUserName = jwtTokenUtils.getUserNameFromRefreshToken(accessTokenRequest.refreshToken());
        UserDetails userDetails = userDetailsService.loadUserByUsername(refreshUserName);
        String accessToken = jwtTokenUtils.generateAccessToken(userDetails);
        String refreshToken = jwtTokenUtils.generateRefreshToken(userDetails);
        return new TokensResponse(accessToken, refreshToken);
    }
}