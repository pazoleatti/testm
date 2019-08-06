package com.aplana.sbrf.taxaccounting.model.jms;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Сообщение для отправки в ЭДО
 */
@Getter
@Setter
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "TaxMessageDocument")
public class TaxMessageDocument extends BaseMessage {

    @XmlElement(name = "login")
    private String login;

    @XmlElement(name = "filename")
    private String filename;
}
