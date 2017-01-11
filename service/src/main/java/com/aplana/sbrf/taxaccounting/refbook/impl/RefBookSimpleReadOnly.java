package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.FiasRefBookDaoImpl;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Date;
import java.util.List;
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
	// Справочник "Виды деклараций"
	public static final Long DECLARATION_TYPE_REF_BOOK_ID = 207L;
	public static final String DECLARATION_TYPE_TABLE_NAME = "DECLARATION_TYPE";
	// Справочник "Системные роли"
	public static final Long SEC_ROLE_REF_BOOK_ID = 95L;
	public static final String SEC_ROLE_TABLE_NAME = "SEC_ROLE";
    // Справочник "Пользователи"
    public static final Long USER_REF_BOOK_ID = 74L;
    public static final String USER_TABLE_NAME = "SEC_USER";
    // Справочник "Типы подразделений банка"
    public static final Long DEPARTMENT_TYPE_REF_BOOK_ID = 103L;
    public static final String DEPARTMENT_TYPE_TABLE_NAME = "DEPARTMENT_TYPE";
    // Справочник "Типы налоговых форм"
    public static final Long FORM_DATA_KIND_REF_BOOK_ID = 94L;
    public static final String FORM_DATA_KIND_TABLE_NAME = "FORM_KIND";
	// Справочник "Цвета"
	public static final Long COLOR_REF_BOOK_ID = 1L;
	public static final String COLOR_TABLE_NAME = "COLOR";
    // Справочник "АСНУ"
    public static final Long ASNU_REF_BOOK_ID = 900L;
    public static final String ASNU_TABLE_NAME = "REF_BOOK_ASNU";

    //Справочник "Статус действия"
    public static final Long FIAS_OPERSTAT_ID = FiasRefBookDaoImpl.OPERSTAT_ID;
    public static final String FIAS_OPERSTAT_TABLE_NAME = FiasRefBookDaoImpl.OPERSTAT_TABLE_NAME;

    //Справочник "Типы адресных объектов"
    public static final Long FIAS_SOCRBASE_ID = FiasRefBookDaoImpl.SOCRBASE_ID;
    public static final String FIAS_SOCRBASE_TABLE_NAME = FiasRefBookDaoImpl.SOCRBASE_TABLE_NAME;

    //Справочник ФИАС "Реестр адресообразующих объектов" (край -> область -> город -> район -> улица)
    public static final Long FIAS_ADDR_OBJECT_ID = FiasRefBookDaoImpl.ADDR_OBJECT_ID;
    public static final String FIAS_ADDR_OBJECT_TABLE_NAME = FiasRefBookDaoImpl.ADDR_OBJECT_TABLE_NAME;

    //Справочник ФИАС "Реестр объектов адресации" (номера отдельных зданий, сооружений и их типы, интервалы домов, номера отдельных помещений и их типы)
    public static final Long FIAS_HOUSE_ID = FiasRefBookDaoImpl.HOUSE_ID;
    public static final String FIAS_HOUSE_TABLE_NAME = FiasRefBookDaoImpl.HOUSE_TABLE_NAME;

    //Справочник "Интервалы домов"
    public static final Long FIAS_HOUSEINT_ID = FiasRefBookDaoImpl.HOUSEINT_ID;
    public static final String FIAS_HOUSEINT_TABLE_NAME = FiasRefBookDaoImpl.HOUSEINT_TABLE_NAME;

    //Справочник "Сведения по отдельным помещениям"
    public static final Long FIAS_ROOM_ID = FiasRefBookDaoImpl.ROOM_ID;
    public static final String FIAS_ROOM_TABLE_NAME = FiasRefBookDaoImpl.ROOM_TABLE_NAME;

	/** Название таблицы для запроса данных*/
	private String tableName;

	/** Дополнительная фильтрация выборки */
	private String whereClause;

	@Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams, String filter,
			RefBookAttribute sortAttribute, boolean isSortAscending) {


		return refBookDao.getRecords(getRefBookId(), getTableName(), pagingParams, filter, sortAttribute, isSortAscending, getWhereClause());
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long parentRecordId, Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return refBookDao.getChildrenRecords(getRefBookId(), getTableName(), parentRecordId, pagingParams, filter, sortAttribute, true);
    }

    @Override
    public Long getRowNum(Date version, Long recordId,
                          String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        return refBookDao.getRowNum(getRefBookId(), getTableName(), recordId, filter, sortAttribute, isSortAscending, getWhereClause());
    }

    @Override
    public List<Long> getParentsHierarchy(Long uniqueRecordId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, RefBookValue> getRecordData(Long recordId) {
		return refBookDao.getRecordData(getRefBookId(), getTableName(), recordId);
    }

    @Override
    public Map<Long, Map<String, RefBookValue>> getRecordData(List<Long> recordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<RefBookAttributePair, String> getAttributesValues(List<RefBookAttributePair> attributePairs) {
        return refBookDao.getAttributesValues(attributePairs);
    }

    @Override
    public List<Long> getUniqueRecordIds(Date version, String filter) {
        return refBookDao.getUniqueRecordIds(getRefBookId(), getTableName(), filter);
    }

    @Override
    public int getRecordsCount(Date version, String filter) {
        return refBookDao.getRecordsCount(getRefBookId(), getTableName(), filter);
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

	@Override
	public Map<Long, RefBookValue> dereferenceValues(Long attributeId, Collection<Long> recordIds) {
		return refBookDao.dereferenceValues(tableName, attributeId, recordIds);
	}

    @Override
    public List<String> getMatchedRecords(List<RefBookAttribute> attributes, List<Map<String, RefBookValue>> records, Integer accountPeriodId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ReferenceCheckResult> getInactiveRecordsInPeriod(@NotNull List<Long> recordIds, @NotNull Date periodFrom, Date periodTo) {
        return refBookDao.getInactiveRecords(tableName, recordIds);
    }
}