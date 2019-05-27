package com.aplana.sbrf.taxaccounting.model.jms;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * Сообщение для отправки в ЭДО
 */
@Getter
@Setter
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "TaxMessageDocument")
public class TaxMessageDocument extends BaseMessage {

    @XmlElement(name = "UUID")
    private String uuid;

    @XmlElement(name = "datetime")
    private Date dateTime = new Date();

    @XmlElement(name = "source")
    private Integer source;

    @XmlElement(name = "destination")
    private Integer destination;

    @XmlElement(name = "login")
    private String login;

    @XmlElement(name = "filename")
    private String filename;
}
