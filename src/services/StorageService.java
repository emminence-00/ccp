package services;

import java.io.*;
import java.util.*;
import models.*;

public class StorageService {
    private static final String DATA_FILE = "bank_data.dat";

    // This method saves the entire state of the bank to a file
    public static void saveState(Map<String, User> users, Map<String, Account> accounts, List<Transaction> transactions,
            List<Loan> loans) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            Map<String, Object> data = new HashMap<>();
            data.put("users", users);
            data.put("accounts", accounts);
            data.put("transactions", transactions);
            data.put("loans", loans);

            oos.writeObject(data);
            System.out.println("Data saved successfully to " + DATA_FILE);
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    // This method loads the bank state from the file
    @SuppressWarnings("unchecked")
    public static Map<String, Object> loadState() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            return (Map<String, Object>) ois.readObject();
        } catch (Exception e) {
            System.err.println("Error loading data: " + e.getMessage());
            return null;
        }
    }
}
