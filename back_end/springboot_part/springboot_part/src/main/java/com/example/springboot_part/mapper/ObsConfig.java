package com.example.springboot_part.mapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.obs.services.ObsClient;;

@Configuration
public class ObsConfig {
    @Value("${huawei.obs.endpoint}")
    private String endpoint;

    @Value("${huawei.obs.access-key}")
    private String ak;

    @Value("${huawei.obs.secret-key}")
    private String sk;

    @Bean
    public ObsClient obsClient() {
        return new ObsClient(ak, sk, endpoint);
    }
}
