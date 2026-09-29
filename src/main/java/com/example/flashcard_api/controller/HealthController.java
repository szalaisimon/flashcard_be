package com.example.flashcard_api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/{version}/health", version = "1")
@RequiredArgsConstructor
public class HealthController {

    @GetMapping()
    public boolean health(){
        return true;
    }
}
