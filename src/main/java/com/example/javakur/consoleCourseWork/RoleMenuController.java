package com.example.javakur.consoleCourseWork;

import com.example.javakur.security.UserSession;

import java.util.List;

interface RoleMenuController {
    List<String> options();
    void handle(UserSession session, int choice);
}
