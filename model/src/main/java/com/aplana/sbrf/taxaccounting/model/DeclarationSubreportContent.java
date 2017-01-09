package com.aplana.sbrf.taxaccounting.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;


@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DeclarationSubreportContent implements Ordered, Serializable{
	private static final long serialVersionUID = 1L;

    private String alias;
    private String name;
    private int order;
    private String blobDataId;
    private String fileName;
    private List<DeclarationSubreportParamContent> subreportParams;

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getBlobDataId() {
        return blobDataId;
    }

    public void setBlobDataId(String blobDataId) {
        this.blobDataId = blobDataId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<DeclarationSubreportParamContent> getSubreportParams() {
        return subreportParams;
    }

    public void setSubreportParams(List<DeclarationSubreportParamContent> subreportParams) {
        this.subreportParams = subreportParams;
    }
}
