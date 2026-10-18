package com.projects.Device.Service;

import com.projects.Device.Service.entity.Device;
import com.projects.Device.Service.model.DeviceType;
import com.projects.Device.Service.repository.DeviceRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

@Slf4j
@SpringBootTest
class DeviceServiceApplicationTests {
    public static final int NUMBER_OF_DEVICES = 200;
    public static final int USERS = 10;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
    }

    @Disabled("Enable manually to populate sample devices after generating users.")
    @Test
    void createDevices() {
        List<Long> userIds = jdbcTemplate.queryForList(
                "SELECT id FROM users ORDER BY id LIMIT ?", Long.class, USERS);
        if (userIds.size() < USERS) {
            throw new IllegalStateException("Create at least " + USERS + " users before generating devices.");
        }

        for (int i = 1; i <= NUMBER_OF_DEVICES; i++) {
            var device = Device.builder()
                    .name("Device" + i)
                    .type(DeviceType.values()[i % DeviceType.values().length])
                    .location("Location" + ((i % 3) + 1))
                    .userId(userIds.get(i % userIds.size()))
                    .build();
            deviceRepository.save(device);
        }
        log.info("Device Repository has been populated");
    }
}
