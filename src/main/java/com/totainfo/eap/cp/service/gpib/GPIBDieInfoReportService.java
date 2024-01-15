package com.totainfo.eap.cp.service.gpib;

import com.totainfo.eap.cp.base.service.EapBaseService;
import com.totainfo.eap.cp.dao.ILotDao;
import com.totainfo.eap.cp.entity.DielInfo;
import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.handler.ClientHandler;
import com.totainfo.eap.cp.handler.MesHandler;
import com.totainfo.eap.cp.trx.gpib.GPIBDieInfoReport.GPIBDieInfoReportI;
import com.totainfo.eap.cp.trx.gpib.GPIBDieInfoReport.GPIBDieInfoReportO;
import com.totainfo.eap.cp.trx.mes.EAPUploadDieResult.EAPUploadDieResultO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant.RETURN_CODE_OK;

/**
 * @author xiaobin.Guo
 * @date 2024年01月14日 15:45
 */
@Service("dieInfoReport")
public class GPIBDieInfoReportService extends EapBaseService<GPIBDieInfoReportI, GPIBDieInfoReportO> {

    @Resource
    private ILotDao lotDao;

    @Override
    public void mainProc(String evtNo, GPIBDieInfoReportI inTrx, GPIBDieInfoReportO outTrx) {
        LotInfo lotInfo = lotDao.getCurLotInfo();
        if(lotInfo == null){
            return;
        }
        String waferId = inTrx.getWaferId();
        Map<String, List<DielInfo>> waferDieMap = lotInfo.getWaferDieMap();
        if(waferDieMap == null){
            waferDieMap = new HashMap<>();
        }
        List<DielInfo> dielInfos = waferDieMap.get(waferId);
        if(dielInfos == null){
            dielInfos = new ArrayList<>();
        }
        String siteNum = inTrx.getSiteNum();
        String result = inTrx.getResult();
        String coorDinate = inTrx.getStartCoorDinate();
        DielInfo dielInfo = parserResult(result, Integer.parseInt(siteNum), coorDinate);
        dielInfos.add(dielInfo);

        int dieCount = lotInfo.getDieCount() == 0 ? 30: lotInfo.getDieCount();
        if(dielInfos.size() >= dieCount){
            String lotNo = lotInfo.getLotId();
            String userId = lotInfo.getUserId();
            EAPUploadDieResultO eapUploadDieResultO = MesHandler.uploadDieResult(evtNo, lotNo, waferId, dielInfos, userId);
            if (!RETURN_CODE_OK.equals(eapUploadDieResultO.getRtnCode())) {
                outTrx.setRtnCode(eapUploadDieResultO.getRtnCode());
                outTrx.setRtnMesg(eapUploadDieResultO.getRtnMesg());
                ClientHandler.sendMessage(evtNo, false, 2, outTrx.getRtnMesg());
                return;
            }
            //上报后将已经上报的数据清空，重新累计
            dielInfos.clear();
        }
        waferDieMap.put(waferId, dielInfos);
        lotInfo.setWaferDieMap(waferDieMap);
        lotDao.addLotInfo(lotInfo);
    }



    public DielInfo parserResult(String testreult, int siteNum,String coordinate) {
        DielInfo dielInfo = new DielInfo();
        dielInfo.setCoordinate(coordinate);

        String site = null;
        String result = null;
        int bytenum = getReultBytenum(siteNum);
        for (int i = 1; i <= bytenum; i++)
        {
            String testret = stringToBinary(testreult.substring(i - 1, bytenum));
            int lth = testret.length();
            for (int k = 1; k <= siteNum; k++) {
                result = (testret.charAt(lth - k) == '0') ? "Pass" : "Fail";
                site = "site" + (k + (i - 1) * 4) + "Result";
                if (k >= 4){
                    break;
                }

            }
        }
        dielInfo.setSiteNum(site);
        dielInfo.setResult(result);
        return dielInfo;
    }



    private  int getReultBytenum(int sitenum)
    {
        int tt = sitenum / 4;
        int ll=sitenum % 4;
        return tt + (ll > 0 ? 1 : 0);
    }

    private  String stringToBinary(String str)
    {
        byte[] data = str.getBytes();
        StringBuilder sb = new StringBuilder("");
        for (byte item :data) {
            sb.append(binary2decimal(item, 8));
        }
        return sb.toString();
    }

    public String binary2decimal(int decNum , int digit) {
        StringBuffer binStr = new StringBuffer();
        for(int i= digit-1;i>=0;i--) {
            binStr.append((decNum>>i)&1);
        }
        return binStr.toString();
    }
}
