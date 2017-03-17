package com.aplana.sbrf.taxaccounting.model.ndfl;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.*;

/**
 * Данные о физическом лице - получателе дохода
 *
 * @author Andrey Drunk
 */
public class NdflPerson extends IdentityObject<Long> {

    // №пп
    private Integer rowNum;

    // Физическое лицо
    private Long personId;

    // Идентификатор ФЛ REF_BOOK_PERSON.RECORD_ID
    private Long recordId;

    // Идентификатор налоговой формы к которой относятся данные
    private Long declarationDataId;

    // Уникальный код клиента (Графа 2)
    private String inp;

    // СНИЛС
    private String snils;

    // Налогоплательщик.Фамилия (Графа 3)
    private String lastName;

    // Налогоплательщик.Имя (Графа 4)
    private String firstName;

    // Налогоплательщик.Отчество (Графа 5)
    private String middleName;

    // Налогоплательщик.Дата рождения (Графа 6)
    private Date birthDay;

    // Гражданство (код страны) (Графа 7)
    private String citizenship;

    // ИНН.В Российской федерации (Графа 8)
    private String innNp;

    // ИНН.В стране гражданства (Графа 9)
    private String innForeign;

    // Документ удостоверяющий личность.Код (Графа 10)
    private String idDocType;

    // Документ удостоверяющий личность.Номер (Графа 11)
    private String idDocNumber;

    // Cтатус (Графа 12)
    private String status;

    // Адрес регистрации в Российской Федерации.Код субъекта (Графа 13)
    private String regionCode;

    // Адрес регистрации в Российской Федерации.Индекс (Графа 14)
    private String postIndex;

    // Адрес регистрации в Российской Федерации.Район (Графа 15)
    private String area;

    // Адрес регистрации в Российской Федерации.Город (Графа 16)
    private String city;

    // Адрес регистрации в Российской Федерации.Населенный пункт (Графа 17)
    private String locality;

    // Адрес регистрации в Российской Федерации.Улица (Графа 18)
    private String street;

    // Адрес регистрации в Российской Федерации.Дом (Графа 19)
    private String house;

    // Адрес регистрации в Российской Федерации.Корпус (Графа 20)
    private String building;

    // Адрес регистрации в Российской Федерации.Квартира (Графа 21)
    private String flat;

    // Код страны проживания вне РФ
    private String countryCode;

    // Адрес проживания вне РФ
    private String address;

    // Дополнительная информация
    private String additionalData;

    // Сведения о доходах физического лица
    private List<NdflPersonIncome> incomes;

    // Стандартные, социальные и имущественные налоговые вычеты
    private List<NdflPersonDeduction> deductions;

    // Cведения о доходах в виде авансовых платежей
    private List<NdflPersonPrepayment> prepayments;

    public NdflPerson() {
        super();
        incomes = new ArrayList<NdflPersonIncome>();
        deductions = new ArrayList<NdflPersonDeduction>();
        prepayments = new ArrayList<NdflPersonPrepayment>();
    }

    public static final String TABLE_NAME = "ndfl_person";
    public static final String SEQ = "seq_ndfl_person";



    public static final String[] COLUMNS = {"id", "declaration_data_id", "person_id", "row_num", "inp", "snils", "last_name", "first_name", "middle_name", "birth_day", "citizenship", "inn_np", "inn_foreign", "id_doc_type", "id_doc_number", "status", "post_index", "region_code", "area", "city", "locality", "street", "house", "building", "flat", "country_code", "address", "additional_data"};

    /**
     * Список полей бина значения которых передаются в запрос. Порядок соответсвует порядку наименований столбцов в COLUMNS
     */
    public static final String[] FIELDS = {"id", "declarationDataId", "personId", "rowNum", "inp", "snils", "lastName", "firstName", "middleName", "birthDay", "citizenship", "innNp", "innForeign", "idDocType", "idDocNumber", "status", "postIndex", "regionCode", "area", "city", "locality", "street", "house", "building", "flat", "countryCode", "address", "additionalData"};

    public Long getDeclarationDataId() {
        return declarationDataId;
    }
    public void setDeclarationDataId(Long declarationDataId) {
        this.declarationDataId = declarationDataId;
    }

    public Integer getRowNum() {
        return rowNum;
    }
    public void setRowNum(Integer rowNum) {
        this.rowNum = rowNum;
    }

    public Long getPersonId() {
        return personId;
    }
    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public String getInp() {
        return inp;
    }

    public void setInp(String inp) {
        this.inp = inp;
    }

    public String getSnils() {
        return snils;
    }

    public void setSnils(String snils) {
        this.snils = snils;
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

    public Date getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(Date birthDay) {
        this.birthDay = birthDay;
    }

    public String getCitizenship() {
        return citizenship;
    }

    public void setCitizenship(String citizenship) {
        this.citizenship = citizenship;
    }

    public String getInnNp() {
        return innNp;
    }

    public void setInnNp(String innNp) {
        this.innNp = innNp;
    }

    public String getInnForeign() {
        return innForeign;
    }

    public void setInnForeign(String innForeign) {
        this.innForeign = innForeign;
    }

    public String getIdDocType() {
        return idDocType;
    }

    public void setIdDocType(String idDocType) {
        this.idDocType = idDocType;
    }

    public String getIdDocNumber() {
        return idDocNumber;
    }

    public void setIdDocNumber(String idDocNumber) {
        this.idDocNumber = idDocNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPostIndex() {
        return postIndex;
    }

    public void setPostIndex(String postIndex) {
        this.postIndex = postIndex;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getHouse() {
        return house;
    }

    public void setHouse(String house) {
        this.house = house;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getFlat() {
        return flat;
    }

    public void setFlat(String flat) {
        this.flat = flat;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(String additionalData) {
        this.additionalData = additionalData;
    }

    public List<NdflPersonIncome> getIncomes() {
        return incomes != null ? incomes : Collections.<NdflPersonIncome>emptyList();
    }

    public void setIncomes(List<NdflPersonIncome> incomes) {
        this.incomes = incomes;
    }

    public List<NdflPersonDeduction> getDeductions() {
        return deductions != null ? deductions : Collections.<NdflPersonDeduction>emptyList();
    }

    public void setDeductions(List<NdflPersonDeduction> deductions) {
        this.deductions = deductions;
    }

    public List<NdflPersonPrepayment> getPrepayments() {
        return prepayments != null ? prepayments : Collections.<NdflPersonPrepayment>emptyList();
    }
    public void setPrepayments(List<NdflPersonPrepayment> prepayments) {
        this.prepayments = prepayments;
    }

    public Long getRecordId() {
        return recordId;
    }
    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.NO_FIELD_NAMES_STYLE).append("id", id)
                .append("rowNum", rowNum)
                .append("personId", personId)
                .append("declarationDataId", declarationDataId)
                .append("inp", inp)
                .append("snils", snils)
                .append("lastName", lastName)
                .append("firstName", firstName)
                .append("middleName", middleName)
                .append("birthDay", birthDay)
                .append("citizenship", citizenship)
                .append("innNp", innNp)
                .append("innForeign", innForeign)
                .append("idDocType", idDocType)
                .append("idDocNumber", idDocNumber)
                .append("status", status)
                .append("postIndex", postIndex)
                .append("regionCode", regionCode)
                .append("area", area)
                .append("city", city)
                .append("locality", locality)
                .append("street", street)
                .append("house", house)
                .append("building", building)
                .append("flat", flat)
                .append("countryCode", countryCode)
                .append("address", address)
                .append("additionalData", additionalData)
                .toString();
    }
}
