package com.aplana.sbrf.taxaccounting.dao.identification;

import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.identification.Address;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.identification.PersonDocument;
import com.aplana.sbrf.taxaccounting.model.identification.PersonIdentifier;
import com.aplana.sbrf.taxaccounting.model.refbook.AddressObject;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Andrey Drunk
 */
public class NaturalPersonPrimaryRnuRowMapper extends NaturalPersonPrimaryRowMapper {

    private Long asnuId;

    public Long getAsnuId() {
        return asnuId;
    }

    public void setAsnuId(Long asnuId) {
        this.asnuId = asnuId;
    }

    @Override
    public NaturalPerson mapRow(ResultSet rs, int rowNum) throws SQLException {

        NaturalPerson person = new NaturalPerson();

        person.setPersonId(SqlUtils.getLong(rs, "id"));
        person.setRefBookPersonId(SqlUtils.getLong(rs, "person_id"));

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
            personIdentifier.setInp(inp);
            personIdentifier.setAsnuId(asnuId);
            person.getPersonIdentityList().add(personIdentifier);
        }

        String documentTypeCode = rs.getString("id_doc_type");
        String documentNumber = rs.getString("id_doc_number");

        if (documentNumber != null && documentTypeCode != null) {
            PersonDocument personDocument = new PersonDocument();
            personDocument.setDocumentNumber(documentNumber);
            personDocument.setDocType(getDocTypeByCode(documentTypeCode));
            person.getPersonDocumentList().add(personDocument);
        }

        person.setTaxPayerStatus(getTaxpayerStatusByCode(rs.getString("status")));
        person.setAddress(buildAddress(rs));

        //rs.getString("additional_data")
        return person;
    }

    private Address buildAddress(ResultSet rs) throws SQLException {

        if (getFiasAddres() != null) {
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

            return address;
        } else {
            return null;
        }
    }

    public AddressObject getFiasAddres() {
        //TODO Получить адрес в справонике фиас
        return new AddressObject();
    }

}
