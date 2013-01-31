package com.aplana.sbrf.taxaccounting.model.script.range;

import java.io.Serializable;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;

/**
 * Описывает диапазон ячеек таблицы. Используется в аналогах Excel-функций внутри скриптов.
 * Диапазон задается границей ячеек с координатами от [colFromAlias; rowFrom] до [colToAlias; rowTo].
 * Диапазон столбцов и строк начинается с 1 согласно {@link Column#getOrder} и {@link DataRow#getOrder}
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 25.01.13 14:47
 */

public class Range implements Serializable {

	private static final String WRONG_ROW_RANGE = "Неверно указан диапазон строк %d - %d.";

	/** левая граница диапазона */
	private String colFromAlias;
	/** верхняя граница диапазона */
	private int rowFrom;
	/** правая граница диапазона */
	private String colToAlias;
	/** нижняя граница диапазона */
	private int rowTo;

	/**
	 * Конструктор по умолчанию
	 *
	 * @param colFromAlias псевдоним левого столбца
	 * @param rowFrom индекс верхней строки
	 * @param colToAlias псевдоним правого столбца
	 * @param rowTo индекс нижней строки
	 */
	public Range(String colFromAlias, int rowFrom, String colToAlias, int rowTo) {
		this.colFromAlias = colFromAlias;
		this.rowFrom = rowFrom;
		this.colToAlias = colToAlias;
		this.rowTo = rowTo;
		checkRowRange();
	}

	public String getColFromAlias() {
		return colFromAlias;
	}

	public void setColFromAlias(String colFromAlias) {
		this.colFromAlias = colFromAlias;
	}

	public int getRowFrom() {
		return rowFrom;
	}

	public void setRowFrom(int rowFrom) {
		this.rowFrom = rowFrom;
		checkRowRange();
	}

	public String getColToAlias() {
		return colToAlias;
	}

	public void setColToAlias(String colToAlias) {
		this.colToAlias = colToAlias;
	}

	public int getRowTo() {
		return rowTo;
	}

	public void setRowTo(int rowTo) {
		this.rowTo = rowTo;
		checkRowRange();
	}

	private void checkRowRange() {
		if (rowTo < rowFrom || rowFrom < 0)
			throw new IndexOutOfBoundsException(String.format(WRONG_ROW_RANGE, rowFrom, rowTo));
	}

	/**
	 * Возвращает координаты прямоугольника, соответствующего данному диапазону по отношению к указанной таблице данных
	 *
	 * @param formData таблица данных
	 * @return прямоугольник
	 */
	public Rect getRangeRect(FormData formData) {
		int colFrom = getColumnIndex(formData, getColFromAlias());
		int colTo = getColumnIndex(formData, getColToAlias());
		if (rowTo >= formData.getDataRows().size())
			throw new IndexOutOfBoundsException(String.format(WRONG_ROW_RANGE, rowFrom, rowTo, formData.getDataRows().size()));
		return new Rect(colFrom, rowFrom, colTo, rowTo);
	}

	/**
	 * Возвращает по алиасу индекс столбца
	 *
	 * @param formData таблица значений
	 * @param colAlias псевдоним столбца
	 * @return индекс столбца
	 * @throws IllegalArgumentException если такого псевдонима столбца не существует в объекте FormData
	 */
	public static int getColumnIndex(FormData formData, String colAlias) {
		List<Column> cols = formData.getFormColumns();
		for (int i = 0; i < cols.size(); i++) {
			if (cols.get(i).getAlias().equals(colAlias)) {
				return i;
			}
		}
		throw new IllegalArgumentException("Wrong column alias requested: " + colAlias);
	}

	@Override
	public String toString() {
		return "Range{" +
				"colFromAlias=" + colFromAlias +
				", rowFrom=" + rowFrom +
				", colToAlias=" + colToAlias +
				", rowTo=" + rowTo +
				'}';
	}

}
