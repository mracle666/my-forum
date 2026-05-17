package com.myforum.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.myforum.common.PasswordUtils;
import com.myforum.mapper.UserMapper;
import com.myforum.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;

    @Override
    public void run(String... args) {
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getRole, 1)) == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@myforum.com");
            admin.setPassword(PasswordUtils.hash("admin123"));
            admin.setRole(1);
            admin.setStatus(1);
            admin.setBio("系统管理员");
            userMapper.insert(admin);
            log.info("已创建默认管理员账号: admin / admin123");
        }
    }
}
