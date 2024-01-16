package com.totainfo.eap.cp.tcp.job;

import com.totainfo.eap.cp.tcp.server.EchoServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TcpServerStartJob implements CommandLineRunner {



    @Value("${tcp.server.port}")
    private int serverPort;

    @Autowired
    private EchoServer echoServer;

    @Override
    public void run(String... args) {
        new Thread(()->{
            echoServer.start(serverPort);
        }).start();

    }
}
