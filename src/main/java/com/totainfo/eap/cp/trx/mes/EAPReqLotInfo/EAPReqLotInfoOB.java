package com.totainfo.eap.cp.trx.mes.EAPReqLotInfo;

/**
 * @author xiaobin.Guo
 * @date 2023年09月14日 11:16
 */
public class EAPReqLotInfoOB {
    private String TolerantOfContinusFails;
    private String IsLoadMAP;
    private String StartingCoordinates;
    private String FirstTestBIN;
    private String BackTestBIN;

    public String getTolerantOfContinusFails() {
        return TolerantOfContinusFails;
    }

    public void setTolerantOfContinusFails(String tolerantOfContinusFails) {
        TolerantOfContinusFails = tolerantOfContinusFails;
    }

    public String getIsLoadMAP() {
        return IsLoadMAP;
    }

    public void setIsLoadMAP(String isLoadMAP) {
        IsLoadMAP = isLoadMAP;
    }

    public String getStartingCoordinates() {
        return StartingCoordinates;
    }

    public void setStartingCoordinates(String startingCoordinates) {
        StartingCoordinates = startingCoordinates;
    }

    public String getFirstTestBIN() {
        return FirstTestBIN;
    }

    public void setFirstTestBIN(String firstTestBIN) {
        FirstTestBIN = firstTestBIN;
    }

    public String getBackTestBIN() {
        return BackTestBIN;
    }

    public void setBackTestBIN(String backTestBIN) {
        BackTestBIN = backTestBIN;
    }
}
