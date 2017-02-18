package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.ndfl.NdflPersonDao;
import com.aplana.sbrf.taxaccounting.model.IdentityObject;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.ndfl.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Andrey Drunk
 */
@Repository
@Transactional
public class NdflPersonDaoImpl extends AbstractDao implements NdflPersonDao {

    private static final Log LOG = LogFactory.getLog(NdflPersonDaoImpl.class);

    private static final String DUPLICATE_ERORR_MSG = "Попытка перезаписать уже сохранённые данные!";

    @Override
    public NdflPerson get(long ndflPersonId) {
        try {
            NdflPerson ndflPerson = getJdbcTemplate().queryForObject("select " + createColumns(NdflPerson.COLUMNS, "np") + " from ndfl_person np where np.id = ?", new Object[]{ndflPersonId}, new NdflPersonDaoImpl.NdflPersonRowMapper());

            List<NdflPersonIncome> ndflPersonIncomes = findIncomes(ndflPersonId);
            List<NdflPersonDeduction> ndflPersonDeductions = findDeductions(ndflPersonId);
            List<NdflPersonPrepayment> ndflPersonPrepayments = findPrepayments(ndflPersonId);

            ndflPerson.setIncomes(ndflPersonIncomes);
            ndflPerson.setDeductions(ndflPersonDeductions);
            ndflPerson.setPrepayments(ndflPersonPrepayments);

            return ndflPerson;
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("Сущность класса NdflPerson с id = %d не найдена в БД", ndflPersonId);
        }
    }

