package com.aplana.sbrf.taxaccounting.model.datarow;

import java.io.Serializable;

/**
 * Диапазон строк НФ
 * 
 * @author sgoryachkin
 *
 */
public class DataRowRange implements Serializable{
	private static final long serialVersionUID = -7454326888865205588L;

	/**
	 * Максимальное количество строк диапазона
	 */
	private int limit;

	/**
	 * С какого индекса строки начинается диапазон (ндекс от 0)
	 */
	private int offset;

	public DataRowRange(){
	}

	public DataRowRange(int offset, int limit){
		this.offset = offset;
		this.limit = limit;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}
	
}
