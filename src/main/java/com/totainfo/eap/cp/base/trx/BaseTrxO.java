package com.totainfo.eap.cp.base.trx;

public class BaseTrxO {

	private String trxId;
	private String trxType;
	private String rtnCode;
	private String rtnMesg;


	public String getTrxId() {
		return trxId;
	}

	public void setTrxId(String trxId) {
		this.trxId = trxId;
	}

	public String getTrxType() {
		return trxType;
	}

	public void setTrxType(String trxType) {
		this.trxType = trxType;
	}

	public String getRtnCode() {
		return rtnCode;
	}

	public void setRtnCode(String rtnCode) {
		this.rtnCode = rtnCode;
	}

	public String getRtnMesg() {
		return rtnMesg;
	}

	public void setRtnMesg(String rtnMesg) {
		this.rtnMesg = rtnMesg;
	}
}
