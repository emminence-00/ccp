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
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new AuthenticationException("Registration failed: Full Name cannot be empty.");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new AuthenticationException("Registration failed: Email cannot be empty.");
        }
        if (!SecurityUtils.validateEmail(email)) {
            throw new AuthenticationException("Registration failed: Invalid email format.");
        }
        if (users.containsKey(email)) {
            throw new AuthenticationException("Registration failed: Email already registered.");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new AuthenticationException("Registration failed: Phone number cannot be empty.");
        }
        if (!SecurityUtils.validatePhone(phone)) {
            throw new AuthenticationException("Registration failed: Invalid phone number. Must be between 10 to 15 digits.");
        }
        if (nationalId == null || nationalId.trim().isEmpty()) {
            throw new AuthenticationException("Registration failed: National ID cannot be empty.");
        }
        if (!SecurityUtils.validateNationalId(nationalId)) {
            throw new AuthenticationException("Registration failed: Invalid National ID. Must be at least 5 characters.");
        }
        if (dob == null) {
            throw new AuthenticationException("Registration failed: Date of Birth cannot be empty.");
        }
        if (dob.isAfter(LocalDate.now())) {
            throw new AuthenticationException("Registration failed: Date of Birth cannot be in the future.");
        }
        if (dob.isAfter(LocalDate.now().minusYears(18))) {
            throw new AuthenticationException("Registration failed: You must be at least 18 years old to register.");
        }
        if (password == null || password.isEmpty()) {
            throw new AuthenticationException("Registration failed: Password cannot be empty.");
        }
        if (password.length() < 6) {
            throw new AuthenticationException("Registration failed: Password must be at least 6 characters long.");
        }

        String passwordHash = SecurityUtils.hashPassword(password);
        User user = new User(fullName, email, phone, dob, nationalId, passwordHash, role);
        users.put(email, user);
        System.out.println("User registered: " + email);
    }

    public User login(String email, String password) throws AuthenticationException {
        if (email == null || email.trim().isEmpty()) {
            throw new AuthenticationException("Login failed: Email cannot be empty.");
        }
        if (!SecurityUtils.validateEmail(email)) {
            throw new AuthenticationException("Login failed: Invalid email format.");
        }
        if (password == null || password.isEmpty()) {
            throw new AuthenticationException("Login failed: Password cannot be empty.");
        }

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
