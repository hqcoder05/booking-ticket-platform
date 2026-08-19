package com.booking_ticket_platform.auth.mapper;

import com.booking_ticket_platform.auth.dto.UserDTO;
import com.booking_ticket_platform.auth.entity.User;
import java.util.List;

public class UserMapper {

    public static UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }

        return UserDTO.builder()
            .id(user.getId())
            .email(user.getEmail())
            .role(user.getRole())
            .build();   
    }

    public static List<UserDTO> toDTOList(List<User> users) {
        if (users == null) {
            return List.of();
        }

        return users.stream()
            .map(UserMapper::toDTO)
            .toList();
    }

    public static User toEntity(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }

        return User.builder()
            .id(userDTO.getId())
            .email(userDTO.getEmail())
            .role(userDTO.getRole())
            .build();
    }

    //Cap nhat thong tin Entity tu DTO
    public void updateEntityFromDTO(UserDTO userDTO, User user) {
        if (userDTO == null || user == null) {
            return;
        }

        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail());
        }

        if (userDTO.getRole() != null) {
            user.setRole(userDTO.getRole());
        }
    }
}
