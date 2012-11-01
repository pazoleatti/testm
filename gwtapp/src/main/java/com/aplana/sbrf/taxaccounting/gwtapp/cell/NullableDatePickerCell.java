package com.aplana.sbrf.taxaccounting.gwtapp.cell;

import java.util.Date;

import com.google.gwt.cell.client.DatePickerCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Стандартный DatePickerCell не позволяет работать с null-полями.
 * Данный класс-наследник исправляет его проблемы
 * TODO: нужно добавить возможность сброса значений в null
 */
public class NullableDatePickerCell extends DatePickerCell {
	public NullableDatePickerCell(DateTimeFormat dateTimeFormat) {
		super(dateTimeFormat);
	}

	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context,	Date value, SafeHtmlBuilder sb) {
		super.render(context, value, sb);
		if (value == null) {
			// Если значение в ячейке равно null, то стандартная реализация не рендерит ничего
			// Из-за этого обрамляющий div получается нулевой длины и нажать на него невозможно.
			// Для того, чтобы исправить ситуацию, в div выводим nonbreaking-пробел
			sb.appendHtmlConstant("&nbsp;");
		}
	}

	@Override
	protected void onEnterKeyDown(Context context, Element parent, Date value, NativeEvent event, ValueUpdater<Date> valueUpdater) {
		// Если значение в ячейке равно null, то стандартная реализация onEnterKeyDown устанавливала значение DatePicker'а в null
		// что вызывало ошибку. В данном классе, если значение ячейки равно null, то в picker'е устанавливается текущая дата
		super.onEnterKeyDown(context, parent, value == null ? new Date(): value, event, valueUpdater);
	}
}
