package com.aplana.sbrf.taxaccounting.dao.identification;

import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import org.apache.commons.collections4.map.HashedMap;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrey Drunk
 */
public class NaturalPersonRefbookHandler extends NaturalPersonHandler {

    /**
     *
     */
    private Map<Long, NaturalPerson> refbookPersonTempMap;

    /**
     * Карта для создания идкнтификаторов ФЛ
     */
    private Map<Long, Map<Long, PersonIdentifier>> identitiesMap;

    /**
     * Карта для создания ТБ ФЛ
     */
    private Map<Long, Map<Long, PersonTb>> personTbMap;

    /**
     * Карта для создания документов ФЛ
     */
    private Map<Long, Map<Long, IdDoc>> documentsMap;

    /**
     * Кэш справочника страны
     */
    private Map<Long, RefBookCountry> countryMap;

    /**
     * Кэш справочника статусы Налогоплателищика
     */
    private Map<Long, RefBookTaxpayerState> taxpayerStatusMap;

    /**
     * Кэш справочника типы документов
     */
    private Map<Long, RefBookDocType> docTypeMap;

    private Map<Long, RefBookAsnu> asnuMap;

    /**
     *
     */
    public NaturalPersonRefbookHandler() {
        super();
        refbookPersonTempMap = new HashMap<>();
        identitiesMap = new HashMap<>();
        documentsMap = new HashMap<>();
        personTbMap = new HashedMap<>();
    }

    @Override
    public void processRow(ResultSet rs, int rowNum, Map<Long, Map<Long, NaturalPerson>> map) throws SQLException {

        //Идентификатор записи первичной формы
        Long primaryPersonId = SqlUtils.getLong(rs, PRIMARY_PERSON_ID);

        //if (primaryPersonId == null) {throw new ServiceException("Не задано значение PRIMARY_PERSON_ID");}

        //Список сходных записей
        Map<Long, NaturalPerson> similarityPersonMap = map.get(primaryPersonId);

        if (similarityPersonMap == null) {
            similarityPersonMap = new HashMap<>();
            map.put(primaryPersonId, similarityPersonMap);
        }

        //Идентификатор справочника
        Long refBookPersonId = SqlUtils.getLong(rs, REFBOOK_PERSON_ID);

        NaturalPerson naturalPerson = similarityPersonMap.get(refBookPersonId);

        if (naturalPerson == null) {
            naturalPerson = buildNaturalPerson(rs, refBookPersonId, primaryPersonId);
            similarityPersonMap.put(refBookPersonId, naturalPerson);
        }

        //Добавляем документы физлица
        addPersonDocument(rs, naturalPerson);

        // инициализируем ДУЛ включаемый в отчетность
        initReportDoc(rs, naturalPerson);

        //Добавляем идентификаторы
        addPersonIdentifier(rs, naturalPerson);

        // добавляем Тербанки
        addPersonTb(rs, naturalPerson);

        //Адрес
        Address address = buildAddress(rs);
        naturalPerson.setAddress(address);

    }

    private void addPersonIdentifier(ResultSet rs, NaturalPerson naturalPerson) throws SQLException {

        Long refBookPersonId = naturalPerson.getId();
        Long personIdentifierId = SqlUtils.getLong(rs, "book_id_tax_payer_id");
        Map<Long, PersonIdentifier> personIdentityMap = identitiesMap.get(refBookPersonId);

        if (personIdentityMap == null) {
            personIdentityMap = new HashMap<Long, PersonIdentifier>();
            identitiesMap.put(refBookPersonId, personIdentityMap);
        }

        if (personIdentifierId != null && !personIdentityMap.containsKey(personIdentifierId)) {
            PersonIdentifier personIdentifier = new PersonIdentifier();
            personIdentifier.setId(personIdentifierId);

            personIdentifier.setInp(rs.getString("inp"));
            RefBookAsnu asnu = new RefBookAsnu();
            asnu.setId(SqlUtils.getLong(rs, "as_nu"));
            personIdentifier.setAsnu(asnu);
            personIdentifier.setPerson(naturalPerson);
            personIdentityMap.put(personIdentifierId, personIdentifier);
            naturalPerson.getPersonIdentityList().add(personIdentifier);
        }
    }

    private void addPersonDocument(ResultSet rs, NaturalPerson naturalPerson) throws SQLException {
        Long refBookPersonId = naturalPerson.getId();
        Long docId = SqlUtils.getLong(rs, "ref_book_id_doc_id");
        Map<Long, IdDoc> pesonDocumentMap = documentsMap.get(refBookPersonId);

        if (pesonDocumentMap == null) {
            pesonDocumentMap = new HashMap<Long, IdDoc>();
            documentsMap.put(refBookPersonId, pesonDocumentMap);
        }

        if (docId != null && !pesonDocumentMap.containsKey(docId)) {
            Long docTypeId = SqlUtils.getLong(rs, "doc_id");
            RefBookDocType docType = getDocTypeById(docTypeId);
            IdDoc personDocument = new IdDoc();
            personDocument.setId(docId);

            personDocument.setDocType(docType);
            personDocument.setDocumentNumber(rs.getString("doc_number"));
            personDocument.setPerson(naturalPerson);
            pesonDocumentMap.put(docId, personDocument);
            naturalPerson.getDocuments().add(personDocument);
        }
    }

