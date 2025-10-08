package com.example.demo.validators;

import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

@Component
public class PasswordValidator {
    
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 50;
    
    // Au moins une lettre minuscule, une majuscule, un chiffre et un caractère spécial
    private static final String PASSWORD_PATTERN = 
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$";
    
    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
    
    public void validate(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        
        if (password.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(
                "Password must be at least " + MIN_LENGTH + " characters long"
            );
        }
        
        if (password.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                "Password must not exceed " + MAX_LENGTH + " characters"
            );
        }
        
        if (!pattern.matcher(password).matches()) {
            throw new IllegalArgumentException(
                "Password must contain at least: " +
                "1 uppercase letter, 1 lowercase letter, 1 digit, and 1 special character (@$!%*?&#)"
            );
        }
    }
    
    public boolean isValid(String password) {
        try {
            validate(password);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}