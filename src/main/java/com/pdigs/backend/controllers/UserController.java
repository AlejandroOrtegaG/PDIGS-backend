package com.pdigs.backend.controllers;

import com.pdigs.backend.models.Follows;
import com.pdigs.backend.models.Product;
import com.pdigs.backend.models.Subscriptions;
import com.pdigs.backend.models.User;
import com.pdigs.backend.repositories.FollowsRepository;
import com.pdigs.backend.repositories.ProductRepository;
import com.pdigs.backend.repositories.SubscriptionsRepository;
import com.pdigs.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FollowsRepository followsRepository;
    @Autowired
    private SubscriptionsRepository subscriptionsRepository;
    @Autowired
    private ProductRepository productRepository;

    @PostMapping("/signup")
    public ResponseEntity<String> createUser(@RequestBody User user) {
        System.out.println("user: "+ user);
        userRepository.save(user);
        return ResponseEntity.ok("User created successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String email, @RequestParam String password) {
        User user = userRepository.findUserByEmail(email);
        if (user != null && user.getPassword().equals(password)) return ResponseEntity.ok("User logged in successfully");

        return ResponseEntity.ok("Email or password is incorrect");
    }

    @GetMapping
    public Iterable<User> getUsers() {
        return userRepository.findAll();
    }

    @PutMapping
    public ResponseEntity<String> updateUser(@RequestParam(value = "id") Long id, @RequestBody User user) {
        if (userRepository.existsById(id)) {
            userRepository.save(user);
            return ResponseEntity.ok("User updated successfully");
        } else {
            return ResponseEntity.badRequest().body("User not found");
        }
    }

    @DeleteMapping
    public ResponseEntity<String> deleteUser(@RequestParam(value = "id") Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.ok("User deleted successfully");
        } else {
            return ResponseEntity.badRequest().body("User not found");
        }
    }

    @GetMapping("/countFollowers")
    public ResponseEntity<Integer> countFollowers(@RequestParam(value = "id") Long id) {
        int followersCount = followsRepository.countByFollowed(userRepository.findById(id).orElse(null));
        return ResponseEntity.ok(followersCount);
    }
    @GetMapping("/countFolloweds")
    public ResponseEntity<Integer> countFolloweds(@RequestParam(value = "id") Long id) {
        int followedsCount = followsRepository.countByFollower(userRepository.findById(id).orElse(null));
        return ResponseEntity.ok(followedsCount);
    }
    @GetMapping("/getFollowers")
    public ResponseEntity<List<User>> getFollowers(@RequestParam(value = "id") Long id) {
        Iterable<Follows> follows = followsRepository.getFollowsByFollowed(userRepository.findById(id).orElse(null));
        List<User> followers = new ArrayList<>();
        for(Follows follower : follows){
            followers.add(follower.getFollower());
        }
        return ResponseEntity.ok(followers);
    }
    @GetMapping("/getFolloweds")
    public ResponseEntity<List<User>> getFolloweds(@RequestParam(value = "id") Long id) {
        Iterable<Follows> follows = followsRepository.getFollowsByFollower(userRepository.findById(id).orElse(null));
        List<User> followeds = new ArrayList<>();
        for(Follows followed : follows){
            followeds.add(followed.getFollower());
        }
        return ResponseEntity.ok(followeds);
    }
    @GetMapping("/getSubscribedsTo")
    public ResponseEntity<List<User>> getSubscribedsTo(@RequestParam(value = "id") Long id) {
        Iterable<Subscriptions> subscribedsTo = subscriptionsRepository.getSubscriptionsBySubscribedTo(userRepository.findById(id).orElse(null));
        List<User> subscribeds = new ArrayList<>();
        for(Subscriptions subscriber : subscribedsTo){
            subscribeds.add(subscriber.getSubscriber());
        }
        return ResponseEntity.ok(subscribeds);
    }
    @GetMapping("/getSuscribers")
    public ResponseEntity<List<User>> getSubscribers(@RequestParam(value = "id") Long id) {
        Iterable<Subscriptions> subscribers = subscriptionsRepository.getSubscriptionsBySuscriber(userRepository.findById(id).orElse(null));
        List<User> subscribersTo = new ArrayList<>();
        for(Subscriptions subscriber : subscribers){
            subscribersTo.add(subscriber.getSubscriber());
        }
        return ResponseEntity.ok(subscribersTo);
    }

    @GetMapping("/getProducts")
    public ResponseEntity<List<Integer>> getProducts(@RequestParam(value = "id") Integer id) {
        Iterable<Product> products = productRepository.findAllBySellerId(id);
        List<Integer> productsList = new ArrayList<>();
        for(Product product : products){
            productsList.add(product.getId());
        }
        return ResponseEntity.ok(productsList);
    }
}