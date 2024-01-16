package com.totainfo.eap.cp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author xiaobin.Guo
 * @date 2023年09月14日 10:10
 */
@EnableScheduling
@SpringBootApplication
public class TotainfoEapApplication  {
    public static void main(String[] args) {
        SpringApplication.run(TotainfoEapApplication.class, args);
    }
}
