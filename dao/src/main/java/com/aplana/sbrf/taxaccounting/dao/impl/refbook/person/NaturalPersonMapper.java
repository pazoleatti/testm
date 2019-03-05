package com.aplana.sbrf.taxaccounting.dao.impl.refbook.person;

import com.aplana.sbrf.taxaccounting.dao.identification.RefDataHolder;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.refbook.Address;
import com.aplana.sbrf.taxaccounting.model.refbook.IdDoc;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookCountry;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class NaturalPersonMapper implements RowMapper<NaturalPerson> {

    private RefDataHolder refDataHolder;

    public NaturalPersonMapper(RefDataHolder refDataHolder) {
        this.refDataHolder = refDataHolder;
    }

    @Override
    public NaturalPerson mapRow(ResultSet rs, int rowNum) throws SQLException {
        NaturalPerson result = new NaturalPerson();
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

        result.setCitizenship(refDataHolder.getCountryById(SqlUtils.getLong(rs, "citizenship_country_id")));
        result.setSource(refDataHolder.getAsnuMap().get(SqlUtils.getLong(rs, "asnu_id")));
        result.setTaxPayerState(refDataHolder.getTaxpayerStatusById(SqlUtils.getLong(rs, "state_id")));
        result.setAddress(getAddress(rs));
        result.setReportDoc(getReportDoc(rs));
        if (result.getReportDoc() != null) {
            result.getReportDoc().setPerson(result);
        }
        return result;
    }

    private Address getAddress(ResultSet rs) throws SQLException {
        Address address = new Address();
        RefBookCountry country = refDataHolder.getCountryById(SqlUtils.getLong(rs, "address_country_id"));
        address.setCountry(country != null ? country : new RefBookCountry());
        address.setPostalCode(rs.getString("postal_code"));
        address.setRegionCode(rs.getString("region_code"));
        address.setDistrict(rs.getString("district"));
        address.setCity(rs.getString("city"));
        address.setLocality(rs.getString("locality"));
        address.setStreet(rs.getString("street"));
        address.setHouse(rs.getString("house"));
        address.setBuild(rs.getString("building"));
        address.setAppartment(rs.getString("apartment"));
        address.setCountry((country != null ? country : new RefBookCountry()));
        address.setAddressIno(rs.getString("address_foreign"));
        return address;
    }

    private IdDoc getReportDoc(ResultSet rs) throws SQLException {
        IdDoc personDocument = null;

        if (SqlUtils.getLong(rs, "d_id") != null) {
            personDocument = new IdDoc();
            personDocument.setId(SqlUtils.getLong(rs, "d_id"));
            personDocument.setDocumentNumber(rs.getString("doc_number"));
            personDocument.setDocType(refDataHolder.getDocTypeById(SqlUtils.getLong(rs, "doc_type_id")));
        }
        return personDocument;
    }
}
