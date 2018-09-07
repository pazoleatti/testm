package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDepartmentDataDao;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class RefBookMapperFactory {

    @Autowired
    private RefBookDepartmentDataDao refBookDepartmentDataDao;

    @SuppressWarnings("unchecked")
    public <T extends RefBookSimple> RowMapper<T> getMapper(long refBookId) {
        if (RefBook.Id.OKTMO.getId() == refBookId) {
            return new OktmoMapper();
        } else if (RefBook.Id.COUNTRY.getId() == refBookId) {
            return new CountryMapper();
        } else if (RefBook.Id.DOCUMENT_CODES.getId() == refBookId) {
            return new DepartmentDocTypeMapper();
        } else if (RefBook.Id.ASNU.getId() == refBookId) {
            return new AsnuMapper();
        } else if (RefBook.Id.PERSON.getId() == refBookId) {
            return new PersonMapper();
        } else if (RefBook.Id.INCOME_CODE.getId() == refBookId) {
            return new IncomeTypeMapper();
        } else if (RefBook.Id.DEDUCTION_MARK.getId() == refBookId) {
            return new DeductionMarkMapper();
        } else if (RefBook.Id.PERSON_ADDRESS.getId() == refBookId) {
            return new PersonAddressMapper();
        } else if (RefBook.Id.TAXPAYER_STATUS.getId() == refBookId) {
            return new TaxPayerStatusMapper();
        } else if (RefBook.Id.DEPARTMENT.getId() == refBookId) {
            return new DepartmentMapper();
        } else if (RefBook.Id.PRESENT_PLACE.getId() == refBookId) {
            return new PresentPlaceMapper();
        } else if (RefBook.Id.MARK_SIGNATORY_CODE.getId() == refBookId) {
            return new SignatoryMarkMapper();
        } else if (RefBook.Id.REORGANIZATION.getId() == refBookId) {
            return new ReorganizationMapper();
        }
        throw new IllegalArgumentException("Unknown mapper for refBook = " + refBookId);
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

    public static class PersonMapper<T> implements RowMapper<RefBookPerson> {

        @Override
        public RefBookPerson mapRow(ResultSet rs, int rowNum) throws SQLException {
            RefBookPerson result = new RefBookPerson();
            result.setId(rs.getLong("id"));
            result.setRecordId(rs.getLong("record_id"));
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
            result.setApartment(rs.getString("APPARTMENT"));
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

    public class DepartmentMapper<T> implements RowMapper<RefBookDepartment> {
        @Override
        public RefBookDepartment mapRow(ResultSet resultSet, int i) throws SQLException {
            RefBookDepartment result = new RefBookDepartment();
            result.setId(resultSet.getInt("id"));
            result.setName(resultSet.getString("name"));
            result.setFullName(refBookDepartmentDataDao.fetchFullName(result.getId()));
            return result;
        }
    }

    public class PresentPlaceMapper<T> implements RowMapper<RefBookPresentPlace> {
        @Override
        public RefBookPresentPlace mapRow(ResultSet resultSet, int i) throws SQLException {
            RefBookPresentPlace result = new RefBookPresentPlace();
            result.setId(resultSet.getLong("id"));
            result.setCode(resultSet.getString("code"));
            result.setName(resultSet.getString("name"));
            return result;
        }
    }

    public class SignatoryMarkMapper<T> implements RowMapper<RefBookSignatoryMark> {
        @Override
        public RefBookSignatoryMark mapRow(ResultSet resultSet, int i) throws SQLException {
            RefBookSignatoryMark result = new RefBookSignatoryMark();
            result.setId(resultSet.getLong("id"));
            result.setCode(resultSet.getString("code"));
            result.setName(resultSet.getString("name"));
            return result;
        }
    }

    public class ReorganizationMapper<T> implements RowMapper<RefBookReorganization> {
        @Override
        public RefBookReorganization mapRow(ResultSet resultSet, int i) throws SQLException {
            RefBookReorganization result = new RefBookReorganization();
            result.setId(resultSet.getLong("id"));
            result.setCode(resultSet.getString("code"));
            result.setName(resultSet.getString("name"));
            return result;
        }
    }
}
