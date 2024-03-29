package com.szymonharabasz.complexsystems.ui;

import jakarta.enterprise.context.Dependent;

@Dependent
public class GreetService {

    public String greet(String name) {
        if (name == null || name.isEmpty()) {
            return "Hello anonymous user";
        } else {
            return "Hello " + name;
        }
    }
}
