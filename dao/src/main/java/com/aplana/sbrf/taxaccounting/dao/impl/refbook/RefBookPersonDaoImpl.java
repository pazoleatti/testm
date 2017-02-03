package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.PersonData;
import org.springframework.beans.factory.annotation.Autowired;
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

    private String buildFindPersonBySearchCriteriaQuery() {
        StringBuilder SQL = new StringBuilder();
        SQL.append("SELECT rpp.id, rpp.last_name, rpp.first_name, rpp.middle_name, rpp.sex, rpp.birth_date, rpp.citizenship, rtp.inp, ras.id, ras.code, rpp.inn, rpp.inn_foreign, rpp.snils, rts.code, rid.doc_id, rid.doc_number, rpp.pension, rpp.medical, rpp.social, rpp.version \n");
        SQL.append("FROM ref_book_person rpp \n");
        SQL.append("LEFT JOIN ref_book_id_tax_payer rtp \n");
        SQL.append("ON rpp.id = rtp.person_id \n");
        SQL.append("LEFT JOIN ref_book_asnu ras \n");
        SQL.append("ON rtp.as_nu = ras.id \n");
        SQL.append("LEFT JOIN ref_book_id_doc rid \n");
        SQL.append("ON rpp.id = rid.person_id \n");
        SQL.append("LEFT JOIN ref_book_taxpayer_state rts \n");
        SQL.append("ON rpp.taxpayer_state                            = rts.id \n");
        SQL.append("WHERE (lower(rtp.inp)                            = :inp \n");
        SQL.append("AND ras.id                                       = :asnuId) \n");
        SQL.append("OR (REPLACE(REPLACE(rpp.snils, ' ', ''), '-','') = :snils) \n");
        SQL.append("OR (REPLACE(rpp.inn, ' ', '')                    = :inn) \n");
        SQL.append("OR (REPLACE(rpp.inn_foreign, ' ', '')            = :innForeign) \n");
        SQL.append("OR (REPLACE(lower(rid.doc_number), ' ', '')      = :docNumber \n");
        SQL.append("AND rid.doc_id                                   = :docTypeId) \n");
        SQL.append("OR (REPLACE(lower(rpp.last_name), ' ', '')       = :lastName \n");
        SQL.append("AND REPLACE(lower(rpp.first_name), ' ', '')      = :firstName \n");
        SQL.append("AND REPLACE(lower(rpp.middle_name), ' ', '')     = :middleName \n");
        SQL.append("AND rpp.birth_date                               = :birthDate)");
        return SQL.toString();
    }

    public List<PersonData> findPersonByPersonData(PersonData personData) {
        String sqlQuery = buildFindPersonBySearchCriteriaQuery();

        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("inp", personData.getInp());
        param.addValue("asnuId", personData.getAsnuId());

        param.addValue("snils", personData.getSnils());
        param.addValue("inn", personData.getInn());
        param.addValue("innForeign", personData.getInnForeign());

        param.addValue("docNumber", personData.getDocumentNumber());
        param.addValue("docTypeId", null);

        param.addValue("lastName", personData.getLastName());
        param.addValue("firstName", personData.getFirstName());
        param.addValue("middleName", personData.getMiddleName());
        param.addValue("birthDate", personData.getBirthDate());



        return getNamedParameterJdbcTemplate().query(sqlQuery, param, new PersonDataMapper());
    }

    @Override
    public Long createPerson(PersonData personData) {
        return null;
    }

    class PersonDataMapper implements RowMapper<PersonData> {
        @Override
        public PersonData mapRow(ResultSet rs, int i) throws SQLException {
            PersonData person = new PersonData();
            person.setId(rs.getLong(1));
            person.setLastName(rs.getString(2));

            person.setStatus(VersionedObjectStatus.getStatusById(SqlUtils.getInteger(rs, "status")));
            return person;
        }
    }

}
