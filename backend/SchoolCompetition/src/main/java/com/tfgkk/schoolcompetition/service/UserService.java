package com.tfgkk.schoolcompetition.service;

import com.tfgkk.schoolcompetition.dto.UserDTO;
import com.tfgkk.schoolcompetition.dto.UserLoginDTO;
import com.tfgkk.schoolcompetition.entity.User;
import java.util.List;

public interface UserService {
    UserDTO login(UserLoginDTO loginDTO);
    UserDTO register(User user);
    List<UserDTO> getAllUsers();
    void deleteUser(Long id);
    void updateProfile(UserDTO userDTO);
    void updatePassword(Long userId, String oldPassword, String newPassword);
    void importStudents(MultipartFile file) throws IOException;
    void updateStudentAccount(Long id, String username, String password);
}
