package com.aplana.sbrf.taxaccounting.model.refbook;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;
import com.aplana.sbrf.taxaccounting.model.SecuredEntity;
import com.aplana.sbrf.taxaccounting.model.json.ISODateDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * Настройки подразделений
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true, of = {"kpp", "oktmo", "startDate", "endDate"})
public class DepartmentConfig extends IdentityObject<Long> implements SecuredEntity {

    private String kpp;
    private RefBookOktmo oktmo;

    @JsonDeserialize(using = ISODateDeserializer.class)
    private Date startDate;
    @JsonDeserialize(using = ISODateDeserializer.class)
    private Date endDate;

    private Integer rowOrd;
    private RefBookDepartment department;
    private String taxOrganCode;
    private RefBookPresentPlace presentPlace;
    private String name;
    private String phone;
    private RefBookSignatoryMark signatoryMark;
    private String signatorySurName;
    private String signatoryFirstName;
    private String signatoryLastName;
    private String approveDocName;
    private String approveOrgName;
    private RefBookReorganization reorganization;
    private String reorgKpp;
    private String reorgInn;
    private long permissions;

    @Builder(toBuilder = true)
    public DepartmentConfig(Long id, String kpp, RefBookOktmo oktmo, Date startDate, Date endDate, RefBookDepartment department, String taxOrganCode, RefBookPresentPlace presentPlace, String name, String phone, RefBookSignatoryMark signatoryMark, String signatorySurName, String signatoryFirstName, String signatoryLastName, String approveDocName, String approveOrgName, RefBookReorganization reorganization, String reorgKpp, String reorgInn) {
        super(id);
        this.kpp = kpp;
        this.oktmo = oktmo;
        this.startDate = startDate;
        this.endDate = endDate;
        this.department = department;
        this.taxOrganCode = taxOrganCode;
        this.presentPlace = presentPlace;
        this.name = name;
        this.phone = phone;
        this.signatoryMark = signatoryMark;
        this.signatorySurName = signatorySurName;
        this.signatoryFirstName = signatoryFirstName;
        this.signatoryLastName = signatoryLastName;
        this.approveDocName = approveDocName;
        this.approveOrgName = approveOrgName;
        this.reorganization = reorganization;
        this.reorgKpp = reorgKpp;
        this.reorgInn = reorgInn;
    }

    public DepartmentConfig id(Long id) {
        this.id = id;
        return this;
    }

    public DepartmentConfig kpp(String kpp) {
        this.kpp = kpp;
        return this;
    }

    public DepartmentConfig oktmo(RefBookOktmo oktmo) {
        this.oktmo = oktmo;
        return this;
    }

    public DepartmentConfig startDate(Date startDate) {
        this.startDate = startDate;
        return this;
    }

    public DepartmentConfig endDate(Date endDate) {
        this.endDate = endDate;
        return this;
    }

    public DepartmentConfig department(RefBookDepartment department) {
        this.department = department;
        return this;
    }

    public DepartmentConfig taxOrganCode(String taxOrganCode) {
        this.taxOrganCode = taxOrganCode;
        return this;
    }

    public DepartmentConfig presentPlace(RefBookPresentPlace presentPlace) {
        this.presentPlace = presentPlace;
        return this;
    }

    public DepartmentConfig signatoryMark(RefBookSignatoryMark signatoryMark) {
        this.signatoryMark = signatoryMark;
        return this;
    }
}
