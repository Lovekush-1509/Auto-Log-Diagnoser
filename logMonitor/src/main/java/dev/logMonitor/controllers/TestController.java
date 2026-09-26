package dev.logMonitor.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    Logger  logger = LoggerFactory.getLogger(TestController.class);

    @GetMapping("/sayHello")
    public String test(){
        logger.info("request coming for test............................");
        return "say Hello";
    }
}
