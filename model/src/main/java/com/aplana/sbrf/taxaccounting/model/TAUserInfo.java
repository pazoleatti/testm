package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

public class TAUserInfo implements Serializable{
	private static final long serialVersionUID = 8435914534838847536L;
	
	private TAUser user;
	private String ip;
	
	public String getIp() {
		return ip;
	}
	
	public void setIp(String ip) {
		this.ip = ip;
	}

	public TAUser getUser() {
		return user;
	}

	public void setUser(TAUser user) {
		this.user = user;
	}

	@Override
	public String toString() {
		return "TAUserInfo [user=" + user + ", ip=" + ip + "]";
	}


}
