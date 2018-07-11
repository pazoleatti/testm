package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.identification.NaturalPersonRefbookHandler;
import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import org.apache.commons.lang3.StringUtils;
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

/**
 * @author Andrey Drunk
 */
@Repository
public class RefBookPersonDaoImpl extends AbstractDao implements RefBookPersonDao {

    @Autowired
    RefBookDao refBookDao;
    @Autowired
    RefBookMapperFactory refBookMapperFactory;

    /**
     * Облегченный маппер только с основными полями
     */
    private static RowMapper<RefBookPerson> lightMapper = new RowMapper<RefBookPerson>() {
        @Override
        public RefBookPerson mapRow(ResultSet rs, int rowNum) throws SQLException {
            RefBookPerson result = new RefBookPerson();
            result.setId(rs.getLong("id"));
            result.setRecordId(rs.getLong("record_id"));
            result.setFirstName(rs.getString("first_name"));
            result.setLastName(rs.getString("last_name"));
            result.setMiddleName(rs.getString("middle_name"));
            result.setInn(rs.getString("INN"));
            result.setInnForeign(rs.getString("INN_FOREIGN"));
            result.setSnils(rs.getString("SNILS"));
            result.setBirthDate(rs.getDate("BIRTH_DATE"));
            result.setOldId(SqlUtils.getLong(rs, "OLD_ID"));
            result.setDocNumber(rs.getString("docNumber"));
            return result;
        }
    };

    /**
     * Маппер с полным набором полей
     */
    private static RowMapper<RefBookPerson> mapper = new RowMapper<RefBookPerson>() {
        @Override
        public RefBookPerson mapRow(ResultSet rs, int rowNum) throws SQLException {
            RefBookPerson result = new RefBookPerson();
            result.setId(rs.getLong("id"));
            result.setRecordId(rs.getLong("record_id"));
            result.setFirstName(rs.getString("first_name"));
            result.setLastName(rs.getString("last_name"));
            result.setMiddleName(rs.getString("middle_name"));
            result.setVersion(rs.getDate("version"));
            result.setVersionEnd(rs.getDate("versionEnd"));
            result.setInn(rs.getString("INN"));
            result.setInnForeign(rs.getString("INN_FOREIGN"));
            result.setSnils(rs.getString("SNILS"));
            result.setBirthDate(rs.getDate("BIRTH_DATE"));
            result.setInn(rs.getString("INN"));
            result.setBirthPlace(rs.getString("BIRTH_PLACE"));
            result.setEmployee(SqlUtils.getInteger(rs, "EMPLOYEE"));
            result.setOldId(SqlUtils.getLong(rs, "OLD_ID"));

            RefBookTaxpayerState taxpayerState = new RefBookTaxpayerState();
            taxpayerState.setId(SqlUtils.getLong(rs, "TAXPAYER_STATE_ID"));
            taxpayerState.setCode(rs.getString("TAXPAYER_STATE_CODE"));
            taxpayerState.setName(rs.getString("TAXPAYER_STATE_NAME"));
            result.setTaxpayerState(taxpayerState);

            RefBookCountry citizenship = new RefBookCountry();
            citizenship.setId(SqlUtils.getLong(rs, "CITIZENSHIP_ID"));
            citizenship.setCode(rs.getString("CITIZENSHIP_CODE"));
            citizenship.setName(rs.getString("CITIZENSHIP_NAME"));
            result.setCitizenship(citizenship);

            RefBookAddress address = new RefBookAddress();
            // Заполняем только id, т.к все остальное получим как конкатенацию строк прямо из БД
            address.setId(SqlUtils.getLong(rs, "A_ID"));
            result.setAddress(address);
            result.setAddressAsText(rs.getString("ADDRESS_ADDRESS_FULL"));

            RefBookAsnu asnu = new RefBookAsnu();
            asnu.setId(SqlUtils.getLong(rs, "SOURCE_ID_ID"));
            asnu.setCode(rs.getString("SOURCE_ID_CODE"));
            asnu.setName(rs.getString("SOURCE_ID_NAME"));
            asnu.setType(rs.getString("SOURCE_ID_TYPE"));
            asnu.setPriority(rs.getInt("SOURCE_ID_PRIORITY"));
            result.setSource(asnu);
            return result;
        }
    };

    @Override
    public void clearRnuNdflPerson(Long declarationDataId) {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("declarationDataId", declarationDataId);
        getNamedParameterJdbcTemplate().update("update NDFL_PERSON set PERSON_ID = null where DECLARATION_DATA_ID = :declarationDataId", values);
    }

    @Override
    public void fillRecordVersions(Date version) {
        //long time = System.currentTimeMillis();
        getJdbcTemplate().update("call person_pkg.FillRecordVersions(?)", version);
        //System.out.println("fillRecordVersions (" + (System.currentTimeMillis() - time) + " ms)");
    }

