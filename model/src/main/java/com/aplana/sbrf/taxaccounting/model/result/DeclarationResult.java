package com.aplana.sbrf.taxaccounting.model.result;

/**
 * Модель для передачи данных о ПНФ/КНФ
 */
public class DeclarationResult {
    /**
     * Подразделение
     */
    private String department;
    /**
     * Период
     */
    private String reportPeriod;
    /**
     * Год периода
     */
    private Integer reportPeriodYear;
    /**
     * Состояние
     */
    private String state;
    /**
     * АСНУ
     */
    private String asnuName;
    /**
     * Тип налоговой форы
     */
    private String declarationFormKind;
    /**
     * Создал
     */
    private String creationUserName;
    /**
     * Дата и время создания формы
     */
    private String creationDate;
    /**
     * Права
     */
    private long permissions;

    /**
     * Вид НФ
     */
    private int declarationType;

    /**
     * КПП
     */
    private String kpp;

    /**
     * ОКТМО
     */
    private String oktmo;

    /**
     * Код налогового органа
     */
    private String taxOrganCode;

    /**
     * Состояние ЭД
     */
    private String docState;

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getReportPeriod() {
        return reportPeriod;
    }

    public void setReportPeriod(String reportPeriod) {
        this.reportPeriod = reportPeriod;
    }

    public Integer getReportPeriodYear() {
        return reportPeriodYear;
    }

    public void setReportPeriodYear(Integer reportPeriodYear) {
        this.reportPeriodYear = reportPeriodYear;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getAsnuName() {
        return asnuName;
    }

    public void setAsnuName(String asnuName) {
        this.asnuName = asnuName;
    }

    public String getDeclarationFormKind() {
        return declarationFormKind;
    }

    public void setDeclarationFormKind(String declarationFormKind) {
        this.declarationFormKind = declarationFormKind;
    }

    public String getCreationUserName() {
        return creationUserName;
    }

    public void setCreationUserName(String creationUserName) {
        this.creationUserName = creationUserName;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public long getPermissions() {
        return permissions;
    }

    public void setPermissions(long permissions) {
        this.permissions = permissions;
    }

    public int getDeclarationType() {
        return declarationType;
    }

    public void setDeclarationType(int declarationType) {
        this.declarationType = declarationType;
    }

    public String getKpp() {
        return kpp;
    }

    public void setKpp(String kpp) {
        this.kpp = kpp;
    }

    public String getOktmo() {
        return oktmo;
    }

    public void setOktmo(String oktmo) {
        this.oktmo = oktmo;
    }

    public String getTaxOrganCode() {
        return taxOrganCode;
    }

    public void setTaxOrganCode(String taxOrganCode) {
        this.taxOrganCode = taxOrganCode;
    }

    public String getDocState() {
        return docState;
    }

    public void setDocState(String docState) {
        this.docState = docState;
    }
}
