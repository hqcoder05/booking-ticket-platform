package com.booking_ticket_platform.auth.service;

import org.springframework.stereotype.Service;
import com.booking_ticket_platform.auth.repository.UserRepository;
import com.booking_ticket_platform.auth.dto.UserDTO;
import com.booking_ticket_platform.auth.dto.UserUpdateRequest;
import com.booking_ticket_platform.shared.exception.ConflictException;
import com.booking_ticket_platform.shared.exception.DuplicateResourceException;
import org.springframework.transaction.annotation.Transactional;
import com.booking_ticket_platform.auth.entity.User;
import com.booking_ticket_platform.auth.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.booking_ticket_platform.auth.dto.UserCreateRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;
import com.booking_ticket_platform.shared.exception.ResourceNotFoundException;

@Service
public class UserService {



    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;



    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }



    @Transactional
    public UserDTO createUser(UserCreateRequest userCreateRequest) {
        // Logic to create a new user
        //1. Kiem tra trung lap email
        if (userRepository.existsByEmail(userCreateRequest.getEmail())) {
            throw new DuplicateResourceException("Ho so nguoi dung voi email: " + userCreateRequest.getEmail() + " da ton tai.");
        }

        //2. Chuyen doi UserDTO sang Entity (User) va luu vao CSDL
        User user = User.builder()
                .email(userCreateRequest.getEmail())
                .passwordHash(passwordEncoder.encode(userCreateRequest.getPassword())) 
                .role(userCreateRequest.getRole() != null ? userCreateRequest.getRole() : "CUSTOMER")
                .build();

        User savedUser = userRepository.save(user);

        return UserMapper.toDTO(savedUser);
    }



    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return userRepository.findAll(pageable).map(UserMapper::toDTO);
        } 
        return userRepository.searchUsers(search.trim(), pageable).map(UserMapper::toDTO);
    }



    @Transactional(readOnly = true)
    public UserDTO getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return UserMapper.toDTO(user);
    }



    @Transactional
    public UserDTO updateUser(UUID userId, UserUpdateRequest userUpdateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        
        userRepository.findByEmail(userUpdateRequest.getEmail())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(userId)) {
                        throw new DuplicateResourceException("Email da duoc su dung" + userUpdateRequest.getEmail());
                    }
                });
        


        user.setEmail(userUpdateRequest.getEmail());



        if (userUpdateRequest.getPassword() != null && !userUpdateRequest.getPassword().trim().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(userUpdateRequest.getPassword()));
        }



        User updateUser = userRepository.save(user);

        return UserMapper.toDTO(updateUser);
    }



    @Transactional
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        
        if (userRepository.hasRelatedRecords(userId)) {
            throw new ConflictException("Khong the xoa nguoi dung boi vi co ban ghi lien quan (viec lam, ca lam,...)");
        }

        userRepository.delete(user);
    }
}
