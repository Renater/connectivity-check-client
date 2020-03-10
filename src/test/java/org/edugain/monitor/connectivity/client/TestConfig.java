package org.edugain.monitor.connectivity.client;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootConfiguration
@EnableAutoConfiguration

@ComponentScan(basePackages = { "org.edugain.monitor.connectivity.client" })
@TestConfiguration
public class TestConfig {

}
