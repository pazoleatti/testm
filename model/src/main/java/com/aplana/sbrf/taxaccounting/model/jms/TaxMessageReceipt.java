package com.aplana.sbrf.taxaccounting.model.jms;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Технологическая квитанция
 */
@Getter
@Setter
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "TaxMessageReceipt")
public class TaxMessageReceipt extends BaseMessage {

    @XmlElement(name = "login")
    private String login;

    @XmlElement(name = "filename")
    private String filename;

    @XmlElement(name = "status")
    private MessageStatus status;

}
