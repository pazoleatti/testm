package com.aplana.sbrf.taxaccounting.model.refbook;

import com.aplana.sbrf.taxaccounting.model.Permissive;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Модель физлица для работы с реестром физлиц. Записи реестра физлиц содержат ссылки на таблицы справочники.
 * Чтобы работать с таким ссылками как с объектами, ссылки разыменовываются и объекты представляются в виде Мапы строка-значение справочника.
 */
@Getter @Setter
public class RegistryPersonDTO extends PermissivePerson {

    /**
     * Исходный идентификатор физлица
     */
    private Long oldId;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegistryPersonDTO)) return false;

        RegistryPersonDTO that = (RegistryPersonDTO) o;

        if (getState() != null ? !getState().equals(that.getState()) : that.getState() != null) return false;
        if (getEndDate() != null ? !getEndDate().equals(that.getEndDate()) : that.getEndDate() != null)
            return false;
        if (getLastName() != null ? !getLastName().equals(that.getLastName()) : that.getLastName() != null)
            return false;
        if (getFirstName() != null ? !getFirstName().equals(that.getFirstName()) : that.getFirstName() != null)
            return false;
        if (getMiddleName() != null ? !getMiddleName().equals(that.getMiddleName()) : that.getMiddleName() != null)
            return false;
        if (getBirthDate() != null ? !getBirthDate().equals(that.getBirthDate()) : that.getBirthDate() != null)
            return false;
        if (getCitizenship() != null ? !getCitizenship().equals(that.getCitizenship()) : that.getCitizenship() != null)
            return false;
        if (getReportDoc() != null ? !getReportDoc().equals(that.getReportDoc()) : that.getReportDoc() != null)
            return false;
        if (getInn() != null ? !getInn().equals(that.getInn()) : that.getInn() != null) return false;
        if (getInnForeign() != null ? !getInnForeign().equals(that.getInnForeign()) : that.getInnForeign() != null)
            return false;
        if (getSnils() != null ? !getSnils().equals(that.getSnils()) : that.getSnils() != null) return false;
        if (getTaxPayerState() != null ? !getTaxPayerState().equals(that.getTaxPayerState()) : that.getTaxPayerState() != null)
            return false;
        if (getSource() != null ? !getSource().equals(that.getSource()) : that.getSource() != null) return false;
        return getAddress() != null ? getAddress().equals(that.getAddress()) : that.getAddress() == null;
    }

    @Override
    public int hashCode() {
        int result = getState() != null ? getState().hashCode() : 0;
        result = 31 * result + (getEndDate() != null ? getEndDate().hashCode() : 0);
        result = 31 * result + (getLastName() != null ? getLastName().hashCode() : 0);
        result = 31 * result + (getFirstName() != null ? getFirstName().hashCode() : 0);
        result = 31 * result + (getMiddleName() != null ? getMiddleName().hashCode() : 0);
        result = 31 * result + (getBirthDate() != null ? getBirthDate().hashCode() : 0);
        result = 31 * result + (getCitizenship() != null ? getCitizenship().hashCode() : 0);
        result = 31 * result + (getReportDoc() != null ? getReportDoc().hashCode() : 0);
        result = 31 * result + (getInn() != null ? getInn().hashCode() : 0);
        result = 31 * result + (getInnForeign() != null ? getInnForeign().hashCode() : 0);
        result = 31 * result + (getSnils() != null ? getSnils().hashCode() : 0);
        result = 31 * result + (getTaxPayerState() != null ? getTaxPayerState().hashCode() : 0);
        result = 31 * result + (getSource() != null ? getSource().hashCode() : 0);
        result = 31 * result + (getAddress() != null ? getAddress().hashCode() : 0);
        return result;
    }

    /*
     * Набор методов для сериализации объекта в JSON
     */

    @JsonProperty("inn")
    public Permissive<String> getInnForJson() {
        return inn;
    }

    @JsonProperty("innForeign")
    public Permissive<String> getInnForeignForJson() {
        return innForeign;
    }

    @JsonProperty("snils")
    public Permissive<String> getSnilsForJson() {
        return snils;
    }

    @JsonProperty("address")
    public Permissive<Address> getAddressForJson() {
        return address;
    }
}
