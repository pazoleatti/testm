package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.List;

/**
 * Результат запроса на получение данных с разбивкой на страницы
 * Используется для возвращения результатов запроса, соответствующих {@link PagingParams }.
 * Содержит в себе список записей, удовлетворяющих условиям паджинации, и общее количество записей,
 * удовлетворяющих поисковому запросу в БД.
 * @author dsultanbekov
 * @param <T> - тип запрашиваемых записей
 */
public class PagingResult<T> {
	List<T> records;
	long totalRecordCount;
	
	/**
	 * @return список записей, попавших в запрошенный диапазон значений
	 */
	public List<T> getRecords() {
		return records;
	}
	public void setRecords(List<T> records) {
		this.records = records;
	}
	/**
	 * @return общее количество записей (на всех страницах)
	 */
	public long getTotalRecordCount() {
		return totalRecordCount;
	}
	public void setTotalRecordCount(long totalRecordCount) {
		this.totalRecordCount = totalRecordCount;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("PagingResult{");
		sb.append("totalRecordCount=").append(totalRecordCount);
		sb.append(", records=").append(records);
		sb.append('}');
		return sb.toString();
	}
}
