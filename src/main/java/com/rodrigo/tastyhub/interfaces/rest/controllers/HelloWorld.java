package com.rodrigo.tastyhub.interfaces.rest.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello-world")
public class HelloWorld {
    @GetMapping
    public String helloWorld() {
        System.out.println("Olá");
        return "Hello World!";
    }
}
