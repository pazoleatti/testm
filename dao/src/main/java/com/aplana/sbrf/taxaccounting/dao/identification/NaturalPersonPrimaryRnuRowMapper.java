package com.aplana.sbrf.taxaccounting.dao.identification;

import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.identification.Address;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.identification.PersonDocument;
import com.aplana.sbrf.taxaccounting.model.identification.PersonIdentifier;
import com.aplana.sbrf.taxaccounting.model.refbook.FiasCheckInfo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * @author Andrey Drunk
 */
public class NaturalPersonPrimaryRnuRowMapper extends NaturalPersonPrimaryRowMapper {

    private Long asnuId;

    private Map<Long, FiasCheckInfo> fiasAddressIdsMap;

    public Long getAsnuId() {
        return asnuId;
    }

    public void setAsnuId(Long asnuId) {
        this.asnuId = asnuId;
    }

    public Map<Long, FiasCheckInfo> getFiasAddressIdsMap() {
        return fiasAddressIdsMap;
    }

    public void setFiasAddressIdsMap(Map<Long, FiasCheckInfo> fiasAddressIdsMap) {
        this.fiasAddressIdsMap = fiasAddressIdsMap;
    }

    @Override
    public NaturalPerson mapRow(ResultSet rs, int rowNum) throws SQLException {

        NaturalPerson person = new NaturalPerson();

        Long ndflPersonId = SqlUtils.getLong(rs, "id");

        person.setPrimaryPersonId(ndflPersonId);
        person.setId(SqlUtils.getLong(rs, "person_id"));
        person.setNum(SqlUtils.getInteger(rs, "row_num"));

        person.setSnils(rs.getString("snils"));
        person.setLastName(rs.getString("last_name"));
        person.setFirstName(rs.getString("first_name"));
        person.setMiddleName(rs.getString("middle_name"));
        person.setBirthDate(rs.getDate("birth_day"));

        person.setCitizenship(getCountryByCode(rs.getString("citizenship")));
        person.setInn(rs.getString("inn_np"));
        person.setInnForeign(rs.getString("inn_foreign"));

        String inp = rs.getString("inp");
        if (inp != null && asnuId != null) {
            PersonIdentifier personIdentifier = new PersonIdentifier();
            personIdentifier.setNaturalPerson(person);
            personIdentifier.setInp(inp);
            personIdentifier.setAsnuId(asnuId);
            person.getPersonIdentityList().add(personIdentifier);
        }

        String documentTypeCode = rs.getString("id_doc_type");
        String documentNumber = rs.getString("id_doc_number");

        if (documentNumber != null && documentTypeCode != null) {
            PersonDocument personDocument = new PersonDocument();
            personDocument.setNaturalPerson(person);
            personDocument.setDocumentNumber(documentNumber);
            personDocument.setDocType(getDocTypeByCode(documentTypeCode, person));
            person.getPersonDocumentList().add(personDocument);
        }

        person.setTaxPayerStatus(getTaxpayerStatusByCode(rs.getString("status")));

        //Используются все адреса, а не только те, которые прошли проверку по ФИАС
        person.setAddress(buildAddress(rs));

        //rs.getString("additional_data")
        return person;
    }

    public Address buildAddress(ResultSet rs) throws SQLException {
        Address address = new Address();
        address.setCountry(getCountryByCode(rs.getString("country_code")));
        address.setRegionCode(rs.getString("region_code"));
        address.setPostalCode(rs.getString("post_index"));
        address.setDistrict(rs.getString("area"));
        address.setCity(rs.getString("city"));
        address.setLocality(rs.getString("locality"));
        address.setStreet(rs.getString("street"));
        address.setHouse(rs.getString("house"));
        address.setBuild(rs.getString("building"));
        address.setAppartment(rs.getString("flat"));
        address.setAddressIno(rs.getString("address"));
        //Тип адреса. Значения: 0 - в РФ 1 - вне РФ
        int addressType = (address.getAddressIno() != null && !address.getAddressIno().isEmpty()) ? 1 : 0;
        address.setAddressType(addressType);
        return address;
    }

    public FiasCheckInfo getFiasAddressId(NaturalPerson person, Long ndflPersonId) {

        if (fiasAddressIdsMap != null) {
            FiasCheckInfo result = fiasAddressIdsMap.get(ndflPersonId);
            if (result == null) {
                logger.warn("Не найден адрес физического лица " + IdentificationUtils.buildNotice(person));
            }
            return result;
        } else {
            logger.warn("Не проинициализирован кэш справочника для проверки адресов физических лиц!");
        }
        return null;
    }
}