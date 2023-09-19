package com.totainfo.eap.cp.dao;


import com.totainfo.eap.cp.entity.EqptInfo;

import java.util.List;

public interface IEqptDao {

    boolean addEqpt(EqptInfo eqptInfo);

    EqptInfo getEqpt();

    EqptInfo getEqptWithLock();
}
