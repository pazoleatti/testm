package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.model.identification.TaxpayerStatus;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class RefBookMapperFactory {

    @SuppressWarnings("unchecked")
    public <T extends RefBookSimple> RowMapper<T> getMapper(RefBook refBook) {
        if (RefBook.Id.OKTMO.getId() == refBook.getId()) {
            return new OktmoMapper();
        } else if (RefBook.Id.COUNTRY.getId() == refBook.getId()) {
            return new CountryMapper();
        } else if (RefBook.Id.DOCUMENT_CODES.getId() == refBook.getId()) {
            return new DepartmentDocTypeMapper();
        } else if (RefBook.Id.ASNU.getId() == refBook.getId()) {
            return new AsnuMapper();
        } else if (RefBook.Id.PERSON.getId() == refBook.getId()) {
            return new PersonMapper();
        } else if (RefBook.Id.INCOME_CODE.getId() == refBook.getId()) {
            return new IncomeTypeMapper();
        } else if (RefBook.Id.DEDUCTION_MARK.getId() == refBook.getId()) {
            return new DeductionMarkMapper();
        } else if (RefBook.Id.PERSON_ADDRESS.getId() == refBook.getId()) {
            return new PersonAddressMapper();
        } else if (RefBook.Id.TAXPAYER_STATUS.getId() == refBook.getId()) {
            return new TaxPayerStatusMapper();
        }
        throw new IllegalArgumentException("Unknown mapper for refBook = " + refBook.getId());
    }

    public class OktmoMapper<T> implements RowMapper<RefBookOktmo> {

        @Override
        public RefBookOktmo mapRow(ResultSet rs, int rowNum) throws SQLException {
            RefBookOktmo result = new RefBookOktmo();
            result.setId(rs.getLong("id"));
            result.setName(rs.getString("name"));
            result.setCode(rs.getString("code"));
            result.setSection(rs.getInt("razd"));
            return result;
        }
    }

    public class CountryMapper<T> implements RowMapper<RefBookCountry> {

        @Override
        public RefBookCountry mapRow(ResultSet rs, int rowNum) throws SQLException {
            RefBookCountry result = new RefBookCountry();
            result.setId(rs.getLong("id"));
            result.setName(rs.getString("name"));
            result.setCode(rs.getString("code"));
            return result;
        }
    }

    public class DepartmentDocTypeMapper<T> implements RowMapper<RefBookDocType> {

        @Override
        public RefBookDocType mapRow(ResultSet rs, int rowNum) throws SQLException {
            RefBookDocType result = new RefBookDocType();
            result.setId(rs.getLong("id"));
            result.setName(rs.getString("name"));
            result.setCode(rs.getString("code"));
            result.setPriority(rs.getInt("priority"));
            return result;
        }
    }

    public class AsnuMapper<T> implements RowMapper<RefBookAsnu> {

        @Override
        public RefBookAsnu mapRow(ResultSet rs, int rowNum) throws SQLException {
            RefBookAsnu result = new RefBookAsnu();
            result.setId(rs.getLong("id"));
            result.setName(rs.getString("name"));
            result.setCode(rs.getString("code"));
            result.setType(rs.getString("type"));
            result.setPriority(rs.getInt("priority"));
            return result;
        }
    }

    public class PersonMapper<T> implements RowMapper<RefBookPerson> {

        @Override
        public RefBookPerson mapRow(ResultSet rs, int rowNum) throws SQLException {
            RefBookPerson result = new RefBookPerson();
            result.setId(rs.getLong("id"));
            result.setFirstName(rs.getString("first_name"));
            result.setLastName(rs.getString("last_name"));
            result.setMiddleName(rs.getString("middle_name"));
            // TODO: там еще куча полей, но я не знаю какие будут нужны и не тащу лишние
            return result;
        }
    }

    public class IncomeTypeMapper<T> implements RowMapper<RefBookIncomeType> {

        @Override
        public RefBookIncomeType mapRow(ResultSet rs, int rowNum) throws SQLException {
            RefBookIncomeType result = new RefBookIncomeType();
            result.setId(rs.getLong("id"));
            result.setName(rs.getString("name"));
            result.setCode(rs.getString("code"));
            return result;
        }
    }

    public class DeductionMarkMapper<T> implements RowMapper<RefBookDeductionMark> {

        @Override
        public RefBookDeductionMark mapRow(ResultSet rs, int rowNum) throws SQLException {
            RefBookDeductionMark result = new RefBookDeductionMark();
            result.setId(rs.getLong("id"));
            result.setName(rs.getString("name"));
            result.setCode(rs.getString("code"));
            return result;
        }
    }

    public class PersonAddressMapper<T> implements RowMapper<RefBookAddress> {

        @Override
        public RefBookAddress mapRow(ResultSet rs, int rowNum) throws SQLException {
            RefBookAddress result = new RefBookAddress();
            result.setId(rs.getLong("id"));
            result.setRegionCode(rs.getString("REGION_CODE"));
            result.setPostalCode(rs.getString("POSTAL_CODE"));
            result.setDistrict(rs.getString("DISTRICT"));
            result.setCity(rs.getString("CITY"));
            result.setLocality(rs.getString("LOCALITY"));
            result.setStreet(rs.getString("STREET"));
            result.setHouse(rs.getString("HOUSE"));
            result.setBuild(rs.getString("BUILD"));
            result.setAppartment(rs.getString("APPARTMENT"));
            return result;
        }
    }

    public class TaxPayerStatusMapper<T> implements RowMapper<RefBookTaxpayerState> {

        @Override
        public RefBookTaxpayerState mapRow(ResultSet rs, int rowNum) throws SQLException {
            RefBookTaxpayerState result = new RefBookTaxpayerState();
            result.setId(rs.getLong("id"));
            result.setName(rs.getString("name"));
            result.setCode(rs.getString("code"));
            return result;
        }
    }
}
