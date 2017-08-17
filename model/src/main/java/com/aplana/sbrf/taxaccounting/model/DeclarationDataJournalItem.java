package com.aplana.sbrf.taxaccounting.model;

import org.joda.time.LocalDateTime;

import java.io.Serializable;

/**
 * DTO-Класс, содержащий информацию о параметрах декларации и связанных с ним объектов в "плоском" виде
 * Используется для таблицы "Список налоговых форм"
 */

public class DeclarationDataJournalItem implements Serializable {
    private static final long serialVersionUID = -5255606476850599681L;

    // Идентификатор записи с данными декларации
    private Long declarationDataId;

    /**
     * Тип налоговой форы
     */
    private String declarationKind;

    /**
     * Вид налоговой форы
     */
    private String declarationType;
    /**
     * Название подразделения
     */
    private String department;

    /**
     * АСНУ
     */
    private String asnuName;
    /**
     * Период
     */
    private String reportPeriod;
    /**
     * Статус налоговой формы
     */
    private String state;
    /**
     * Файл
     */
    private String fileName;
    /**
     * Дата и время создания формы
     */
    private LocalDateTime creationDate;

    /**
     * Создал
     */
    private String creationUserName;

    public Long getDeclarationDataId() {
        return declarationDataId;
    }

    public void setDeclarationDataId(Long declarationDataId) {
        this.declarationDataId = declarationDataId;
    }

    public String getDeclarationKind() {
        return declarationKind;
    }

    public void setDeclarationKind(String declarationKind) {
        this.declarationKind = declarationKind;
    }

    public String getDeclarationType() {
        return declarationType;
    }

    public void setDeclarationType(String declarationType) {
        this.declarationType = declarationType;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getAsnuName() {
        return asnuName;
    }

    public void setAsnuName(String asnuName) {
        this.asnuName = asnuName;
    }

    public String getReportPeriod() {
        return reportPeriod;
    }

    public void setReportPeriod(String reportPeriod) {
        this.reportPeriod = reportPeriod;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public String getCreationUserName() {
        return creationUserName;
    }

    public void setCreationUserName(String creationUserName) {
        this.creationUserName = creationUserName;
    }
}
