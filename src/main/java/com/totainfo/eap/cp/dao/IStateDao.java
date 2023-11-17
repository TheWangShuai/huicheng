package com.totainfo.eap.cp.dao;

import com.totainfo.eap.cp.entity.LotInfo;
import com.totainfo.eap.cp.entity.StateInfo;

public interface IStateDao {
    void addStateInfo(StateInfo stateInfo);

    StateInfo getStateInfo();
}
