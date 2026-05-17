package com.myforum;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.myforum.mapper")
public class MyForumApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyForumApplication.class, args);
    }
}
