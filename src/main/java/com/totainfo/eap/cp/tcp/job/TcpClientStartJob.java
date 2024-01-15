package com.totainfo.eap.cp.tcp.job;

import com.totainfo.eap.cp.tcp.client.EchoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TcpClientStartJob implements CommandLineRunner {



    @Value("${tcp.server.ip}")
    private String serverIp;

    @Value("${tcp.server.port}")
    private int serverPort;

    @Autowired
    private EchoClient echoClient;

    @Override
    public void run(String... args) throws Exception {
        String eqptId = "GPIB";
        new Thread(()->{
                echoClient.start(eqptId, serverIp, serverPort);
        }).start();

    }
}
