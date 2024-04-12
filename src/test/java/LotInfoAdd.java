import com.fasterxml.jackson.core.SerializableString;
import com.totainfo.eap.cp.TotainfoEapApplication;
import com.totainfo.eap.cp.commdef.GenergicStatDef;
import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.dao.impl.StateDao;
import com.totainfo.eap.cp.entity.DieCountInfo;
import com.totainfo.eap.cp.entity.DielInfo;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.entity.StateInfo;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoO;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoOB;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoOC;
import com.totainfo.eap.cp.util.JacksonUtils;
import com.totainfo.eap.cp.util.LogUtils;
import lombok.NoArgsConstructor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author WangShuai
 * @date 2024/3/29
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TotainfoEapApplication.class)
@NoArgsConstructor
public class LotInfoAdd {


    @Resource
    private ILotDao lotDao;

    @Resource
    private StateDao stateDao;

    private static Queue<String> slotMapQueue = new LinkedList<String>();

    @Test
    public void LotInfoAddToRedis(){

//        EAPReqLotInfoOB clientLotInfo = lotDao.getClientLotInfo();
//        LotInfo lotInfo = lotDao.getCurLotInfo();
//        //todo 上报mes
//        String slotId = "";
//        if (slotMapQueue.isEmpty()){
//            if (clientLotInfo == null){
//                String sampleValue = null;
//                for (EAPReqLotInfoOB eapReqLotInfoOB : lotInfo.getParamList1()) {
//                    if ("Sample".equals(eapReqLotInfoOB.getParamName())){
//                        sampleValue = eapReqLotInfoOB.getParamValue();
//                    }
//                }
//                EAPReqLotInfoOC eapReqLotInfoOC = JacksonUtils.string2Object(sampleValue, EAPReqLotInfoOC.class);
//                String[] split = eapReqLotInfoOC.getDatas().split(",");
//                slotMapQueue.addAll(Arrays.asList(split));
//            }else{
//                EAPReqLotInfoOC eapReqLotInfoOC = JacksonUtils.string2Object(clientLotInfo.getParamValue(), EAPReqLotInfoOC.class);
//                String[] split = eapReqLotInfoOC.getDatas().split(",");
//                slotMapQueue.addAll(Arrays.asList(split));
//            }
//        }else {
//            slotId = slotMapQueue.poll();
//            System.out.println(slotId);
//        }




        LotInfo lotInfo = getLotInfo();
        lotDao.addLotInfo(lotInfo);
        LotInfo curLotInfo = lotDao.getCurLotInfo();
        StateInfo stateInfo = stateDao.getStateInfo();
        if(stateInfo == null) {
            stateInfo = new StateInfo();
        }
        stateInfo.setStep(GenergicStatDef.StepName.EIGTH);
        stateInfo.setState(GenergicStatDef.StepStat.COMP);
        stateDao.addStateInfo(stateInfo);

    }

