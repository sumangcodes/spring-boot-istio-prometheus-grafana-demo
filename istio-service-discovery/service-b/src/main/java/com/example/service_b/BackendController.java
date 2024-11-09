package com.example.service_b;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BackendController {

    @GetMapping("/service2/hello")
    public String sayHello() {
        return "Hello from Microservice 2";
    }
}