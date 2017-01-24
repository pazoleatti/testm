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

	// Справочник "Виды деклараций"
	public static final Long DECLARATION_TYPE_REF_BOOK_ID = 207L;
    // Справочник "Типы подразделений банка"
    public static final Long DEPARTMENT_TYPE_REF_BOOK_ID = 103L;
    // Справочник "Типы налоговых форм"(declaration)
    public static final Long DECLARATION_DATA_KIND_REF_BOOK_ID = 932L;
    // Справочник "Випы налоговых форм"(declaration)
    public static final Long DECLARATION_DATA_TYPE_REF_BOOK_ID = 931L;
    //Справочник "Статус действия"
    public static final Long FIAS_OPERSTAT_ID = FiasRefBookDaoImpl.OPERSTAT_ID;
    //Справочник "Типы адресных объектов"
    public static final Long FIAS_SOCRBASE_ID = FiasRefBookDaoImpl.SOCRBASE_ID;
    //Справочник ФИАС "Реестр адресообразующих объектов" (край -> область -> город -> район -> улица)
    public static final Long FIAS_ADDR_OBJECT_ID = FiasRefBookDaoImpl.ADDR_OBJECT_ID;
    //Справочник ФИАС "Реестр объектов адресации" (номера отдельных зданий, сооружений и их типы, интервалы домов, номера отдельных помещений и их типы)
    public static final Long FIAS_HOUSE_ID = FiasRefBookDaoImpl.HOUSE_ID;
    //Справочник "Интервалы домов"
    public static final Long FIAS_HOUSEINT_ID = FiasRefBookDaoImpl.HOUSEINT_ID;
    //Справочник "Сведения по отдельным помещениям"
    public static final Long FIAS_ROOM_ID = FiasRefBookDaoImpl.ROOM_ID;
    //Справочник "Настройки подразделений"
    public static final Long NDFL_REFBOOK_ID = RefBook.WithTable.NDFL.getRefBookId();
    public static final Long TABLE_NDFL_REFBOOK_ID = RefBook.WithTable.NDFL.getTableRefBookId();


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
		return refBook.getTableName();
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