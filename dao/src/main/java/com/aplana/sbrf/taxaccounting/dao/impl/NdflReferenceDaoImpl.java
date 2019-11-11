package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.NdflReferenceDao;
import com.aplana.sbrf.taxaccounting.model.refbook.NumFor2Ndfl;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@Transactional
public class NdflReferenceDaoImpl extends AbstractDao implements NdflReferenceDao {

    @Override
    public int updateField(final List<Long> uniqueRecordIds, String alias, final String value) {
        getJdbcTemplate().batchUpdate("update ndfl_references set " + alias + " = ? where ID = ?", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Long uniqueRecordId = uniqueRecordIds.get(i);
                ps.setString(1, value);
                ps.setLong(2, uniqueRecordId);
            }

            @Override
            public int getBatchSize() {
                return uniqueRecordIds.size();
            }
        });
        return uniqueRecordIds.size();
    }

    @Override
    public Integer countSequenceByYear(Integer year) {
        return getJdbcTemplate().queryForObject("select count(*) from user_sequences where sequence_name='SEQ_NDFL_REFERENCES_" + year + "'", Integer.class);
    }

    @Override
    public Integer getNextSprNum(Integer year) {
        return getJdbcTemplate().queryForObject("select SEQ_NDFL_REFERENCES_" + year + ".nextval from dual", Integer.class);
    }

    @Override
    public void createSequence(Integer year) {
        getJdbcTemplate().execute("CREATE SEQUENCE SEQ_NDFL_REFERENCES_" + year
                + " MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER NOCYCLE");
    }

    @Override
    public List<NumFor2Ndfl> getCorrSprNum(Long personId, int year, String kpp, String oktmo, int declarationTypeId) {
        return getJdbcTemplate().query("select nr.num, nr.correction_num " +
                        "from ndfl_references nr " +
                        "left join declaration_data dd on nr.declaration_data_id = dd.id " +
                        //Год отчетного периода
                        "left join department_report_period drp on dd.department_report_period_id = drp.id " +
                        "left join report_period rp on drp.report_period_id = rp.id " +
                        "left join tax_period tp on rp.tax_period_id = tp.id " +
                        //Физлицо
                        "left join ref_book_person p on nr.person_id = p.id " +
                        //Вид формы
                        "left join declaration_template dtemp on dd.declaration_template_id = dtemp.id " +
                        "left join declaration_type dtype on dtemp.declaration_type_id = dtype.id " +
                        "where p.record_id = ? " + // Физическое лицо
                        "and tp.year = ? " + // Форма.Период.Год
                        "and dd.kpp = ? " + // Форма.КПП
                        "and dd.oktmo = ? " + // Форма."ОКТМО"
                        "and dtype.id = ? ", // Форма."Макет формы"."Вид формы"
                new Object[]{personId, year, kpp, oktmo, declarationTypeId},
                new _2NdflNumRowMapper());
    }

    private static final class _2NdflNumRowMapper implements RowMapper<NumFor2Ndfl> {
        @Override
        public NumFor2Ndfl mapRow(ResultSet rs, int index) throws SQLException {

            NumFor2Ndfl numFor2Ndfl = new NumFor2Ndfl();

            numFor2Ndfl.setSprNum(rs.getInt("num"));
            numFor2Ndfl.setCorrNum(rs.getInt("correction_num"));

            return numFor2Ndfl;
        }
    }

    public Boolean checkExistingAnnulReport(Long declarationDataId, Integer num, String lastName, String firstName, String middleName, String innNp, String idDocNumber) {
        Boolean result = false;
        Integer resultCount = getJdbcTemplate().queryForObject("select count(*) from ndfl_references nr " +
                        "left join ndfl_person np on np.id = nr.ndfl_person_id " +
                        "where nr.correction_num = 99 and nr.declaration_data_id = ? and nr.num = ? and np.last_name = ? and np.first_name = ? and np.middle_name = ? and np.inn_np = ? and np.id_doc_number = ?  ",
                new Object[] {declarationDataId, num, lastName, firstName, middleName, innNp, idDocNumber}, Integer.class);
        if (resultCount > 0) result = true;
        return result;
    }

}
