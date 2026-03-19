package com.melonecom;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching   // 开启Spring Boot基于注解的缓存管理支持
@MapperScan("com.melonecom.mapper")
@SpringBootApplication
public class MelonEcomApplication {
      public static void main(String[] args) {
        SpringApplication.run(MelonEcomApplication.class, args);
    }
}
