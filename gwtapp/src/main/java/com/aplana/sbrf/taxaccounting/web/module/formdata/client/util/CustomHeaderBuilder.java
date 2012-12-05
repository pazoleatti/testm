package com.aplana.sbrf.taxaccounting.web.module.formdata.client.util;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.google.gwt.dom.builder.shared.TableCellBuilder;
import com.google.gwt.dom.builder.shared.TableRowBuilder;
import com.google.gwt.user.cellview.client.AbstractCellTable.Style;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.AbstractHeaderOrFooterBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.cell.client.Cell.Context;

public class CustomHeaderBuilder extends AbstractHeaderOrFooterBuilder<DataRow> {
	public CustomHeaderBuilder(AbstractCellTable<DataRow> table, boolean isFooter) {
		super(table, isFooter);
    }
	@Override
	protected boolean buildHeaderOrFooterImpl() {
		boolean tableHasGroupHeader = false;
    	
		for (int i=0; i<getTable().getColumnCount(); i++) {
			if (((DataRowColumn<?>)getTable().getColumn(i)).getRowGroup() != null) {
				tableHasGroupHeader = true;
				break;
			}
		}
		TableRowBuilder tr = startRow();
		// Таблица имеет дополнительный заголовок
		if (tableHasGroupHeader) {
			for(int i=0; i<getTable().getColumnCount(); i++) {
				if (((DataRowColumn<?>)getTable().getColumn(i)).getRowGroup() == null) {
					oneCell(tr, getTable().getHeader(i).getKey().toString());
				} else {
					int j=i+1;
					for (; j<getTable().getColumnCount(); j++) {
						if (!((DataRowColumn<?>)getTable().getColumn(i)).getRowGroup().equals(
								((DataRowColumn<?>)getTable().getColumn(j)).getRowGroup())) {
							break;
						}
					}
					groupColumns(tr, j-i, ((DataRowColumn<?>)getTable().getColumn(i)).getRowGroup());
					i=j-1;
				}
			}
			tr = startRow();
		}
		for (int i=0; i<getTable().getColumnCount(); i++) {
			buildHeader(tr, getTable().getHeader(i), getTable().getColumn(i), tableHasGroupHeader);
		}
		tr.endTR();

		return true;
    }

	private void buildHeader(TableRowBuilder out, Header<?> header, Column<DataRow, ?> column, boolean haveParentHeader) {
		if (((DataRowColumn<?>)column).getRowGroup() != null || !haveParentHeader) {
			Style style = getTable().getResources().style();
			StringBuilder classesBuilder = new StringBuilder(style.header());
	
			TableCellBuilder th = out.startTH().className(classesBuilder.toString());
	
			Context context = new Context(0, 2, header.getKey());
			renderHeader(th, context, header);
	
			th.endTH();
		}
	}
    
	private void oneCell(TableRowBuilder out, String name) {
		Style style = getTable().getResources().style();
		TableCellBuilder th = out.startTH().rowSpan(2).className(style.header()).text(name);
		th.endTH();
	}

	private void groupColumns(TableRowBuilder out, int len, String groupName) {
		Style style = getTable().getResources().style();
		TableCellBuilder th = out.startTH().colSpan(len).className(style.header());
		th.text(groupName).endTH();
	}
}