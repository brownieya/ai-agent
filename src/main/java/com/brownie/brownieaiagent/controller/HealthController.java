package com.brownie.brownieaiagent.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//test
@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public String healthCheck() {return "success";}
}
