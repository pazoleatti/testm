package com.aplana.sbrf.taxaccounting.model.source;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Данные  связок источники-приемники с клиента
 * @author Denis Loshkarev
 */
public class SourceClientData implements Serializable{
    private static final long serialVersionUID = -6352210000657626039L;

    /** Данные связок источников-приемников */
    private List<SourcePair> sourcePairs;
    /** Начало периода действия назначений */
    private Date periodStart;
    /** Окончание периода действия назначений */
    private Date periodEnd;
    /** Подразделение-источник */
    private Integer sourceDepartmentId;
    /** Подразделение-приемник */
    private Integer destinationDepartmentId;
    /** Начало периода в текстовом представлении. Используется в обработке ошибок */
    private String periodStartName;
    /** Окончание периода в текстовом представлении. Используется в обработке ошибок */
    private String periodEndName;
    /** Режим работы */
    private SourceMode mode;
    /** Признак того, что идет обработка назначений источников для деклараций */
    private Boolean isDeclaration;


    /** --- Используется только при редактировании --- */
    /** Начало старого периода действия назначений */
    private Date oldPeriodStart;
    /** Окончание старого периода действия назначений */
    private Date oldPeriodEnd;
    /** Начало старого периода в текстовом представлении. Используется в обработке ошибок */
    private String oldPeriodStartName;
    /** Окончание старого периода в текстовом представлении. Используется в обработке ошибок */
    private String oldPeriodEndName;

    public String getOldPeriodStartName() {
        return oldPeriodStartName;
    }

    public void setOldPeriodStartName(String oldPeriodStartName) {
        this.oldPeriodStartName = oldPeriodStartName;
    }

    public String getOldPeriodEndName() {
        return oldPeriodEndName;
    }

    public void setOldPeriodEndName(String oldPeriodEndName) {
        this.oldPeriodEndName = oldPeriodEndName;
    }

    public Date getOldPeriodStart() {
        return oldPeriodStart;
    }

    public void setOldPeriodStart(Date oldPeriodStart) {
        this.oldPeriodStart = oldPeriodStart;
    }

    public Date getOldPeriodEnd() {
        return oldPeriodEnd;
    }

    public void setOldPeriodEnd(Date oldPeriodEnd) {
        this.oldPeriodEnd = oldPeriodEnd;
    }

    public Boolean getDeclaration() {
        return isDeclaration;
    }

    public List<SourcePair> getSourcePairs() {
        return sourcePairs;
    }

    public void setSourcePairs(List<SourcePair> sourcePairs) {
        this.sourcePairs = sourcePairs;
    }

    public Date getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(Date periodStart) {
        this.periodStart = periodStart;
    }

    public Date getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(Date periodEnd) {
        this.periodEnd = periodEnd;
    }

    public Integer getSourceDepartmentId() {
        return sourceDepartmentId;
    }

    public void setSourceDepartmentId(Integer sourceDepartmentId) {
        this.sourceDepartmentId = sourceDepartmentId;
    }

    public Integer getDestinationDepartmentId() {
        return destinationDepartmentId;
    }

    public void setDestinationDepartmentId(Integer destinationDepartmentId) {
        this.destinationDepartmentId = destinationDepartmentId;
    }

    public SourceMode getMode() {
        return mode;
    }

    public void setMode(SourceMode mode) {
        this.mode = mode;
    }

    public String getPeriodStartName() {
        return periodStartName;
    }

    public void setPeriodStartName(String periodStartName) {
        this.periodStartName = periodStartName;
    }

    public String getPeriodEndName() {
        return periodEndName;
    }

    public void setPeriodEndName(String periodEndName) {
        this.periodEndName = periodEndName;
    }

    public Boolean isDeclaration() {
        return isDeclaration;
    }

    public void setDeclaration(Boolean declaration) {
        isDeclaration = declaration;
    }
}
