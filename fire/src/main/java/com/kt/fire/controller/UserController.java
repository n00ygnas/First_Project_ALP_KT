package com.kt.fire.controller;

import com.kt.fire.dto.UserRequestDto;
import com.kt.fire.entity.Users;
import com.kt.fire.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRequestDto.Register request) {
        try {
            Users user = userService.register(
                request.getEmail(),
                request.getPassword(),
                request.getName(),
                request.getDistrictIds()
            );
            return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "name", user.getName()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserRequestDto.Login request) {
        Optional<Users> user = userService.login(request.getEmail(), request.getPassword());
        if (user.isPresent()) {
            return ResponseEntity.ok(Map.of(
                "id", user.get().getId(),
                "email", user.get().getEmail(),
                "name", user.get().getName()
            ));
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }

    @GetMapping("/{userId}/interests")
    public ResponseEntity<?> getUserInterests(@PathVariable Long userId) {
        List<String> districtIds = userService.getUserDistrictIds(userId);
        return ResponseEntity.ok(districtIds);
    }

    @PutMapping("/{userId}/interests")
    public ResponseEntity<?> updateUserInterests(
            @PathVariable Long userId,
            @RequestBody List<String> districtIds) {
        try {
            System.out.println("Updating interests for user: " + userId);
            System.out.println("New district IDs: " + districtIds);
            
            if (userId == null) {
                return ResponseEntity.badRequest().body("User ID is required");
            }
            if (districtIds == null) {
                return ResponseEntity.badRequest().body("District IDs list is required");
            }

            userService.updateUserInterests(userId, districtIds);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Error updating user interests: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 