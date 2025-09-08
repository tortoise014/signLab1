package com.signlab1;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.signlab1.mapper")
public class SignLab1Application {

    public static void main(String[] args) {
        SpringApplication.run(SignLab1Application.class, args);
    }

}
