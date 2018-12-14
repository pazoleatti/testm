package com.aplana.sbrf.taxaccounting.dao.impl.refbook.person;

import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.refbook.Address;
import com.aplana.sbrf.taxaccounting.model.refbook.IdDoc;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookCountry;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDocType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookTaxpayerState;
import com.aplana.sbrf.taxaccounting.model.refbook.RegistryPerson;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RegistryPersonMapper implements RowMapper<RegistryPerson> {
    @Override
    public RegistryPerson mapRow(ResultSet rs, int rowNum) throws SQLException {
        RegistryPerson result = new RegistryPerson();
        result.setId(SqlUtils.getLong(rs, "id"));
        result.setRecordId(SqlUtils.getLong(rs, "record_id"));
        result.setOldId(SqlUtils.getLong(rs, "old_id"));
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

        result.setReportDoc(getReportDoc(rs));
        if (result.getReportDoc() != null) {
            result.getReportDoc().setPerson(stub(result));
        }
        result.setCitizenship(getCitizenship(rs));
        result.setSource(getAsnu(rs));
        result.setTaxPayerState(getTaxpayerState(rs));
        result.setAddress(getAddress(rs));

        return result;
    }

    private RegistryPerson stub(RegistryPerson person) {
        RegistryPerson docPersonStub = new RegistryPerson();
        docPersonStub.setId(person.getId());
        docPersonStub.setOldId(person.getOldId());
        docPersonStub.setRecordId(person.getRecordId());
        return docPersonStub;
    }

    private RefBookDocType getDocType(ResultSet rs) throws SQLException {
        RefBookDocType docType = null;
        if (SqlUtils.getLong(rs, "doc_type_id") != null) {
            docType = new RefBookDocType();
            docType.setId(SqlUtils.getLong(rs, "doc_type_id"));
            docType.setName(rs.getString("doc_name"));
            docType.setCode(rs.getString("doc_code"));
            docType.setPriority(rs.getInt("doc_type_priority"));
        }
        return docType;
    }

    private IdDoc getReportDoc(ResultSet rs) throws SQLException {
        IdDoc personDocument = null;
        if (SqlUtils.getLong(rs, "d_id") != null) {
            personDocument = new IdDoc();
            personDocument.setId(SqlUtils.getLong(rs, "d_id"));
            personDocument.setDocumentNumber(rs.getString("doc_number"));
            personDocument.setDocType(getDocType(rs));
        }
        return personDocument;
    }

    private RefBookCountry getCitizenship(ResultSet rs) throws SQLException {
        RefBookCountry citizenship = null;
        if (SqlUtils.getLong(rs, "citizenship_country_id") != null) {
            citizenship = new RefBookCountry();
            citizenship.setId(SqlUtils.getLong(rs, "citizenship_country_id"));
            citizenship.setCode(rs.getString("citizenship_country_code"));
            citizenship.setName(rs.getString("citizenship_country_name"));
        }
        return citizenship;
    }

    private RefBookAsnu getAsnu(ResultSet rs) throws SQLException {
        RefBookAsnu refBookAsnu = null;
        if (SqlUtils.getLong(rs, "asnu_id") != null) {
            refBookAsnu = new RefBookAsnu();
            refBookAsnu.setId(SqlUtils.getLong(rs, "asnu_id"));
            refBookAsnu.setCode(rs.getString("asnu_code"));
            refBookAsnu.setName(rs.getString("asnu_name"));
            refBookAsnu.setType(rs.getString("asnu_type"));
            refBookAsnu.setPriority(rs.getInt("asnu_priority"));
        }
        return refBookAsnu;
    }

    private RefBookTaxpayerState getTaxpayerState(ResultSet rs) throws SQLException {
        RefBookTaxpayerState taxpayerStatus = null;
        if (SqlUtils.getLong(rs, "state_id") != null) {
            taxpayerStatus = new RefBookTaxpayerState();
            taxpayerStatus.setId(SqlUtils.getLong(rs, "state_id"));
            taxpayerStatus.setCode(rs.getString("state_code"));
            taxpayerStatus.setName(rs.getString("state_name"));
        }
        return taxpayerStatus;
    }

    private Address getAddress(ResultSet rs) throws SQLException {
        Address address = new Address();
        address.setPostalCode(rs.getString("postal_code"));
        address.setRegionCode(rs.getString("region_code"));
        address.setDistrict(rs.getString("district"));
        address.setCity(rs.getString("city"));
        address.setLocality(rs.getString("locality"));
        address.setStreet(rs.getString("street"));
        address.setHouse(rs.getString("house"));
        address.setBuild(rs.getString("building"));
        address.setAppartment(rs.getString("apartment"));
        address.setCountry(getCountry(rs));
        address.setAddressIno(rs.getString("address_foreign"));
        return address;
    }

    private RefBookCountry getCountry(ResultSet rs) throws SQLException {
        RefBookCountry country = null;
        if (SqlUtils.getLong(rs, "address_country_id") != null) {
            country = new RefBookCountry();
            country.setId(SqlUtils.getLong(rs, "address_country_id"));
            country.setCode(rs.getString("address_country_code"));
            country.setName(rs.getString("address_country_name"));
        }
        return country;
    }
}
