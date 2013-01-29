package com.aplana.sbrf.taxaccounting.model.script.range;

import java.io.Serializable;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;

/**
 * Описывает диапазон ячеек таблицы. Используется в аналогах Excel-функций внутри скриптов.
 * Диапазон задается границей ячеек с координатами от [colFrom; rowFrom] до [colTo; rowTo].
 * Диапазон столбцов и строк начинается с 1 согласно {@link Column#getOrder} и {@link DataRow#getOrder}
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 25.01.13 14:47
 */

public class Range implements Serializable {

	private static final String WRONG_BOUNDS = "Неверно указан диапазон: (%d; %d) - (%d; %d)";

	/** левая граница диапазона */
	private int colFrom;
	/** верхняя граница диапазона */
	private int rowFrom;
	/** правая граница диапазона */
	private int colTo;
	/** нижняя граница диапазона */
	private int rowTo;

	/**
	 * Конструктор по умолчанию
	 *
	 * @param colFrom индекс левого столбца
	 * @param rowFrom индекс верхней строки
	 * @param colTo индекс правого столбца
	 * @param rowTo индекс нижней строки
	 */
	public Range(int colFrom, int rowFrom, int colTo, int rowTo) {
		if (colTo < colFrom || rowTo < rowFrom || colFrom < 1 || rowFrom < 1)
			throw new IllegalArgumentException(String.format(WRONG_BOUNDS, colFrom, rowFrom, colTo, rowTo));
		this.colFrom = colFrom;
		this.rowFrom = rowFrom;
		this.colTo = colTo;
		this.rowTo = rowTo;
	}

	public int getColFrom() {
		return colFrom;
	}

	public void setColFrom(int colFrom) {
		this.colFrom = colFrom;
	}

	public int getRowFrom() {
		return rowFrom;
	}

	public void setRowFrom(int rowFrom) {
		this.rowFrom = rowFrom;
	}

	public int getColTo() {
		return colTo;
	}

	public void setColTo(int colTo) {
		this.colTo = colTo;
	}

	public int getRowTo() {
		return rowTo;
	}

	public void setRowTo(int rowTo) {
		this.rowTo = rowTo;
	}

	@Override
	public String toString() {
		return "Range{" +
				"colFrom=" + colFrom +
				", rowFrom=" + rowFrom +
				", colTo=" + colTo +
				", rowTo=" + rowTo +
				'}';
	}

}
