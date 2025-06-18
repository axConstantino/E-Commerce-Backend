package com.axconstantino.auth.application.service;

import com.axconstantino.auth.application.command.AuthenticateCommand;
import com.axconstantino.auth.application.dto.TokenResponse;
import com.axconstantino.auth.application.usecase.LoginUser;
import com.axconstantino.auth.domain.model.Token;
import com.axconstantino.auth.domain.model.TokenType;
import com.axconstantino.auth.domain.model.User;
import com.axconstantino.auth.domain.repository.UserRepository;
import com.axconstantino.auth.domain.exception.BadCredentialsException;
import com.axconstantino.auth.infrastructure.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginUserService implements LoginUser {

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Override
    @Transactional
    public TokenResponse execute(AuthenticateCommand command, HttpServletRequest httpRequest) {
        log.info("Attempting to authenticate user with email: {}", command.email());

        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> {
                    log.warn("Authentication failed for email {}: User not found", command.email());
                    return new BadCredentialsException("Invalid email or password");
                });

        if (!passwordEncoder.matches(command.password(), user.getPassword())) {
            log.warn("Authentication failed for user {}: Invalid password", user.getId());
            throw new BadCredentialsException("Invalid email or password");
        }

        log.info("User {} authenticated successfully", user.getId());
        tokenService.revokeAllUserTokens(user);

        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");

        Token accessToken = tokenService.createToken(jwtProvider.generateAccessToken(user), TokenType.ACCESS_TOKEN, user,ipAddress, userAgent);
        Token refreshToken = tokenService.createToken(jwtProvider.generateRefreshToken(user), TokenType.REFRESH_TOKEN, user, ipAddress, userAgent);

        user.addToken(accessToken);
        user.addToken(refreshToken);

        userRepository.save(user);
        tokenService.saveTokenInCache(user, accessToken);
        tokenService.saveTokenInCache(user, refreshToken);

        log.info("New tokens generated for user ID: {}", user.getId());
        return new TokenResponse(accessToken.getToken(), refreshToken.getToken());
    }
}
