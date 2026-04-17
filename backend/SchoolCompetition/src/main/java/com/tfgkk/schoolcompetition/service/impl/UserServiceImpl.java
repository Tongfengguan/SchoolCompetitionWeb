package com.tfgkk.schoolcompetition.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.tfgkk.schoolcompetition.common.BCryptUtil;
import com.tfgkk.schoolcompetition.common.BusinessException;
import com.tfgkk.schoolcompetition.dto.UserDTO;
import com.tfgkk.schoolcompetition.dto.UserLoginDTO;
import com.tfgkk.schoolcompetition.entity.User;
import com.tfgkk.schoolcompetition.repository.UserRepository;
import com.tfgkk.schoolcompetition.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(user -> {
            UserDTO dto = new UserDTO();
            BeanUtils.copyProperties(user, dto);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long id) {
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
