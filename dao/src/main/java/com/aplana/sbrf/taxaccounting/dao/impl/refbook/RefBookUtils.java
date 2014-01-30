package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.Filter;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.SimpleFilterTreeListener;
import com.aplana.sbrf.taxaccounting.dao.mapper.RefBookValueMapper;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Класс для формирования стандартных запросов для провайдеров данных справочников.
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 08.10.13 14:39
 */
@Repository
public class RefBookUtils extends AbstractDao {

	@Autowired
	private RefBookDao refBookDao;

    @Autowired
    private ApplicationContext applicationContext;

	/**
	 * Формирует простой sql-запрос по принципу: один справочник - одна таблица
	 */
	public PreparedStatementData getSimpleQuery(RefBook refBook, String tableName, RefBookAttribute sortAttribute,
			String filter, PagingParams pagingParams, boolean isSortAscending, String whereClause) {
		PreparedStatementData ps = new PreparedStatementData();
		ps.appendQuery("SELECT ");
		ps.appendQuery("id ");
		ps.appendQuery(RefBook.RECORD_ID_ALIAS);
		for (RefBookAttribute attribute : refBook.getAttributes()) {
			ps.appendQuery(", ");
			ps.appendQuery(attribute.getAlias());
		}
		ps.appendQuery(" FROM (SELECT ");
		if (isSupportOver()) {
			RefBookAttribute defaultSort = refBook.getSortAttribute();
			String sortColumn = sortAttribute == null ? (defaultSort == null ? "id" : defaultSort.getAlias()) : sortAttribute.getAlias();
            String sortDirection = isSortAscending ? "ASC" : "DESC";
			ps.appendQuery("row_number() over (order by '" + sortColumn + "' "+sortDirection+") as row_number_over");
		} else {
			ps.appendQuery("rownum row_number_over");
		}
		ps.appendQuery(", t.* FROM ");
		ps.appendQuery(tableName);
		ps.appendQuery(" t");

		PreparedStatementData filterPS = new PreparedStatementData();
        SimpleFilterTreeListener simpleFilterTreeListener =  applicationContext.getBean("simpleFilterTreeListener", SimpleFilterTreeListener.class);
        simpleFilterTreeListener.setRefBook(refBook);
        simpleFilterTreeListener.setPs(filterPS);

		Filter.getFilterQuery(filter, simpleFilterTreeListener);
		if (filterPS.getQuery().length() > 0) {
			ps.appendQuery(" WHERE ");
			ps.appendQuery(filterPS.getQuery().toString());
			if (filterPS.getParams().size() > 0) {
				ps.addParam(filterPS.getParams());
			}
		}
		if (whereClause != null && whereClause.trim().length() > 0) {
			if (filterPS.getQuery().length() > 0) {
				ps.appendQuery(" AND ");
			} else {
				ps.appendQuery(" WHERE ");
			}
			ps.appendQuery(whereClause);
		}

		ps.appendQuery(")");
		if (pagingParams != null) {
			ps.appendQuery(" WHERE row_number_over BETWEEN ? AND ?");
			ps.addParam(pagingParams.getStartIndex());
			ps.addParam(pagingParams.getStartIndex() + pagingParams.getCount());
		}
		return ps;
	}

    public PreparedStatementData getSimpleQuery(RefBook refBook, String tableName, RefBookAttribute sortAttribute, String filter, PagingParams pagingParams, String whereClause) {
        return getSimpleQuery(refBook, tableName, sortAttribute, filter, pagingParams, true, whereClause);
    }

