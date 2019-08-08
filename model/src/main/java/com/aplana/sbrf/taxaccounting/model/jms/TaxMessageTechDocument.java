package com.aplana.sbrf.taxaccounting.model.jms;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Ответ из ФНС
 */
@Getter
@Setter
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "TaxMessageTechDocument")
public class TaxMessageTechDocument extends BaseMessage {

    @XmlElement(name = "parentDocument")
    private String parentDocument;

    @XmlElement(name = "parentFileName")
    private String parentFileName;

    @XmlElement(name = "documentType")
    private String documentType;

    @XmlElement(name = "filename")
    private String fileName;

    @XmlElement(name = "status")
    private MessageStatus status;

    // todo additionalData
}
