package com.energy.User.Service.controller;

import com.energy.User.Service.DTO.UserRequest;
import com.energy.User.Service.DTO.UserResponse;
import com.energy.User.Service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("{userId}")
    public UserResponse getUserById(@PathVariable("userId") Long userId) {
        return userService.getUserById(userId);
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody UserRequest userRequest) {
        UserResponse createdUser = userService.createUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PutMapping("{userId}")
    public ResponseEntity<UserResponse> updateUser(@RequestBody UserRequest userRequest,
                                                   @PathVariable("userId") Long userId) {
        UserResponse updatedUserResponse = userService.updateUser(userRequest,  userId);
        return ResponseEntity.ok().body(updatedUserResponse);
    }

    @DeleteMapping("{userId}")
    public ResponseEntity<Void> deleteUserById(@PathVariable("userId") Long userId) {
        userService.deleteUserById(userId);
        return ResponseEntity.noContent().build();
    }
}
