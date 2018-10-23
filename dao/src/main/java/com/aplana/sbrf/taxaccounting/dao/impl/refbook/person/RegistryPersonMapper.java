package com.aplana.sbrf.taxaccounting.dao.impl.refbook.person;

import com.aplana.sbrf.taxaccounting.model.identification.RefBookDocType;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RegistryPersonMapper implements RowMapper<RegistryPerson> {
    @Override
    public RegistryPerson mapRow(ResultSet rs, int rowNum) throws SQLException {
        RegistryPerson result = new RegistryPerson();
        result.setId(rs.getLong("id"));
        result.setRecordId(rs.getLong("record_id"));
        result.setOldId(rs.getLong("old_id"));
        result.setVip(rs.getBoolean("vip"));
        result.setFirstName(rs.getString("first_name"));
        result.setLastName(rs.getString("last_name"));
        result.setMiddleName(rs.getString("middle_name"));
        result.setBirthDate(rs.getDate("birth_date"));
        result.setBirthPlace(rs.getString("birth_place"));
        result.setStartDate(rs.getDate("start_date"));
        result.setEndDate(rs.getDate("end_date"));
        result.setInn(rs.getString("inn"));
        result.setInnForeign(rs.getString("inn_foreign"));
        result.setSnils(rs.getString("snils"));

        RefBookDocType docType = new RefBookDocType();
        docType.setId(rs.getLong("doc_type_id"));
        docType.setName(rs.getString("doc_name"));
        docType.setCode(rs.getString("doc_code"));
        docType.setPriority(rs.getInt("doc_type_priority"));

        IdDoc personDocument = new IdDoc();
        personDocument.setDocType(docType);
        personDocument.setId(rs.getLong("d_id"));
        personDocument.setDocumentNumber(rs.getString("doc_number"));

        if (personDocument.getId().equals(0L)) {
            result.setReportDoc(null);
        } else {
            result.setReportDoc(personDocument);
        }

        RefBookCountry citizenship = new RefBookCountry();
        citizenship.setId(rs.getLong("citizenship_country_id"));
        citizenship.setCode(rs.getString("citizenship_country_code"));
        citizenship.setName(rs.getString("citizenship_country_name"));

        result.setCitizenship(citizenship);

        RefBookAsnu refBookAsnu = new RefBookAsnu();
        refBookAsnu.setId(rs.getLong("asnu_id"));
        refBookAsnu.setCode(rs.getString("asnu_code"));
        refBookAsnu.setName(rs.getString("asnu_name"));
        refBookAsnu.setType(rs.getString("asnu_type"));
        refBookAsnu.setPriority(rs.getInt("asnu_priority"));

        result.setSource(refBookAsnu);

        RefBookTaxpayerState taxpayerStatus = new RefBookTaxpayerState();
        taxpayerStatus.setId(rs.getLong("state_id"));
        taxpayerStatus.setCode(rs.getString("state_code"));
        taxpayerStatus.setName(rs.getString("state_name"));

        result.setTaxPayerState(taxpayerStatus);

        Address address = new Address();
        RefBookCountry country = new RefBookCountry();
        country.setId(rs.getLong("address_country_id"));
        country.setCode(rs.getString("address_country_code"));
        country.setName(rs.getString("address_country_name"));

        address.setCountry(country);

        address.setPostalCode(rs.getString("postal_code"));
        address.setRegionCode(rs.getString("region_code"));
        address.setDistrict(rs.getString("district"));
        address.setCity(rs.getString("city"));
        address.setLocality(rs.getString("locality"));
        address.setStreet(rs.getString("street"));
        address.setHouse(rs.getString("house"));
        address.setBuild(rs.getString("building"));
        address.setAppartment(rs.getString("apartment"));
        address.setAddressIno(rs.getString("address_foreign"));

        result.setAddress(address);

        return result;
    }
}
