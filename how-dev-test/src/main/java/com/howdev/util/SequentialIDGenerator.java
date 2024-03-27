package com.howdev.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SequentialIDGenerator class
 *
 * @author haozhifeng
 * @date 2024/03/24
 */
public class SequentialIDGenerator {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final ConcurrentHashMap<String, AtomicLong> idMap = new ConcurrentHashMap<>();

    public static synchronized String generateID() {
        LocalDateTime dateTime = LocalDateTime.now();
        // 获取当前秒的时间戳
        String currentSecond = dateTime.format(FORMATTER);
        // 获取或创建当前秒的自增计数器
        AtomicLong counter = idMap.computeIfAbsent(currentSecond, k -> new AtomicLong(0));

        // 自增并获取当前ID
        long id = counter.incrementAndGet();
        if (id > 9999) {
            // 如果超过9999，则等待下一秒
            try {
                // 等待至下一秒开始
                Thread.sleep(1000 - (System.currentTimeMillis() % 1000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            // 递归调用以获取新ID
            return generateID();
        }

        // 返回格式化的ID
        return currentSecond + String.format("%04d", id);
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10000; i++) {
            System.out.println(generateID());
        }
    }
}
