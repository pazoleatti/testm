package com.aplana.sbrf.taxaccounting.web.module.formdata.client.cell;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * Ячейка для вывода номера столбца
 */
public class IndexCell extends AbstractCell<DataRow> {

	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context, DataRow value, SafeHtmlBuilder sb) {
		if (value == null) {
			return;
		}
		SafeHtml safeValue = SafeHtmlUtils.fromString(String.valueOf(context.getIndex() + 1));
		sb.append(safeValue);
	}
}
