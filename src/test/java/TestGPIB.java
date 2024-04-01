import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.tcp.server.EchoServerHandler;
import com.totainfo.eap.cp.trx.gpib.GPIBDeviceNameReport.GPIBDeviceNameReportI;
import com.totainfo.eap.cp.trx.gpib.GPIBLotEndReport.GPIBLotEndReportI;
import com.totainfo.eap.cp.trx.gpib.GPIBLotStartReport.GPIBLotStartReportI;
import com.totainfo.eap.cp.trx.gpib.GPIBWaferStartReport.GPIBWaferStartReportI;
import com.totainfo.eap.cp.util.AsyncUtils;
import com.totainfo.eap.cp.util.GUIDGenerator;
import com.totainfo.eap.cp.util.JacksonUtils;
import com.totainfo.eap.cp.util.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import javax.annotation.Resource;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.totainfo.eap.cp.commdef.GenericDataDef.equipmentNo;

/**
 * @author WangShuai
 * @date 2024/3/25
 */
public class TestGPIB {

    @Resource
    private ApplicationContext context;
    @Resource
    private  static EchoServerHandler echoServerHandler;

    private Map<String, ChannelHandlerContext> socketMap = new ConcurrentHashMap<>();

    @Test
    public void TestGBIP(){


          String message = "++device";

          String message1 = "= \"b\n" +
                  "\"\n" +
                  "RECEIVED[01,015]";
        message = message.replaceAll("\\r","")
                        .replaceAll("\\n","")
                        .replaceAll(" ","")
                        .replaceAll("=","")
                        .replaceAll("RECEIVED","")
                        .replaceAll("MSTRCVED","")
                        .replaceAll("\"","")
                        .replaceAll("SPERCVED","");

//        message = message.replaceAll("\\r","").replaceAll("\\n", "");
        String[] strs = message.split("\\[");
        String evtNo = GUIDGenerator.javaGUID();
        EapBaseService eapBaseService;
        for(String item:strs){
            if(item.startsWith("RECEIVED")){
                continue;
            }
            if(item.contains("++device")){
                GPIBLotEndReportI endReportI = new GPIBLotEndReportI();
                String result = item.substring(1).trim();
                System.out.println(result);
            }
        }
    }


    @Test
    public void changeModel(){
        changeMode("++device");
    }

    public static void changeMode(String modeCmmd) {
        if (modeCmmd.equals("++master")) {
            String s = echoServerHandler.sendForReply("GPIB", modeCmmd);
            System.out.println(s);
            String format ="++addr 1";
            String s1 = echoServerHandler.sendForReply("GPIB", format);
            System.out.println(s1);
        } else {
            String s = echoServerHandler.sendForReply("GPIB", modeCmmd);
            System.out.println(s);
        }

    }
}