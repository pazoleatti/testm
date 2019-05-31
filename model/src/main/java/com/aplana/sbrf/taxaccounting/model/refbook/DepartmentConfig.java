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
 * Настройка подразделений
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true, of = {"kpp", "oktmo", "startDate", "endDate"})
public class DepartmentConfig extends IdentityObject<Long> implements SecuredEntity {

    // КПП
    private String kpp;
    // ОКТМО
    private RefBookOktmo oktmo;

    @JsonDeserialize(using = ISODateDeserializer.class)
    private Date startDate;
    @JsonDeserialize(using = ISODateDeserializer.class)
    private Date endDate;
    // Порядковый номер
    private Integer rowOrd;
    // Тербанк настройки
    private RefBookDepartment department;
    // Код налогового органа конечного
    private String taxOrganCode;
    // Место, по которому представляется документ.
    private RefBookPresentPlace presentPlace;
    // Наименование для титульного листа
    private String name;
    // Номер контактного телефона
    private String phone;
    // Признак лица, подписавшего документ
    private RefBookSignatoryMark signatoryMark;
    // Фамилия подписанта
    private String signatorySurName;
    // Имя подписанта
    private String signatoryFirstName;
    // Отчество подписанта
    private String signatoryLastName;
    // Наименование документа, подтверждающего полномочия
    private String approveDocName;
    // Наименование организации-представителя налогоплательщика
    private String approveOrgName;
    // Формы реорганизации и ликвидации
    private RefBookReorganization reorganization;
    // КПП реорганизованного обособленного подразделения
    private String reorgKpp;
    // ИНН реорганизованного обособленного подразделения
    private String reorgInn;
    // Наименование подразделения для титульного листа отчетных форм по реорганизованному подразделению
    private String reorgSuccessorName;
    // Код причины постановки организации по месту нахождения организации правопреемника
    private String reorgSuccessorKpp;
    // Права
    private long permissions;

    // Конструктор по всем полям для Lombok.Builder
    @Builder(toBuilder = true)
    public DepartmentConfig(Long id, String kpp, RefBookOktmo oktmo, Date startDate, Date endDate, Integer rowOrd, RefBookDepartment department, String taxOrganCode, RefBookPresentPlace presentPlace, String name, String phone, RefBookSignatoryMark signatoryMark, String signatorySurName, String signatoryFirstName, String signatoryLastName, String approveDocName, String approveOrgName, RefBookReorganization reorganization, String reorgKpp, String reorgInn, String reorgSuccessorName, String reorgSuccessorKpp) {
        super(id);
        this.kpp = kpp;
        this.oktmo = oktmo;
        this.startDate = startDate;
        this.endDate = endDate;
        this.rowOrd = rowOrd;
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
        this.reorgSuccessorName = reorgSuccessorName;
        this.reorgSuccessorKpp = reorgSuccessorKpp;
    }

    // Fluent setters

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
