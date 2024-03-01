package com.totainfo.eap.cp.dao;

import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.entity.StateInfo;

public interface IStateDao {
    void addStateInfo(StateInfo stateInfo);

    void setStateInfo(String step, String state, String lotId);

    StateInfo getStateInfo();

    void removeState();
}
