package com.aplana.sbrf.taxaccounting.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DeclarationTemplateContent {
	@XmlElement
	private DeclarationType type;

	public DeclarationType getType() {
		return type;
	}

	public void setType(DeclarationType type) {
		this.type = type;
	}
}
