package com.memorymap.memorymap.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/hello")
    public String sayHello(){
        return "Hello, MemoryMap!";
    }
}

// @RestController —
// tells Spring "this class handles web requests and returns raw data (text/JSON),
// not HTML pages."
//@GetMapping("/hello") — maps HTTP GET requests to /hello to the method below it.