package dev.sosnovsky.task.management.system.service;


import dev.sosnovsky.task.management.system.dto.AccessTokenRequest;
import dev.sosnovsky.task.management.system.dto.LoginRequest;
import dev.sosnovsky.task.management.system.dto.TokensResponse;

public interface AuthenticationService {

    TokensResponse login(LoginRequest loginRequest);

    TokensResponse getNewTokensByRefresh(AccessTokenRequest accessTokenRequest);
}