package com.heightful.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.heightful.dto.AccountLoginRequestDto;
import com.heightful.dto.AccountLoginResponseDto;
import com.heightful.dto.AccountRegistrationRequestDto;
import com.heightful.dto.AccountRegistrationResponseDto;
import com.heightful.entity.Account;
import com.heightful.event.AccountRegistrationEvent;
import com.heightful.exception.EmailAlreadyExistsException;
import com.heightful.exception.InvalidLoginCredentialsException;
import com.heightful.exception.UsernameAlreadyExistsException;
import com.heightful.repository.AccountRepository;
import com.heightful.security.JwtService;

@Service
public class AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    @Autowired
    private final AccountRepository accountRepository;

    @Autowired
    private final RabbitMqService rabbitMqService;

    @Autowired
    private final JwtService jwtService;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    public AccountService(AccountRepository accountRepository, JwtService jwtService,
            PasswordEncoder passwordEncoder, RabbitMqService rabbitMqService) {
        this.accountRepository = accountRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.rabbitMqService = rabbitMqService;
    }

    public AccountLoginResponseDto loginAccount(AccountLoginRequestDto request) {
        Optional<Account> accountOptional = accountRepository.findByUsername(request.getUsername());

        if (accountOptional.isPresent() &&
                passwordEncoder.matches(
                        request.getPassword(),
                        accountOptional.get().getPassword())) {
            String token = jwtService.generateToken(request.getUsername());
            return new AccountLoginResponseDto(token);
        }

        throw new InvalidLoginCredentialsException("Invalid username or password");
    }

    public AccountRegistrationResponseDto registerAccount(AccountRegistrationRequestDto request) {
        logger.debug("Attempting to register account: {}", request.getUsername());

        if (accountRepository.findByEmail(request.getEmail()).isPresent()) {
            logger.warn("Account registration failed, email already exists: {}",
                    request.getEmail());
            throw new EmailAlreadyExistsException("Email already exists");
        }

        if (accountRepository.findByUsername(request.getUsername()).isPresent()) {
            logger.warn("Account registration failed, username already exists: {}",
                    request.getUsername());
            throw new UsernameAlreadyExistsException("Username already exists");
        }

        Account account = new Account();
        account.setUsername(request.getUsername());
        account.setEmail(request.getEmail());
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        accountRepository.save(account);

        AccountRegistrationEvent accountRegistrationEvent = new AccountRegistrationEvent(
                account.getId(),
                account.getUsername(),
                account.getEmail());
        rabbitMqService.publishAccountRegistrationEvent(accountRegistrationEvent);

        String token = jwtService.generateToken(request.getUsername());

        logger.info("User registered successfully: {}", request.getUsername());
        return new AccountRegistrationResponseDto(token);
    }
}
