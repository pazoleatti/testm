package com.aplana.sbrf.taxaccounting.model.jms;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * Базовый класс для сообщений JMS
 */
@Getter
@Setter
@XmlAccessorType(XmlAccessType.NONE)
public abstract class BaseMessage {

    @XmlAttribute
    protected String formatVersion;

}
