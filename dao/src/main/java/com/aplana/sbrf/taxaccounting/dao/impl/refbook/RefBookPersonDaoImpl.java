package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.identification.NaturalPersonRefbookHandler;
import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.person.SelectPersonQueryGenerator;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.mapper.RefBookValueMapper;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.Permissive;
import com.aplana.sbrf.taxaccounting.model.filter.refbook.RefBookPersonFilter;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
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


    private static final RowMapper<RefBookPerson> PERSON_MAPPER = new RowMapper<RefBookPerson>() {
        @Override
        public RefBookPerson mapRow(ResultSet rs, int rowNum) throws SQLException {
            RefBookPerson result = new RefBookPerson();
            result.setId(rs.getLong("id"));
            result.setRecordId(rs.getLong("record_id"));
            result.setOldId(rs.getLong("old_id"));
            result.setVip(rs.getBoolean("vip"));
            result.setFirstName(rs.getString("first_name"));
            result.setLastName(rs.getString("last_name"));
            result.setMiddleName(rs.getString("middle_name"));
            result.setBirthDate(rs.getDate("birth_date"));
            result.setBirthPlace(rs.getString("birth_place"));
            result.setVersion(rs.getDate("version"));
            result.setVersionEnd(rs.getDate("version_to"));

            result.setDocName(Permissive.of(rs.getString("doc_name")));
            result.setDocNumber(Permissive.of(rs.getString("doc_number")));
            result.setInn(Permissive.of(rs.getString("inn")));
            result.setInnForeign(Permissive.of(rs.getString("inn_foreign")));
            result.setSnils(Permissive.of(rs.getString("snils")));

            Long addressId = SqlUtils.getLong(rs, "address_id");
            if (addressId == null) {
                result.setAddress(Permissive.<RefBookAddress>of(null));
                result.setForeignAddress(Permissive.<RefBookAddress>of(null));
            } else {
                RefBookAddress address = new RefBookAddress();
                RefBookAddress foreignAddress = new RefBookAddress();

                address.setId(rs.getLong("address_id"));
                foreignAddress.setId(rs.getLong("address_id"));

                address.setAddressType(rs.getInt("address_type"));
                foreignAddress.setAddressType(rs.getInt("address_type"));

                address.setPostalCode(rs.getString("postal_code"));
                address.setRegionCode(rs.getString("region_code"));
                address.setDistrict(rs.getString("district"));
                address.setCity(rs.getString("city"));
                address.setLocality(rs.getString("locality"));
                address.setStreet(rs.getString("street"));
                address.setBuild(rs.getString("building"));
                address.setHouse(rs.getString("house"));
                address.setApartment(rs.getString("apartment"));

                address.setAddress(rs.getString("address"));
                foreignAddress.setAddress(rs.getString("address"));

                Long countryId = SqlUtils.getLong(rs, "address_country_id");
                if (countryId == null) {
                    address.setCountry(null);
                    foreignAddress.setCountry(null);
                } else {
                    RefBookCountry country = new RefBookCountry();
                    country.setId(countryId);
                    country.setCode(rs.getString("address_country_code"));
                    country.setName(rs.getString("address_country_name"));
                    address.setCountry(country);
                    foreignAddress.setCountry(country);
                }

                result.setAddress(Permissive.of(address));
                result.setForeignAddress(Permissive.of(foreignAddress));
            }

            Long countryId = SqlUtils.getLong(rs, "citizenship_country_id");
            if (countryId == null) {
                result.setCitizenship(null);
            } else {
                RefBookCountry citizenship = new RefBookCountry();
                citizenship.setId(countryId);
                citizenship.setCode(rs.getString("citizenship_country_code"));
                citizenship.setName(rs.getString("citizenship_country_name"));
                result.setCitizenship(citizenship);
            }

            Long stateId = SqlUtils.getLong(rs, "state_id");
            if (stateId == null) {
                result.setTaxpayerState(null);
            } else {
                RefBookTaxpayerState taxpayerState = new RefBookTaxpayerState();
                taxpayerState.setId(stateId);
                taxpayerState.setCode(rs.getString("state_code"));
                taxpayerState.setName(rs.getString("state_name"));
                result.setTaxpayerState(taxpayerState);
            }

            Long asnuId = SqlUtils.getLong(rs, "asnu_id");
            if (asnuId == null) {
                result.setSource(null);
            } else {
                RefBookAsnu asnu = new RefBookAsnu();
                asnu.setId(asnuId);
                asnu.setCode(rs.getString("asnu_code"));
                asnu.setName(rs.getString("asnu_name"));
                asnu.setType(rs.getString("asnu_type"));
                asnu.setPriority(rs.getInt("asnu_priority"));
                result.setSource(asnu);
            }

            return result;
        }
    };

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
    public void setDuplicate(List<Long> recordIds, Long originalId) {
        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("originalId", originalId);
        getNamedParameterJdbcTemplate().update(String.format("update ref_book_person set record_id = :originalId, old_id = record_id, old_status = status, status = -1 " +
                "where old_id is null and %s", SqlUtils.transformToSqlInStatement("record_id", recordIds)), valueMap);
        getNamedParameterJdbcTemplate().update(String.format("update ref_book_person set record_id = :originalId " +
                "where old_id is not null and %s", SqlUtils.transformToSqlInStatement("record_id", recordIds)), valueMap);
    }

    @Override
    public void changeRecordId(List<Long> recordIds, Long originalId) {
        Map<String, Object> valueMap = new HashMap<>();
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
        List<RefBookPerson> persons = getJdbcTemplate().query(query, PERSON_MAPPER);

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
        String sql = "SELECT r.*, row_number() over (order by id asc) as rn FROM (" +
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
}
