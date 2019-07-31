package com.aplana.sbrf.taxaccounting.model.jms;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.Date;

/**
 * Базовый класс для сообщений JMS
 */
@Getter
@Setter
@XmlAccessorType(XmlAccessType.NONE)
public abstract class BaseMessage {

    @XmlAttribute
    protected String formatVersion;

    @XmlElement(name = "UUID")
    private String uuid;

    @XmlElement(name = "datetime")
    private Date dateTime = new Date();

    @XmlElement(name = "source")
    private Integer source;

    @XmlElement(name = "destination")
    private Integer destination;
}
