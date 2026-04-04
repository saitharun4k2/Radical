package com.online.MiniUdemy.dataloader; // Change this to your preferred package

import com.online.MiniUdemy.entity.User;
import com.online.MiniUdemy.enums.Role;
import com.online.MiniUdemy.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Injecting the Repository and Password Encoder
    public DataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        
        // 1. Check if the admin already exists using the email
        if (userRepository.findByEmail("admin@radical.com").isEmpty()) {
            
            // 2. Create the new Admin User object
            User admin = new User();
            admin.setName("System Admin");
            admin.setEmail("admin@radical.com");
            
            // 3. ENCODE THE PASSWORD! Spring Security requires this to log in.
            admin.setPassword(passwordEncoder.encode("admin123")); 
            
            // 4. Set the Role using your Enum
            admin.setRole(Role.ADMIN);
            
            // 5. Explicitly set enabled to true (though your entity defaults to true, this is a good safety net)
            admin.setEnabled(true); 

            // 6. Save to the database
            userRepository.save(admin);
            
            System.out.println("✅ INITIALIZATION SUCCESS: Default Admin account created!");
            System.out.println("   Email: admin@radical.com");
            System.out.println("   Password: admin123");
            
        } else {
            System.out.println("⚡ INITIALIZATION SKIPPED: Admin account already exists.");
        }
    }
}