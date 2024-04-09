import com.totainfo.eap.cp.TotainfoEapApplication;
import com.totainfo.eap.cp.commdef.GenergicStatDef;
import com.totainfo.eap.cp.dao.impl.LotDao;
import com.totainfo.eap.cp.dao.impl.StateDao;
import com.totainfo.eap.cp.entity.DieCountInfo;
import com.totainfo.eap.cp.entity.StateInfo;
import com.totainfo.eap.cp.handler.ClientHandler;
import com.totainfo.eap.cp.trx.client.EAPReportDieInfo.DieInfoOA;
import com.totainfo.eap.cp.util.DateUtils;
import com.totainfo.eap.cp.util.LogUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.*;


/**
 * @author WangShuai
 * @date 2024/4/2
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TotainfoEapApplication.class)
@NoArgsConstructor
public class TestTemp {


    @Resource
    private LotDao lotDao;

    private static StateDao stateDao;
    @Test
    public void testTemp() throws InterruptedException {




        String evtNo = UUID.randomUUID().toString();
        StateInfo stateInfo = stateDao.getStateInfo();
        if (stateInfo == null){
            stateInfo = new StateInfo();
        }
            stateInfo.setStep("1");
            stateInfo.setState("1");
            stateInfo.setLotNo("123456789");
            stateDao.addStateInfo(stateInfo);


        DieCountInfo dieCountInfo = new DieCountInfo();
        List<DieInfoOA> dieInfoOAS = new ArrayList<>();
        DieInfoOA dieInfoOA = new DieInfoOA();
        DieInfoOA dieInfoOA1 = new DieInfoOA();
        DieInfoOA dieInfoOAOld = new DieInfoOA();



        String waferId = "EAP013451";
        String waferId1 = "EAqweqewP013451";
        String currentDate = DateUtils.getcurrentTimestampStr("yyyy-MM-dd HH:mm:ss");
        Thread.sleep(5000);
        String currentDate1 = DateUtils.getcurrentTimestampStr("yyyy-MM-dd HH:mm:ss");
        dieInfoOA.setWaferStartTime(currentDate);
        dieInfoOA.setWorkId(waferId);
        dieInfoOA.setDeviceName("NESADFGAWE_SAK1324123");
        dieInfoOA1.setWaferStartTime(currentDate1);
        dieInfoOA1.setWorkId(waferId1);
        dieInfoOA1.setDeviceName("qwddsagwer_dafsd");

        dieInfoOAS.add(dieInfoOA);
        dieInfoOAS.add(dieInfoOA1);
        dieCountInfo.setDieInfoOAS(dieInfoOAS);
        lotDao.addDieCount(dieCountInfo);


        Thread.sleep(10000);


        DieCountInfo dieCount = lotDao.getDieCount();
        for (DieInfoOA infoOA : dieCount.getDieInfoOAS()) {

            if ("EAP013451".equals(infoOA.getWorkId())){

                dieInfoOAOld = dieInfoOA;
            }
        }

        dieInfoOAOld.setDieCount(56);
        lotDao.addDieCount(dieCountInfo);
        Map<String, String> waferTime = lotDao.getWaferTime();
        // 使用正则表达式提取数字部分
        String numericPart = "28";
        String temperatureRang = "3";
        int tempRang = Integer.parseInt(temperatureRang);

        String temperature = "25";
        int tem = Integer.parseInt(temperature);

        double value = Double.parseDouble(numericPart);
        if(value < tem - tempRang || value > tem + tempRang){
            System.out.println("KVM采集的温度不在范围内");
        }

    }
    @Test
    public  void setFlowStep(){
        String step = "1";
        String stepSate = "1";
        String evtNo = UUID.randomUUID().toString();
        StateInfo stateInfo = stateDao.getStateInfo();
        LogUtils.info("Redis中存在的状态信息为: " + stateInfo);
        if (stateInfo == null){
            ClientHandler.sendMessage(evtNo,false,2,"设备的步骤信息在Redis中不存在!");
            return;
        }
        if (Integer.parseInt(step) >= Integer.parseInt(stateInfo.getStep())){
            stateInfo.setStep(step);
            stateInfo.setState(stepSate);
            stateDao.addStateInfo(stateInfo);
        }
    }
//    @Test
//    public String testReturn(EapReportMESDataInfoO eapReportMESDataInfoO){
//
//        eapReportMESDataInfoO.setRtnCode(GenergicStatDef.Constant.SERVICE_EXCEPTION);
//        eapReportMESDataInfoO.setRtnMesg("系统发生异常，请联系管理员");
//
//        String returnMsg = JacksonUtils.object2String(eapReportMESDataInfoO);
//
//        return returnMsg;
//    }
}