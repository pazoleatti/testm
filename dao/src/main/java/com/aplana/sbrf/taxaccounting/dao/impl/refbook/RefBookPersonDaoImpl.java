package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.model.PersonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Andrey Drunk
 */
@Repository
@Transactional
public class RefBookPersonDaoImpl extends AbstractDao implements RefBookPersonDao {

    @Autowired
    RefBookDao refBookDao;

    private static final String FIND_PERSON_QUERY = buildFindPersonQuery();

    private static String buildFindPersonQuery() {
        StringBuilder SQL = new StringBuilder();
        SQL.append("SELECT \n");
        SQL.append("  --фл \n");
        SQL.append("  person.id AS person_id, person.record_id AS person_record_id, person.last_name AS last_name, person.first_name AS first_name, person.middle_name AS middle_name, person.sex AS sex, person.birth_date AS birth_date, person.inn AS inn, person.inn_foreign AS inn_foreign, person.snils AS snils, person.pension AS pension, person.medical AS midical, person.social AS social, \n");
        SQL.append("  --ссылки на записи \n");
        SQL.append("  person.citizenship AS citizenship_ref_id, person.taxpayer_state AS status_ref_id, \n");
        SQL.append("  --документы \n");
        SQL.append("  person_doc.doc_number AS document_number, person_doc.doc_id AS document_type_ref_id, \n");
        SQL.append("  --идентификаторы \n");
        SQL.append("  taxpayer_id.inp AS inp, taxpayer_id.as_nu AS asnu_ref_id, \n");
        SQL.append("  -- адрес \n");
        SQL.append("  addr.id AS addr_id, addr.address_type, addr.country_id, addr.region_code, addr.postal_code, addr.district, addr.city, addr.locality, addr.street, addr.house, addr.build, addr.appartment, addr.status, addr.record_id \n");
        SQL.append("FROM ref_book_person person \n");
        SQL.append("LEFT JOIN ref_book_id_tax_payer taxpayer_id \n");
        SQL.append("ON person.id = taxpayer_id.person_id --Идентификаторы физлица \n");
        SQL.append("LEFT JOIN ref_book_asnu asnu \n");
        SQL.append("ON taxpayer_id.as_nu = asnu.id --АСНУ \n");
        SQL.append("LEFT JOIN ref_book_id_doc person_doc \n");
        SQL.append("ON person.id = person_doc.person_id --ДУЛ \n");
        SQL.append("LEFT JOIN ref_book_address addr \n");
        SQL.append("ON person.address                                    = addr.id \n");
        SQL.append("WHERE person.status                                  = 0 \n");
        SQL.append("AND ((lower(taxpayer_id.inp)                         = :inp \n");
        SQL.append("AND taxpayer_id.as_nu                                = :asnuId) \n");
        SQL.append("OR (REPLACE(REPLACE(person.snils, ' ', ''), '-', '') = :snils) \n");
        SQL.append("OR (REPLACE(person.inn, ' ', '')                     = :inn) \n");
        SQL.append("OR (REPLACE(person.inn_foreign, ' ', '')             = :innForeign) \n");
        SQL.append("OR (REPLACE(lower(person_doc.doc_number), ' ', '')   = :docNumber \n");
        SQL.append("AND person_doc.doc_id                                = :docTypeId) \n");
        SQL.append("OR (REPLACE(lower(person.last_name), ' ', '')        = :lastName \n");
        SQL.append("AND REPLACE(lower(person.first_name), ' ', '')       = :firstName \n");
        SQL.append("AND REPLACE(lower(person.middle_name), ' ', '')      = :middleName \n");
        SQL.append("AND person.birth_date                                = :birthDate))");
        return SQL.toString();
    }

    public List<PersonData> findPersonByPersonData(PersonData personData) {

        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("inp", clean(personData.getInp()));
        param.addValue("asnuId", personData.getAsnuId());
        param.addValue("snils", cleanSnils(personData.getSnils()));
        param.addValue("inn", clean(personData.getInn()));
        param.addValue("innForeign", clean(personData.getInnForeign()));

        param.addValue("docNumber", clean(personData.getDocumentNumber()));
        param.addValue("docTypeId", personData.getDocumentTypeId());

        param.addValue("lastName", clean(personData.getLastName()));
        param.addValue("firstName", clean(personData.getFirstName()));
        param.addValue("middleName", clean(personData.getMiddleName()));
        param.addValue("birthDate", personData.getBirthDate());

        return getNamedParameterJdbcTemplate().query(FIND_PERSON_QUERY, param, new PersonDataMapper());
    }

    private String clean(String string) {
        if (string != null && !string.isEmpty()) {
            return string.replace(" ", "").toLowerCase();
        } else {
            return null;
        }
    }

    private String cleanSnils(String string) {
        if (string != null && !string.isEmpty()) {
            return clean(string).replace("-", "");
        } else {
            return null;
        }
    }

    class PersonDataMapper implements RowMapper<PersonData> {
        @Override
        public PersonData mapRow(ResultSet rs, int i) throws SQLException {
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
            person.setAsnuId(SqlUtils.getLong(rs, "citizenship_ref_id"));

            //documents
            person.setDocumentTypeId(SqlUtils.getLong(rs, "document_type_ref_id"));
            person.setDocumentNumber(rs.getString("asnu_ref_id"));

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

            person.setPension(SqlUtils.getInteger(rs, "pension"));
            person.setMedical(SqlUtils.getInteger(rs, "midical"));
            person.setSocial(SqlUtils.getInteger(rs, "social"));
            person.setEmployee(SqlUtils.getInteger(rs, "employee"));

            return person;
        }
    }

}
