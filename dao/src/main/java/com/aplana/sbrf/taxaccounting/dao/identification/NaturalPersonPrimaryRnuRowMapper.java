package com.aplana.sbrf.taxaccounting.dao.identification;

import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.identification.Address;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.identification.PersonDocument;
import com.aplana.sbrf.taxaccounting.model.identification.PersonIdentifier;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * @author Andrey Drunk
 */
public class NaturalPersonPrimaryRnuRowMapper extends NaturalPersonPrimaryRowMapper {

    private Long asnuId;

    private Map<Long, Long> fiasAddressIdsMap;

    public Long getAsnuId() {
        return asnuId;
    }

    public void setAsnuId(Long asnuId) {
        this.asnuId = asnuId;
    }

    public Map<Long, Long> getFiasAddressIdsMap() {
        return fiasAddressIdsMap;
    }

    public void setFiasAddressIdsMap(Map<Long, Long> fiasAddressIdsMap) {
        this.fiasAddressIdsMap = fiasAddressIdsMap;
    }

    @Override
    public NaturalPerson mapRow(ResultSet rs, int rowNum) throws SQLException {

        NaturalPerson person = new NaturalPerson();

        Long ndflPersonId = SqlUtils.getLong(rs, "id");

        person.setPrimaryPersonId(ndflPersonId);
        person.setId(SqlUtils.getLong(rs, "person_id"));

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
            personDocument.setDocType(getDocTypeByCode(documentTypeCode));
            person.getPersonDocumentList().add(personDocument);
        }

        //добавлено для 115 макета
        person.setPension(SqlUtils.getInteger(rs, "pension"));
        person.setMedical(SqlUtils.getInteger(rs, "medical"));
        person.setSocial(SqlUtils.getInteger(rs, "social"));
        person.setNum(SqlUtils.getInteger(rs, "num"));
        person.setSex(SqlUtils.getInteger(rs, "sex"));

        person.setTaxPayerStatus(getTaxpayerStatusByCode(rs.getString("status")));

        //Используются только адреса которые прошли проверку по ФИАС
        Long fiasAddressId = getFiasAddressId(ndflPersonId);

        if (fiasAddressId != null) {
            person.setAddress(buildAddress(rs));
        }

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

    public Long getFiasAddressId(Long ndflPersonId) {
        if (fiasAddressIdsMap != null) {
            Long result = fiasAddressIdsMap.get(ndflPersonId);
            if (result == null) {
                logger.warn("Не найден адрес физического лица " + ndflPersonId);
            }
            return result;
        } else {
            logger.warn("Не проинициализирован кэш справочника для проверки адресов физических лиц!");
        }
        return null;
    }
}
