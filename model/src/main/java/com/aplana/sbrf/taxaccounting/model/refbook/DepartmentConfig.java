package com.aplana.sbrf.taxaccounting.model.refbook;

import com.aplana.sbrf.taxaccounting.model.SecuredEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * Запись настройки подразделений
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentConfig extends RefBookSimple<Long> implements SecuredEntity {
    private Date startDate;
    private Date endDate;
    private Long recordId;

    private Integer rowOrd;
    private RefBookDepartment department;
    private String kpp;
    private RefBookOktmo oktmo;
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

    @Override
    public String toString() {
        return "DepartmentConfig{" +
                "id=" + id +
                ", kpp='" + kpp + '\'' +
                ", oktmoCode=" + oktmo.getCode() +
                ", taxOrganCode='" + taxOrganCode + '\'' +
                '}';
    }
}
