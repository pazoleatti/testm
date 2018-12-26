package com.aplana.sbrf.taxaccounting.model.refbook;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;
import com.aplana.sbrf.taxaccounting.model.Permissive;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Модель физлица для работы с реестром физлиц. Записи реестра физлиц содержат ссылки на таблицы справочники.
 * Чтобы работать с таким ссылками как с объектами, ссылки разыменовываются и объекты представляются в виде Мапы строка-значение справочника.
 */
@Getter
@Setter
@EqualsAndHashCode(exclude = {"oldId"}, callSuper = false)
public class RegistryPersonDTO extends IdentityObject<Long> implements PermissivePerson {

    /**
     * Исходный идентификатор физлица
     */
    private Long oldId;
    /**
     * Идентификатор группы версий
     */
    private Long recordId;

    private long permissions;

    private boolean vip;

    /**
     * Статус из справочника
     */
    private Integer state;

    private Date startDate;

    /**
     * Окончание даты действия версии
     */
    private Date endDate;

    /**
     * Фамилия
     */
    private String lastName;

    /**
     * Имя
     */
    private String firstName;

    /**
     * Отчество
     */
    private String middleName;

    /**
     * Дата рождения
     */
    private Date birthDate;

    private String birthPlace;

    /**
     * Гражданство
     */
    private Permissive<RefBookCountry> citizenship;

    /**
     * Документ включаемый в отчетность
     */
    private Permissive<IdDoc> reportDoc;

    /**
     * ИНН
     */
    private Permissive<String> inn;

    /**
     * ИНН в иностранном государстве
     */
    private Permissive<String> innForeign;

    /**
     * СНИЛС
     */
    private Permissive<String> snils;

    /**
     * Статус налогоплательщика
     */
    private Permissive<RefBookTaxpayerState> taxPayerState;

    /**
     * Система-источник
     */
    private RefBookAsnu source;

    /**
     * Адрес
     */
    private Permissive<Address> address;

    private Permissive<List<IdDoc>> documents;

    private List<PersonIdentifier> personIdentityList;

    private List<PersonTb> personTbList;

    private List<RegistryPersonDTO> duplicates;

    private RegistryPersonDTO original;

    public RegistryPersonDTO() {
        this.personIdentityList = new ArrayList<>();
        this.personTbList = new ArrayList<>();
        this.duplicates = new ArrayList<>();
    }

}
