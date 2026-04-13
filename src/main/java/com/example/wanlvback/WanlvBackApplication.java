package com.example.wanlvback;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@ConfigurationPropertiesScan
@MapperScan("com.example.wanlvback.mapper")
public class WanlvBackApplication {
    public static void main(String[] args) {
        SpringApplication.run(WanlvBackApplication.class, args);
    }

}
