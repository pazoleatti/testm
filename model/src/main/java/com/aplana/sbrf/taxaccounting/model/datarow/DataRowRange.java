package com.aplana.sbrf.taxaccounting.model.datarow;

import java.io.Serializable;

/**
 * Диапазон строк НФ
 * <ul>
 *   <li>offset - начало диапазона</li>
 *   <li>count - величина смещения</li>
 * </ul>
 *
 * @author sgoryachkin
 *
 */
public class DataRowRange implements Serializable{
	private static final long serialVersionUID = -7454326888865205588L;

	/**
	 * Количество строк диапазона
	 */
	private int count;

	/**
	 * С какого индекса строки начинается диапазон (индекс от 1)
	 */
	private int offset;

	public DataRowRange(){
	}

	public DataRowRange(int offset, int count){
		this.offset = offset;
		this.count = count;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}
	
}
