package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.model.PersonData;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Andrey Drunk
 */
@Repository
@Transactional
public class RefBookPersonDaoImpl extends AbstractDao implements RefBookPersonDao {

    @Autowired
    RefBookDao refBookDao;

    private static final String FIND_REFBOOK_PERSON_BY_PRIMARY_RNU_NDFL_QUERY = buildFindRefBookPersonByPrimaryRnuNdflQuery();
    private static String buildFindRefBookPersonByPrimaryRnuNdflQuery() {
        StringBuilder SQL = new StringBuilder();
        SQL.append("WITH t AS \n");
        SQL.append("  (SELECT MAX(version) version, record_id, :pVersion calc_date \n");
        SQL.append("  FROM ref_book_person r \n");
        SQL.append("  WHERE status = 0 \n");
        SQL.append("  AND version <= :pVersion \n");
        SQL.append("  AND NOT EXISTS \n");
        SQL.append("    (SELECT 1 \n");
        SQL.append("    FROM ref_book_person r2 \n");
        SQL.append("    WHERE r2.record_id = r.record_id \n");
        SQL.append("    AND r2.status     !=             -1 \n");
        SQL.append("    AND r2.version BETWEEN r.version + interval '1' DAY AND :pVersion \n");
        SQL.append("    ) \n");
        SQL.append("  GROUP BY record_id \n");
        SQL.append("  ) \n");
        SQL.append("SELECT \n");
        SQL.append("  --фл \n");
        SQL.append("  dubl.last_name AS ndfl_person_ln, dubl.id AS ndfl_person_id, person.id AS person_id, person.record_id AS person_record_id, person.last_name AS last_name, person.first_name AS first_name, person.middle_name AS middle_name, person.sex AS sex, person.birth_date AS birth_date, person.inn AS inn, person.inn_foreign AS inn_foreign, person.snils AS snils, person.pension AS pension, person.medical AS midical, person.social AS social, person.employee AS employee, \n");
        SQL.append("  --ссылки на записи \n");
        SQL.append("  person.citizenship AS citizenship_ref_id, person.taxpayer_state AS status_ref_id, person.version, \n");
        SQL.append("  --документы \n");
        SQL.append("  person_doc.doc_number AS document_number, person_doc.doc_id AS document_type_ref_id, \n");
        SQL.append("  --идентификаторы \n");
        SQL.append("  taxpayer_id.id AS inp_id, taxpayer_id.inp AS inp, taxpayer_id.as_nu AS asnu_ref_id, \n");
        SQL.append("  -- адрес \n");
        SQL.append("  addr.id AS addr_id, addr.address_type, addr.country_id, addr.region_code, addr.postal_code, addr.district, addr.city, addr.locality, addr.street, addr.house, addr.build, addr.appartment, addr.status, addr.record_id, addr.address \n");
        SQL.append("FROM t \n");
        SQL.append("JOIN \n");
        SQL.append("  (SELECT np.id id, lower(np.last_name) last_name, lower(np.first_name) first_name, lower(np.middle_name) middle_name, np.birth_day birthday, REPLACE(REPLACE(lower(np.snils), ' ', ''), '-', '') snils, np.inn_np inn, np.inn_foreign inn_f, dt.id id_doc_type, REPLACE(lower(np.id_doc_number), ' ', '') id_doc_number, lower(np.inp) inp, :pVersion calc_date \n");
        SQL.append("  FROM ndfl_person np \n");
        SQL.append("  LEFT JOIN ref_book_doc_type dt \n");
        SQL.append("  ON (dt.code               = np.id_doc_type \n");
        SQL.append("  AND dt.status             = 0) \n");
        SQL.append("  WHERE declaration_data_id = :pDeclarationId \n");
        SQL.append("  ) dubl ON (dubl.calc_date = t.calc_date) \n");
        SQL.append("JOIN ref_book_person person \n");
        SQL.append("ON (person.status                                    = 0 \n");
        SQL.append("AND person.version                                   = t.version \n");
        SQL.append("AND person.record_id                                 = t.record_id \n");
        SQL.append("AND ((REPLACE(lower(person.last_name), ' ', '')      = dubl.last_name \n");
        SQL.append("AND REPLACE(lower(person.first_name), ' ', '')       = dubl.first_name \n");
        SQL.append("AND REPLACE(lower(person.middle_name), ' ', '')      = dubl.middle_name \n");
        SQL.append("AND person.birth_date                                = dubl.birthday \n");
        SQL.append("OR (REPLACE(REPLACE(person.snils, ' ', ''), '-', '') = dubl.snils) \n");
        SQL.append("OR (REPLACE(person.inn, ' ', '')                     = dubl.inn) \n");
        SQL.append("OR (REPLACE(person.inn_foreign, ' ', '')             = dubl.inn_f)))) \n");
        SQL.append("LEFT JOIN ref_book_address addr \n");
        SQL.append("ON (addr.id = person.address) \n");
        SQL.append("LEFT JOIN ref_book_id_doc person_doc \n");
        SQL.append("ON (person_doc.person_id = person.id) \n");
        SQL.append("LEFT JOIN ref_book_id_tax_payer taxpayer_id \n");
        SQL.append("ON (taxpayer_id.person_id                           = person.id) \n");
        SQL.append("WHERE (taxpayer_id.as_nu                            = :asnuId \n");
        SQL.append("AND (lower(taxpayer_id.inp)                         = dubl.inp) \n");
        SQL.append("OR (person_doc.doc_id                               = dubl.id_doc_type \n");
        SQL.append("AND (REPLACE(lower(person_doc.doc_number), ' ', '') = dubl.id_doc_number)))");
        return SQL.toString();
    }

