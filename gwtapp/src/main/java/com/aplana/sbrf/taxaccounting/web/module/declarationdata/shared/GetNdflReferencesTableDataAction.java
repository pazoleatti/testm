package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Date;

public class GetNdflReferencesTableDataAction extends UnsecuredActionImpl<GetNdflReferencesResult> {
    Long declarationDataId;
    String refNumber;
    String lastNamePattrern;
    String firstNamePattern;
    String middleNamePattern;
    Date birthDateFrom;
    Date birthDateBefore;

    public Long getDeclarationDataId() {
        return declarationDataId;
    }

    public void setDeclarationDataId(Long declarationDataId) {
        this.declarationDataId = declarationDataId;
    }

    public String getRefNumber() {
        return refNumber;
    }

    public void setRefNumber(String refNumber) {
        this.refNumber = refNumber;
    }

    public String getLastNamePattrern() {
        return lastNamePattrern;
    }

    public void setLastNamePattrern(String lastNamePattrern) {
        this.lastNamePattrern = lastNamePattrern;
    }

    public String getFirstNamePattern() {
        return firstNamePattern;
    }

    public void setFirstNamePattern(String firstNamePattern) {
        this.firstNamePattern = firstNamePattern;
    }

    public String getMiddleNamePattern() {
        return middleNamePattern;
    }

    public void setMiddleNamePattern(String middleNamePattern) {
        this.middleNamePattern = middleNamePattern;
    }

    public Date getBirthDateFrom() {
        return birthDateFrom;
    }

    public void setBirthDateFrom(Date birthDateFrom) {
        this.birthDateFrom = birthDateFrom;
    }

    public Date getBirthDateBefore() {
        return birthDateBefore;
    }

    public void setBirthDateBefore(Date birthDateBefore) {
        this.birthDateBefore = birthDateBefore;
    }

    @Override
    public String toString() {
        return "GetNdflReferencesTableDataAction{" +
                "declarationDataId=" + declarationDataId +
                ", refNumber='" + refNumber + '\'' +
                ", lastNamePattrern='" + lastNamePattrern + '\'' +
                ", firstNamePattern='" + firstNamePattern + '\'' +
                ", middleNamePattern='" + middleNamePattern + '\'' +
                ", birthDateFrom=" + birthDateFrom +
                ", birthDateBefore=" + birthDateBefore +
                '}';
    }
}
