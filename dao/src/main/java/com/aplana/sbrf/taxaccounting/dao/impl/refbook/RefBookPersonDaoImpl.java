package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.identification.NaturalPersonRefbookHandler;
import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.person.RefBookPersonMapper;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.person.SelectPersonOriginalDuplicatesQueryGenerator;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.person.SelectPersonQueryGenerator;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.mapper.RefBookValueMapper;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.dao.util.DBUtils;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.Permissive;
import com.aplana.sbrf.taxaccounting.model.filter.refbook.RefBookPersonFilter;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;


@Repository
public class RefBookPersonDaoImpl extends AbstractDao implements RefBookPersonDao {

    @Autowired
    RefBookDao refBookDao;
    @Autowired
    RefBookMapperFactory refBookMapperFactory;
    @Autowired
    private DBUtils dbUtils;


    private static final RowMapper<RegistryPerson> REGISTRY_CARD_PERSON_MAPPER = new RowMapper<RegistryPerson>() {
        @Override
        public RegistryPerson mapRow(ResultSet rs, int i) throws SQLException {
            RegistryPerson result = new RegistryPerson();
            result.setId(rs.getLong("id"));
            result.setRecordId(rs.getLong("record_id"));
            result.setOldId(rs.getLong("old_id"));
            result.setVersion(rs.getDate("version"));
            result.setRecordVersionTo(rs.getDate("record_version_to"));
            result.setLastName(rs.getString("last_name"));
            result.setFirstName(rs.getString("first_name"));
            result.setMiddleName(rs.getString("middle_name"));
            result.setBirthDate(rs.getDate("birth_date"));
            result.setVip(rs.getBoolean("vip"));
            Map<String, RefBookValue> source = new HashMap<>();
            source.put("SOURCE_ID", new RefBookValue(RefBookAttributeType.REFERENCE, rs.getLong("SOURCE_ID")));
            result.setSource(source);

            Map<String, RefBookValue> citizenship = new HashMap<>();
            citizenship.put("CITIZENSHIP", new RefBookValue(RefBookAttributeType.REFERENCE, rs.getLong("citizenship")));
            result.setCitizenship(Permissive.of(citizenship));

            Map<String, RefBookValue> reportDoc = new HashMap<>();
            reportDoc.put("REPORT_DOC", new RefBookValue(RefBookAttributeType.REFERENCE, rs.getLong("report_doc")));
            result.setReportDoc(Permissive.of(reportDoc));

            result.setInn(Permissive.of(rs.getString("inn")));
            result.setInnForeign(Permissive.of(rs.getString("inn_foreign")));
            result.setSnils(Permissive.of(rs.getString("snils")));

            Map<String, RefBookValue> taxPayerState = new HashMap<>();
            taxPayerState.put("TAXPAYER_STATE", new RefBookValue(RefBookAttributeType.REFERENCE, rs.getLong("TAXPAYER_STATE")));
            result.setTaxPayerState(Permissive.of(taxPayerState));

            Map<String, RefBookValue> address = new HashMap<>();
            address.put("ADDRESS", new RefBookValue(RefBookAttributeType.REFERENCE, rs.getLong("ADDRESS")));
            result.setAddress(Permissive.of(address));

            return result;
        }
    };

    private static final RowMapper<RegistryPerson> REGISTRY_CARD_PERSON_ORIGINAL_MAPPER = new RowMapper<RegistryPerson>() {
        @Override
        public RegistryPerson mapRow(ResultSet rs, int rowNum) throws SQLException {
            RegistryPerson result = new RegistryPerson();
            result.setId(rs.getLong("id"));
            result.setRecordId(rs.getLong("record_id"));
            result.setFirstName(rs.getString("first_name"));
            result.setLastName(rs.getString("last_name"));
            result.setMiddleName(rs.getString("middle_name"));
            result.setBirthDate(rs.getDate("BIRTH_DATE"));
            result.setOldId(SqlUtils.getLong(rs, "OLD_ID"));
            result.setVersion(rs.getDate("VERSION"));
            result.setState(rs.getInt("STATUS"));
            return result;
        }
    };