    @Override
    public Map<Long, Map<Long, NaturalPerson>> findPersonForUpdateFromPrimaryRnuNdfl(Long declarationDataId, Long asnuId, Date version, NaturalPersonRefbookHandler naturalPersonHandler) {
        SimpleJdbcCall call = new SimpleJdbcCall(getJdbcTemplate()).withCatalogName("person_pkg").withFunctionName("GetPersonForUpd");
        call.declareParameters(new SqlOutParameter("ref_cursor", CURSOR, naturalPersonHandler), new SqlParameter("p_declaration", Types.NUMERIC));
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("p_declaration", declarationDataId);
        params.addValue("p_asnu", asnuId);
        call.execute(params);
        return naturalPersonHandler.getResult();
    }

    @Override
    public Map<Long, Map<Long, NaturalPerson>> findPersonForCheckFromPrimaryRnuNdfl(Long declarationDataId, Long asnuId, Date version, NaturalPersonRefbookHandler naturalPersonHandler) {
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
     *
     * @param declarationDataId
     * @return
     */
    @Override
    public List<NaturalPerson> findNaturalPersonPrimaryDataFromNdfl(long declarationDataId, RowMapper<NaturalPerson> naturalPersonRowMapper) {

        StringBuilder SQL = new StringBuilder();
        SQL.append("SELECT id, declaration_data_id, person_id, row_num, inp, snils, last_name, first_name, middle_name, birth_day, citizenship, inn_np, inn_foreign, id_doc_type, id_doc_number, status, post_index, region_code, area, city, locality, street, house, building, flat, country_code, address, additional_data, NULL correct_num, NULL period, NULL rep_period, NULL num, NULL sv_date  \n");
        SQL.append("FROM ndfl_person \n");
        SQL.append("WHERE declaration_data_id = :declarationDataId");

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("declarationDataId", declarationDataId);

        return getNamedParameterJdbcTemplate().query(SQL.toString(), params, naturalPersonRowMapper);

    }


    @Override
    public void setDuplicate(List<Long> recordIds, Long originalId) {
        Map<String, Object> valueMap = new HashMap<String, Object>();
        valueMap.put("originalId", originalId);
        getNamedParameterJdbcTemplate().update(String.format("update ref_book_person set record_id = :originalId, old_id = record_id, old_status = status, status = -1 " +
                "where old_id is null and %s", SqlUtils.transformToSqlInStatement("record_id", recordIds)), valueMap);
        getNamedParameterJdbcTemplate().update(String.format("update ref_book_person set record_id = :originalId " +
                "where old_id is not null and %s", SqlUtils.transformToSqlInStatement("record_id", recordIds)), valueMap);
    }

    @Override
    public void changeRecordId(List<Long> recordIds, Long originalId) {
        Map<String, Object> valueMap = new HashMap<String, Object>();
        valueMap.put("originalId", originalId);
        getNamedParameterJdbcTemplate().update(String.format("update ref_book_person set record_id = :originalId " +
                "where old_id is not null and %s", SqlUtils.transformToSqlInStatement("old_id", recordIds)), valueMap);
    }

    @Override
    public void setOriginal(List<Long> recordIds) {
        getJdbcTemplate().update(String.format("update ref_book_person set record_id = old_id, old_id = null, status = old_status, old_status = null " +
                "where %s", SqlUtils.transformToSqlInStatement("old_id", recordIds)));
        getJdbcTemplate().update(String.format("delete from ref_book_id_doc " +
                "where %s", SqlUtils.transformToSqlInStatement("duplicate_record_id", recordIds)));
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
    @SuppressWarnings("unchecked")
    public PagingResult<RefBookPerson> getDuplicates(Long personId, PagingParams pagingParams) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("personId", personId);
        params.addValue("start", pagingParams.getStartIndex() + 1);
        params.addValue("end", pagingParams.getStartIndex() + pagingParams.getCount());

        String baseSql = "select rbp.*, (select doc_number from REF_BOOK_ID_DOC where person_id = rbp.id and inc_rep = 1 and rownum = 1) as docNumber \n" +
                "from ref_book_person rbp \n" +
                "join (\n" +
                "  select old_id, max(version) version from ref_book_person \n" +
                "  where record_id = :personId and old_id is not null and old_status = 0 \n" +
                "  group by old_id\n" +
                ") v on v.version = rbp.version and v.old_id = rbp.old_id and old_status = 0";
        List<RefBookPerson> list = getNamedParameterJdbcTemplate().query(
                "select * from (select r.*, row_number() over (order by id) as rn from (\n" + baseSql + ") r)\n where rn between :start and :end",
                params, lightMapper);

        int totalCount = getNamedParameterJdbcTemplate().queryForObject("select count(*) from (" + baseSql + ")", params, Integer.class);
        return new PagingResult<>(list, totalCount);
    }

    private final String PERSON_SQL = "select p.*, (select min(version) - interval '1' day from ref_book_person where status in (0,2) and record_id = p.record_id and version > p.version) as versionEnd from (\n" +
            "  select frb.*, s.id as TAXPAYER_STATE_ID, s.code as TAXPAYER_STATE_CODE, s.name as TAXPAYER_STATE_NAME, \n" +
            "  c.id as CITIZENSHIP_ID, c.code as CITIZENSHIP_CODE, c.name as CITIZENSHIP_NAME, \n" +
            "  asnu.id as SOURCE_ID_ID, asnu.name as SOURCE_ID_NAME, asnu.code as SOURCE_ID_CODE, asnu.priority as SOURCE_ID_PRIORITY, asnu.type as SOURCE_ID_TYPE,\n" +
            "  a.id as A_ID, a.REGION_CODE || ',' || a.POSTAL_CODE || ',' || a.DISTRICT || ',' || a.CITY || ',' || a.LOCALITY || ',' || a.STREET || ',' || a.HOUSE || ',' || a.BUILD || ',' || a.APPARTMENT as ADDRESS_ADDRESS_FULL\n" +
            "  from REF_BOOK_PERSON frb \n" +
            "  left join REF_BOOK_TAXPAYER_STATE s on s.id = frb.TAXPAYER_STATE\n" +
            "  left join REF_BOOK_COUNTRY c on c.id = frb.CITIZENSHIP\n" +
            "  left join REF_BOOK_ADDRESS a on a.id = frb.ADDRESS \n" +
            "  left join REF_BOOK_ASNU asnu on asnu.id = frb.SOURCE_ID \n" +
            "  where frb.status = 0%s) p ";

    @Override
    public PagingResult<RefBookPerson> getPersons(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (version != null) {
            params.addValue("version", version);
        }
        if (pagingParams != null) {
            params.addValue("start", pagingParams.getStartIndex() + 1);
            params.addValue("end", pagingParams.getStartIndex() + pagingParams.getCount());
        }

        String baseSql = String.format(PERSON_SQL, version != null ? " and frb.version = (select max(version) from REF_BOOK_PERSON where version <= :version and record_id = frb.record_id and status = 0)" : "") +
                (StringUtils.isNotEmpty(filter) ? "where " + filter : "");

        String sortColumnName = "id";
        if (sortAttribute != null) {
            if (sortAttribute.getAttributeType() == RefBookAttributeType.REFERENCE) {
                sortColumnName = sortAttribute.getAlias() + "_" + sortAttribute.getRefBookAttribute().getAlias();
            } else {
                sortColumnName = sortAttribute.getAlias();
            }

        }
        String direction = "asc";
        if (pagingParams != null && StringUtils.isNotEmpty(pagingParams.getDirection())) {
            direction = pagingParams.getDirection();
        }
        String finalQuery = pagingParams != null ?
                "select /*+ FIRST_ROWS */* from (select r.*, row_number() over (order by " + sortColumnName + " " + direction + ") as rn from (\n" + baseSql + ") r)\n where rn between :start and :end"
                : baseSql;
        List<RefBookPerson> list = getNamedParameterJdbcTemplate().query(finalQuery, params, mapper);

        int totalCount = getNamedParameterJdbcTemplate().queryForObject("select count(*) from (" + baseSql + ")", params, Integer.class);
        return new PagingResult<>(list, totalCount);
    }

    @Override
    public PagingResult<RefBookPerson> getPersonVersions(long recordId, PagingParams pagingParams) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("recordId", recordId);
        params.addValue("start", pagingParams.getStartIndex() + 1);
        params.addValue("end", pagingParams.getStartIndex() + pagingParams.getCount());

        String baseSql = String.format(PERSON_SQL, " and frb.record_id = :recordId");
        List<RefBookPerson> list = getNamedParameterJdbcTemplate().query(
                "select * from (select r.*, row_number() over (order by version) as rn from (\n" + baseSql + ") r)\n where rn between :start and :end",
                params, mapper);

        int totalCount = getNamedParameterJdbcTemplate().queryForObject("select count(*) from (" + baseSql + ")", params, Integer.class);
        return new PagingResult<>(list, totalCount);
    }

    @Override
    public RefBookPerson getOriginal(Long recordId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("recordId", recordId);
        try {
            return getNamedParameterJdbcTemplate().queryForObject("select p.*, (select doc_number from REF_BOOK_ID_DOC where person_id = p.id and inc_rep = 1 and rownum = 1) as docNumber \n" +
                    "from ref_book_person p where p.record_id = :recordId and p.old_id is null and p.status = 0", params, lightMapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
