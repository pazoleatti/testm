package com.aplana.sbrf.taxaccounting.dao.impl.refbook.person;

import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.Permissive;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

class RefBookPersonMapper implements RowMapper<RefBookPerson> {

    @Override
    public RefBookPerson mapRow(ResultSet rs, int rowNum) throws SQLException {
        RefBookPerson result = new RefBookPerson();
        result.setId(rs.getLong("id"));
        result.setRecordId(rs.getLong("record_id"));
        result.setOldId(rs.getLong("old_id"));
        result.setVip(rs.getBoolean("vip"));
        result.setFirstName(rs.getString("first_name"));
        result.setLastName(rs.getString("last_name"));
        result.setMiddleName(rs.getString("middle_name"));
        result.setBirthDate(rs.getDate("birth_date"));
        result.setBirthPlace(rs.getString("birth_place"));
        result.setVersion(rs.getDate("start_date"));
        result.setVersionEnd(rs.getDate("end_date"));
        result.setInn(Permissive.of(rs.getString("inn")));
        result.setInnForeign(Permissive.of(rs.getString("inn_foreign")));
        result.setSnils(Permissive.of(rs.getString("snils")));

        RefBookDocType docType = new RefBookDocType();
        docType.setId(SqlUtils.getLong(rs, "doc_type_id"));
        docType.setCode(rs.getString("doc_code"));
        docType.setName(rs.getString("doc_name"));
        result.setDocType(Permissive.of(docType));

        result.setDocNumber(Permissive.of(rs.getString("doc_number")));

        Long addressId = SqlUtils.getLong(rs, "address_id");
        if (addressId == null) {
            result.setAddress(Permissive.<RefBookAddress>of(null));
            result.setForeignAddress(Permissive.<RefBookAddress>of(null));
        } else {
            RefBookAddress address = new RefBookAddress();
            RefBookAddress foreignAddress = new RefBookAddress();

            address.setId(rs.getLong("address_id"));
            foreignAddress.setId(rs.getLong("address_id"));

            address.setAddressType(rs.getInt("address_type"));
            foreignAddress.setAddressType(rs.getInt("address_type"));

            address.setPostalCode(rs.getString("postal_code"));
            address.setRegionCode(rs.getString("region_code"));
            address.setDistrict(rs.getString("district"));
            address.setCity(rs.getString("city"));
            address.setLocality(rs.getString("locality"));
            address.setStreet(rs.getString("street"));
            address.setBuild(rs.getString("building"));
            address.setHouse(rs.getString("house"));
            address.setApartment(rs.getString("apartment"));

            address.setAddress(rs.getString("address"));
            foreignAddress.setAddress(rs.getString("address"));

            Long countryId = SqlUtils.getLong(rs, "address_country_id");
            if (countryId == null) {
                address.setCountry(null);
                foreignAddress.setCountry(null);
            } else {
                RefBookCountry country = new RefBookCountry();
                country.setId(countryId);
                country.setCode(rs.getString("address_country_code"));
                country.setName(rs.getString("address_country_name"));
                address.setCountry(country);
                foreignAddress.setCountry(country);
            }

            result.setAddress(Permissive.of(address));
            result.setForeignAddress(Permissive.of(foreignAddress));
        }

        Long countryId = SqlUtils.getLong(rs, "citizenship_country_id");
        if (countryId == null) {
            result.setCitizenship(null);
        } else {
            RefBookCountry citizenship = new RefBookCountry();
            citizenship.setId(countryId);
            citizenship.setCode(rs.getString("citizenship_country_code"));
            citizenship.setName(rs.getString("citizenship_country_name"));
            result.setCitizenship(citizenship);
        }

        Long stateId = SqlUtils.getLong(rs, "state_id");
        if (stateId == null) {
            result.setTaxpayerState(null);
        } else {
            RefBookTaxpayerState taxpayerState = new RefBookTaxpayerState();
            taxpayerState.setId(stateId);
            taxpayerState.setCode(rs.getString("state_code"));
            taxpayerState.setName(rs.getString("state_name"));
            result.setTaxpayerState(taxpayerState);
        }

        Long asnuId = SqlUtils.getLong(rs, "asnu_id");
        if (asnuId == null) {
            result.setSource(null);
        } else {
            RefBookAsnu asnu = new RefBookAsnu();
            asnu.setId(asnuId);
            asnu.setCode(rs.getString("asnu_code"));
            asnu.setName(rs.getString("asnu_name"));
            asnu.setType(rs.getString("asnu_type"));
            asnu.setPriority(rs.getInt("asnu_priority"));
            result.setSource(asnu);
        }

        return result;
    }
}
