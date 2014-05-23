package com.aplana.sbrf.taxaccounting.web.widget.cell;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * @author Fail Mukhametdinov
 */
public class AutoNumerationCell extends AbstractCell<Object> {

    @Override
    public void render(Context context, Object value, SafeHtmlBuilder sb) {
        if (value == null) {
            return;
        }
        SafeHtml safeValue = SafeHtmlUtils.fromString(String.valueOf(value));
        sb.append(safeValue);
    }
}
