package com.aplana.sbrf.taxaccounting.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.List;

/**
 * Тип спецотчета
 */
@Setter
@Getter
@ToString
public class DeclarationSubreport implements Ordered, Serializable {
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

    @XmlTransient
    public String getBlobDataId() {
        return blobDataId;
    }
}
