package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.IdDocDao;
import com.aplana.sbrf.taxaccounting.dao.util.DBUtils;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.refbook.IdDoc;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDocType;
import com.aplana.sbrf.taxaccounting.model.refbook.RegistryPerson;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import static com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils.in;

@Repository
public class IdDocDaoImpl extends AbstractDao implements IdDocDao {

    @Override
    public void deleteByIds(List<Long> ids) {
        if (ids == null) throw new NullPointerException("Ids list is null");
        if (ids.isEmpty()) throw new DaoException("Ids list is empty");

        String resetReportDocsQuery = "update ref_book_person set report_doc = null where " + in("report_doc", ids);
        getJdbcTemplate().update(resetReportDocsQuery);

        String deleteDocsQuery = "delete from ref_book_id_doc where " + in("id", ids);
        getJdbcTemplate().update(deleteDocsQuery);
    }

    public void createBatch(final Collection<IdDoc> idDocs) {
        saveNewObjects(idDocs, IdDoc.TABLE_NAME, DBUtils.Sequence.REF_BOOK_RECORD.getName(), IdDoc.COLUMNS, IdDoc.FIELDS);
    }

    @Override
    public void createBatchWithDefinedIds(Collection<IdDoc> idDocs) {
        saveNewObjectsWithDefinedIds(idDocs, IdDoc.TABLE_NAME, DBUtils.Sequence.REF_BOOK_RECORD.getName(), IdDoc.COLUMNS, IdDoc.FIELDS);
    }

    @Override
    public void updateBatch(Collection<IdDoc> idDocs) {
        updateObjects(idDocs, IdDoc.TABLE_NAME, IdDoc.COLUMNS, IdDoc.FIELDS);
    }

    @Override
    public List<IdDoc> getByPerson(RegistryPerson person) {
        Long recordId = person.getRecordId();
        String query = "select distinct \n" +
                "doc.id d_id, doc.doc_number, doc.person_id doc_person_id, doc_type.id doc_type_id, doc_type.code doc_code, doc_type.name doc_name, doc_type.priority doc_type_priority \n" +
                "from ref_book_id_doc doc \n" +
                "left join ref_book_doc_type doc_type on doc_type.id = doc.doc_id \n" +
                "where doc.person_id in (select id from ref_book_person where record_id = :recordId)";
        List<IdDoc> result = getNamedParameterJdbcTemplate().query(query, new MapSqlParameterSource("recordId", recordId), ID_DOC_MAPPER);
        return result;
    }

    @Override
    public int findIdDocCount(Long personRecordId) {
        String query = "select count(doc.id)\n" +
                "from ref_book_id_doc doc \n" +
                "left join ref_book_doc_type doc_type on doc_type.id = doc.doc_id \n" +
                "where doc.person_id in (select id from ref_book_person where record_id = :personRecordId)";
        return getNamedParameterJdbcTemplate().queryForObject(query, new MapSqlParameterSource("personRecordId", personRecordId), Integer.class);
    }

    private static RowMapper<IdDoc> ID_DOC_MAPPER = new RowMapper<IdDoc>() {
        @Override
        public IdDoc mapRow(ResultSet rs, int rowNum) throws SQLException {
            RefBookDocType docType = new RefBookDocType();
            docType.setId(rs.getLong("doc_type_id"));
            docType.setName(rs.getString("doc_name"));
            docType.setCode(rs.getString("doc_code"));
            docType.setPriority(rs.getInt("doc_type_priority"));

            IdDoc result = new IdDoc();
            result.setDocType(docType);
            result.setId(rs.getLong("d_id"));
            result.setDocumentNumber(rs.getString("doc_number"));

            RegistryPerson person = new RegistryPerson();
            person.setId(rs.getLong("doc_person_id"));
            result.setPerson(person);
            return result;
        }
    };
}
