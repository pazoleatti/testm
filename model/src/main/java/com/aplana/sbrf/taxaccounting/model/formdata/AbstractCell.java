package com.aplana.sbrf.taxaccounting.model.formdata;

import java.io.Serializable;

import com.aplana.sbrf.taxaccounting.model.Column;

abstract public class AbstractCell implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2499576913367192605L;

	/**
	 * Связанная колонка
	 */
	private Column column;

	/**
	 * Значение диапазона по горизонтали
	 */
	private int colSpan = 1;

	/**
	 * Значение диапазона по вертикали
	 */
	private int rowSpan = 1;

	/**
	 * Ссылка на главную ячейку. Не должна быть null если ячейку перекрывает 
	 * другая ячейка в таблице из-за объединения. (SBRFACCTAX-2082)
	 * Значение не храниться не в БД не в XML. Вычисляется в соответствии с colSpan rowSpan
	 */
	private AbstractCell valueOwner;

	/**
	 * Конструктор только для сериализации
	 */
	public AbstractCell() {

	}

	public AbstractCell(Column column) {
		this.column = column;
	}

	public Column getColumn() {
		return column;
	}

	public void setColumn(Column column) {
		this.column = column;
	}

	/**
	 * Возвращает
	 * 
	 * @return значение атрибута colSpan
	 */
	public int getColSpan() {
		return colSpan;
	}

	/**
	 * Задаёт количество столбцов, на которые должна "растягиваться" данная
	 * ячейка (аналогично атрибуту colspan html-тега TD) Если значение 1, то
	 * объединение ячеек не требуется
	 * 
	 * @param colSpan
	 *            значение атрибута colSpan
	 * @throws IllegalArgumentException
	 *             если задаётся значение меньше 1
	 */
	public void setColSpan(int colSpan) {
		if (colSpan < 1) {
			throw new IllegalArgumentException(
					"colSpan value can not be less than 1");
		}
		this.colSpan = colSpan;
	}

	/**
	 * Возвращает количество строк, на которые должна "растягиваться" данная
	 * ячейка (аналогично атрибуту rowspan html-тега TD)
	 * 
	 * @return значение атрибута rowSpan
	 */
	public int getRowSpan() {
		return rowSpan;
	}

	/**
	 * Задаёт количество строк, на которые должна "растягиваться" данная ячейка
	 * (аналогично атрибуту rowspan html-тега TD) Если значение 1, то
	 * объединение ячеек не требуется
	 * 
	 * @param rowSpan
	 *            значение атрибута rowSpan
	 * @throws IllegalArgumentException
	 *             если задаётся значение меньше 1
	 */
	public void setRowSpan(int rowSpan) {
		if (rowSpan < 1) {
			throw new IllegalArgumentException(
					"rowSpan value can not be less than 1");
		}
		this.rowSpan = rowSpan;
	}

	/**
	 * Получает ячейку которая главная в группе (SBRFACCTAX-2082)
	 * Не использовать в скриптах!
	 * 
	 * @return
	 */
	public AbstractCell getValueOwner() {
		return valueOwner;
	}

	/**
	 * Устанавливает ячейку которая главная в группе (SBRFACCTAX-2082)
	 * Не использовать в скриптах!
	 * 
	 * @param valueOwner
	 */
	public void setValueOwner(AbstractCell valueOwner) {
		this.valueOwner = valueOwner;
	}
	
	/**
	 * Возвращает признак наличия главной ячейки (SBRFACCTAX-2082)
	 * Для использования в скриптах
	 * 
	 * @return 
	 * true - ячейка принадлежит объединенной группе и является не главной в группе. Её значение это - не её значение
	 * false - ячейка главная в группе или не состоит в группе
	 */
	public boolean hasValueOwner(){
		return this.valueOwner != null;
	}
	
	abstract public Object getValue();

	abstract public Object setValue(Object object);

}
