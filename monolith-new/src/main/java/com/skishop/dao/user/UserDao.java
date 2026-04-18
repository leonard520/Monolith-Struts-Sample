package com.skishop.dao.user;

import com.skishop.domain.user.User;

public interface UserDao {
    User findByEmail(String email);
    User findById(String id);
    void insert(User user);
    void updatePassword(String userId, String passwordHash, String salt);
    void updateStatus(String userId, String status);
}
