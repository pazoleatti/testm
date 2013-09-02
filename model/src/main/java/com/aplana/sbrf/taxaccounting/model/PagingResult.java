package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Результат запроса на получение данных с разбивкой на страницы
 * Используется для возвращения результатов запроса, соответствующих {@link PagingParams }.
 * Содержит в себе список записей, удовлетворяющих условиям паджинации, и общее количество записей,
 * удовлетворяющих поисковому запросу в БД.
 * @author dsultanbekov
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @param <T> - тип запрашиваемых записей
 */
public class PagingResult<T> extends ArrayList<T>{
	private static final long serialVersionUID = 4359122077734311449L;
	
	int totalRecordCount;
	
	/**
	 * @return список записей, попавших в запрошенный диапазон значений
	 */
	@Deprecated
	public List<T> getRecords() {
		return this;
	}
	@Deprecated
	public void setRecords(List<T> records) {
		clear();
		addAll(records);
	}
	/**
	 * @return общее количество записей (на всех страницах)
	 */
	public int getTotalRecordCount() {
		return totalRecordCount;
	}
	public void setTotalRecordCount(int totalRecordCount) {
		this.totalRecordCount = totalRecordCount;
	}

}

