package rs.raf.app.bootstrap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import rs.raf.app.model.User;
import rs.raf.app.repositories.UsersRepository;

@Component
public class BootstrapData implements CommandLineRunner {

    private final UsersRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public BootstrapData(UsersRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        System.out.println("Loading Data...");

        User user1 = new User();
        user1.setUsername("user1");
        user1.setPassword(this.passwordEncoder.encode("user1"));
        this.userRepository.save(user1);

        User user2 = new User();
        user2.setUsername("user2");
        user2.setPassword(this.passwordEncoder.encode("user2"));
        this.userRepository.save(user2);

        User user3 = new User();
        user3.setUsername("user3");
        user3.setPassword(this.passwordEncoder.encode("user3"));
        this.userRepository.save(user3);

        User user4 = new User();
        user4.setUsername("user4");
        user4.setPassword(this.passwordEncoder.encode("user4"));
        this.userRepository.save(user4);

        System.out.println("Data loaded!");
    }
}
