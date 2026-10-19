package com.energy.User.Service.service;

import com.energy.User.Service.DTO.UserRequest;
import com.energy.User.Service.DTO.UserResponse;
import com.energy.User.Service.entity.User;
import com.energy.User.Service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserResponse getUserById(final Long userId) {
        User user = userRepository.findById(userId).orElse(null);

        if (user == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");

        return toUserResponse(user);
    }

    public UserResponse createUser(UserRequest userRequest) {
        if(userRepository.existsByEmail(userRequest.getEmail()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");

        final User user = User.builder()
                .name(userRequest.getName())
                .surname(userRequest.getSurname())
                .email(userRequest.getEmail())
                .address(userRequest.getAddress())
                .alerting(userRequest.isAlerting())
                .energyAlertingThreshold(userRequest.getEnergyAlertingThreshold())
                .build();

        User savedUser = userRepository.save(user);
        return toUserResponse(savedUser);
    }

    public UserResponse updateUser(UserRequest userRequest, Long userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (userRepository.existsByEmailAndIdNot(userRequest.getEmail(), userId))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");

        user.setName(userRequest.getName());
        user.setSurname(userRequest.getSurname());
        user.setAddress(userRequest.getAddress());
        user.setEmail(userRequest.getEmail());
        user.setAlerting(userRequest.isAlerting());
        user.setEnergyAlertingThreshold(userRequest.getEnergyAlertingThreshold());

        final User updatedUser = userRepository.save(user);
        return toUserResponse(updatedUser);
    }

    public void deleteUserById(final Long userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        userRepository.delete(user);
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .email(user.getEmail())
                .address(user.getAddress())
                .alerting(user.isAlerting())
                .energyAlertingThreshold(user.getEnergyAlertingThreshold())
                .build();
    }
}
