package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.PersonTbDao;
import com.aplana.sbrf.taxaccounting.dao.util.DBUtils;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.refbook.PersonTb;
import com.aplana.sbrf.taxaccounting.model.refbook.RegistryPerson;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

@Repository
public class PersonTbDaoImpl extends AbstractDao implements PersonTbDao{
    @Override
    public void saveBatch(Collection<PersonTb> personTbs) {
        saveNewObjects(personTbs, PersonTb.TABLE_NAME, DBUtils.Sequence.REF_BOOK_RECORD.getName(), PersonTb.COLUMNS, PersonTb.FIELDS);
    }

    @Override
    public List<PersonTb> getByPerson(RegistryPerson person) {
        Long recordId = person.getRecordId();
        String query = "select distinct \n" +
                "tb.id, tb.import_date, \n" +
                "dep.id dep_id, dep.name dep_name \n" +
                "from ref_book_person_tb tb \n" +
                "left join department dep on dep.id = tb.tb_department_id \n" +
                "where person_id in (select id from ref_book_person where record_id = :recordId)";
        List<PersonTb> result = getNamedParameterJdbcTemplate().query(query, new MapSqlParameterSource("recordId", recordId), PERSON_TB_MAPPER);
        for (PersonTb personTb : result) {
            personTb.setPerson(person);
        }
        return result;
    }

    private static RowMapper<PersonTb> PERSON_TB_MAPPER = new RowMapper<PersonTb>() {
        @Override
        public PersonTb mapRow(ResultSet rs, int rowNum) throws SQLException {
            Department department = new Department();
            department.setId(rs.getInt("dep_id"));
            department.setName(rs.getString("dep_name"));

            PersonTb result = new PersonTb();

            result.setId(rs.getLong("id"));
            result.setImportDate(rs.getDate("import_date"));
            result.setTbDepartment(department);

            return result;
        }
    };
}
