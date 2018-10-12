package com.aplana.sbrf.taxaccounting.model.refbook;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;
import com.aplana.sbrf.taxaccounting.model.identification.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RegistryPerson extends IdentityObject<Long> {

    private Long recordId;

    private Date startDate;

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
    protected List<PersonIdentifier> personIdentityList;

    /**
     * ДУЛ включаемый в отчетность
     */
    private PersonDocument reportDoc;

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

    private Long oldId;

    private boolean vip;

    public static final String TABLE_NAME = "ref_book_person";

    public static final String[] COLUMNS = {"id", "record_id", "old_id", "start_date", "end_date", "last_name",
            "first_name", "middle_name", "inn", "inn_foreign", "snils", "taxpayer_state", "birth_date", "citizenship",
            "report_doc", "source_id", "vip", "region_code", "postal_code", "district", "city", "locality", "street", "house",
            "build", "appartment", "country_id", "address_foreign"};

    /**
     * Список полей бина значения которых передаются в запрос. Порядок соответсвует порядку наименований столбцов в COLUMNS
     */
    public static final String[] FIELDS = {"id", "recordId", "oldId", "startDate", "endDate", "lastName", "firstName",
            "middleName", "inn", "innForeign", "snils", "taxPayerStatus.id", "birthDate", "citizenship.id",
            "reportDoc.id", "source.id", "vip", "address.regionCode", "address.postalCode", "address.district", "address.city",
            "address.locality", "address.street", "address.house", "address.build", "address.appartment",
            "address.country.id", "address.addressIno"};

    public RegistryPerson() {
        this.personIdentityList = new ArrayList<>();
        this.documents = new ArrayList<>();
        this.personTbList = new ArrayList<>();
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
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

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public TaxpayerStatus getTaxPayerStatus() {
        return taxPayerStatus;
    }

    public void setTaxPayerStatus(TaxpayerStatus taxPayerStatus) {
        this.taxPayerStatus = taxPayerStatus;
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

    public List<PersonIdentifier> getPersonIdentityList() {
        return personIdentityList;
    }

    public void setPersonIdentityList(List<PersonIdentifier> personIdentityList) {
        this.personIdentityList = personIdentityList;
    }

    public PersonDocument getReportDoc() {
        return reportDoc;
    }

    public void setReportDoc(PersonDocument reportDoc) {
        this.reportDoc = reportDoc;
    }

    public List<PersonDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(List<PersonDocument> documents) {
        this.documents = documents;
    }

    public List<PersonTb> getPersonTbList() {
        return personTbList;
    }

    public void setPersonTbList(List<PersonTb> personTbList) {
        this.personTbList = personTbList;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public RefBookAsnu getSource() {
        return source;
    }

    public void setSource(RefBookAsnu source) {
        this.source = source;
    }

    public Long getOldId() {
        return oldId;
    }

    public void setOldId(Long oldId) {
        this.oldId = oldId;
    }

    public boolean isVip() {
        return vip;
    }

    public void setVip(boolean vip) {
        this.vip = vip;
    }
}
