package com.aplana.sbrf.taxaccounting.model.jms;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.NONE)
public class MessageStatus {
    @XmlElement(name = "code")
    private int code;
    @XmlElement(name = "detail")
    private String detail;
}