    @Override
    public Map<Long, List<PersonData>> findRefBookPersonByPrimaryRnuNdfl(Long declarationDataId, Long asnuId, Date version) {

        MapSqlParameterSource param = new MapSqlParameterSource();

        param.addValue("pDeclarationId", declarationDataId);
        param.addValue("asnuId", asnuId);
        param.addValue("pVersion", version);

        //long time = System.currentTimeMillis();
        Map<Long, List<PersonData>> result = getNamedParameterJdbcTemplate().query(FIND_REFBOOK_PERSON_BY_PRIMARY_RNU_NDFL_QUERY, param, new PersonDataExtractor());
        //System.out.println("findRefBookPersonByPrimaryRnuNdfl size: " + result.size() + " (" + (System.currentTimeMillis() - time) + " ms)");

        return result;
    }

    private static final String FIND_REFBOOK_PERSON_BY_PRIMARY_1151111_QUERY = buildFindRefBookPersonByPrimary1151111lQuery();
    private static String buildFindRefBookPersonByPrimary1151111lQuery() {
        StringBuilder SQL = new StringBuilder();
        SQL.append("WITH t AS \n");
        SQL.append("  (SELECT MAX(version) version, record_id, :pVersion calc_date \n");
        SQL.append("  FROM ref_book_person r \n");
        SQL.append("  WHERE status = 0 \n");
        SQL.append("  AND version <= :pVersion \n");
        SQL.append("  AND NOT EXISTS \n");
        SQL.append("    (SELECT 1 \n");
        SQL.append("    FROM ref_book_person r2 \n");
        SQL.append("    WHERE r2.record_id = r.record_id \n");
        SQL.append("    AND r2.status     !=             -1 \n");
        SQL.append("    AND r2.version BETWEEN r.version + interval '1' DAY AND :pVersion \n");
        SQL.append("    ) \n");
        SQL.append("  GROUP BY record_id \n");
        SQL.append("  ) \n");
        SQL.append("SELECT \n");
        SQL.append("  --фл \n");
        SQL.append("  dubl.last_name AS ndfl_person_ln, dubl.id AS ndfl_person_id, person.id AS person_id, person.record_id AS person_record_id, person.last_name AS last_name, person.first_name AS first_name, person.middle_name AS middle_name, person.sex AS sex, person.birth_date AS birth_date, person.inn AS inn, person.inn_foreign AS inn_foreign, person.snils AS snils, person.pension AS pension, person.medical AS midical, person.social AS social, person.employee AS employee, \n");
        SQL.append("  --ссылки на записи \n");
        SQL.append("  person.citizenship AS citizenship_ref_id, person.taxpayer_state AS status_ref_id, person.version, \n");
        SQL.append("  --документы \n");
        SQL.append("  person_doc.doc_number AS document_number, person_doc.doc_id AS document_type_ref_id, \n");
        SQL.append("  --идентификаторы \n");
        SQL.append("  NULL AS inp, NULL AS asnu_ref_id, \n");
        SQL.append("  -- адрес \n");
        SQL.append("  addr.id AS addr_id, addr.address_type, addr.country_id, addr.region_code, addr.postal_code, addr.district, addr.city, addr.locality, addr.street, addr.house, addr.build, addr.appartment, addr.status, addr.record_id, addr.address \n");
        SQL.append("FROM t \n");
        SQL.append("JOIN \n");
        SQL.append("  (SELECT np.id id, lower(np.FAMILIA) last_name, lower(np.IMYA) first_name, lower(np.OTCHESTVO) middle_name, np.DATA_ROZD birthday, REPLACE(REPLACE(lower(np.SNILS), ' ', ''), '-', '') snils, np.INNFL inn, dt.id id_doc_type, REPLACE(lower(np.SER_NOM_DOC), ' ', '') id_doc_number, :pVersion calc_date \n");
        SQL.append("  FROM raschsv_pers_sv_strah_lic np \n");
        SQL.append("  LEFT JOIN ref_book_doc_type dt \n");
        SQL.append("  ON (dt.code               = np.KOD_VID_DOC \n");
        SQL.append("  AND dt.status             = 0) \n");
        SQL.append("  WHERE declaration_data_id = :pDeclarationId \n");
        SQL.append("  ) dubl ON (dubl.calc_date = t.calc_date) \n");
        SQL.append("JOIN ref_book_person person \n");
        SQL.append("ON (person.status                                    = 0 \n");
        SQL.append("AND person.version                                   = t.version \n");
        SQL.append("AND person.record_id                                 = t.record_id \n");
        SQL.append("AND ((REPLACE(lower(person.last_name), ' ', '')      = dubl.last_name \n");
        SQL.append("AND REPLACE(lower(person.first_name), ' ', '')       = dubl.first_name \n");
        SQL.append("AND REPLACE(lower(person.middle_name), ' ', '')      = dubl.middle_name \n");
        SQL.append("AND person.birth_date                                = dubl.birthday \n");
        SQL.append("OR (REPLACE(REPLACE(person.snils, ' ', ''), '-', '') = dubl.snils) \n");
        SQL.append("OR (REPLACE(person.inn, ' ', '')                     = dubl.inn)))) \n");
        SQL.append("LEFT JOIN ref_book_address addr \n");
        SQL.append("ON (addr.id = person.address) \n");
        SQL.append("LEFT JOIN ref_book_id_doc person_doc \n");
        SQL.append("ON (person_doc.person_id                            = person.id) \n");
        SQL.append("WHERE ((person_doc.doc_id                           = dubl.id_doc_type \n");
        SQL.append("AND (REPLACE(lower(person_doc.doc_number), ' ', '') = dubl.id_doc_number)))");
        return SQL.toString();
    }

