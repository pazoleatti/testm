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

	private void buildHeader(TableRowBuilder out, Header<?> header, int colSpan, int rowSpan, boolean needBorder) {
		Style style = getTable().getResources().style();
		StringBuilder classesBuilder = new StringBuilder(style.header());

		TableCellBuilder th = out.startTH().colSpan(colSpan).rowSpan(rowSpan).className(classesBuilder.toString());

		Context context = new Context(0, 2, header.getKey());
		if (!needBorder && colSpan == 1) {
            th.style().borderStyle(com.google.gwt.dom.client.Style.BorderStyle.NONE);
		} else {
            th.style().width(0, com.google.gwt.dom.client.Style.Unit.EM);
        }
		renderHeader(th, context, header);

		th.endTH();
	}

	protected void buildOurHeader(List<DataRow<HeaderCell>> newHeaders) {
		int c = 0;
		for (DataRow<HeaderCell> header : newHeaders) {
			TableRowBuilder tr = startRow();
			for (; c<offset; c++) {
				Header defHeader = new TextHeader(getTable().getHeader(c).getValue().toString());
				buildHeader(tr, defHeader, 0, newHeaders.size(), true);
			}
			for (int i=offset; i<getTable().getColumnCount(); i++) {
				Header newHeader;
				String colAlias = ((DataRowColumn)getTable().getColumn(i)).getAlias();
				if (!newHeaders.isEmpty() && (colAlias != null) && (header.getCell(colAlias).getValue() != null) && !header.getCell(colAlias).hasValueOwner()) {
                    String columnWidth = getTable().getColumnWidth(getTable().getColumn(i));
					boolean needBorder = (header.getCell(colAlias).getColumn().getWidth() != 0)
                            && !columnWidth.equals("0em") && !columnWidth.equals("0.0em")
                            && !columnWidth.equals("0px") && !columnWidth.equals("0.0px");
                    if (needBorder) {
                        newHeader = new TextHeader(header.getCell(colAlias).getValue().toString());
                    } else {
                        newHeader = new TextHeader("");
                    }
                    buildHeader(tr, newHeader, header.getCell(colAlias).getColSpan(), header.getCell(colAlias).getRowSpan(), needBorder);
				}

			}
			tr.endTR();
		}
	}
}