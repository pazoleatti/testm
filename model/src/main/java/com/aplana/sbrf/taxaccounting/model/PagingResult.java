package com.aplana.sbrf.taxaccounting.model;

import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.Collection;
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
	
	private int totalCount;

	public PagingResult() {
		super();
	}

	public PagingResult(Collection<? extends T> c) {
		super(c);
	}

	public PagingResult(Collection<? extends T> c, int totalCount) {
		this(c);
		this.totalCount = totalCount;
	}

    /**
     * @return список записей, попавших в запрошенный диапазон значений
	 * @deprecated используйте сам объект, как массив
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
	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(@Min(0) int totalCount) {
		this.totalCount = totalCount;
	}

}

