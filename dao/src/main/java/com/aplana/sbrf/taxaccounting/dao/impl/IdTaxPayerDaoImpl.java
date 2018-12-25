package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.IdTaxPayerDao;
import com.aplana.sbrf.taxaccounting.dao.util.DBUtils;
import com.aplana.sbrf.taxaccounting.model.refbook.PersonIdentifier;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;
import com.aplana.sbrf.taxaccounting.model.refbook.RegistryPerson;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import static com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils.in;

@Repository
public class IdTaxPayerDaoImpl extends AbstractDao implements IdTaxPayerDao {
    @Override
    public void createBatch(Collection<PersonIdentifier> personIdentifiers) {
        saveNewObjects(personIdentifiers, PersonIdentifier.TABLE_NAME, DBUtils.Sequence.REF_BOOK_RECORD.getName(), PersonIdentifier.COLUMNS, PersonIdentifier.FIELDS);
    }

    @Override
    public List<PersonIdentifier> getByPerson(RegistryPerson person) {
        Long recordId = person.getRecordId();
        String query = "select distinct \n" +
                "inp.id, inp.inp, \n" +
                "asnu.id asnu_id, asnu.code asnu_code, asnu.name asnu_name, asnu.type asnu_type, asnu.priority asnu_priority \n" +
                "from ref_book_id_tax_payer inp \n" +
                "left join ref_book_asnu asnu on asnu.id = inp.as_nu \n" +
                "where inp.person_id in (select id from ref_book_person where record_id = :recordId)";
        List<PersonIdentifier> result = getNamedParameterJdbcTemplate().query(query, new MapSqlParameterSource("recordId", recordId), ID_TAX_PAYER_MAPPER);
        for (PersonIdentifier inp: result) {
            inp.setPerson(person);
        }
        return result;
    }

    @Override
    public void updateBatch(Collection<PersonIdentifier> personIdentifiers) {
        updateObjects(personIdentifiers, PersonIdentifier.TABLE_NAME, PersonIdentifier.COLUMNS, PersonIdentifier.FIELDS);
    }

    @Override
    public void deleteByIds(Collection<Long> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            String query = "delete from ref_book_id_tax_payer where " + in("id", ids);
            getJdbcTemplate().update(query);
        }
    }

    private static RowMapper<PersonIdentifier> ID_TAX_PAYER_MAPPER = new RowMapper<PersonIdentifier>() {
        @Override
        public PersonIdentifier mapRow(ResultSet rs, int rowNum) throws SQLException {
            RefBookAsnu refBookAsnu = new RefBookAsnu();
            refBookAsnu.setId(rs.getLong("asnu_id"));
            refBookAsnu.setCode(rs.getString("asnu_code"));
            refBookAsnu.setName(rs.getString("asnu_name"));
            refBookAsnu.setType(rs.getString("asnu_type"));
            refBookAsnu.setPriority(rs.getInt("asnu_priority"));

            PersonIdentifier result = new PersonIdentifier();
            result.setAsnu(refBookAsnu);
            result.setId(rs.getLong("id"));
            result.setInp(rs.getString("inp"));

            return result;
        }
    };
}
