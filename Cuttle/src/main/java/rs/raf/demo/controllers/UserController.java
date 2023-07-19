package rs.raf.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import rs.raf.demo.model.User;
import rs.raf.demo.responses.ResponseDto;
import rs.raf.demo.services.UserService;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@CrossOrigin
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDto create(@Valid @RequestBody User user) {
        return this.userService.create(user);
    }

    @GetMapping
    public Page<User> all(@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "10") Integer size) {
        return this.userService.paginate(page, size);
    }

    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> me() {
        Optional<User> user = this.userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());

        if (user.isPresent()) return ResponseEntity.ok(user.get());
        else return ResponseEntity.status(404).body("User not found");
    }

}