    private void initReportDoc(ResultSet rs, NaturalPerson naturalPerson) throws SQLException {
        Long reportDocId = rs.getLong("report_doc");
        IdDoc personDocument = documentsMap.get(naturalPerson.getId()).get(reportDocId);
        naturalPerson.setReportDoc(personDocument != null ? personDocument: new IdDoc());
    }

    private void addPersonTb(ResultSet rs, NaturalPerson naturalPerson) throws SQLException {
        Long refBookPersonId = naturalPerson.getId();
        Long personTbId = SqlUtils.getLong(rs, "ref_book_person_tb_id");
        Map<Long, PersonTb> tbMap = personTbMap.get(refBookPersonId);

        if (tbMap == null) {
            tbMap = new HashMap<>();
            personTbMap.put(refBookPersonId, tbMap);
        }

        if (personTbId != null && !tbMap.containsKey(personTbId)) {
            PersonTb personTb = new PersonTb();
            personTb.setId(personTbId);
            Department department = new Department();
            department.setId(rs.getInt("tb_department_id"));
            personTb.setTbDepartment(department);
            personTb.setImportDate(rs.getDate("import_date"));
            personTb.setPerson(naturalPerson);
            tbMap.put(personTbId, personTb);
            naturalPerson.getPersonTbList().add(personTb);
        }
    }

    private NaturalPerson buildNaturalPerson(ResultSet rs, Long refBookPersonId, Long primaryPersonId) throws SQLException {
        NaturalPerson naturalPerson = refbookPersonTempMap.get(refBookPersonId);
        if (naturalPerson != null) {
            return naturalPerson;
        } else {

            NaturalPerson person = new NaturalPerson();

            //person
            person.setId(refBookPersonId);

            person.setRecordId(SqlUtils.getLong(rs, "person_record_id"));
            person.setStartDate(rs.getDate("start_date"));
            person.setEndDate(rs.getDate("end_date"));

            person.setLastName(rs.getString("last_name"));
            person.setFirstName(rs.getString("first_name"));
            person.setMiddleName(rs.getString("middle_name"));
            person.setInn(rs.getString("inn"));
            person.setInnForeign(rs.getString("inn_foreign"));
            person.setSnils(rs.getString("snils"));
            person.setBirthDate(rs.getDate("birth_date"));

            //ссылки на справочники
            person.setTaxPayerState(getTaxpayerStatusById(SqlUtils.getLong(rs, "taxpayer_state")));
            person.setCitizenship(getCountryById(SqlUtils.getLong(rs, "citizenship")));

            //additional
            person.setSource(asnuMap.get(SqlUtils.getLong(rs, "source_id")));
            person.setRecordId(SqlUtils.getLong(rs, "record_id"));
            person.setOldId(SqlUtils.getLong(rs, "old_id"));
            person.setVip(rs.getBoolean("vip"));

            refbookPersonTempMap.put(refBookPersonId, person);

            return person;
        }


    }

    private Address buildAddress(ResultSet rs) throws SQLException {
        Address address = new Address();
        RefBookCountry country = getCountryById(SqlUtils.getLong(rs, "country_id"));
        address.setCountry(country != null ? country : new RefBookCountry());
        address.setRegionCode(rs.getString("region_code"));
        address.setPostalCode(rs.getString("postal_code"));
        address.setDistrict(rs.getString("district"));
        address.setCity(rs.getString("city"));
        address.setLocality(rs.getString("locality"));
        address.setStreet(rs.getString("street"));
        address.setHouse(rs.getString("house"));
        address.setBuild(rs.getString("build"));
        address.setAppartment(rs.getString("appartment"));
        address.setAddressIno(rs.getString("address_foreign"));
        return address;
    }

    public Map<Long, RefBookCountry> getCountryMap() {
        return countryMap;
    }

    public void setCountryMap(Map<Long, RefBookCountry> countryMap) {
        this.countryMap = countryMap;
    }

    public Map<Long, RefBookTaxpayerState> getTaxpayerStatusMap() {
        return taxpayerStatusMap;
    }

    public void setTaxpayerStatusMap(Map<Long, RefBookTaxpayerState> taxpayerStatusMap) {
        this.taxpayerStatusMap = taxpayerStatusMap;
    }

    public Map<Long, RefBookDocType> getDocTypeMap() {
        return docTypeMap;
    }

    public void setDocTypeMap(Map<Long, RefBookDocType> docTypeMap) {
        this.docTypeMap = docTypeMap;
    }

    public Map<Long, RefBookAsnu> getAsnuMap() {
        return asnuMap;
    }

    public void setAsnuMap(Map<Long, RefBookAsnu> asnuMap) {
        this.asnuMap = asnuMap;
    }

    private RefBookTaxpayerState getTaxpayerStatusById(Long taxpayerStatusId) {
        if (taxpayerStatusId != null) {
            return taxpayerStatusMap != null ? taxpayerStatusMap.get(taxpayerStatusId) : new RefBookTaxpayerState(taxpayerStatusId, null);
        } else {
            return null;
        }
    }

    private RefBookCountry getCountryById(Long countryId) {
        if (countryId != null) {
            return countryMap != null ? countryMap.get(countryId) : new RefBookCountry(countryId, null);
        } else {
            return null;
        }
    }

    private RefBookDocType getDocTypeById(Long docTypeId) {
        if (docTypeId != null) {
            RefBookDocType refBookDocType = new RefBookDocType();
            refBookDocType.setId(docTypeId);
            refBookDocType.setCode(null);
            return docTypeMap != null ? docTypeMap.get(docTypeId) : refBookDocType;
        } else {
            return null;
        }
    }


}