    private static final RowMapper<RegistryPerson> REGISTRY_CARD_PERSON_DUPLICATE_MAPPER = new RowMapper<RegistryPerson>() {
        @Override
        public RegistryPerson mapRow(ResultSet rs, int i) throws SQLException {
            RegistryPerson result = new RegistryPerson();
            result.setId(rs.getLong("id"));
            result.setRecordId(rs.getLong("record_id"));
            result.setOldId(rs.getLong("old_id"));
            result.setVersion(rs.getDate("version"));
            result.setState(rs.getInt("status"));
            result.setLastName(rs.getString("last_name"));
            result.setFirstName(rs.getString("first_name"));
            result.setMiddleName(rs.getString("middle_name"));
            result.setBirthDate(rs.getDate("birth_date"));
            result.setVip(rs.getBoolean("vip"));
            Map<String, RefBookValue> reportDoc = new HashMap<>();
            reportDoc.put("REPORT_DOC", new RefBookValue(RefBookAttributeType.REFERENCE, rs.getLong("report_doc")));
            result.setReportDoc(Permissive.of(reportDoc));
            result.setInn(Permissive.of(rs.getString("inn")));
            result.setSnils(Permissive.of(rs.getString("snils")));


            return result;
        }
    };

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
    public Map<Long, Map<Long, NaturalPerson>> findPersonForUpdateFromPrimaryRnuNdfl(Long declarationDataId, Long asnuId, NaturalPersonRefbookHandler naturalPersonHandler) {
        SimpleJdbcCall call = new SimpleJdbcCall(getJdbcTemplate()).withCatalogName("person_pkg").withFunctionName("GetPersonForUpd");
        call.declareParameters(new SqlOutParameter("ref_cursor", CURSOR, naturalPersonHandler), new SqlParameter("p_declaration", Types.NUMERIC));
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("p_declaration", declarationDataId);
        params.addValue("p_asnu", asnuId);
        call.execute(params);
        return naturalPersonHandler.getResult();
    }

