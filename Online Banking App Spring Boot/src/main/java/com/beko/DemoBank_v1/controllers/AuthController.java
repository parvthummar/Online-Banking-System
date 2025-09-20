package com.beko.DemoBank_v1.controllers;

import com.beko.DemoBank_v1.helpers.Token;
import com.beko.DemoBank_v1.helpers.authorization.JwtService;
import com.beko.DemoBank_v1.models.User;
import com.beko.DemoBank_v1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
public class AuthController {

    private UserRepository userRepository;

    public JwtService jwtService;

    @Autowired
    public AuthController(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> requestMap,
                                   HttpSession session, HttpServletResponse response) {

        String email = requestMap.get("email");
        String password = requestMap.get("password");

        if (email.isEmpty() || email == null || password.isEmpty() || password == null) {
            return ResponseEntity.badRequest().body("Username or Password Cannot Be Empty.");
        }

        String getEmailInDatabase = userRepository.getUserEmail(email);

        if (getEmailInDatabase != null) {
            String getPasswordInDatabase = userRepository.getUserPassword(getEmailInDatabase);

            if (!BCrypt.checkpw(password, getPasswordInDatabase)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect Username or Password");
            }
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
        }

        int verified = userRepository.isVerified(getEmailInDatabase);

        if (verified != 1) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Account verification required.");
        }

        User user = userRepository.getUserDetails(getEmailInDatabase);

        String jwt = jwtService.generateToken(user.getEmail());
        String token = Token.generateToken();

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Authentication confirmed");
        responseBody.put("access_token", jwt);

        session.setAttribute("user", user);
        session.setAttribute("token", jwt);
        session.setAttribute("authenticated", true);

        return ResponseEntity.ok(responseBody);
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Logged out successfully.");
    }
}
