package at.technikum.paperless.api;


import at.technikum.paperless.service.UserLoginService;
import at.technikum.paperless.utils.JWTUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import at.technikum.paperless.dto.AuthRequest;

@RestController
public class UsersController {
    public record TokenResponse(String token) {}
    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/api/v1/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        var authentification = new UsernamePasswordAuthenticationToken(
                authRequest.getUsername(), authRequest.getPassword());
        authenticationManager.authenticate(authentification);

        var token = jwtUtils.generateToken(authRequest.getUsername());
        return ResponseEntity.ok(new TokenResponse(token));
        //return "dummy-token";
    }
}