    @Override
    public List<NdflPerson> findPerson(long declarationDataId) {
        try {
            return getJdbcTemplate().query("select " + createColumns(NdflPerson.COLUMNS, "np") + " from ndfl_person np where np.declaration_data_id = ?", new Object[]{declarationDataId}, new NdflPersonDaoImpl.NdflPersonRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPerson>();
        }
    }

    @Override
    public List<NdflPersonIncome> findPersonIncome(long declarationDataId) {
        try {
            return getJdbcTemplate().query("select " + createColumns(NdflPersonIncome.COLUMNS, "npi") + " from ndfl_person_income npi "
                    + " inner join ndfl_person np on npi.ndfl_person_id = np.id"
                    + " where np.declaration_data_id = ?", new Object[]{declarationDataId}, new NdflPersonDaoImpl.NdflPersonIncomeRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonIncome>();
        }
    }

    @Override
    public List<NdflPersonDeduction> findNdflPersonDeduction(long declarationDataId) {
        try {
            return getJdbcTemplate().query("select " + createColumns(NdflPersonDeduction.COLUMNS, "npd") + " from ndfl_person_deduction npd "
                    + " inner join ndfl_person np on npd.ndfl_person_id = np.id"
                    + " where np.declaration_data_id = ?", new Object[]{declarationDataId}, new NdflPersonDaoImpl.NdflPersonDeductionRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonDeduction>();
        }
    }

    @Override
    public List<NdflPersonPrepayment> findNdflPersonPrepayment(long declarationDataId) {
        try {
            return getJdbcTemplate().query("select " + createColumns(NdflPersonPrepayment.COLUMNS, "npp") + " from ndfl_person_prepayment npp "
                    + " inner join ndfl_person np on npp.ndfl_person_id = np.id"
                    + " where np.declaration_data_id = ?", new Object[]{declarationDataId}, new NdflPersonDaoImpl.NdflPersonPrepaymentRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonPrepayment>();
        }
    }

    @Override
    public List<NdflPersonIncome> findIncomes(long ndflPersonId) {
        try {
            return getJdbcTemplate().query("select " + createColumns(NdflPersonIncome.COLUMNS, "npi") + " from ndfl_person_income npi where npi.ndfl_person_id = ?", new Object[]{ndflPersonId}, new NdflPersonDaoImpl.NdflPersonIncomeRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonIncome>();
        }
    }

    @Override
    public List<NdflPersonIncome> findIncomesForPersonByKppOktmo(long ndflPersonId, String kpp, String oktmo) {
        String sql = "select " + createColumns(NdflPersonIncome.COLUMNS, "npi") +
                " from NDFL_PERSON_INCOME npi where " +
                "npi.NDFL_PERSON_ID = :ndflPersonId and (npi.OKTMO is null or npi.OKTMO = :oktmo) and npi.KPP = :kpp";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("ndflPersonId", ndflPersonId)
                .addValue("oktmo", oktmo)
                .addValue("kpp", kpp);
        try {
            return getNamedParameterJdbcTemplate().query(sql, params, new NdflPersonDaoImpl.NdflPersonIncomeRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonIncome>();
        }
    }

    @Override
    public List<NdflPersonIncome> findIncomesByPeriodAndDeclarationDataId(long declarationDataId, Date startDate, Date endDate, String kpp, String oktmo) {
        String sql = "SELECT " + createColumns(NdflPersonIncome.COLUMNS, "npi") + " FROM ndfl_person_income npi " +
                " INNER JOIN ndfl_person np ON npi.ndfl_person_id = np.id " +
                " WHERE np.declaration_data_id = :declaration_data_id" +
                " AND ((npi.tax_date >= :startDate AND npi.tax_date <= :endDate) OR npi.tax_date IS NULL)" +
                " AND ((npi.payment_date >= :startDate AND npi.payment_date <= :endDate) OR npi.payment_date IS NULL)" +
                " AND (npi.tax_date IS NOT NULL OR npi.payment_date IS NOT NULL) " +
                " AND (npi.kpp = :kpp or npi.kpp is null) AND (npi.oktmo = :oktmo or npi.oktmo is null)";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("declaration_data_id", declarationDataId)
                .addValue("startDate", startDate)
                .addValue("endDate", endDate)
                .addValue("kpp", kpp)
                .addValue("oktmo", oktmo);
        try {
            return getNamedParameterJdbcTemplate().query(sql, params, new NdflPersonDaoImpl.NdflPersonIncomeRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonIncome>();
        }
    }

    @Override
    public List<NdflPersonIncome> findIncomesByPeriodAndNdflPersonId(long ndflPersonId, Date startDate, Date endDate) {
        String sql = "SELECT " + createColumns(NdflPersonIncome.COLUMNS, "npi") + " FROM ndfl_person_income npi " +
                " WHERE npi.ndfl_person_id = :ndflPersonId" +
                " AND (((npi.tax_date between :startDate AND :endDate) OR npi.tax_date IS NULL)" +
                " OR (npi.payment_date between :startDate AND :endDate) OR npi.payment_date IS NULL)" +
                " AND (npi.tax_date IS NOT NULL OR npi.payment_date IS NOT NULL)";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("ndflPersonId", ndflPersonId)
                .addValue("startDate", startDate)
                .addValue("endDate", endDate);
        try {
            return getNamedParameterJdbcTemplate().query(sql, params, new NdflPersonDaoImpl.NdflPersonIncomeRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonIncome>();
        }
    }

    @Override
    public List<NdflPersonDeduction> findDeductionsByPeriodAndNdflPersonId(long ndflPersonId, Date startDate, Date endDate) {
        String sql = "SELECT " + createColumns(NdflPersonDeduction.COLUMNS, "npd") + " FROM ndfl_person_deduction npd " +
                " WHERE npd.ndfl_person_id = :ndflPersonId" +
                " AND npd.PERIOD_CURR_DATE between :startDate AND :endDate";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("ndflPersonId", ndflPersonId)
                .addValue("startDate", startDate)
                .addValue("endDate", endDate);
        try {
            return getNamedParameterJdbcTemplate().query(sql, params, new NdflPersonDaoImpl.NdflPersonDeductionRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonDeduction>();
        }
    }

    @Override
    public List<NdflPersonPrepayment> findPrepaymentsByPeriodAndNdflPersonId(long ndflPersonId, Date startDate, Date endDate) {
        String sql = "SELECT " + createColumns(NdflPersonPrepayment.COLUMNS, "npp") + " FROM ndfl_person_prepayment npp " +
                " WHERE npp.ndfl_person_id = :ndflPersonId" +
                " AND npp.NOTIF_DATE between :startDate AND :endDate";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("ndflPersonId", ndflPersonId)
                .addValue("startDate", startDate)
                .addValue("endDate", endDate);
        try {
            return getNamedParameterJdbcTemplate().query(sql, params, new NdflPersonDaoImpl.NdflPersonPrepaymentRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonPrepayment>();
        }
    }


    @Override
    public PagingResult<NdflPerson> findNdflPersonByParameters(long declarationDataId, Map<String, Object> parameters, PagingParams pagingParams) {
        parameters.put("declarationDataId", declarationDataId);
        String query = buildQuery(parameters);
        String totalQuery = query;
        if (pagingParams != null) {
            long lastindex = pagingParams.getStartIndex() + pagingParams.getCount();
            totalQuery = "select * from \n (" +
                    "select rating.*, rownum rnum from \n (" +
                    query + ") rating where rownum < " +
                    lastindex +
                    ") where rnum >= " +
                    pagingParams.getStartIndex();
        }
        List<NdflPerson> result = getNamedParameterJdbcTemplate().query(totalQuery, parameters, new NdflPersonDaoImpl.NdflPersonRowMapper());
        return new PagingResult<NdflPerson>(result, getCount(totalQuery, parameters));
    }

    public static String buildQuery(Map<String, Object> parameters) {

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT " + createColumns(NdflPerson.COLUMNS, "np") + " \n");
        sb.append("FROM ndfl_person np \n");
        sb.append("WHERE np.declaration_data_id = :declarationDataId \n");

        if (parameters != null && !parameters.isEmpty()) {

            if (contains(parameters, "lastName")) {
                sb.append("AND lower(np.last_name) = lower(:lastName) \n");
            }

            if (contains(parameters, "firstName")) {
                sb.append("AND lower(np.first_name) = lower(:firstName) \n");
            }

            if (contains(parameters, "middleName")) {
                sb.append("AND (np.middle_name is null OR lower(np.middle_name) = lower(:middleName)) \n");
            }

            if (contains(parameters, "snils")) {
                sb.append("AND np.snils = :snils \n");
            }

            if (contains(parameters, "inn")) {
                sb.append("AND np.inn_np = :inn \n");
            }

            if (contains(parameters, "inp")) {
                sb.append("AND np.inp = :inp \n");
            }

            if (contains(parameters, "fromBirthDay")) {
                sb.append("AND (np.birth_day is null OR np.birth_day >= :fromBirthDay) \n");
            }

            if (contains(parameters, "toBirthDay")) {
                sb.append("AND (np.birth_day is null OR np.birth_day <= :toBirthDay) \n");
            }

            if (contains(parameters, "idDocNumber")) {
                sb.append("AND (np.id_doc_number is null OR np.id_doc_number = :idDocNumber) \n");
            }
        }
        sb.append("ORDER BY row_num \n");
        return sb.toString();

    }

    private static boolean contains(Map<String, Object> param, String key) {
        return param.containsKey(key) && param.get(key) != null;
    }


    /**
     * Метод вернет количество строк в запросе
     *
     * @param sqlQuery   запрос
     * @param parameters параметры сапроса
     * @return количество строк
     */
    @Override
    public int getCount(String sqlQuery, Map<String, Object> parameters) {
        StringBuilder sb = new StringBuilder();
        sb.append("select count(*) from (");
        sb.append(sqlQuery);
        sb.append(")");
        return getNamedParameterJdbcTemplate().queryForObject(sb.toString(), parameters, Integer.class);
    }

    @Override
    public List<NdflPersonDeduction> findDeductions(long ndflPersonId) {
        try {
            return getJdbcTemplate().query("select " + createColumns(NdflPersonDeduction.COLUMNS, "npi") + " from ndfl_person_deduction npi where npi.ndfl_person_id = ?", new Object[]{ndflPersonId}, new NdflPersonDaoImpl.NdflPersonDeductionRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonDeduction>();
        }
    }

    @Override
    public List<NdflPersonPrepayment> findPrepayments(long ndflPersonId) {
        try {
            return getJdbcTemplate().query("select " + createColumns(NdflPersonPrepayment.COLUMNS, "npi") + " from ndfl_person_prepayment npi where npi.ndfl_person_id = ?", new Object[]{ndflPersonId}, new NdflPersonDaoImpl.NdflPersonPrepaymentRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonPrepayment>();
        }
    }

    @Override
    public List<NdflPerson> findNdflPersonByPairKppOktmo(long declarationDataId, String kpp, String oktmo) {
        String sql = "SELECT DISTINCT " + createColumns(NdflPerson.COLUMNS, "np") + " FROM ndfl_person np " +
                "INNER JOIN ndfl_person_income npi " +
                "ON np.id = npi.ndfl_person_id " +
                "WHERE (npi.kpp = :kpp or npi.kpp is null) " +
                "AND (npi.oktmo = :oktmo or npi.oktmo is null) " +
                "AND np.DECLARATION_DATA_ID in (select id from DECLARATION_DATA where id = :declarationDataId)";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("declarationDataId", declarationDataId)
                .addValue("kpp", kpp)
                .addValue("oktmo", oktmo);
        try {
            return getNamedParameterJdbcTemplate().query(sql, params, new NdflPersonDaoImpl.NdflPersonRowMapper());
        } catch (EmptyResultDataAccessException ex) {
            return new ArrayList<NdflPerson>();
        }
    }



    @Override
    public List<NdflPersonPrepayment> findPrepaymentsByDeclarationDataId(long declarationDataId, String kpp, String oktmo) {
        String sql = "SELECT " + createColumns(NdflPersonPrepayment.COLUMNS, "npi") + " FROM ndfl_person_prepayment npi " +
                " INNER JOIN ndfl_person np ON npi.ndfl_person_id = np.id " +
                " INNER JOIN ndfl_person_income npi ON np.id = npi.ndfl_person_id " +
                " WHERE np.declaration_data_id = :declarationDataId " +
                " AND (npi.kpp = :kpp or npi.kpp is null) AND (npi.oktmo = :oktmo or npi.oktmo is null)";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("declarationDataId", declarationDataId)
                .addValue("kpp", kpp)
                .addValue("oktmo", oktmo);
        try {
            return getNamedParameterJdbcTemplate().query(sql, params, new NdflPersonDaoImpl.NdflPersonPrepaymentRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<NdflPersonPrepayment>();
        }
    }


    @Override
    public int[] updatePersonRefBookReferences(List<NdflPerson> ndflPersonList) {
        String updateSql = "UPDATE ndfl_person SET person_id = ? WHERE id = ?";
        try {
            int[] result = getJdbcTemplate().batchUpdate(updateSql, new UpdateNdflPersonBatch(ndflPersonList));
            return result;
        } catch (DataAccessException e) {
            throw new DaoException("Ошибка при обновлении идентификаторов физлиц", e);
        }
    }

    class UpdateNdflPersonBatch implements BatchPreparedStatementSetter {

        final List<NdflPerson> ndflPersonList;

        public UpdateNdflPersonBatch(List<NdflPerson> ndflPersonList) {
            this.ndflPersonList = ndflPersonList;
        }

        @Override
        public void setValues(PreparedStatement ps, int i) throws SQLException {
            NdflPerson ndflPerson = ndflPersonList.get(i);
            ps.setLong(1, ndflPerson.getPersonId());
            ps.setLong(2, ndflPerson.getId());
        }

        @Override
        public int getBatchSize() {
            return ndflPersonList.size();
        }

    }

    @Override
    public Long save(final NdflPerson ndflPerson) {

        saveNewObject(ndflPerson, NdflPerson.TABLE_NAME, NdflPerson.SEQ, NdflPerson.COLUMNS, NdflPerson.FIELDS);

        List<NdflPersonIncome> ndflPersonIncomes = ndflPerson.getIncomes();
        if (ndflPersonIncomes == null || ndflPersonIncomes.isEmpty()) {
            throw new DaoException("Пропущены обязательные данные о доходах!");
        }
        saveDetails(ndflPerson, ndflPersonIncomes);

        List<NdflPersonDeduction> ndflPersonDeductions = ndflPerson.getDeductions();
        saveDetails(ndflPerson, ndflPersonDeductions);

        List<NdflPersonPrepayment> ndflPersonPrepayments = ndflPerson.getPrepayments();
        saveDetails(ndflPerson, ndflPersonPrepayments);

        return ndflPerson.getId();
    }

    private void saveDetails(NdflPerson ndflPerson, List<? extends NdflPersonOperation> details) {
        for (NdflPersonOperation detail : details) {
            detail.setNdflPersonId(ndflPerson.getId());
            saveNewObject(detail, detail.getTableName(), detail.getSeq(), detail.getColumns(), detail.getFields());
        }
    }

    /**
     * Метод сохраняет новый объект в БД и возвращает этот же объект с присвоенным id
     *
     * @param identityObject объект обладающий суррогатным ключом
     * @param table          наименование таблицы используемой для хранения данных объекта
     * @param seq            наименование последовательностт используемой для генерации ключей
     * @param columns        массив содержащий наименование столбцов таблицы для вставки в insert
     * @param fields         массив содержащий наименования параметров соответствующих столбцам
     * @param <E>            тип объекта
     */
    private <E extends IdentityObject> void saveNewObject(E identityObject, String table, String seq, String[] columns, String[] fields) {

        if (identityObject.getId() != null) {
            throw new DaoException(DUPLICATE_ERORR_MSG);
        }

        String insert = createInsert(table, seq, columns, fields);
        NamedParameterJdbcTemplate jdbcTemplate = getNamedParameterJdbcTemplate();
        KeyHolder keyHolder = new GeneratedKeyHolder();

        MapSqlParameterSource sqlParameterSource = prepareParameters(identityObject, fields);

        jdbcTemplate.update(insert, sqlParameterSource, keyHolder, new String[]{"ID"});
        identityObject.setId(keyHolder.getKey().longValue());


    }

    @Override
    public void delete(Long id) {
        int count = getJdbcTemplate().update("delete from ndfl_person where id = ?", id);
        if (count == 0) {
            throw new DaoException("Не удалось удалить сущность класса NdflPerson с id = %d, так как она не существует", id);
        }
    }

    @Override
    public List<NdflPersonIncome> findNdflPersonIncomeConsolidatedRNU(long declarationDataId, String kpp, String oktmo) {
        String oktmoNull = null;
        if (oktmo == null) {
            oktmoNull = "npi.OKTMO is null";
        } else {
            oktmoNull = "npi.OKTMO = :oktmo";
        }
        String query = "select " + createColumns(NdflPersonIncome.COLUMNS, "npi") + " from NDFL_PERSON_INCOME npi " +
                "where npi.KPP = :kpp and " + oktmoNull + " and npi.NDFL_PERSON_ID in " +
                "(select np.id from NDFL_PERSON np where np.PERSON_ID in (select nr.person_id from NDFL_REFERENCES nr " +
                "where nr.DECLARATION_DATA_ID = :declarationDataId and nr.ERRTEXT is not null) " +
                "and np.DECLARATION_DATA_ID in (select dd.id from declaration_data dd " +
                "inner join DECLARATION_DATA_CONSOLIDATION ddc on dd.id = ddc.SOURCE_DECLARATION_DATA_ID " +
                "where ddc.TARGET_DECLARATION_DATA_ID = :declarationDataId))";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("declarationDataId", declarationDataId)
                .addValue("kpp", kpp)
                .addValue("oktmo", oktmo);
        return getNamedParameterJdbcTemplate().query(query, params, new NdflPersonDaoImpl.NdflPersonIncomeRowMapper());
    }

    @Override
    public List<NdflPersonDeduction> findDeductionsByNdflPersonAndOperation(long ndflPersonId, long operationId) {
        String sql = "select " + createColumns(NdflPersonDeduction.COLUMNS, "npd") + " " +
                "from NDFL_PERSON_DEDUCTION npd where npd.NDFL_PERSON_ID = :ndflPersonId " +
                "and npd.operation_id = :operationId";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("ndflPersonId", ndflPersonId)
                .addValue("operationId", operationId);
        try {
            return getNamedParameterJdbcTemplate().query(sql, params, new NdflPersonDaoImpl.NdflPersonDeductionRowMapper());
        } catch(EmptyResultDataAccessException ex) {
            return new ArrayList<NdflPersonDeduction>();
        }
    }

    @Override
    public List<NdflPersonPrepayment> findPrepaymentsByNdflPersonAndOperation(long ndflPersonId, long operationId) {
        String sql = "select " + createColumns(NdflPersonPrepayment.COLUMNS, "npp") + " " +
                "from NDFL_PERSON_PREPAYMENT npp where npp.NDFL_PERSON_ID = :ndflPersonId " +
                "and npp.operation_id = :operationId";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("ndflPersonId", ndflPersonId)
                .addValue("operationId", operationId);
        try {
            return getNamedParameterJdbcTemplate().query(sql, params, new NdflPersonDaoImpl.NdflPersonPrepaymentRowMapper());
        } catch(EmptyResultDataAccessException ex) {
            return new ArrayList<NdflPersonPrepayment>();
        }
    }

    @Override
    public NdflPersonIncome getIncome(long id) {
        String sql = "select " + createColumns(NdflPersonIncome.COLUMNS, "npi") + " from NDFL_PERSON_INCOME npi " +
                "where npi.id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        return getNamedParameterJdbcTemplate().queryForObject(sql, params, new NdflPersonDaoImpl.NdflPersonIncomeRowMapper());
    }

    @Override
    public NdflPersonDeduction getDeduction(long id) {
        String sql = "select " + createColumns(NdflPersonDeduction.COLUMNS, "npd") + " from NDFL_PERSON_DEDUCTION npd " +
                "where npd.id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        return getNamedParameterJdbcTemplate().queryForObject(sql, params, new NdflPersonDaoImpl.NdflPersonDeductionRowMapper());
    }

    @Override
    public NdflPersonPrepayment getPrepayment(long id) {
        String sql = "select " + createColumns(NdflPersonPrepayment.COLUMNS, "npp") + " from NDFL_PERSON_PREPAYMENT npp " +
                "where npp.id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        return getNamedParameterJdbcTemplate().queryForObject(sql, params, new NdflPersonDaoImpl.NdflPersonPrepaymentRowMapper());
    }

    private <E> MapSqlParameterSource prepareParameters(E entity, String[] fields) {
        MapSqlParameterSource result = new MapSqlParameterSource();
        BeanPropertySqlParameterSource defaultSource = new BeanPropertySqlParameterSource(entity);
        Set fieldsSet = new HashSet<String>();
        fieldsSet.addAll(Arrays.asList(fields));
        for (String paramName : defaultSource.getReadablePropertyNames()) {
            if (fieldsSet.contains(paramName)) {
                result.addValue(paramName, defaultSource.getValue(paramName));
            }
        }
        return result;
    }

    private static String createColumns(String[] columns, String alias) {
        List<String> list = new ArrayList<String>();
        for (String col : columns) {
            list.add(alias + "." + col);
        }
        String columnsNames = toSqlString(list.toArray());
        return columnsNames.replace("(", "").replace(")", "");
    }

    private static String createInsert(String table, String seq, String[] columns, String[] fields) {
        StringBuilder sb = new StringBuilder();
        sb.append("insert into ").append(table);
        sb.append(toSqlString(columns));
        sb.append(" VALUES ");
        sb.append(toSqlParameters(fields, seq));
        return sb.toString();
    }

    /**
     * Метод преобразует массив {"a", "b", "c"} в строку "(a, b, c)"
     *
     * @param a исходный массив
     * @return строка
     */
    public static String toSqlString(Object[] a) {
        if (a == null) {
            return "";
        }
        int iMax = a.length - 1;
        if (iMax == -1) {
            return "";
        }
        StringBuilder b = new StringBuilder();
        b.append('(');
        for (int i = 0; ; i++) {
            b.append(a[i]);
            if (i == iMax) {
                return b.append(')').toString();
            }
            b.append(", ");
        }
    }

    public static String toSqlParameters(String[] fields, String seq) {
        List<String> result = new ArrayList<String>();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].equals("id")) {
                result.add(seq + ".nextval");
            } else {
                result.add(":" + fields[i]);
            }
        }
        return toSqlString(result.toArray());
    }

    //>-------------------------<The DAO row mappers>-----------------------------<

    private static final class NdflPersonRowMapper implements RowMapper<NdflPerson> {
        @Override
        public NdflPerson mapRow(ResultSet rs, int index) throws SQLException {

            NdflPerson person = new NdflPerson();

            person.setId(SqlUtils.getLong(rs, "id"));
            person.setDeclarationDataId(SqlUtils.getLong(rs, "declaration_data_id"));
            person.setRowNum(SqlUtils.getInteger(rs, "row_num"));
            person.setPersonId(SqlUtils.getLong(rs, "person_id"));

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


    private static final class NdflPersonIncomeRowMapper implements RowMapper<NdflPersonIncome> {
        @Override
        public NdflPersonIncome mapRow(ResultSet rs, int index) throws SQLException {

            NdflPersonIncome personIncome = new NdflPersonIncome();

            personIncome.setId(SqlUtils.getLong(rs, "id"));
            personIncome.setRowNum(rs.getInt("row_num"));
            personIncome.setNdflPersonId(SqlUtils.getLong(rs, "ndfl_person_id"));

            personIncome.setOperationId(SqlUtils.getLong(rs, "operation_id"));
            personIncome.setOktmo(rs.getString("oktmo"));
            personIncome.setKpp(rs.getString("kpp"));

            personIncome.setIncomeCode(rs.getString("income_code"));
            personIncome.setIncomeType(rs.getString("income_type"));
            personIncome.setIncomeAccruedDate(rs.getDate("income_accrued_date"));
            personIncome.setIncomePayoutDate(rs.getDate("income_payout_date"));
            personIncome.setIncomeAccruedSumm(rs.getBigDecimal("income_accrued_summ"));
            personIncome.setIncomePayoutSumm(rs.getBigDecimal("income_payout_summ"));
            personIncome.setTotalDeductionsSumm(rs.getBigDecimal("total_deductions_summ"));
            personIncome.setTaxBase(rs.getBigDecimal("tax_base"));
            personIncome.setTaxRate(SqlUtils.getInteger(rs, "tax_rate"));
            personIncome.setTaxDate(rs.getDate("tax_date"));

            personIncome.setCalculatedTax(SqlUtils.getLong(rs, "calculated_tax"));
            personIncome.setWithholdingTax(SqlUtils.getLong(rs, "withholding_tax"));
            personIncome.setNotHoldingTax(SqlUtils.getLong(rs, "not_holding_tax"));
            personIncome.setOverholdingTax(SqlUtils.getLong(rs, "overholding_tax"));
            personIncome.setRefoundTax(SqlUtils.getLong(rs, "refound_tax"));

            personIncome.setTaxTransferDate(rs.getDate("tax_transfer_date"));
            personIncome.setPaymentDate(rs.getDate("payment_date"));
            personIncome.setPaymentNumber(rs.getString("payment_number"));
            personIncome.setTaxSumm(SqlUtils.getInteger(rs, "tax_summ"));
            personIncome.setSourceId(SqlUtils.getLong(rs, "source_id"));
            return personIncome;
        }
    }

    private static final class NdflPersonDeductionRowMapper implements RowMapper<NdflPersonDeduction> {

        @Override
        public NdflPersonDeduction mapRow(ResultSet rs, int i) throws SQLException {

            NdflPersonDeduction personDeduction = new NdflPersonDeduction();
            personDeduction.setId(SqlUtils.getLong(rs, "id"));
            personDeduction.setNdflPersonId(SqlUtils.getLong(rs, "ndfl_person_id"));
            personDeduction.setRowNum(rs.getInt("row_num"));
            personDeduction.setOperationId(SqlUtils.getLong(rs, "operation_id"));

            personDeduction.setTypeCode(rs.getString("type_code"));

            personDeduction.setNotifType(rs.getString("notif_type"));
            personDeduction.setNotifDate(rs.getDate("notif_date"));
            personDeduction.setNotifNum(rs.getString("notif_num"));
            personDeduction.setNotifSource(rs.getString("notif_source"));
            personDeduction.setNotifSumm(rs.getBigDecimal("notif_summ"));

            personDeduction.setIncomeAccrued(rs.getDate("income_accrued"));
            personDeduction.setIncomeCode(rs.getString("income_code"));
            personDeduction.setIncomeSumm(rs.getBigDecimal("income_summ"));

            personDeduction.setPeriodPrevDate(rs.getDate("period_prev_date"));
            personDeduction.setPeriodPrevSumm(rs.getBigDecimal("period_prev_summ"));
            personDeduction.setPeriodCurrDate(rs.getDate("period_curr_date"));
            personDeduction.setPeriodCurrSumm(rs.getBigDecimal("period_curr_summ"));
            personDeduction.setSourceId(SqlUtils.getLong(rs, "source_id"));

            return personDeduction;
        }
    }

    private static final class NdflPersonPrepaymentRowMapper implements RowMapper<NdflPersonPrepayment> {

        @Override
        public NdflPersonPrepayment mapRow(ResultSet rs, int i) throws SQLException {

            NdflPersonPrepayment personPrepayment = new NdflPersonPrepayment();
            personPrepayment.setId(SqlUtils.getLong(rs, "id"));
            personPrepayment.setNdflPersonId(SqlUtils.getLong(rs, "ndfl_person_id"));
            personPrepayment.setRowNum(rs.getInt("row_num"));
            personPrepayment.setOperationId(SqlUtils.getLong(rs, "operation_id"));

            personPrepayment.setSumm(rs.getLong("summ"));
            personPrepayment.setNotifNum(rs.getString("notif_num"));
            personPrepayment.setNotifDate(rs.getDate("notif_date"));
            personPrepayment.setNotifSource(rs.getString("notif_source"));
            personPrepayment.setSourceId(SqlUtils.getLong(rs, "source_id"));

            return personPrepayment;
        }
    }


}
