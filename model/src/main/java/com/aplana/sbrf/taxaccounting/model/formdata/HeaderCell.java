package com.aplana.sbrf.taxaccounting.model.formdata;

import com.aplana.sbrf.taxaccounting.model.Column;

/**
 * Класс, содержащий информацию о ячейке заголовка налоговой формы: значение и
 * параметры объединения ячеек
 * 
 * @author sgoryachkin
 */
public class HeaderCell extends AbstractCell {

	private static final long serialVersionUID = 5772154644676131690L;
	
	private String value;

	/**
	 * Конструктор только для сериализации
	 */
	public HeaderCell() {
		super();
	}

	public HeaderCell(Column column) {
		super(column);
	}

	@Override
    public Object getValue() {
		if (hasValueOwner()) {
			return getValueOwner().getValue();
		}
		return this.value;
	}

	@Override
	public Object setValue(Object value, Integer rowNumber) {
		// Устанавливаем значение в главную ячейку (SBRFACCTAX-2082)
		if (hasValueOwner()) {
			getValueOwner().setValue(value, rowNumber);
			return getValueOwner().getValue();
		}
		this.value = String.valueOf(value);
		return this.value;
	}

	@Override
	public Object setValue(Object object, Integer rowNumber, boolean force) {
		return setValue(value, rowNumber);
	}

	private void setValue(String value) {
		this.value = value;
	}

	/**
	 * Клонирует данные ячейки для локальных изменений
	 * @return склонированная строка
	 */
	public HeaderCell clone() {
		HeaderCell cell = new HeaderCell();
		cell.setColSpan(this.getColSpan());
		cell.setRowSpan(this.getRowSpan());
		cell.setColumn(this.getColumn());
		cell.setValueOwner(this.getValueOwner());
		cell.setValue(this.value);
		return cell;
	}
}
