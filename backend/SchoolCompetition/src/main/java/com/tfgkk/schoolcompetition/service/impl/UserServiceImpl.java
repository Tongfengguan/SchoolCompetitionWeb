package com.tfgkk.schoolcompetition.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.tfgkk.schoolcompetition.common.BCryptUtil;
import com.tfgkk.schoolcompetition.common.BusinessException;
import com.tfgkk.schoolcompetition.common.JwtUtil;
import com.tfgkk.schoolcompetition.dto.UserDTO;
import com.tfgkk.schoolcompetition.dto.UserLoginDTO;
import com.tfgkk.schoolcompetition.entity.User;
import com.tfgkk.schoolcompetition.repository.RegistrationRepository;
import com.tfgkk.schoolcompetition.repository.UserRepository;
import com.tfgkk.schoolcompetition.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDTO login(UserLoginDTO loginDTO) {
        User user = userRepository.findByUsername(loginDTO.getUsername())
                .orElseThrow(() -> new BusinessException("账号或密码错误"));

        if (!BCryptUtil.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }

        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);

        // 🔥 签发 JWT Token
        String token = JwtUtil.generateToken(user.getUsername(), user.getRole());
        userDTO.setToken(token);

        return userDTO;
    }

    @Override
    public UserDTO register(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new BusinessException("用户名已存在");
        }
        if (user.getRole() == null) {
            user.setRole("student");
        }
        // 密码加密
        user.setPassword(BCryptUtil.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(savedUser, userDTO);
        return userDTO;
    }

    @Override
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(user -> {
            UserDTO dto = new UserDTO();
            BeanUtils.copyProperties(user, dto);
            return dto;
        });
    }

    @Autowired
    private RegistrationRepository registrationRepository;

    @Override
    @Transactional
    public void deleteUser(Long id) {
        // ✨ 先查出用户名（学号）
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            // 根据用户名清理报名记录
            registrationRepository.deleteByStudentId(user.getUsername());
        }
        
        // 再删除用户本身
        userRepository.deleteById(id);
    }

    @Override
    public void updateProfile(UserDTO userDTO) {
        User user = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new BusinessException("用户不存在"));
        user.setName(userDTO.getName());
        user.setPhone(userDTO.getPhone());
        userRepository.save(user);
    }

    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        if (!BCryptUtil.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        
        user.setPassword(BCryptUtil.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public void importStudents(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), User.class, new PageReadListener<User>(dataList -> {
            for (User item : dataList) {
                if (item.getPhone() == null || item.getPhone().isBlank()) continue;
                if (userRepository.findByUsername(item.getPhone()).isPresent()) continue;

                User student = new User();
                student.setName(item.getName());
                student.setPhone(item.getPhone());
                student.setUsername(item.getPhone());
                student.setPassword(BCryptUtil.encode(item.getPhone())); // 默认使用手机号作为密码
                student.setRole("student");

                userRepository.save(student);
            }
        })).sheet().doRead();
    }

    @Override
    public void updateStudentAccount(Long id, String username, String password) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (username != null && !username.isBlank()) {
            Optional<User> existing = userRepository.findByUsername(username);
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                throw new BusinessException("该账号名已存在");
            }
            user.setUsername(username);
        }

        if (password != null && !password.isBlank()) {
            user.setPassword(BCryptUtil.encode(password));
        }

        userRepository.save(user);
    }
}
