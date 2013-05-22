package com.aplana.sbrf.taxaccounting.web.widget.datarow;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.google.gwt.dom.builder.shared.TableCellBuilder;
import com.google.gwt.dom.builder.shared.TableRowBuilder;
import com.google.gwt.user.cellview.client.*;
import com.google.gwt.user.cellview.client.AbstractCellTable.Style;
import com.google.gwt.cell.client.Cell.Context;

import java.util.List;

/**
 * @author Eugene Stetsenko
 */
public class CustomHeaderBuilder extends AbstractHeaderOrFooterBuilder<DataRow<Cell>> {
	private int offset = 0;
	private List<DataRow<HeaderCell>> newHeaders;
	Style style;
	public CustomHeaderBuilder(AbstractCellTable<DataRow<Cell>> table,
	                           boolean isFooter,
	                           int offset,
	                           List<DataRow<HeaderCell>> newHeaders) {
		super(table, isFooter);
		this.offset = offset;
		this.newHeaders = newHeaders;
    }
	@Override
	protected boolean buildHeaderOrFooterImpl() {
		style = getTable().getResources().style();
		buildOurHeader(newHeaders);
		return true;
    }

	private void buildHeader(TableRowBuilder out, Header<?> header, int colSpan, int rowSpan) {
		Style style = getTable().getResources().style();
		StringBuilder classesBuilder = new StringBuilder(style.header());

		TableCellBuilder th = out.startTH().colSpan(colSpan).rowSpan(rowSpan).className(classesBuilder.toString());

		Context context = new Context(0, 2, header.getKey());
		renderHeader(th, context, header);

		th.endTH();
	}

	protected void buildOurHeader(List<DataRow<HeaderCell>> newHeaders) {
		for (DataRow<HeaderCell> header : newHeaders) {
			TableRowBuilder tr = startRow();
			for (int i=0; i<offset; i++) {
				tr.startTH().className(style.header()).text("").endTH();
			}
			for (int i=0; i<getTable().getColumnCount(); i++) {
				Header newHeader;
				String colAlias = ((DataRowColumn)getTable().getColumn(i)).getAlias();
				if (!newHeaders.isEmpty() && (colAlias != null) && (header.getCell(colAlias).getValue() != null) && !header.getCell(colAlias).hasValueOwner()) {
					newHeader = new TextHeader(header.getCell(colAlias).getValue().toString());
					buildHeader(tr, newHeader, header.getCell(colAlias).getColSpan(), header.getCell(colAlias).getRowSpan());
				}

			}
			tr.endTR();
		}
	}
}