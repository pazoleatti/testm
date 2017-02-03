package com.aplana.sbrf.taxaccounting.model.ndfl;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

import java.util.*;

/**
 * Данные о физическом лице - получателе дохода
 *
 * @author Andrey Drunk
 */
public class NdflPerson extends IdentityObject<Long> {

    /**
     * Порядковый номер строки
     */
    private Integer rowNum;

    /**
     * Ссылка на справочник физлиц
     */
    private Long personId;

    /**
     * Ссылка на декларацию к которой относятся данные
     */
    private Long declarationDataId;
    // Уникальный код клиента
    private String inp;
    private String snils;
    private String lastName;
    private String firstName;
    private String middleName;
    private Date birthDay;
    // Цифровой код гражданства
    private String citizenship;
    private String innNp;
    private String innForeign;
    // Код вида документа
    private String idDocType;
    private String idDocNumber;
    // Cтатус налогоплательщика
    private String status;
    private String postIndex;
    // Код Региона
    private String regionCode;
    // Район
    private String area;
    // Город
    private String city;
    // Населенный пункт
    private String locality;
    // Улица
    private String street;
    // Дом
    private String house;
    // Корпус
    private String building;
    // Квартира
    private String flat;
    private String countryCode;
    private String address;
    private String additionalData;

    private List<NdflPersonIncome> incomes;
    private List<NdflPersonDeduction> deductions;
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
    public static final String[] FIELDS = {"id", "declarationDataId", "person_id", "row_num", "inp", "snils", "lastName", "firstName", "middleName", "birthDay", "citizenship", "innNp", "innForeign", "idDocType", "idDocNumber", "status", "postIndex", "regionCode", "area", "city", "locality", "street", "house", "building", "flat", "countryCode", "address", "additionalData"};

    public Object[] createPreparedStatementArgs() {
        return new Object[]{declarationDataId, personId, rowNum, inp, snils, lastName, firstName, middleName, birthDay, citizenship,
                innNp, innForeign, idDocType, idDocNumber, status, postIndex, regionCode, area, city,
                locality, street, house, building, flat, countryCode, address, additionalData
        };
    }


    public Map<String, String> getSqlParameterSource() {
        Map<String, String> map = new LinkedHashMap<String, String>();
        map.put("id", "id");
        map.put("declarationDataId", "declaration_data_id");
        map.put("personId", "person_id");
        map.put("rowNum", "row_num");
        map.put("inp", "inp");
        map.put("snils", "snils");
        map.put("lastName", "last_name");
        map.put("firstName", "first_name");
        map.put("middleName", "middle_name");
        map.put("birthDay", "birth_day");
        map.put("citizenship", "citizenship");
        map.put("innNp", "inn_np");
        map.put("innForeign", "inn_foreign");
        map.put("idDocType", "id_doc_type");
        map.put("idDocNumber", "id_doc_number");
        map.put("status", "status");
        map.put("postIndex", "post_index");
        map.put("regionCode", "region_code");
        map.put("area", "area");
        map.put("city", "city");
        map.put("locality", "locality");
        map.put("street", "street");
        map.put("house", "house");
        map.put("building", "building");
        map.put("flat", "flat");
        map.put("countryCode", "country_code");
        map.put("address", "address");
        map.put("additionalData", "additional_data");
        return map;
    }


    public Map<String, String> getColumns() {
        Map<String, String> map = new LinkedHashMap<String, String>();
        map.put("id", "id");
        map.put("declarationDataId", "declaration_data_id");
        map.put("personId", "person_id");
        map.put("rowNum", "row_num");
        map.put("inp", "inp");
        map.put("snils", "snils");
        map.put("lastName", "last_name");
        map.put("firstName", "first_name");
        map.put("middleName", "middle_name");
        map.put("birthDay", "birth_day");
        map.put("citizenship", "citizenship");
        map.put("innNp", "inn_np");
        map.put("innForeign", "inn_foreign");
        map.put("idDocType", "id_doc_type");
        map.put("idDocNumber", "id_doc_number");
        map.put("status", "status");
        map.put("postIndex", "post_index");
        map.put("regionCode", "region_code");
        map.put("area", "area");
        map.put("city", "city");
        map.put("locality", "locality");
        map.put("street", "street");
        map.put("house", "house");
        map.put("building", "building");
        map.put("flat", "flat");
        map.put("countryCode", "country_code");
        map.put("address", "address");
        map.put("additionalData", "additional_data");
        return map;
    }


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
}
