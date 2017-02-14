package com.aplana.sbrf.taxaccounting.model;

import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.List;

/**
 * Модельный класс для спец отчетов
 * @author lhaziev
 */
public class DeclarationSubreport implements Ordered, Serializable{
	private static final long serialVersionUID = 1L;

    private long id;
    private String alias;
    private String name;
    private int order;
    private String blobDataId;
    private List<DeclarationSubreportParam> declarationSubreportParams;
    /**
     * Возможность поиска/выбора записи при формировании спец. отчета
     */
    private boolean selectRecord;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    @XmlTransient
    public String getBlobDataId() {
        return blobDataId;
    }

    public void setBlobDataId(String blobDataId) {
        this.blobDataId = blobDataId;
    }

    public List<DeclarationSubreportParam> getDeclarationSubreportParams() {
        return declarationSubreportParams;
    }

    public void setDeclarationSubreportParams(List<DeclarationSubreportParam> declarationSubreportParams) {
        this.declarationSubreportParams = declarationSubreportParams;
    }

    public boolean isSelectRecord() {
        return selectRecord;
    }

    public void setSelectRecord(boolean selectRecord) {
        this.selectRecord = selectRecord;
    }
}
