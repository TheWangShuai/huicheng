package com.totainfo.eap.cp.base.trx;

public class BaseTrxI {
	private String trxId;
	private String trxName;
	private String trypeId;
	private String actionFlg;

	public String getTrxId() {
		return trxId;
	}

	public void setTrxId(String trxId) {
		this.trxId = trxId;
	}

	public String getTrxName() {
		return trxName;
	}

	public void setTrxName(String trxName) {
		this.trxName = trxName;
	}

	public String getTrypeId() {
		return trypeId;
	}

	public void setTrypeId(String trypeId) {
		this.trypeId = trypeId;
	}

	public String getActionFlg() {
		return actionFlg;
	}

	public void setActionFlg(String actionFlg) {
		this.actionFlg = actionFlg;
	}
}
