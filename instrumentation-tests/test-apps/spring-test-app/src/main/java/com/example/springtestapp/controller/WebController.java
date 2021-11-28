package com.example.springtestapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebController {
  private static final Logger LOGGER = LoggerFactory.getLogger(WebController.class);

  @RequestMapping("/greeting")
  public String greeting() {
    LOGGER.info("HTTP request received");
    return "Mock response";
  }
}
