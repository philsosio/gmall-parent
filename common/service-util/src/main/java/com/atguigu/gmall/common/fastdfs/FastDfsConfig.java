package com.atguigu.gmall.common.fastdfs;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Import(FastDfsClient.class)
@PropertySource("classpath:aaa.properties")
public class FastDfsConfig {
}
