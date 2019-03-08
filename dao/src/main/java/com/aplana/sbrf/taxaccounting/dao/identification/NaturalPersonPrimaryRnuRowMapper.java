package com.aplana.sbrf.taxaccounting.dao.identification;

import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Andrey Drunk
 */
public class NaturalPersonPrimaryRnuRowMapper extends NaturalPersonPrimaryRowMapper {

    private RefBookAsnu asnu;

    public RefBookAsnu getAsnu() {
        return asnu;
    }

    public void setAsnu(RefBookAsnu asnu) {
        this.asnu = asnu;
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

        String citizenshipCode = rs.getString("citizenship");
        RefBookCountry citizenship = getCountryByCode(citizenshipCode);
        citizenship.setCode(citizenshipCode);
        person.setCitizenship(citizenship);


        person.setInn(rs.getString("inn_np"));
        person.setInnForeign(rs.getString("inn_foreign"));

        String inp = rs.getString("inp");
        if (inp != null) {
            PersonIdentifier personIdentifier = new PersonIdentifier();
            personIdentifier.setPerson(person);
            personIdentifier.setInp(inp);
            personIdentifier.setAsnu(asnu);
            person.getPersonIdentityList().add(personIdentifier);
            person.setSource(asnu);
        }

        String documentTypeCode = rs.getString("id_doc_type");
        String documentNumber = rs.getString("id_doc_number");
        RefBookDocType refBookDocType = getDocTypeByCode(documentTypeCode);
        if (documentNumber != null) {
            IdDoc personDocument = new IdDoc();
            personDocument.setPerson(person);
            personDocument.setDocumentNumber(documentNumber);
            refBookDocType.setCode(documentTypeCode);
            personDocument.setDocType(refBookDocType);
            person.getDocuments().add(personDocument);
        }

        String stateCode = rs.getString("status");
        RefBookTaxpayerState state = getTaxpayerStatusByCode(stateCode);
        state.setCode(stateCode);
        person.setTaxPayerState(state);

        person.setAddress(buildAddress(rs));

        return person;
    }

    public Address buildAddress(ResultSet rs) throws SQLException {
        Address address = new Address();
        RefBookCountry country = getCountryByCode(rs.getString("country_code"));
        address.setCountry(country != null ? country : new RefBookCountry());
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
    }

}