	/**
	 * Возвращает элементы справочника в формате для пейджинга. Является реализацией метода {@link RefBookDataProvider#getRecords}
	 * @param refBookId
	 * @param tableName
	 * @param pagingParams
	 * @param filter
	 * @param sortAttribute
	 * @param whereClause дополнительный фильтр для секции WHERE
	 * @return
	 */
	public PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId, String tableName, PagingParams pagingParams,
			String filter, RefBookAttribute sortAttribute, boolean isSortAscending, String whereClause) {
		RefBook refBook = refBookDao.get(refBookId);
		// получаем страницу с данными
		PreparedStatementData ps = getSimpleQuery(refBook, tableName, sortAttribute, filter, pagingParams, isSortAscending, whereClause);
		List<Map<String, RefBookValue>> records = getRecordsData(ps, refBook);
		PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>(records);
		// получаем информацию о количестве всех записей с текущим фильтром
		ps = getSimpleQuery(refBook, tableName, sortAttribute, filter, null, isSortAscending, whereClause);
		result.setTotalCount(getRecordsCount(ps));
		return result;
	}

    /**
     * Перегруженная функция с восходящей сортировкой по умолчанию
     *
     * @param refBookId
     * @param tableName
     * @param pagingParams
     * @param filter
     * @param sortAttribute
	 * @param whereClause
     * @return
     */
    public PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId, String tableName, PagingParams pagingParams,
			String filter, RefBookAttribute sortAttribute, String whereClause) {
        return getRecords(refBookId, tableName, pagingParams, filter, sortAttribute, true, whereClause);
    }

	/**
	 * Возвращает элементы справочника
	 * @param ps
	 * @param refBook
	 * @return
	 */
	private List<Map<String, RefBookValue>> getRecordsData(PreparedStatementData ps, RefBook refBook) {
		if (ps.getParams().size() > 0) {
			return getJdbcTemplate().query(ps.getQuery().toString(), ps.getParams().toArray(), new RefBookValueMapper(refBook));
		} else {
			return getJdbcTemplate().query(ps.getQuery().toString(), new RefBookValueMapper(refBook));
		}
	}

	/**
	 * Возвращает количество записей в выборке
	 * @param ps
	 * @return
	 */
	private Integer getRecordsCount(PreparedStatementData ps) {
		if (ps.getParams().size() > 0) {
			return getJdbcTemplate().queryForInt("select count(*) from (" + ps.getQuery().toString() + ")", ps.getParams().toArray());
		} else {
			return getJdbcTemplate().queryForInt("select count(*) from (" + ps.getQuery().toString() + ")");
		}
	}

    /**
     *
     * @param attributes
     * @param record
     * @return
     */
    public List<String> checkFillRequiredRefBookAtributes(List<RefBookAttribute> attributes, Map<String, RefBookValue> record){
        List<String> errors = new ArrayList<String>();
        for (RefBookAttribute a :attributes){
            if (a.isRequired() && (!record.containsKey(a.getAlias()) || record.get(a.getAlias()).isEmpty())){
                errors.add(a.getName());
            }
        }

        return errors;
    }

    public List<String> checkRefBookAtributeValues(List<RefBookAttribute> attributes, Map<String,RefBookValue> record) {
        List<String> errors = new ArrayList<String>();
        String okatoRegex = "\\d{11}";
        String codeTSRegex = "\\d{3}(\\d{2}|[?]{2})";
        Pattern okatoPattern = Pattern.compile(okatoRegex);
        Pattern codeTSPattern = Pattern.compile(codeTSRegex);
        for (RefBookAttribute a :attributes){
            //Должны содержать только цифры - Код валюты. Цифровой, Определяющая часть кода ОКАТО, Определяющая часть кода ОКТМО, Цифровой код валюты выпуска
            if ((a.getId() == 64L || a.getId() == 12L || a.getId() == 810L) &&
                    !NumberUtils.isNumber(record.get(a.getAlias()).getStringValue())){
                //TODO добавить еще Определяющая часть кода ОКТМО
                errors.add("Значение атрибута " + a.getName() + " должно содержать только цифры");
            }

            //Проверка формата для кода окато
            if ((a.getId() == 7L) && !okatoPattern.matcher(record.get(a.getAlias()).getStringValue()).matches()) {
                errors.add("Значение атрибута " + a.getName() + " должно быть задано в формате ×××××××××××, где × - цифра");
            }

            //Проверка формата для кода ТС
            if ((a.getId() == 411L) && !codeTSPattern.matcher(record.get(a.getAlias()).getStringValue()).matches()) {
                errors.add("Значение атрибута " + a.getName() + " должно быть задано в формате ××××× или ***??, где × - цифра");
            }
        }

        return errors;
    }

	public Map<String, RefBookValue> getRecordData(final Long refBookId, final String tableName, final Long recordId) {
		RefBook refBook = refBookDao.get(refBookId);
		StringBuilder sql = new StringBuilder("SELECT id ");
		sql.append(RefBook.RECORD_ID_ALIAS);
		for (RefBookAttribute attribute : refBook.getAttributes()) {
			sql.append(", ");
			sql.append(attribute.getAlias());
		}
		sql.append(" FROM ");
		sql.append(tableName);
		sql.append(" WHERE id = :id");
		Map<String, Long> params = new HashMap<String, Long>();
		params.put("id", recordId);
		return getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params, new RefBookValueMapper(refBook));
	}

}
