package com.aplana.sbrf.taxaccounting.dao.impl.refbook.person;

import com.aplana.sbrf.taxaccounting.dao.identification.NaturalPersonRefbookHandler;
import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.RefBookMapperFactory;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.mapper.RefBookValueMapper;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.dao.util.DBUtils;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.filter.refbook.RefBookPersonFilter;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;


@Repository
public class RefBookPersonDaoImpl extends AbstractDao implements RefBookPersonDao {

    @Autowired
    RefBookDao refBookDao;
    @Autowired
    RefBookMapperFactory refBookMapperFactory;

    @Override
    public void clearRnuNdflPerson(Long declarationDataId) {
        HashMap<String, Object> values = new HashMap<>();
        values.put("declarationDataId", declarationDataId);
        getNamedParameterJdbcTemplate().update("update NDFL_PERSON set PERSON_ID = null where DECLARATION_DATA_ID = :declarationDataId", values);
    }

    @Override
    public void fillRecordVersions() {
        getJdbcTemplate().update("call person_pkg.FillRecordVersions()");
    }

    @Override
    public Map<Long, Map<Long, NaturalPerson>> findPersonForUpdateFromPrimaryRnuNdfl(Long declarationDataId, NaturalPersonRefbookHandler naturalPersonHandler) {
        SimpleJdbcCall call = new SimpleJdbcCall(getJdbcTemplate()).withCatalogName("person_pkg").withFunctionName("GetPersonForUpd");
        call.declareParameters(new SqlOutParameter("ref_cursor", CURSOR, naturalPersonHandler), new SqlParameter("p_declaration", Types.NUMERIC));
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("p_declaration", declarationDataId);
        call.execute(params);
        return naturalPersonHandler.getResult();
    }

    @Override
    public Map<Long, Map<Long, NaturalPerson>> findPersonForCheckFromPrimaryRnuNdfl(Long declarationDataId, NaturalPersonRefbookHandler naturalPersonHandler) {
        SimpleJdbcCall call = new SimpleJdbcCall(getJdbcTemplate()).withCatalogName("person_pkg").withFunctionName("GetPersonForCheck");
        call.declareParameters(new SqlOutParameter("ref_cursor", CURSOR, naturalPersonHandler), new SqlParameter("p_declaration", Types.NUMERIC), new SqlParameter("p_asnu", Types.NUMERIC));
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("p_declaration", declarationDataId);
        call.execute(params);
        return naturalPersonHandler.getResult();
    }

    /**
     * Получение данных о ФЛ из ПНФ
     */
    @Override
    public List<NaturalPerson> findNaturalPersonPrimaryDataFromNdfl(long declarationDataId, RowMapper<NaturalPerson> naturalPersonRowMapper) {

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("declarationDataId", declarationDataId);

        String SQL = "" +
                "SELECT id, declaration_data_id, person_id, row_num, inp, snils, last_name, first_name, middle_name, " +
                "   birth_day, citizenship, inn_np, inn_foreign, id_doc_type, id_doc_number, status, post_index, region_code, " +
                "   area, city, locality, street, house, building, flat, country_code, address, additional_data, " +
                "   NULL correct_num, NULL period, NULL rep_period, NULL num, NULL sv_date  \n" +
                "FROM ndfl_person \n" +
                "WHERE declaration_data_id = :declarationDataId";
        return getNamedParameterJdbcTemplate().query(SQL, params, naturalPersonRowMapper);
    }

