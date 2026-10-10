package com.ledgr;

import com.ledgr.config.DotenvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LedgrApplication {

    public static void main(String[] args) {
        DotenvLoader.load();
        SpringApplication.run(LedgrApplication.class, args);
    }
}
