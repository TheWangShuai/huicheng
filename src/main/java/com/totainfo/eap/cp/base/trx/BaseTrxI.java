package com.totainfo.eap.cp.base.trx;

public class BaseTrxI {
	private String trxId;
	private String trypeId;
	private String actionFlg;

	public String getTrxId() {
		return trxId;
	}

	public void setTrxId(String trxId) {
		this.trxId = trxId;
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
