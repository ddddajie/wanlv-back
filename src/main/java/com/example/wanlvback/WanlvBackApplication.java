package com.example.wanlvback;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@MapperScan("com.example.wanlvback.mapper")
@EnableScheduling
public class WanlvBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(WanlvBackApplication.class, args);
    }
}
