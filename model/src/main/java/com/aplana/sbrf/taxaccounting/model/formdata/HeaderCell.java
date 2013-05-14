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
	
	private Object value;

	/**
	 * Конструктор только для сериализации
	 */
	public HeaderCell() {
		super();
	}

	public HeaderCell(Column column) {
		super(column);
	}

	public Object getValue() {
		if (hasValueOwner()) {
			return getValueOwner().getValue();
		}
		return this.value;
	}

	@Override
	public Object setValue(Object value) {
		// Устанавливаем значение в главную ячейку (SBRFACCTAX-2082)
		if (hasValueOwner()) {
			getValueOwner().setValue(value);
			return getValueOwner().getValue();
		}
		this.value = value;
		return this.value;
	}

}
