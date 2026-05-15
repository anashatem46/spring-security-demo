package org.learnjava.securitydemo.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.learnjava.securitydemo.config.JwtService;
import org.learnjava.securitydemo.user.Role;
import org.learnjava.securitydemo.user.User;
import org.learnjava.securitydemo.user.UserRepository;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void registerSavesUserWithEncodedPasswordAndReturnsToken() {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Aly")
                .lastname("Hassan")
                .email("aly@example.com")
                .password("plain-password")
                .build();
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthenticationResponse response = authenticationService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getFirstname()).isEqualTo("Aly");
        assertThat(savedUser.getLastName()).isEqualTo("Hassan");
        assertThat(savedUser.getEmail()).isEqualTo("aly@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(savedUser.getRole()).isEqualTo(Role.USER);
        assertThat(response.getToken()).isEqualTo("jwt-token");
    }

    @Test
    void authenticateDelegatesToAuthenticationManagerAndReturnsTokenForStoredUser() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("aly@example.com")
                .password("plain-password")
                .build();
        User user = User.builder()
                .email("aly@example.com")
                .password("encoded-password")
                .role(Role.USER)
                .build();
        when(userRepository.findByEmail("aly@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthenticationResponse response = authenticationService.authenticate(request);

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("aly@example.com", "plain-password")
        );
        assertThat(response.getToken()).isEqualTo("jwt-token");
    }
}
