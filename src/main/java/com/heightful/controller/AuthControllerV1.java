package com.heightful.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.heightful.dto.AccountLoginRequestDto;
import com.heightful.dto.AccountLoginResponseDto;
import com.heightful.dto.AccountRegistrationRequestDto;
import com.heightful.dto.AccountRegistrationResponseDto;
import com.heightful.exception.EmailAlreadyExistsException;
import com.heightful.exception.InvalidJwtTokenException;
import com.heightful.exception.InvalidLoginCredentialsException;
import com.heightful.exception.UsernameAlreadyExistsException;
import com.heightful.service.AccountService;
import com.heightful.util.ResponseHandler;

@RestController
@RequestMapping("/v1/auth")
public class AuthControllerV1 {

    private static final Logger logger = LoggerFactory.getLogger(AuthControllerV1.class);

    @Autowired
    private final AccountService accountService;

    public AuthControllerV1(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/sessions")
    public ResponseEntity<Object> performUserLogin(
            @RequestBody AccountLoginRequestDto request) {
        logger.info("User login attempt for username: {}", request.getUsername());

        AccountLoginResponseDto response = accountService.loginAccount(request);
        logger.info("User login successful for username: {}", request.getUsername());

        return ResponseHandler.generateResponse(HttpStatus.OK,
                "Login successful",
                response,
                null);
    }

    @PostMapping("/accounts")
    public ResponseEntity<Object> registerAccount(
            @RequestBody AccountRegistrationRequestDto request) {
        logger.info("Account registration attempt for username: {}", request.getUsername());

        AccountRegistrationResponseDto response = accountService.registerAccount(request);
        logger.info("Account registration successful for username: {}", request.getUsername());

        return ResponseHandler.generateResponse(HttpStatus.CREATED,
                "Registration successful",
                response,
                null);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Object> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        logger.warn("Email registration failed: {}", ex.getMessage());
        return ResponseHandler.generateResponse(
                HttpStatus.CONFLICT,
                "Email registration failed",
                null,
                ex.getMessage());
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<Object> handleUserAlreadyExistsException(UsernameAlreadyExistsException ex) {
        logger.warn("User registration failed: {}", ex.getMessage());
        return ResponseHandler.generateResponse(
                HttpStatus.CONFLICT,
                "User registration failed",
                null,
                ex.getMessage());
    }

    @ExceptionHandler(InvalidLoginCredentialsException.class)
    public ResponseEntity<Object> handleInvalidCredentialsException(
            InvalidLoginCredentialsException ex) {
        logger.warn("User login failed: {}", ex.getMessage());
        return ResponseHandler.generateResponse(
                HttpStatus.UNAUTHORIZED,
                "Invalid login credentials",
                null,
                ex.getMessage());
    }

    @ExceptionHandler(InvalidJwtTokenException.class)
    public ResponseEntity<Object> handleInvalidJwtTokenException(InvalidJwtTokenException ex) {
        logger.warn("JWT validation failed: {}", ex.getMessage());
        return ResponseHandler.generateResponse(HttpStatus.UNAUTHORIZED,
                "Invalid JWT token",
                null,
                ex.getMessage());
    }
}
