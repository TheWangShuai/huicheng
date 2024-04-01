import com.totainfo.eap.cp.TotainfoEapApplication;
import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.entity.DielInfo;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.handler.MesHandler;
import com.totainfo.eap.cp.trx.mes.EAPUploadDieResult.EAPUploadDieResultO;
import com.totainfo.eap.cp.util.LogUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_CODE_OK;

/**
 * @author WangShuai
 * @date 2024/3/29
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TotainfoEapApplication.class)
public class DieInfoTest {


    @Resource
    private ILotDao lotDao;


    @Test
    public void testDieInfo() {

        LotInfo curLotInfo = lotDao.getCurLotInfo();
        String waferId = "214124";
        Map<String, List<DielInfo>> waferDieMap = curLotInfo.getWaferDieMap();
        if (waferDieMap == null) {
            waferDieMap = new HashMap<>();
        }
        List<DielInfo> dielInfos = waferDieMap.get(waferId);
        if (dielInfos == null) {
            dielInfos = new ArrayList<>();
        }
        String siteNum = "1";
        String result = "Pass";
        String coorDinate = "Y004X004";
        LogUtils.info("从GPIB获取的数据为： + siteNum [" + siteNum + " ] ,result:[" + result + "]  ,coorDinate:[" + coorDinate + "]");
        DielInfo dielInfo = parserResult(result, Integer.parseInt(siteNum), coorDinate);
        LogUtils.info("获取的DieInfo数据为： " + dielInfo);

        dielInfos.add(dielInfo);

        int dieCount = curLotInfo.getDieCount() == 0 ? 30 : curLotInfo.getDieCount();
        if (dielInfos.size() >= dieCount) {
            dielInfos.clear();
        }
        waferDieMap.put(waferId, dielInfos); //214124 214124
        curLotInfo.setWaferDieMap(waferDieMap);
    }


    public DielInfo parserResult(String testreult, int siteNum, String coordinate) {
        DielInfo dielInfo = new DielInfo();
        dielInfo.setCoordinate(coordinate);

        String site = null;
        String result = null;
        int bytenum = getReultBytenum(siteNum);
        for (int i = 1; i <= bytenum; i++) {
            String testret = stringToBinary(testreult.substring(i - 1, bytenum));
            int lth = testret.length();
            for (int k = 1; k <= siteNum; k++) {
                result = (testret.charAt(lth - k) == '0') ? "Pass" : "Fail";
                site = "site" + (k + (i - 1) * 4) + "Result";
                if (k >= 4) {
                    break;
                }

            }
        }
        dielInfo.setSiteNum(site);
        dielInfo.setResult(result);
        return dielInfo;
    }

    private int getReultBytenum(int sitenum) {
        int tt = sitenum / 4;
        int ll = sitenum % 4;
        return tt + (ll > 0 ? 1 : 0);
    }

    private String stringToBinary(String str) {
        byte[] data = str.getBytes();
        StringBuilder sb = new StringBuilder("");
        for (byte item : data) {
            sb.append(binary2decimal(item, 8));
        }
        return sb.toString();
    }

    public String binary2decimal(int decNum, int digit) {
        StringBuffer binStr = new StringBuffer();
        for (int i = digit - 1; i >= 0; i--) {
            binStr.append((decNum >> i) & 1);
        }
        return binStr.toString();
    }

}