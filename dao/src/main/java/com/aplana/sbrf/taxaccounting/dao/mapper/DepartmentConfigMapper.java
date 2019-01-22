package com.aplana.sbrf.taxaccounting.dao.mapper;

import com.aplana.sbrf.taxaccounting.model.refbook.DepartmentConfig;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDepartment;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookOktmo;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookPresentPlace;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookReorganization;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookSignatoryMark;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DepartmentConfigMapper implements RowMapper<DepartmentConfig> {
    public final static String FIELDS = " dc.id, dc.record_id, dc.kpp, oktmo.id oktmo_id, oktmo.code oktmo_code, oktmo.name oktmo_name, dc.version, dc.version_end," +
            " dc.department_id, dc.tax_organ_code, pp.id present_place_id, pp.code present_place_code, pp.name present_place_name, dc.name, dc.phone," +
            " reorg.id reorg_id, reorg.code reorg_code, reorg.name reorg_name, dc.reorg_inn, reorg_kpp, sign.id signatory_id, sign.code signatory_code, sign.name signatory_name, " +
            " dc.signatory_surname, dc.signatory_firstname, dc.signatory_lastname, dc.approve_doc_name, dc.approve_org_name ";

    @Override
    public DepartmentConfig mapRow(ResultSet rs, int rowNum) throws SQLException {
        DepartmentConfig departmentConfig = new DepartmentConfig();
        departmentConfig.setId(rs.getLong("id"));
        departmentConfig.setRecordId(rs.getLong("record_id"));
        departmentConfig.setKpp(rs.getString("kpp"));
        departmentConfig.setOktmo(mapOktmo(rs));
        departmentConfig.setStartDate(rs.getDate("version"));
        departmentConfig.setEndDate(rs.getDate("version_end"));
        departmentConfig.setDepartment(mapDepartment(rs));
        departmentConfig.setTaxOrganCode(rs.getString("tax_organ_code"));
        departmentConfig.setPresentPlace(mapPresentPlace(rs));
        departmentConfig.setName(rs.getString("name"));
        departmentConfig.setPhone(rs.getString("phone"));
        departmentConfig.setReorganization(mapReorganization(rs));
        departmentConfig.setReorgInn(rs.getString("reorg_inn"));
        departmentConfig.setReorgKpp(rs.getString("reorg_kpp"));
        departmentConfig.setSignatoryMark(mapSignatoryMark(rs));
        departmentConfig.setSignatorySurName(rs.getString("signatory_surname"));
        departmentConfig.setSignatoryFirstName(rs.getString("signatory_firstname"));
        departmentConfig.setSignatoryLastName(rs.getString("signatory_lastname"));
        departmentConfig.setApproveDocName(rs.getString("approve_doc_name"));
        departmentConfig.setApproveOrgName(rs.getString("approve_org_name"));
        return departmentConfig;
    }

    private RefBookOktmo mapOktmo(ResultSet rs) throws SQLException {
        RefBookOktmo oktmo = new RefBookOktmo();
        oktmo.setId(rs.getLong("oktmo_id"));
        oktmo.setCode(rs.getString("oktmo_code"));
        oktmo.setName(rs.getString("oktmo_name"));
        return oktmo;
    }

    private RefBookDepartment mapDepartment(ResultSet rs) throws SQLException {
        RefBookDepartment department = new RefBookDepartment();
        department.setId(rs.getInt("department_id"));
        return department;
    }

    private RefBookPresentPlace mapPresentPlace(ResultSet rs) throws SQLException {
        RefBookPresentPlace presentPlace = new RefBookPresentPlace();
        presentPlace.setId(rs.getLong("present_place_id"));
        if (rs.wasNull()) {
            return null;
        }
        presentPlace.setCode(rs.getString("present_place_code"));
        presentPlace.setName(rs.getString("present_place_name"));
        return presentPlace;
    }

    private RefBookReorganization mapReorganization(ResultSet rs) throws SQLException {
        RefBookReorganization reorganization = new RefBookReorganization();
        reorganization.setId(rs.getLong("reorg_id"));
        if (rs.wasNull()) {
            return null;
        }
        reorganization.setCode(rs.getString("reorg_code"));
        reorganization.setName(rs.getString("reorg_name"));
        return reorganization;
    }

    private RefBookSignatoryMark mapSignatoryMark(ResultSet rs) throws SQLException {
        RefBookSignatoryMark signatoryMark = new RefBookSignatoryMark();
        signatoryMark.setId(rs.getLong("signatory_id"));
        if (rs.wasNull()) {
            return null;
        }
        signatoryMark.setCode(rs.getInt("signatory_code"));
        signatoryMark.setName(rs.getString("signatory_name"));
        return signatoryMark;
    }
}
