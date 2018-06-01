package com.aplana.sbrf.taxaccounting.model.consolidation;

import java.util.Date;

public class ConsolidationSourceDataSearchFilter {
    private Date currentDate;
    private Date periodStartDate;
    private Date periodEndDate;
    private Integer dataSelectionDepth;
    private Integer departmentId;
    private Integer declarationType;
    private Integer consolidateDeclarationDataYear;

    private ConsolidationSourceDataSearchFilter(Date currentDate, Date periodStartDate, Date periodEndDate, Integer dataSelectionDepth, Integer departmentId, Integer declarationType, Integer consolidateDeclarationDataYear) {
        this.currentDate = currentDate;
        this.periodStartDate = periodStartDate;
        this.periodEndDate = periodEndDate;
        this.dataSelectionDepth = dataSelectionDepth;
        this.departmentId = departmentId;
        this.declarationType = declarationType;
        this.consolidateDeclarationDataYear = consolidateDeclarationDataYear;
    }

    public Date getCurrentDate() {
        return currentDate;
    }

    public Date getPeriodStartDate() {
        return periodStartDate;
    }

    public Date getPeriodEndDate() {
        return periodEndDate;
    }

    public Integer getDataSelectionDepth() {
        return dataSelectionDepth;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public Integer getDeclarationType() {
        return declarationType;
    }

    public Integer getConsolidateDeclarationDataYear() {
        return consolidateDeclarationDataYear;
    }

    public static class Builder {
        private Date nestedCurrentDate;
        private Date nestedPeriodStartDate;
        private Date nestedPeriodEndDate;
        private Integer nestedDataSelectionDepth;
        private Integer nestedDepartmentId;
        private Integer nestedDeclarationType;
        private Integer nestedConsolidateDeclarationDataYear;

        public Builder currentDate(Date currentDate) {
            this.nestedCurrentDate = currentDate;
            return this;
        }

        public Builder periodStartDate(Date nestedPeriodStartDate) {
            this.nestedPeriodStartDate = nestedPeriodStartDate;
            return this;
        }

        public Builder periodEndDate(Date periodEndDate) {
            this.nestedPeriodEndDate = periodEndDate;
            return this;
        }

        public Builder dataSelectionDepth(Integer dataSelectionDepth) {
            this.nestedDataSelectionDepth = dataSelectionDepth;
            return this;
        }

        public Builder departmentId(Integer departmentId) {
            this.nestedDepartmentId = departmentId;
            return this;
        }

        public Builder declarationType(Integer declarationType) {
            this.nestedDeclarationType = declarationType;
            return this;
        }

        public Builder consolidateDeclarationDataYear(Integer consolidateDeclarationDataYear) {
            this.nestedConsolidateDeclarationDataYear = consolidateDeclarationDataYear;
            return this;
        }

        public ConsolidationSourceDataSearchFilter createConsolidationSourceDataSearchFilter() {
            return new ConsolidationSourceDataSearchFilter(nestedCurrentDate, nestedPeriodStartDate, nestedPeriodEndDate, nestedDataSelectionDepth, nestedDepartmentId, nestedDeclarationType, nestedConsolidateDeclarationDataYear);
        }
    }
}