    @Override
    public Map<Long, List<PersonData>> findRefBookPersonByPrimary1151111(Long declarationDataId, Long asnuId, Date version) {

        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("pDeclarationId", declarationDataId);
        param.addValue("asnuId", asnuId);
        param.addValue("pVersion", version);

        //long time = System.currentTimeMillis();

        Map<Long, List<PersonData>> result = getNamedParameterJdbcTemplate().query(FIND_REFBOOK_PERSON_BY_PRIMARY_1151111_QUERY, param, new PersonDataExtractor());

        //System.out.println("findRefBookPersonByPrimary1151111 size: " + result.size() + " (" + (System.currentTimeMillis() - time) + " ms)");

        return result;
    }


    private class PersonDataExtractor implements ResultSetExtractor<Map<Long, List<PersonData>>> {

        public Map<Long, List<PersonData>> extractData(ResultSet rs) throws SQLException, DataAccessException {

            Map<Long, List<PersonData>> result = new HashMap<Long, List<PersonData>>();

            while (rs.next()) {
                Long ndflPersonId = SqlUtils.getLong(rs, "ndfl_person_id");

                if (ndflPersonId == null) {
                    throw new ServiceException("Не задано значение person_id");
                }

                List<PersonData> personDataList = result.get(ndflPersonId);

                if (personDataList == null) {
                    personDataList = new ArrayList<PersonData>();
                    result.put(ndflPersonId, personDataList);
                }

                PersonData personData = getPersonData(rs);
                personDataList.add(personData);

            }
            return result;
        }

        private PersonData getPersonData(ResultSet rs) throws SQLException {
            PersonData person = new PersonData();

            //person
            person.setId(SqlUtils.getLong(rs, "person_id"));
            person.setRecordId(SqlUtils.getLong(rs, "person_record_id"));
            person.setLastName(rs.getString("last_name"));
            person.setFirstName(rs.getString("first_name"));
            person.setMiddleName(rs.getString("middle_name"));
            person.setSex(SqlUtils.getInteger(rs, "sex"));
            person.setInn(rs.getString("inn"));
            person.setInnForeign(rs.getString("inn_foreign"));
            person.setSnils(rs.getString("snils"));
            person.setBirthDate(rs.getDate("birth_date"));

            //ссылки на справочники
            person.setTaxPayerStatusId(SqlUtils.getLong(rs, "status_ref_id"));
            person.setCitizenshipId(SqlUtils.getLong(rs, "citizenship_ref_id"));

            //identical
            person.setInp(rs.getString("inp"));
            person.setAsnuId(SqlUtils.getLong(rs, "asnu_ref_id"));

            //documents
            person.setDocumentTypeId(SqlUtils.getLong(rs, "document_type_ref_id"));
            person.setDocumentNumber(rs.getString("document_number"));

            //address
            person.setAddressType(SqlUtils.getInteger(rs, "address_type"));
            person.setCountryId(SqlUtils.getLong(rs, "country_id"));
            person.setRegionCode(rs.getString("region_code"));
            person.setPostalCode(rs.getString("postal_code"));
            person.setDistrict(rs.getString("district"));
            person.setCity(rs.getString("city"));
            person.setLocality(rs.getString("locality"));
            person.setStreet(rs.getString("street"));
            person.setHouse(rs.getString("house"));
            person.setBuild(rs.getString("build"));
            person.setAppartment(rs.getString("appartment"));
            person.setAddressIno(rs.getString("address"));

            //additional
            person.setPension(SqlUtils.getInteger(rs, "pension"));
            person.setMedical(SqlUtils.getInteger(rs, "midical"));
            person.setSocial(SqlUtils.getInteger(rs, "social"));
            person.setEmployee(SqlUtils.getInteger(rs, "employee"));

            return person;
        }
    }
}