    @Override
    public void setDuplicates(List<Long> addedDuplicateRecordIds, Long changingPersonRecordId) {
        String sql = "UPDATE ref_book_person SET record_id = :changingPersonRecordId WHERE record_id in (:addedDuplicateRecordIds)";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("changingPersonRecordId", changingPersonRecordId)
                .addValue("addedDuplicateRecordIds", addedDuplicateRecordIds);

        getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public void deleteDuplicates(List<Long> deletedDuplicateOldIds) {
        String sql = "UPDATE ref_book_person SET record_id = old_id where old_id in (:deletedDuplicateOldIds) and old_id <> record_id";
        getNamedParameterJdbcTemplate().update(sql, new MapSqlParameterSource("deletedDuplicateOldIds", deletedDuplicateOldIds));
    }

    @Override
    public void setOriginal(Long changingPersonRecordId, Long changingPersonOldId, Long addedOriginalRecordId) {
        String sql;
        if (changingPersonRecordId == changingPersonOldId) {
            sql = "UPDATE ref_book_person set record_id = :addedOriginalRecordId where record_id = :changingPersonRecordId";
        } else {
            sql = "UPDATE ref_book_person set record_id = :addedOriginalRecordId where record_id = :changingPersonRecordId AND old_id = :changingPersonOldId";
        }
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("addedOriginalRecordId", addedOriginalRecordId)
                .addValue("changingPersonRecordId", changingPersonRecordId)
                .addValue("changingPersonOldId", changingPersonOldId);
        getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public void deleteOriginal(Long changingPersonRecordId, Long changingPersonOldId) {
        String sql = "UPDATE ref_book_person set record_id = old_id where record_id = :changingPersonRecordId AND old_id = :changingPersonOldId";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("changingPersonOldId", changingPersonOldId)
                .addValue("changingPersonRecordId", changingPersonRecordId);
        getNamedParameterJdbcTemplate().update(sql, params);
    }

    @Override
    public List<Long> getDuplicateIds(Set<Long> originalRecordIds) {
        Map<String, Object> params = new HashMap<>();
        return getNamedParameterJdbcTemplate().queryForList(
                String.format("with version as (select old_id, max(version) version from ref_book_person \n" +
                                "where %s and old_id is not null and old_status = 0 \n" +
                                "group by old_id) \n" +
                                "select id \n" +
                                "from ref_book_person rbp \n" +
                                "join version on version.version = rbp.version and version.old_id = rbp.old_id and old_status = 0",
                        SqlUtils.transformToSqlInStatementViaTmpTable("id", originalRecordIds)),
                params,
                Long.class);
    }

    @Override
    public int getCountOfUniqueEntries(long declarationDataId) {
        return getJdbcTemplate().queryForObject("select count(DISTINCT rbp.id) " +
                        "from ref_book_person rbp " +
                        "join ndfl_person np " +
                        "on np.person_id = rbp.id " +
                        "where np.declaration_data_id = ?",
                new Object[]{declarationDataId},
                new int[]{Types.NUMERIC},
                Integer.class);
    }

    @Override
    public List<Integer> getPersonTbIds(long personId) {
        String query = "" +
                "select d.id \n" +
                "from department d, ref_book_person_tb p2tb \n" +
                "where p2tb.person_id = " + personId + " \n" +
                "    and p2tb.tb_department_id = d.id";
        return getJdbcTemplate().queryForList(query, Integer.class);
    }

    @Override
    public PagingResult<RegistryPerson> getPersons(PagingParams pagingParams, RefBookPersonFilter filter) {

        SelectPersonQueryGenerator selectPersonQueryGenerator = new SelectPersonQueryGenerator(filter, pagingParams);
        String query = selectPersonQueryGenerator.generatePagedAndFilteredQuery();
        List<RegistryPerson> persons = getJdbcTemplate().query(query, new RegistryPersonMapper());

        int count = getPersonsCount(filter);

        return new PagingResult<>(persons, count);
    }

    @Override
    public int getPersonsCount(RefBookPersonFilter filter) {
        SelectPersonQueryGenerator selectPersonQueryGenerator = new SelectPersonQueryGenerator(filter);
        String filteredPersonsQuery = selectPersonQueryGenerator.generateFilteredQuery();
        return selectCountOfQueryResults(filteredPersonsQuery);
    }

    private Integer selectCountOfQueryResults(String query) {
        return getJdbcTemplate().queryForObject("select count(*) from (" + query + ")", Integer.class);
    }


    public PagingResult<Map<String, RefBookValue>> fetchPersonsAsMap(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {

        String personSql = "select p.*, (select min(version) - interval '1' day from ref_book_person where status in (0,2) and record_id = p.record_id and version > p.version) as record_version_to from (\n" +
                "  select frb.*, frb.version as record_version_from, s.id as TAXPAYER_STATE_ID, s.code as TAXPAYER_STATE_CODE, s.name as TAXPAYER_STATE_NAME, \n" +
                "  c.id as CITIZENSHIP_ID, c.code as CITIZENSHIP_CODE, c.name as CITIZENSHIP_NAME, \n" +
                "  asnu.id as SOURCE_ID_ID, asnu.name as SOURCE_ID_NAME, asnu.code as SOURCE_ID_CODE, asnu.priority as SOURCE_ID_PRIORITY, asnu.type as SOURCE_ID_TYPE,\n" +
                "  a.id as A_ID, a.REGION_CODE || ',' || a.POSTAL_CODE || ',' || a.DISTRICT || ',' || a.CITY || ',' || a.LOCALITY || ',' || a.STREET || ',' || a.HOUSE || ',' || a.BUILD || ',' || a.APPARTMENT as ADDRESS_ADDRESS_FULL\n" +
                "  from REF_BOOK_PERSON frb \n" +
                "  left join REF_BOOK_TAXPAYER_STATE s on s.id = frb.TAXPAYER_STATE\n" +
                "  left join REF_BOOK_COUNTRY c on c.id = frb.CITIZENSHIP\n" +
                "  left join REF_BOOK_ADDRESS a on a.id = frb.ADDRESS \n" +
                "  left join REF_BOOK_ASNU asnu on asnu.id = frb.SOURCE_ID \n" +
                "  where frb.status = 0%s) p ";

        String baseSql = String.format(personSql, version != null ? " and frb.version = (select max(version) from REF_BOOK_PERSON where version <= :version and record_id = frb.record_id and status = 0)" : "") +
                (isNotEmpty(filter) ? "where " + filter : "");

        MapSqlParameterSource params = new MapSqlParameterSource();
        if (version != null) {
            params.addValue("version", version);
        }
        if (pagingParams != null) {
            params.addValue("start", pagingParams.getStartIndex() + 1);
            params.addValue("end", pagingParams.getStartIndex() + pagingParams.getCount());
        }

        String query = prepareStatement(pagingParams, filter, sortAttribute, baseSql);

        RefBook refBook = getRefBook();

        return new PagingResult<>(getNamedParameterJdbcTemplate().query(query, params, new RefBookValueMapper(refBook)));
    }

    private String prepareStatement(PagingParams pagingParams, String filter, RefBookAttribute sortAttribute, String baseSql) {

        String sortColumnName = "id";
        if (sortAttribute != null) {
            if (sortAttribute.getAttributeType() == RefBookAttributeType.REFERENCE) {
                sortColumnName = sortAttribute.getAlias() + "_" + sortAttribute.getRefBookAttribute().getAlias();
            } else {
                sortColumnName = sortAttribute.getAlias();
            }

        }
        String direction = "asc";
        if (pagingParams != null && isNotEmpty(pagingParams.getDirection())) {
            direction = pagingParams.getDirection();
        }
        String hint = (filter == null || filter.isEmpty()) ? "/*+ FIRST_ROWS */" : "/*+ PARALLEL(16) */";

        return pagingParams != null ?
                "select " + hint + "* from (select r.*, row_number() over (order by " + sortColumnName + " " + direction + ") as rn from (\n" + baseSql + ") r)\n where rn between :start and :end"
                : baseSql;
    }


    public RefBook getRefBook() {
        return refBookDao.get(RefBook.Id.PERSON.getId());
    }

    @Override
    public List<RegistryPerson> fetchOriginal(Long id) {
        String sql = SelectPersonQueryGenerator.SELECT_FULL_PERSON + "\n" +
                "where person.old_id = person.record_id and \n" +
                "person.record_id = (select record_id from ref_book_person where id = :id)\n" +
                "and old_id <> (select old_id from ref_book_person where id = :id)\n" +
                "order by person.start_date desc";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        try {
            return getNamedParameterJdbcTemplate().query(sql, params, new RegistryPersonMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<RegistryPerson> fetchDuplicates(Long id) {
        String sql = SelectPersonQueryGenerator.SELECT_FULL_PERSON + "\n" +
                "where person.old_id <> person.record_id and \n" +
                "person.record_id = (select record_id from ref_book_person where id = :id)\n" +
                "and person.id <> :id\n" +
                "order by person.start_date desc";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        try {
            return getNamedParameterJdbcTemplate().query(sql, params, new RegistryPersonMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public RegistryPerson fetchPersonVersion(Long id) {
        String sql = SelectPersonQueryGenerator.SELECT_FULL_PERSON + "\n" +
                "WHERE person.id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        try {
            return getNamedParameterJdbcTemplate().queryForObject(sql, params, new RegistryPersonMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void updateRegistryPerson(RegistryPerson person) {
        updateObjects(Collections.singletonList(person), RegistryPerson.TABLE_NAME, RegistryPerson.COLUMNS, RegistryPerson.FIELDS);
    }

    @Override
    public void updateRegistryPersonIncRepDocId(Long oldReportDocId, Long newReportDocId) {
        String sqlOldValue = "UPDATE ref_book_id_doc set inc_rep = 0 WHERE id = :oldReportDocId";
        String sqlNewValue = "UPDATE ref_book_id_doc set inc_rep = 1 WHERE id = :newReportDocId";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("oldReportDocId", oldReportDocId)
                .addValue("newReportDocId", newReportDocId);

        if (oldReportDocId != null) {
            getNamedParameterJdbcTemplate().update(sqlOldValue, params);
        }
        if (newReportDocId != null) {
            getNamedParameterJdbcTemplate().update(sqlNewValue, params);
        }
    }

    public List<RegistryPerson> fetchNonDuplicatesVersions(long recordId) {
        Date actualDate = new Date();
        String query = SelectPersonQueryGenerator.SELECT_FULL_PERSON + "\n" +
                        "where person.record_id = :recordId\n" +
                        "and person.old_id = person.record_id";
        MapSqlParameterSource params = new MapSqlParameterSource("recordId", recordId);
        params.addValue("actualDate" , actualDate);
        return getNamedParameterJdbcTemplate().query(query, params, new RegistryPersonMapper());
    }

    @Override
    public PagingResult<RegistryPerson> fetchOriginalDuplicatesCandidates(PagingParams pagingParams, RefBookPersonFilter filter) {

        SelectPersonQueryGenerator selectPersonQueryGenerator = new SelectPersonOriginalDuplicatesQueryGenerator(filter, pagingParams);
        String query = selectPersonQueryGenerator.generatePagedAndFilteredQuery();
        List<RegistryPerson> persons = getJdbcTemplate().query(query, new RegistryPersonMapper());

        selectPersonQueryGenerator.setPagingParams(null);
        int count = selectCountOfQueryResults(selectPersonQueryGenerator.generatePagedAndFilteredQuery());

        return new PagingResult<>(persons, count);
    }

    @Override
    public void saveBatch(Collection<RegistryPerson> persons) {
        saveNewObjects(persons, RegistryPerson.TABLE_NAME, DBUtils.Sequence.REF_BOOK_RECORD.getName(), RegistryPerson.COLUMNS, RegistryPerson.FIELDS);
    }

    @Override
    public void updateBatch(Collection<RegistryPerson> persons) {
        StringBuilder sql = new StringBuilder("UPDATE ")
                .append(RegistryPerson.TABLE_NAME)
                .append(" SET ");
        for (int i = 5; i < RegistryPerson.FIELDS.length; i++) {
            sql.append(RegistryPerson.COLUMNS[i])
                    .append(" = :")
                    .append(RegistryPerson.FIELDS[i]);
            if (RegistryPerson.FIELDS.length - i > 1) {
                sql.append(", ");
            }
        }
        sql.append(" WHERE id = :id");
        BeanPropertySqlParameterSource[] batchArgs = new BeanPropertySqlParameterSource[persons.size()];
        int i = 0;
        for (RegistryPerson identityObject : persons) {
            batchArgs[i] = new BeanPropertySqlParameterSource(identityObject);
            i++;
        }
        getNamedParameterJdbcTemplate().batchUpdate(sql.toString(), batchArgs);
    }

    @Override
    public List<RegistryPerson> findActualRefPersonsByDeclarationDataId(Long declarationDataId) {
        Date actualDate = new Date();
        String query = SelectPersonQueryGenerator.SELECT_FULL_PERSON + "\n" +
                "where\n" +
                "person.record_id in (select r.record_id from ref_book_person r\n" +
                "left join ndfl_person np on np.person_id = r.id\n" +
                "left join declaration_data dd on dd.id = np.declaration_data_id\n" +
                "where declaration_data_id = :declarationDataId)\n" +
                "and person.start_date <= :actualDate and (person.end_date >= :actualDate or person.end_date is null)";
        MapSqlParameterSource params = new MapSqlParameterSource("declarationDataId", declarationDataId);
        params.addValue("actualDate" , actualDate);
        return getNamedParameterJdbcTemplate().query(query, params, new RegistryPersonMapper());
    }
}
