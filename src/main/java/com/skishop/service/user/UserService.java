package com.skishop.service.user;

import com.skishop.dao.user.UserDao;
import com.skishop.domain.user.User;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User findByEmail(String email) {
        return userDao.findByEmail(email);
    }

    public User findById(String userId) {
        return userDao.findById(userId);
    }

    public void register(User user) {
        userDao.insert(user);
    }

    public void updatePassword(String userId, String passwordHash, String salt) {
        userDao.updatePassword(userId, passwordHash, salt);
    }
}