    private static LotInfo getLotInfo() {
        LotInfo lotInfo = new LotInfo();
        String lotInfo1 = "{\n" +
                "    \"rtnCode\": \"0000000\",\n" +
                "    \"rtnMesg\": \"操作成功\",\n" +
                "    \"lotInfo\": {\n" +
                "        \"waferLot\": \"NHJ278\",\n" +
                "        \"device\": \"8989916N-1AU6A13\",\n" +
                "        \"probeCard\": \"AP9916P1S99\",\n" +
                "        \"testProgram\": \"ICNL9916CWAC1D_CP1_1D_ND4_C10\",\n" +
                "        \"loadBoardId\": \"NA\",\n" +
                "        \"deviceId\": \"NA\",\n" +
                "        \"waferId\": null,\n" +
                "        \"temperatureRange\": \"3\",\n" +
                "        \"paramList\": [\n" +
                "            {\n" +
                "                \"paramName\": \"ProductName\",\n" +
                "                \"paramValue\": \"ICNL9916CWAC-HND-P\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"paramName\": \"PicPath\",\n" +
                "                \"paramValue\": \"\\\\\\\\10.8.10.28\\\\RunCardImages\\\\CP\\\\890\\\\ICNL9916-CP.jpg\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"paramName\": \"Temp\",\n" +
                "                \"paramValue\": \"25\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"paramName\": \"MapPostion\",\n" +
                "                \"paramValue\": \"0.0\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"paramName\": \"RangeTemp\",\n" +
                "                \"paramValue\": \"3\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"paramName\": \"Program\",\n" +
                "                \"paramValue\": \"ICNL9916CWAC1D_CP1_1D_ND4_C10\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"paramName\": \"Reference Die\",\n" +
                "                \"paramValue\": \"(56.0);(56.6)\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"paramName\": \"FunctionKey\",\n" +
                "                \"paramValue\": \"1\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"paramName\": \"SetupFile\",\n" +
                "                \"paramValue\": \"8989916N-1AU6A13\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"paramName\": \"BackTest\",\n" +
                "                \"paramValue\": \"[\\\"20\\\",\\\"21\\\",\\\"22\\\",\\\"23\\\",\\\"24\\\",\\\"25\\\",\\\"26\\\",\\\"27\\\",\\\"28\\\",\\\"29\\\",\\\"32\\\",\\\"33\\\",\\\"34\\\",\\\"35\\\",\\\"36\\\",\\\"37\\\",\\\"38\\\",\\\"39\\\",\\\"41\\\",\\\"42\\\",\\\"43\\\",\\\"44\\\",\\\"45\\\",\\\"46\\\",\\\"47\\\",\\\"48\\\",\\\"49\\\",\\\"50\\\",\\\"51\\\",\\\"53\\\",\\\"54\\\",\\\"55\\\",\\\"56\\\",\\\"57\\\",\\\"58\\\",\\\"60\\\",\\\"72\\\",\\\"74\\\",\\\"75\\\",\\\"80\\\",\\\"81\\\",\\\"82\\\",\\\"83\\\",\\\"85\\\",\\\"86\\\",\\\"87\\\",\\\"88\\\",\\\"89\\\",\\\"90\\\",\\\"91\\\",\\\"93\\\",\\\"94\\\",\\\"95\\\",\\\"96\\\",\\\"97\\\",\\\"98\\\",\\\"99\\\",\\\"100\\\",\\\"101\\\",\\\"102\\\",\\\"103\\\",\\\"104\\\",\\\"105\\\",\\\"106\\\",\\\"107\\\",\\\"108\\\",\\\"109\\\",\\\"111\\\",\\\"112\\\",\\\"113\\\",\\\"114\\\",\\\"116\\\",\\\"117\\\",\\\"119\\\",\\\"120\\\",\\\"121\\\"]\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"paramName\": \"LoadMap\",\n" +
                "                \"paramValue\": \"Y\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"paramName\": \"FEPoint\",\n" +
                "                \"paramValue\": \"(65.0);(194.7)\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"paramName\": \"FirstTest\",\n" +
                "                \"paramValue\": \"[\\\"63\\\"]\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"paramName\": \"Sample\",\n" +
                "                \"paramValue\": \"{\\\"type\\\":\\\"1\\\",\\\"datas\\\":\\\"03,04,05,09,10,11,12,15\\\"}\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"paramName\": \"AlarmNum\",\n" +
                "                \"paramValue\": \"10\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"paramName\": \"DieCount\",\n" +
                "                \"paramValue\": \"50\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"paramName\": \"OpNo\",\n" +
                "                \"paramValue\": \"P1120\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"paramName\": \"CustLotNo\",\n" +
                "                \"paramValue\": \"NHJ278000\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"paramName\": \"WaferDevice\",\n" +
                "                \"paramValue\": \"ICNL9916CWAC\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}";

        DielInfo dielInfo = new DielInfo();
        dielInfo.setResult("false");
        dielInfo.setCoordinate("Y004X004");
        dielInfo.setSiteNum("1");
        List<DielInfo> infos = new ArrayList<>();
        infos.add(dielInfo);
        infos.add(dielInfo);
        infos.add(dielInfo);
        infos.add(dielInfo);
        infos.add(dielInfo);
        infos.add(dielInfo);
        infos.add(dielInfo);
        infos.add(dielInfo);
        Map<String, List<DielInfo>> waferDieMap ;

        waferDieMap = new HashMap<>();
        waferDieMap.put("waferDieMap",infos);
        EAPReqLotInfoO eapReqLotInfoO = JacksonUtils.string2Object(lotInfo1, EAPReqLotInfoO.class);
//        HashMap<String, String > stringMap  = new HashMap<String, String>(){{
//            put("WaferLot",eapReqLotInfoO.getLotInfo().getWaferLot());
//        }};
        lotInfo.setLotId("123214");
        lotInfo.setWaferLot(eapReqLotInfoO.getLotInfo().getWaferLot());
        lotInfo.setDevice(eapReqLotInfoO.getLotInfo().getDevice());
        lotInfo.setLoadBoardId(eapReqLotInfoO.getLotInfo().getLoadBoardId());
        lotInfo.setProberCard(eapReqLotInfoO.getLotInfo().getProbeCard());
        lotInfo.setTestProgram(eapReqLotInfoO.getLotInfo().getTestProgram());
        lotInfo.setDeviceId(eapReqLotInfoO.getLotInfo().getDeviceId());
        lotInfo.setUserId("HF0731");
        lotInfo.setDieCount(eapReqLotInfoO.getLotInfo().getDieCount());
        lotInfo.setParamList(eapReqLotInfoO.getLotInfo().getParamList());
        lotInfo.setTemperatureRange(eapReqLotInfoO.getLotInfo().getTemperatureRange());
        return lotInfo;
    }

    @Test
    public void addClientLotInfo(){
        String type = "2";
        String datas = "12,14,16,18,20,22,24";
        EAPReqLotInfoOC eapReqLotInfoOC = new EAPReqLotInfoOC();
        eapReqLotInfoOC.setDatas(datas);
        eapReqLotInfoOC.setType(type);
        String sampleClientValue = JacksonUtils.object2String(eapReqLotInfoOC);
        EAPReqLotInfoOB reqLotInfoOB = new EAPReqLotInfoOB();
        reqLotInfoOB.setParamName("Sample");
        reqLotInfoOB.setParamValue(sampleClientValue);
        lotDao.addClientLotInfo(reqLotInfoOB);
    }


    @Test
    public void programTest(){

        String proGram = "D:\\PGM\\9951BB_BAC_CP1SN4_240315\\9951BB_BAC_CP1SN4_240315.tdl";
        String[] split = proGram.split("\\\\");
        String probeProgram = split[2];
        System.out.println(split);
    }
}
