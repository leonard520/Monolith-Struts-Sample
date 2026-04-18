package com.skishop.service.user;

import com.skishop.dao.user.UserDao;
import com.skishop.domain.user.User;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class UserService {

    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User findById(String userId) {
        return userDao.findById(userId);
    }

    public User findByEmail(String email) {
        return userDao.findByEmail(email);
    }

    public User register(User user) {
        if (user.getId() == null) {
            user.setId(UUID.randomUUID().toString());
        }
        Date now = new Date();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        if (user.getStatus() == null) {
            user.setStatus("ACTIVE");
        }
        if (user.getRole() == null) {
            user.setRole("USER");
        }
        userDao.insert(user);
        return user;
    }

    public void updatePassword(String userId, String passwordHash, String salt) {
        userDao.updatePassword(userId, passwordHash, salt);
    }
}
