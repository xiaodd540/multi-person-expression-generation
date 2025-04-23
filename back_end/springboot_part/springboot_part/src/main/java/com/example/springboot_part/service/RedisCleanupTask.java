package com.example.springboot_part.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Component
public class RedisCleanupTask implements CommandLineRunner {

    private final RedisService redisService;

    public RedisCleanupTask(RedisService redisService) {
        this.redisService = redisService;
    }

    // 服务器启动时检查是否有未执行的任务
    @Override
    public void run(String... args) {
        long now = System.currentTimeMillis() / 1000;
        long lastMidnight = LocalDateTime.now().withHour(3).withMinute(0).withSecond(0).toEpochSecond(ZoneOffset.UTC);

        List<Integer> users = redisService.findAllUserId();
        for (Integer userId : users) {
            if (now > lastMidnight) { // 说明错过了凌晨 6 点的任务
                System.out.println("⚠️ 服务器启动时发现凌晨 6 点任务未执行，正在补偿...");
                redisService.cleanOldRecords(userId); // 执行清理任务
            }
        }
    }

    // 正常情况下，每天凌晨 6 点执行任务
    @Scheduled(cron = "0 0 6 * * ?")
    public void scheduledCleanup() {
        List<Integer> users = redisService.findAllUserId();
        for (Integer userId : users) {
            redisService.cleanOldRecords(userId); // 执行清理任务
        }
    }
}

