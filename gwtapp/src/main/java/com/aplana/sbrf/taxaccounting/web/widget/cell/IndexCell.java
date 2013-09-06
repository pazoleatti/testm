package com.aplana.sbrf.taxaccounting.web.widget.cell;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * Ячейка для вывода номера столбца
 */
public class IndexCell extends AbstractCell<Integer> {

	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context, Integer value, SafeHtmlBuilder sb) {
		if (value == null) {
			return;
		}
		SafeHtml safeValue = SafeHtmlUtils.fromString(String.valueOf(value));
		sb.append(safeValue);
	}
}
