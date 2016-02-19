package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * Модельный класс для спец отчетов
 * @author lhaziev
 */
public class DeclarationSubreport implements Ordered, Serializable{
	private static final long serialVersionUID = 1L;

    private long id;
    private int declarationTemplateId;
    private String alias;
    private String name;
    private int order;
    private String blobDataId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getDeclarationTemplateId() {
        return declarationTemplateId;
    }

    public void setDeclarationTemplateId(int declarationTemplateId) {
        this.declarationTemplateId = declarationTemplateId;
    }

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
}
