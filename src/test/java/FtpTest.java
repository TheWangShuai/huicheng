import com.totainfo.eap.cp.TotainfoEapApplication;
import com.totainfo.eap.cp.trx.ems.EMSDeviceParameterReport.EMSDeviceParameterReportIA;
import com.totainfo.eap.cp.util.FtpUtils;
import com.totainfo.eap.cp.util.LogUtils;
import lombok.NoArgsConstructor;
import org.apache.commons.net.ftp.FTPClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xiaobin.Guo
 * @date 2024年01月30日 9:12
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TotainfoEapApplication.class)
@NoArgsConstructor
public class FtpTest {


    @Value("${ftp.path}")
    private String path;

    @Test
    public void test(){
        Path dataPath = Paths.get(System.getProperty("user.dir")+ File.separator+"tmp", "data","MES_DATA" + ".TXT");
        FTPClient ftpClient = new FTPClient();
        if (!Files.exists(dataPath.getParent())){
            try {
                Files.createDirectories(dataPath.getParent());
            } catch (IOException e) {
                LogUtils.error("文件夹创建失败");
            }
        }
        ftpClient.enterLocalPassiveMode();
        StringBuilder sb = new StringBuilder();
        sb.append("TP_NAME:").append("123").append("\n")
                .append("TESTER_ID:").append("").append("\n")
                .append("PART_NO:").append("").append("\n")
                .append("QTY:").append("").append("\n")
                .append("C_LOT:").append("").append("\n")
                .append("HANDLER:").append("NA").append("\n")
                .append("PROBER:").append("NA").append("\n")
                .append("LOADBOARD:").append("NA").append("\n")
                .append("PROBERCARD:").append("456").append("\n")
                .append("SOCKET:").append("NA").append("\n")
                .append("PROCESS:").append("").append("\n")
                .append("STEP:").append("").append("\n")
                .append("RETEST:").append("NA").append("\n")
                .append("RT_BIN:").append("NA").append("\n")
                .append("SUBCON_NAME:").append("").append("\n")
                .append("SUBCON_LOT:").append("").append("\n")
                .append("DATE_CODE:").append("NA").append("\n")
                .append("OPID:").append("").append("\n")
                .append("PASS_BIN:").append("").append("\n")
                .append("WORK_MODE:").append("").append("\n")
                .append("EXTENSION:").append("").append("\n");
        try {
            Files.write(dataPath, sb.toString().getBytes());
            FtpUtils.uploadFile("localhost", "administrator", "123", 21, path,dataPath.toString());
        } catch (IOException e) {
            LogUtils.error("xtr文件写入失败");
        }
    }

    @Test
    public void listTest(){
        List<EMSDeviceParameterReportIA> emsDeviceParameterReportIAS = new ArrayList<>(2);

        EMSDeviceParameterReportIA emsDeviceParameterReportIA = new EMSDeviceParameterReportIA();
        emsDeviceParameterReportIA.setParamCode("1");
        emsDeviceParameterReportIA.setParamName("温度");
        emsDeviceParameterReportIA.setParamValue("1");
        emsDeviceParameterReportIAS.add(emsDeviceParameterReportIA);

        emsDeviceParameterReportIA = new EMSDeviceParameterReportIA();
        emsDeviceParameterReportIA.setParamCode("2");
        emsDeviceParameterReportIA.setParamName("温度范围");
        emsDeviceParameterReportIA.setParamValue("1");
        emsDeviceParameterReportIAS.add(emsDeviceParameterReportIA);
        System.out.println(emsDeviceParameterReportIAS.size());

    }
}
