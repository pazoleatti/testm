package com.aplana.sbrf.taxaccounting.web.widget.cell;

import com.aplana.sbrf.taxaccounting.model.AutoNumerationColumn;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * @author Fail Mukhametdinov
 */
public class AutoNumerationCell extends com.google.gwt.cell.client.AbstractCell<Object>{

    private AutoNumerationColumn column;

    public AutoNumerationCell(ColumnContext columnContext) {
        this.column = (AutoNumerationColumn) columnContext.getColumn();
    }

    // todo зависит от SBRFACCTAX-7142 0.3.8 Реализовать алгоритм нумерации строк в НФ
    @Override
    public void render(Context context, Object value, SafeHtmlBuilder sb) {
        sb.append('1');
    }
}
