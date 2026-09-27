package dev.logMonitor.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
public class DashboardController {

    Logger logger = LoggerFactory.getLogger(DashboardController.class);
    @Value("classpath:templates/dashboard.html")
    private Resource dashboardTemplate;

    @GetMapping(value = "/dashboard", produces = MediaType.TEXT_HTML_VALUE)
    public String getDashboard() throws IOException {
        logger.info("someoneTrying to get dashboard");
        return dashboardTemplate.getContentAsString(StandardCharsets.UTF_8);
    }
}
