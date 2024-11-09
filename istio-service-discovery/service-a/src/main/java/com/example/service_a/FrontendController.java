package com.example.service_a;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class FrontendController {
    private static final Logger logger = LoggerFactory.getLogger(FrontendController.class);
    @Autowired
    private final RestTemplate restTemplate;

    @Value("${SERVICE_B_URL}")
    private String serviceBUrl;

    public FrontendController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/service-a/call-service-b")
    public String callServiceB() {
        logger.info("the service b url is "+serviceBUrl);
        String response = restTemplate.getForObject(serviceBUrl + "/service2/hello", String.class);
        return "Microservice A calling -> " + response;
    }
}