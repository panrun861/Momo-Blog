package xyz.kuailemao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
public class PasswordGeneratorTest {

    // 注入项目中已配置的PasswordEncoder（和重置密码用的是同一个）
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void generateBcryptPassword() {
        // 你要手工设置的明文密码
        String rawPassword = "Prpr2001!";

        // 生成加密后的密码
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // 打印结果（复制这个结果即可用）
        System.out.println("明文密码：" + rawPassword);
        System.out.println("加密后密码：" + encodedPassword);
    }
}