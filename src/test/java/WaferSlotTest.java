import com.totainfo.eap.cp.TotainfoEapApplication;
import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.dao.impl.StateDao;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoOB;
import com.totainfo.eap.cp.trx.mes.EAPReqLotInfo.EAPReqLotInfoOC;
import com.totainfo.eap.cp.util.JacksonUtils;
import lombok.NoArgsConstructor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author WangShuai
 * @date 2024/4/9
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TotainfoEapApplication.class)
@NoArgsConstructor
public class WaferSlotTest {
    private static Queue<String> slotMapQueue = new LinkedList<String>();
    @Resource
    private ILotDao lotDao;

    @Resource
    private StateDao stateDao;

    @Test
    public void waferSlotTest(){

        String sampleValue = null;
        LotInfo lotInfo = lotDao.getCurLotInfo();
        String slotId = "";
        EAPReqLotInfoOB clientLotInfo = lotDao.getClientLotInfo();
        for (EAPReqLotInfoOB eapReqLotInfoOB : lotInfo.getParamList1()) {
            if ("Sample".equals(eapReqLotInfoOB.getParamName())){
                sampleValue = eapReqLotInfoOB.getParamValue();
            }
        }
        EAPReqLotInfoOC eapReqLotInfoOC;
        EAPReqLotInfoOC eapReqLotInfoOCClient;
        if (slotMapQueue.isEmpty()){
            if (clientLotInfo == null){
                eapReqLotInfoOC = JacksonUtils.string2Object(sampleValue, EAPReqLotInfoOC.class);
                String[] split = eapReqLotInfoOC.getDatas().split(",");
                slotMapQueue.addAll(Arrays.asList(split));
                slotId = slotMapQueue.poll();
            }else{
                eapReqLotInfoOCClient = JacksonUtils.string2Object(clientLotInfo.getParamValue(), EAPReqLotInfoOC.class);
                if ("2".equals(eapReqLotInfoOCClient.getType())){
                    String[] split = eapReqLotInfoOCClient.getDatas().split(",");
                    slotMapQueue.addAll(Arrays.asList(split));
                    slotId = slotMapQueue.poll();
                }else{
                    eapReqLotInfoOC = JacksonUtils.string2Object(sampleValue, EAPReqLotInfoOC.class);
                    String datas = eapReqLotInfoOC.getDatas();
                    String[] split = datas.split(",");
                    int index = 0;

                    for (int i = 0; i < split.length; i++) {
                        if (eapReqLotInfoOCClient.getDatas().equals(split[i])){
                            index = i;
                            break;
                        }
                    }
                    String [] splitsNew = new String[split.length - index];
                    System.arraycopy(split,index,splitsNew,0,split.length - index);
                    slotMapQueue.addAll(Arrays.asList(splitsNew));
                    slotId = slotMapQueue.poll();
                }
            }
        }else {
            slotId = slotMapQueue.poll();
        }
    }
}