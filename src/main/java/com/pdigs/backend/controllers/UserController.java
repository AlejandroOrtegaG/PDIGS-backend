package com.pdigs.backend.controllers;

import com.pdigs.backend.models.Product;
import com.pdigs.backend.models.User;
import com.pdigs.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.*;
import java.util.function.Function;

import static java.time.LocalTime.now;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    @Autowired
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> createUser(@RequestBody User user) {
        System.out.println("user: "+ user);
        userRepository.save(user);
        return ResponseEntity.ok("User created successfully");
    }

    @GetMapping("/login")
    public ResponseEntity<Object> login(@RequestParam String email, @RequestParam String password) {
        User user = userRepository.findUserByEmail(email);
        if (user != null && user.getPassword().equals(sha256(password))){
            return ResponseEntity.ok().body(user);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"Email or password is incorrect\"}");
    }

    // Método para encriptar la contraseña con SHA-256
    private String sha256(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/getUsers")
    public Iterable<User> getUsers(
            @RequestParam(value = "filterField", required = false) String filterField,
            @RequestParam(value = "filterValue", required = false) String filterValue,
            @RequestParam(value = "sortOrder", required = false) String sortOrder) {

        Sort sort = sortOrder == null || sortOrder.isEmpty() || sortOrder.equals("asc")
                ? Sort.by(Sort.Direction.ASC, filterField != null && !filterField.isEmpty() ? filterField: "id")
                : Sort.by(Sort.Direction.DESC, filterField != null && !filterField.isEmpty() ? filterField: "id");


        Map<String, Function<String, Iterable<User>>> filterMethods = new HashMap<>();

        filterMethods.put("name", userRepository::findByName);
        filterMethods.put("email", userRepository::findByEmail);

        if (filterValue != null && !filterValue.isEmpty()) {
            Function<String, Iterable<User>> filterMethod = filterMethods.get(filterField);
            if (filterMethod != null){
                return filterMethod.apply(filterValue);
            }else {
                return new ArrayList<>();
            }
        }
        return userRepository.findAll(sort);
    }

    @GetMapping("/getByFollowers")
    public Iterable<User> orderByFollowers(){
        Iterable<User> users = userRepository.findAll();

        Map<User, Integer> userFollowers = new HashMap<>();

        for (User user : users) {
            userFollowers.put(user, userRepository.getFollowers(user).size());
        }

        List<Map.Entry<User, Integer>> entryList = new ArrayList<>(userFollowers.entrySet());
        entryList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        List<User> sortedUsers = new ArrayList<>();
        for (Map.Entry<User, Integer> entry : entryList) {
            sortedUsers.add(entry.getKey());
        }

        return sortedUsers;
    }

    @PutMapping("/updateUser")
    public ResponseEntity<String> updateUser(@RequestParam(value = "id") Long id, @RequestBody User user) {
        if (userRepository.existsById(id)) {
            userRepository.save(user);
            return ResponseEntity.ok("User updated successfully");
        } else {
            return ResponseEntity.badRequest().body("User not found");
        }
    }

    @DeleteMapping("/deleteUser")
    public ResponseEntity<String> deleteUser(@RequestParam(value = "id") Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.ok("User deleted successfully");
        } else {
            return ResponseEntity.badRequest().body("User not found");
        }
    }

    @GetMapping("/followers")
    public Iterable<User> countFollowers(@RequestParam(value = "id") Long id) {
        Optional<User> user = userRepository.findById(id);
        return userRepository.getFollowers(user.orElse(null));
    }
    @GetMapping("/following")
    public Iterable<User> countFollowing(@RequestParam(value = "id") Long id) {
        Optional<User> user = userRepository.findById(id);
        return userRepository.getFollowing(user.orElse(null));
    }

    @GetMapping("/countFollowers")
    public Integer countFollowersCount(@RequestParam(value = "id") Long id) {
        Optional<User> user = userRepository.findById(id);
        return userRepository.getFollowers(user.orElse(null)).size();
    }
    @GetMapping("/countFollowing")
    public Integer countFollowingCount(@RequestParam(value = "id") Long id) {
        Optional<User> user = userRepository.findById(id);
        return userRepository.getFollowing(user.orElse(null)).size();
    }

    @GetMapping("/getSubscribedsTo")
    public ResponseEntity<List<User>> getSubscribedsTo(@RequestParam(value = "id") Long id) {
        return ResponseEntity.ok(userRepository.getSubscribeds(userRepository.findById(id).orElse(null)));
    }

    @GetMapping("/getSuscribers")
    public ResponseEntity<List<User>> getSubscribers(@RequestParam(value = "id") Long id) {
        return ResponseEntity.ok(userRepository.getSubscribers(userRepository.findById(id).orElse(null)));
    }

    @GetMapping("/getProducts")
    public ResponseEntity<List<Product>> getProducts(@RequestParam(value = "id") Long id) {
        return ResponseEntity.ok(userRepository.getProducts(userRepository.findById(id).orElse(null)));
    }
    @GetMapping("/getProductsLiked")
    public ResponseEntity<List<Product>> getProductsLiked(@RequestParam(value = "id") Long id) {
        return ResponseEntity.ok(userRepository.getProductsLiked(userRepository.findById(id).orElse(null)));
    }
    @GetMapping("/countProducts")
    public ResponseEntity<Integer> countProducts(@RequestParam(value = "id") Long id) {
        return ResponseEntity.ok(userRepository.countByProduct(userRepository.findById(id).orElse(null)));
    }
}