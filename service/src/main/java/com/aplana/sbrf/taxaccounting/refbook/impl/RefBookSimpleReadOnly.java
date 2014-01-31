package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.RefBookUtils;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;

/**
 * Универсальный провайдер данных для справочников, хранящихся в отдельных таблицах. Только для чтения и без версионирования.
 */
@Service("refBookSimpleReadOnly")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class RefBookSimpleReadOnly extends AbstractReadOnlyRefBook {

	// Справочник "Виды налоговых форм"
	public static final Long FORM_TYPE_REF_BOOK_ID = 93L;
	public static final String FORM_TYPE_TABLE_NAME = "FORM_TYPE";
	// Справочник "Системные роли"
	public static final Long SEC_ROLE_REF_BOOK_ID = 95L;
	public static final String SEC_ROLE_TABLE_NAME = "SEC_ROLE";
    // Справочник "ОКТМО"
    public static final Long OKTMO_REF_BOOK_ID = 96L;
    public static final String OKTMO_TABLE_NAME = "REF_BOOK_OKTMO";

	@Autowired
	private RefBookUtils refBookUtils;

	/** Название таблицы для запроса данных*/
	private String tableName;

	/** Дополнительная фильтрация выборки */
	private String whereClause;

	@Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams, String filter,
			RefBookAttribute sortAttribute, boolean isSortAscending) {
		return refBookUtils.getRecords(getRefBookId(), getTableName(), pagingParams, filter, sortAttribute, isSortAscending, getWhereClause());
    }

    @Override
    public Map<String, RefBookValue> getRecordData(Long recordId) {
		return refBookUtils.getRecordData(getRefBookId(), getTableName(), recordId);
    }

	public String getTableName() {
		if (StringUtils.isEmpty(tableName)) {
			throw new IllegalArgumentException("Field \"tableName\" must be set");
		}
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getWhereClause() {
		return whereClause;
	}

	public void setWhereClause(String whereClause) {
		this.whereClause = whereClause;
	}

}