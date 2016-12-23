package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.ndfl.NdflPersonDao;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.ndfl.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * @author Andrey Drunk
 */
@Repository
@Transactional
public class NdflPersonDaoImpl extends AbstractDao implements NdflPersonDao {

    private static final Log LOG = LogFactory.getLog(NdflPersonDaoImpl.class);

    private static final String DUPLICATE_ERORR_MSG = "Попытка перезаписать уже сохранённые данные!";

    @Override
    public List<NdflPerson> findAll() {
        try {
            return getJdbcTemplate().query("select * from ndfl_person np", new NdflPersonDaoImpl.NdflPersonRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public NdflPerson get(long ndflPersonDataId) {
        try {
            return getJdbcTemplate().queryForObject("select * from ndfl_person np where np.id = ?",
                    new Object[]{ndflPersonDataId},
                    new NdflPersonDaoImpl.NdflPersonRowMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("Сущность класса NdflPerson с id = %d не найдена в БД", ndflPersonDataId);
        }
    }

    @Override
    public Long save(NdflPerson ndflPerson) {
        JdbcTemplate jdbcTemplate = getJdbcTemplate();

        if (ndflPerson.getId() != null) {
            throw new DaoException(DUPLICATE_ERORR_MSG);
        }

        ndflPerson.setId(generateId(NdflPerson.SEQ, Long.class));
        jdbcTemplate.update("insert into ndfl_person " + createColumnsAndSource(NdflPerson.COLUMNS), ndflPerson.createPreparedStatementArgs());

        //
        List<NdflPersonIncome> ndflPersonIncomes = ndflPerson.getNdflPersonIncomes();

        if (ndflPersonIncomes == null || ndflPersonIncomes.isEmpty()) {
            //throw new DaoException("Пропущены обязательные данные о доходах!");
        }
        saveNdflPersonDetail(jdbcTemplate, insert(NdflPersonIncome.TABLE_NAME, NdflPersonIncome.COLUMNS), ndflPerson, ndflPersonIncomes);


        List<NdflPersonDeduction> ndflPersonDeductions = ndflPerson.getNdflPersonDeductions();
        saveNdflPersonDetail(jdbcTemplate, insert(NdflPersonDeduction.TABLE_NAME, NdflPersonDeduction.COLUMNS), ndflPerson, ndflPersonDeductions);

        List<NdflPersonPrepayment> ndflPersonPrepayments = ndflPerson.getNdflPersonPrepayments();
        saveNdflPersonDetail(jdbcTemplate, insert(NdflPersonPrepayment.TABLE_NAME, NdflPersonPrepayment.COLUMNS), ndflPerson, ndflPersonPrepayments);

        return ndflPerson.getId();
    }

    private void saveNdflPersonDetail(JdbcTemplate jdbcTemplate, String query, NdflPerson ndflPerson, List<? extends NdflPersonDetail> details){
        for (NdflPersonDetail detail : details) {
            if (detail.getId() != null) {
                throw new DaoException(DUPLICATE_ERORR_MSG);
            }
            detail.setId(generateId(NdflPersonIncome.SEQ, Long.class));
            detail.setNdflPersonId(ndflPerson.getId());
            jdbcTemplate.update(query, detail.createPreparedStatementArgs());
        }
    }

    private static String insert(String table, String[] columns) {
        StringBuilder sb = new StringBuilder();
        return sb.append("insert into ").append(table).append(" ").append(createColumnsAndSource(columns)).toString();
    }

    @Override
    public void delete(Long id) {
        int count = getJdbcTemplate().update("delete from ndfl_person where id = ?", id);
        if (count == 0) {
            throw new DaoException("Не удалось удалить сущность класса NdflPerson с id = %d, так как она не существует", id);
        }
    }

    @Override
    public List<NdflPerson> findNdflPersonByDeclarationDataId(Long declarationDataId) {
        //TODO
        return null;
    }

    private static final class NdflPersonRowMapper implements RowMapper<NdflPerson> {
        @Override
        public NdflPerson mapRow(ResultSet rs, int index) throws SQLException {

            NdflPerson person = new NdflPerson();
            person.setId(SqlUtils.getLong(rs, "id"));
            person.setDeclarationDataId(SqlUtils.getLong(rs, "declaration_data_id"));
            person.setInp(rs.getString("inp"));
            person.setSnils(rs.getString("snils"));
            person.setLastName(rs.getString("last_name"));
            person.setFirstName(rs.getString("first_name"));
            person.setMiddleName(rs.getString("middle_name"));
            person.setBirthDay(rs.getDate("birth_day"));
            person.setCitizenship(rs.getString("citizenship"));

            person.setInnNp(rs.getString("inn_np"));
            person.setInnForeign(rs.getString("inn_foreign"));
            person.setIdDocType(rs.getString("id_doc_type"));
            person.setIdDocNumber(rs.getString("id_doc_number"));
            person.setStatus(rs.getString("status"));
            person.setPostIndex(rs.getString("post_index"));
            person.setRegionCode(rs.getString("region_code"));
            person.setArea(rs.getString("area"));
            person.setCity(rs.getString("city"));

            person.setLocality(rs.getString("locality"));
            person.setStreet(rs.getString("street"));
            person.setHouse(rs.getString("house"));
            person.setBuilding(rs.getString("building"));
            person.setFlat(rs.getString("flat"));
            person.setCountryCode(rs.getString("country_code"));
            person.setAddress(rs.getString("address"));
            person.setAdditionalData(rs.getString("additional_data"));

            return person;
        }
    }


    public static String createColumnsAndSource(String[] columnDescriptors) {
        int iMax = columnDescriptors.length - 1;
        StringBuilder columns = new StringBuilder();
        StringBuilder source = new StringBuilder();
        columns.append(" (");
        source.append(" VALUES (");

        if (iMax == -1) {
            return "";
        }

        for (int i = 0; ; i++) {
            columns.append(columnDescriptors[i]);
            source.append("?");
            if (i == iMax) {
                return columns.append(')').toString() + source.append(')').toString();
            }
            columns.append(", ");
            source.append(", ");
        }
    }

}
