package com.aplana.sbrf.taxaccounting.model.jms;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.*;
import java.util.Date;

/**
 * Базовый класс для сообщений JMS
 */
@Getter
@Setter
@XmlRootElement
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