    @Override
    public Map<Long, Map<Long, NaturalPerson>> findPersonForCheckFromPrimaryRnuNdfl(Long declarationDataId, Long asnuId, NaturalPersonRefbookHandler naturalPersonHandler) {
        SimpleJdbcCall call = new SimpleJdbcCall(getJdbcTemplate()).withCatalogName("person_pkg").withFunctionName("GetPersonForCheck");
        call.declareParameters(new SqlOutParameter("ref_cursor", CURSOR, naturalPersonHandler), new SqlParameter("p_declaration", Types.NUMERIC), new SqlParameter("p_asnu", Types.NUMERIC));
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("p_declaration", declarationDataId);
        params.addValue("p_asnu", asnuId);
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
                        "where np.declaration_data_id = ? and rbp.status = 0",
                new Object[]{declarationDataId},
                new int[]{Types.NUMERIC},
                Integer.class);
    }

    @Override
    public String getPersonDocNumber(long personId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("personId", personId);
        List<String> result = getNamedParameterJdbcTemplate().queryForList(
                "select doc_number from REF_BOOK_ID_DOC where person_id = :personId and inc_rep = 1", params, String.class);
        if (result.size() > 0) {
            return result.get(0);
        } else {
            return null;
        }
    }

    @Override
    public List<Integer> getPersonTbIds(long personId) {
        //language=SQL
        String query = "" +
                "select d.id \n" +
                "from department d, ref_book_person_tb p2tb \n" +
                "where p2tb.person_id = " + personId + " \n" +
                "    and p2tb.tb_department_id = d.id";

        return getJdbcTemplate().queryForList(query, Integer.class);
    }

    @Override
    public PagingResult<RefBookPerson> getPersons(PagingParams pagingParams, RefBookPersonFilter filter) {

        SelectPersonQueryGenerator selectPersonQueryGenerator = new SelectPersonQueryGenerator(filter, pagingParams);
        String query = selectPersonQueryGenerator.generatePagedAndFilteredQuery();
        List<RefBookPerson> persons = getJdbcTemplate().query(query, new RefBookPersonMapper());

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
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        try {
            return getNamedParameterJdbcTemplate().query("select * from ref_book_person where old_id = record_id and record_id = (select record_id from ref_book_person where id = :id) and status in (0,2) order by version desc", params, REGISTRY_CARD_PERSON_ORIGINAL_MAPPER);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<RegistryPerson> fetchDuplicates(Long id, PagingParams pagingParams) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        params.addValue("startIndex", pagingParams.getStartIndex());
        params.addValue("endIndex", pagingParams.getStartIndex() + pagingParams.getCount());
        try {
            return getNamedParameterJdbcTemplate().query("SELECT * FROM (SELECT r.*, row_number() over (order by id asc) as rn FROM (\n" +
                    "SELECT * from ref_book_person WHERE old_id <> record_id and record_id = (SELECT record_id from ref_book_person WHERE id = :id) and status in (0,2) order by version desc\n" +
                    ") r ) WHERE rn between :startIndex and :endIndex", params, REGISTRY_CARD_PERSON_DUPLICATE_MAPPER);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public RegistryPerson fetchPersonWithVersionInfo(Long id) {
        String sql = "SELECT r.*, " +
                (isSupportOver() ? "row_number() over (order by id asc)" : "ROWNUM") +
                " as rn FROM (" +
                "SELECT p.*, p.version as record_version_from, (SELECT min(version) - interval '1' day FROM REF_BOOK_PERSON WHERE status in (0,2) and record_id = p.record_id and version > p.version) as record_version_to " +
                "FROM (" +
                " SELECT frb.* FROM REF_BOOK_PERSON frb " +
                " WHERE id = :id " +
                " ) p " +
                ") r";
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        try {
            return getNamedParameterJdbcTemplate().queryForObject(sql, params, REGISTRY_CARD_PERSON_MAPPER);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void updateRegistryPerson(RegistryPerson person, String query) {
        MapSqlParameterSource personParams = new MapSqlParameterSource();
        personParams.addValue("id", person.getId())
                .addValue(RegistryPerson.UpdatableField.VERSION.getAlias(), person.getVersion())
                .addValue(RegistryPerson.UpdatableField.LAST_NAME.getAlias(), person.getLastName())
                .addValue(RegistryPerson.UpdatableField.FIRST_NAME.getAlias(), person.getFirstName())
                .addValue(RegistryPerson.UpdatableField.MIDDLE_NAME.getAlias(), person.getMiddleName())
                .addValue(RegistryPerson.UpdatableField.BIRTH_DATE.getAlias(), person.getBirthDate());
        if (person.getCitizenship().value() != null && person.getCitizenship().value().get("id") != null) {
            personParams.addValue(RegistryPerson.UpdatableField.CITIZENSHIP.getAlias(), person.getCitizenship().value().get("id").getNumberValue());
        } else {
            personParams.addValue(RegistryPerson.UpdatableField.CITIZENSHIP.getAlias(), null);
        }
        if (person.getReportDoc().value() != null && person.getReportDoc().value().get("id") != null) {
            personParams.addValue(RegistryPerson.UpdatableField.REPORT_DOC.getAlias(), person.getReportDoc().value().get("id").getNumberValue());
        } else {
            personParams.addValue(RegistryPerson.UpdatableField.REPORT_DOC.getAlias(), null);
        }
        if (person.getInn().value() != null) {
            personParams.addValue(RegistryPerson.UpdatableField.INN.getAlias(), person.getInn().value());
        }
        if (person.getInnForeign().value() != null) {
            personParams.addValue(RegistryPerson.UpdatableField.INN_FOREIGN.getAlias(), person.getInnForeign().value());
        }
        if (person.getSnils().value() != null) {
            personParams.addValue(RegistryPerson.UpdatableField.SNILS.getAlias(), person.getSnils().value());
        }
        if (person.getTaxPayerState().value() != null && person.getTaxPayerState().value().get("id") != null) {
            personParams.addValue(RegistryPerson.UpdatableField.TAX_PAYER_STATE.getAlias(), person.getTaxPayerState().value().get("id").getNumberValue());
        } else {
            personParams.addValue(RegistryPerson.UpdatableField.TAX_PAYER_STATE.getAlias(), null);
        }
        if (person.getSource() != null && person.getSource().get("id") != null) {
            personParams.addValue(RegistryPerson.UpdatableField.SOURCE.getAlias(), person.getSource().get("id").getNumberValue());
        } else {
            personParams.addValue(RegistryPerson.UpdatableField.SOURCE.getAlias(), null);
        }
        personParams.addValue(RegistryPerson.UpdatableField.VIP.getAlias(), person.getVip());
        getNamedParameterJdbcTemplate().update(query, personParams);
    }

    @Override
    public void updateRegistryPersonAddress(Map<String, RefBookValue> address, String query) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", address.get("id").getNumberValue());
        if (address.get(RegistryPerson.UpdatableField.REGION_CODE.name()) != null) {
            params.addValue(RegistryPerson.UpdatableField.REGION_CODE.getAlias(), address.get(RegistryPerson.UpdatableField.REGION_CODE.name()).getStringValue());
        }
        if (address.get(RegistryPerson.UpdatableField.POSTAL_CODE.name()) != null) {
            params.addValue(RegistryPerson.UpdatableField.POSTAL_CODE.getAlias(), address.get(RegistryPerson.UpdatableField.POSTAL_CODE.name()).getStringValue());
        }
        if (address.get(RegistryPerson.UpdatableField.DISTRICT.name()) != null) {
            params.addValue(RegistryPerson.UpdatableField.DISTRICT.getAlias(), address.get(RegistryPerson.UpdatableField.DISTRICT.name()).getStringValue());
        }
        if (address.get(RegistryPerson.UpdatableField.CITY.name()) != null) {
            params.addValue(RegistryPerson.UpdatableField.CITY.getAlias(), address.get(RegistryPerson.UpdatableField.CITY.name()).getStringValue());
        }
        if (address.get(RegistryPerson.UpdatableField.LOCALITY.name()) != null) {
            params.addValue(RegistryPerson.UpdatableField.LOCALITY.getAlias(), address.get(RegistryPerson.UpdatableField.LOCALITY.name()).getStringValue());
        }
        if (address.get(RegistryPerson.UpdatableField.STREET.name()) != null) {
            params.addValue(RegistryPerson.UpdatableField.STREET.getAlias(), address.get(RegistryPerson.UpdatableField.STREET.name()).getStringValue());
        }
        if (address.get(RegistryPerson.UpdatableField.HOUSE.name()) != null) {
            params.addValue(RegistryPerson.UpdatableField.HOUSE.getAlias(), address.get(RegistryPerson.UpdatableField.HOUSE.name()).getStringValue());
        }
        if (address.get(RegistryPerson.UpdatableField.BUILD.name()) != null) {
            params.addValue(RegistryPerson.UpdatableField.BUILD.getAlias(), address.get(RegistryPerson.UpdatableField.BUILD.name()).getStringValue());
        }
        if (address.get(RegistryPerson.UpdatableField.APPARTMENT.name()) != null) {
            params.addValue(RegistryPerson.UpdatableField.APPARTMENT.getAlias(), address.get(RegistryPerson.UpdatableField.APPARTMENT.name()).getStringValue());
        }
        if (address.get(RegistryPerson.UpdatableField.COUNTRY_ID.name()) != null) {
            params.addValue(RegistryPerson.UpdatableField.COUNTRY_ID.getAlias(), address.get(RegistryPerson.UpdatableField.COUNTRY_ID.name()).getReferenceValue());
        }
        if (address.get(RegistryPerson.UpdatableField.ADDRESS.name()) != null) {
            params.addValue(RegistryPerson.UpdatableField.ADDRESS.getAlias(), address.get(RegistryPerson.UpdatableField.ADDRESS.name()).getStringValue());
        }
        getNamedParameterJdbcTemplate().update(query, params);
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

    @Override
    public void deleteRegistryPersonFakeVersion(long recordId) {
        String query = "DELETE FROM ref_book_person WHERE RECORD_ID = :recordId AND RECORD_ID = OLD_ID AND STATUS = 2";
        MapSqlParameterSource params = new MapSqlParameterSource("recordId", recordId);
        getNamedParameterJdbcTemplate().update(query, params);
    }

    @Override
    public void saveRegistryPersonFakeVersion(RegistryPerson person) {
        String query = "INSERT INTO ref_book_person(id, version, status, record_id, old_id, vip) VALUES(:id, :version, :status, :recordId, :oldId, :vip)";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", dbUtils.getNextRefBookRecordIds(1).get(0))
                .addValue("version", DateUtils.addDays(person.getRecordVersionTo(), 1))
                .addValue("status", 2)
                .addValue("recordId", person.getRecordId())
                .addValue("oldId", person.getRecordId())
                .addValue("vip", person.getVip());
        getNamedParameterJdbcTemplate().update(query, params);
    }

    public List<RegistryPerson> fetchNonDuplicatesVersions(long recordId) {
        String query = "select person.*, " +
                "(select min(version) - interval '1' day \n" +
                "from ref_book_person p \n" +
                "where status in (0, 2) \n" +
                "and p.version > person.version \n" +
                "and p.record_id = person.record_id \n" +
                ") as record_version_to \n" +
                "from ref_book_person person \n" +
                "where person.status = 0\n" +
                "and person.record_id = :recordId\n" +
                "and old_id = record_id";
        MapSqlParameterSource params = new MapSqlParameterSource("recordId", recordId);
        return getNamedParameterJdbcTemplate().query(query, params, REGISTRY_CARD_PERSON_MAPPER);
    }

    @Override
    public PagingResult<RefBookPerson> fetchOriginalDuplicatesCandidates(PagingParams pagingParams, RefBookPersonFilter filter) {

        SelectPersonQueryGenerator selectPersonQueryGenerator = new SelectPersonOriginalDuplicatesQueryGenerator(filter, pagingParams);
        String query = selectPersonQueryGenerator.generatePagedAndFilteredQuery();
        List<RefBookPerson> persons = getJdbcTemplate().query(query, new RefBookPersonMapper());

        selectPersonQueryGenerator.setPagingParams(null);
        int count = selectCountOfQueryResults(selectPersonQueryGenerator.generatePagedAndFilteredQuery());

        return new PagingResult<>(persons, count);
    }
}
