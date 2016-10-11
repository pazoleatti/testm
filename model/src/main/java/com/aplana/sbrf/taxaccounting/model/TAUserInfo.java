package com.aplana.sbrf.taxaccounting.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UserInfo", propOrder = {
		"user",
		"ip"
})
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
