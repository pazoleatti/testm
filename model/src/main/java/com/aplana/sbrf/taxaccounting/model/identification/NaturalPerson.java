package com.aplana.sbrf.taxaccounting.model.identification;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author Andrey Drunk
 */
public class NaturalPerson extends RefBookObject implements IdentityPerson {

    /**
     * Идентификатор ФЛ в первичной форме
     */
    private Long primaryPersonId;


    /**
     * Фамилия
     */
    private String lastName;

    /**
     * Имя
     */
    private String firstName;

    /**
     * Отчетство
     */
    private String middleName;

    /**
     * ИНН
     */
    private String inn;

    /**
     * ИНН ИН
     */
    private String innForeign;

    /**
     * Снилс
     */
    private String snils;

    /**
     * Номер ФЛ
     */
    private Integer num;

    /**
     * Статус физлица
     */
    private TaxpayerStatus taxPayerStatus;

    /**
     * Гражданство
     */
    private Country citizenship;

    /**
     * Дата рождения
     */
    private Date birthDate;

    /**
     * Список идентификаторов ФЛ
     */
    private List<PersonIdentifier> personIdentityList;

    /**
     * ДУЛ включаемый в отчетность
     */
    private PersonDocument majorDocument;

    /**
     * Список документов ФЛ
     */
    private List<PersonDocument> documents;

    /**
     * Список назначенных подразделений на ФЛ
     */
    private List<PersonTb> personTbList;

    /**
     * Адрес фл
     */
    private Address address;

    /**
     * Источник (АСНУ)
     */
    private RefBookAsnu source;

    /**
     * Система источник
     * @deprecated см {@link #source}
     */
    @Deprecated
    private Long sourceId;


    /**
     * Поле для хранения веса записи при идентификации
     */
    private Double weight;

    /**
     * Флаг указывающий на то что запись в справочнике не надо обновлять
     */
    private boolean needUpdate = true;

    private Long oldId;

    public NaturalPerson() {
        this.personIdentityList = new ArrayList<>();
        this.documents = new ArrayList<>();
        this.personTbList = new ArrayList<>();
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public Long getPrimaryPersonId() {
        return primaryPersonId;
    }

    public void setPrimaryPersonId(Long primaryPersonId) {
        this.primaryPersonId = primaryPersonId;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getInn() {
        return inn;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

    public String getInnForeign() {
        return innForeign;
    }

    public void setInnForeign(String innForeign) {
        this.innForeign = innForeign;
    }

    public String getSnils() {
        return snils;
    }

    public void setSnils(String snils) {
        this.snils = snils;
    }

    public TaxpayerStatus getTaxPayerStatus() {
        return taxPayerStatus;
    }

    public void setTaxPayerStatus(TaxpayerStatus taxPayerStatusId) {
        this.taxPayerStatus = taxPayerStatusId;
    }

    public Country getCitizenship() {
        return citizenship;
    }

    public void setCitizenship(Country citizenship) {
        this.citizenship = citizenship;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }


    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public List<PersonIdentifier> getPersonIdentityList() {
        return personIdentityList;
    }

    public void setPersonIdentityList(List<PersonIdentifier> personIdentityList) {
        this.personIdentityList = personIdentityList;
    }

    /**
     * @deprecated Используйте {@link #getDocuments()}, но учтите, что он возвращает unmodifiableList
     */
    public List<PersonDocument> getPersonDocumentList() {
        return documents;
    }

    public List<PersonDocument> getDocuments() {
        return Collections.unmodifiableList(documents);
    }

    public PersonDocument getMajorDocument() {
        return majorDocument;
    }

    public void setMajorDocument(PersonDocument majorDocument) {
        this.majorDocument = majorDocument;
    }

    public void addDocument(PersonDocument document) {
        documents.add(document);
        if (document.isIncludeReport()) {
            majorDocument = document;
        }
    }

    public List<PersonTb> getPersonTbList() {
        return personTbList;
    }

    public void setPersonTbList(List<PersonTb> personTbList) {
        this.personTbList = personTbList;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public RefBookAsnu getSource() {
        return source;
    }

    public void setSource(RefBookAsnu source) {
        this.source = source;
    }

    /**
     * Получить идентификатор ФЛ, данный метод используется при работе с ФЛ из первичных форм, так как там может быть не более одного идентификатора
     *
     * @return идентификатор ФЛ
     */
    public PersonIdentifier getPersonIdentifier() {
        if (personIdentityList != null && !personIdentityList.isEmpty()) {
            return personIdentityList.get(0);
        }
        return null;
    }


    @Override
    public Double getWeight() {
        return weight;
    }

    @Override
    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public boolean isNeedUpdate() {
        return needUpdate;
    }

    public void setNeedUpdate(boolean needUpdate) {
        this.needUpdate = needUpdate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id(refBook)", id)
                .append("id(primary)", primaryPersonId)
                .append("recordId", recordId)
                .append("lastName", lastName)
                .append("firstName", firstName)
                .append("middleName", middleName)
                .append("inn", inn)
                .append("innForeign", innForeign)
                .append("snils", snils)
                .append("taxPayerStatus", taxPayerStatus)
                .append("citizenship", citizenship)
                .append("birthDate", birthDate)
                .append("personIdentityList", personIdentityList)
                .append("documents", documents)
                .append("address", address)
                .append("weight", weight)
                .toString();
    }

    public Long getOldId() {
        return oldId;
    }

    public void setOldId(Long oldId) {
        this.oldId = oldId;
    }
}
