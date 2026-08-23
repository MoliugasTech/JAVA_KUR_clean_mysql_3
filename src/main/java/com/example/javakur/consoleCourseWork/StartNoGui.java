package com.example.javakur.consoleCourseWork;

import com.example.javakur.repository.UserRepository;
import com.example.javakur.service.AuthenticationService;
import com.example.javakur.util.HibernateUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class StartNoGui {
    public static void main(String[] args) {
        AuthenticationService authenticationService =
                new AuthenticationService(new UserRepository());
        MenuControl menuControl = new MenuControl(
                authenticationService,
                new BufferedReader(new InputStreamReader(System.in)),
                System.out);

        try {
            menuControl.run();
        } finally {
            HibernateUtil.shutdown();
        }
    }
}
