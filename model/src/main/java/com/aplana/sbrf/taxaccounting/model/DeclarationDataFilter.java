package com.aplana.sbrf.taxaccounting.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DeclarationDataFilter implements Serializable {

    private static final long serialVersionUID = -4400641153082281834L;

    private TaxType taxType;

    private List<Integer> reportPeriodIds;

    private List<Integer> departmentIds;

    private List<Long> declarationTypeIds;

    private State formState;

    private Boolean correctionTag;

    private Date correctionDate;

    /*Стартовый индекс списка записей */
    private int startIndex;

    /*Количество записей, которые нужно вернуть*/
    private int countOfRecords;

    private Long declarationDataId;

    private String declarationDataIdStr;

    private DeclarationDataSearchOrdering searchOrdering;

    private String taxOrganCode;

    private String taxOrganKpp;

    private String oktmo;

    private List<Long> docStateIds;

    private List<Long> formKindIds;

    private String fileName;

    private String note;

    private List<Long> asnuIds;

    /**
     * Подразделение пользователя, должно задаваться только пользователю с ролью Оператор
     */
    private Integer userDepartmentId;

    private Boolean controlNs;

    /*true, если сортируем по возрастанию, false - по убыванию*/
    private boolean ascSorting;

    /**
     * мапа ключ-идентификатор типа налоговой формы, значение-Список идентификаторов подразделений,
     * для которых подразделение пользователя назначено исполнителем
     */
    @JsonIgnore
    private Map<Integer, Set<Integer>> declarationTypeDepartmentMap;

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }

    public List<Integer> getReportPeriodIds() {
        return reportPeriodIds;
    }

    public void setReportPeriodIds(List<Integer> reportPeriodIds) {
        this.reportPeriodIds = reportPeriodIds;
    }

    public List<Integer> getDepartmentIds() {
        return departmentIds;
    }

    public void setDepartmentIds(List<Integer> departmentIds) {
        this.departmentIds = departmentIds;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getCountOfRecords() {
        return countOfRecords;
    }

    public void setCountOfRecords(int countOfRecords) {
        this.countOfRecords = countOfRecords;
    }

    public DeclarationDataSearchOrdering getSearchOrdering() {
        return searchOrdering;
    }

    public void setSearchOrdering(DeclarationDataSearchOrdering searchOrdering) {
        this.searchOrdering = searchOrdering;
    }

    public boolean isAscSorting() {
        return ascSorting;
    }

    public void setAscSorting(boolean ascSorting) {
        this.ascSorting = ascSorting;
    }

    public List<Long> getDeclarationTypeIds() {
        return declarationTypeIds;
    }

    public void setDeclarationTypeIds(List<Long> declarationTypeIds) {
        this.declarationTypeIds = declarationTypeIds;
    }

    public State getFormState() {
        return formState;
    }

    public void setFormState(State formState) {
        this.formState = formState;
    }

    public Long getDeclarationDataId() {
        return declarationDataId;
    }

    public void setDeclarationDataId(Long declarationDataId) {
        this.declarationDataId = declarationDataId;
    }

    public String getTaxOrganCode() {
        return taxOrganCode;
    }

    public void setTaxOrganCode(String taxOrganCode) {
        this.taxOrganCode = taxOrganCode;
    }

    public String getTaxOrganKpp() {
        return taxOrganKpp;
    }

    public void setTaxOrganKpp(String taxOrganKpp) {
        this.taxOrganKpp = taxOrganKpp;
    }

    public String getOktmo() {
        return oktmo;
    }

    public void setOktmo(String oktmo) {
        this.oktmo = oktmo;
    }

    public Boolean getCorrectionTag() {
        return correctionTag;
    }

    public void setCorrectionTag(Boolean correctionTag) {
        this.correctionTag = correctionTag;
    }

    public Date getCorrectionDate() {
        return correctionDate;
    }

    /**
     * Устанавливает дату корректировки. Действительно только при установленом {@link #correctionTag}
     *
     * @param correctionDate Дата корректировки
     */
    public void setCorrectionDate(Date correctionDate) {
        this.correctionDate = correctionDate;
    }

    public List<Long> getFormKindIds() {
        return formKindIds;
    }

    public void setFormKindIds(List<Long> formKindIds) {
        this.formKindIds = formKindIds;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<Long> getDocStateIds() {
        return docStateIds;
    }

    public void setDocStateIds(List<Long> docStateIds) {
        this.docStateIds = docStateIds;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getDeclarationDataIdStr() {
        return declarationDataIdStr;
    }

    public void setDeclarationDataIdStr(String declarationDataIdStr) {
        this.declarationDataIdStr = declarationDataIdStr;
    }

    public List<Long> getAsnuIds() {
        return asnuIds;
    }

    public void setAsnuIds(List<Long> asnuIds) {
        this.asnuIds = asnuIds;
    }

    public Integer getUserDepartmentId() {
        return userDepartmentId;
    }

    public void setUserDepartmentId(Integer userDepartmentId) {
        this.userDepartmentId = userDepartmentId;
    }

    public Boolean getControlNs() {
        return controlNs;
    }

    public void setControlNs(Boolean controlNs) {
        this.controlNs = controlNs;
    }

    public Map<Integer, Set<Integer>> getDeclarationTypeDepartmentMap() {
        return declarationTypeDepartmentMap;
    }

    public void setDeclarationTypeDepartmentMap(Map<Integer, Set<Integer>> declarationTypeDepartmentMap) {
        this.declarationTypeDepartmentMap = declarationTypeDepartmentMap;
    }
}
