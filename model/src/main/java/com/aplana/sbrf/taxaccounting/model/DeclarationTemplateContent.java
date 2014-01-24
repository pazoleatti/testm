package com.aplana.sbrf.taxaccounting.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DeclarationTemplateContent {
	@XmlElement
	private DeclarationType type;
	@XmlElement
	private Date version;

	public DeclarationType getType() {
		return type;
	}

	public void setType(DeclarationType type) {
		this.type = type;
	}

	public Date getVersion() {
		return version;
	}

	public void setVersion(Date version) {
		this.version = version;
	}
}
