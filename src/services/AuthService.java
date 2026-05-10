package services;
import models.*;
import exceptions.*;
import utils.*;



import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class AuthService {
    private Map<String, User> users = new HashMap<>();
    private User currentUser;

    public void register(String fullName, String email, String phone, LocalDate dob, String nationalId, String password, Role role) throws AuthenticationException {
        if (users.containsKey(email)) {
            throw new AuthenticationException("Email already registered.");
        }
        if (!SecurityUtils.validateEmail(email)) throw new AuthenticationException("Invalid email format.");
        
        String passwordHash = SecurityUtils.hashPassword(password);
        User user = new User(fullName, email, phone, dob, nationalId, passwordHash, role);
        users.put(email, user);
        System.out.println("User registered: " + email);
    }

    public User login(String email, String password) throws AuthenticationException {
        // Find user by email
        User user = users.get(email);
        
        // Check if user exists
        if (user == null) {
            throw new AuthenticationException("Error: We could not find a user with that email!");
        }

        // Check if account is locked
        if (user.isLocked()) {
            throw new AuthenticationException("Error: Your account is LOCKED because you failed 5 times.");
        }

        // Check if password is correct
        String inputHash = SecurityUtils.hashPassword(password);
        if (user.getPasswordHash().equals(inputHash)) {
            // Success!
            user.resetFailedAttempts();
            this.currentUser = user;
            System.out.println("Login success for " + email);
            return user;
        } else {
            // Fail!
            user.incrementFailedAttempts();
            int attemptsUsed = user.getFailedAttempts();
            int attemptsLeft = 5 - attemptsUsed;
            
            if (attemptsLeft <= 0) {
                throw new AuthenticationException("Error: Password wrong. Account is now LOCKED!");
            } else {
                throw new AuthenticationException("Error: Wrong password. You have " + attemptsLeft + " tries left.");
            }
        }
    }

    public void logout() {
        this.currentUser = null;
    }

    public void resetPassword(String email, String newPassword) throws AuthenticationException {
        User user = users.get(email);
        if (user == null) throw new AuthenticationException("User not found.");
        user.setPasswordHash(SecurityUtils.hashPassword(newPassword));
        user.resetFailedAttempts();
        System.out.println("Password reset for: " + email);
    }

    public User getCurrentUser() { return currentUser; }
    
    public void setUsers(Map<String, User> users) {
        this.users = users;
    }

    public Map<String, User> getAllUsers() { return users; }
}
