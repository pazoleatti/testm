package com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared;

import com.aplana.sbrf.taxaccounting.model.FormStyle;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * User: avanteev
 */
public class InitTypeResult implements Result {
    private List<RefBook> refBookList;
    private List<FormStyle> styles;

    public List<RefBook> getRefBookList() {
        return refBookList;
    }

    public void setRefBookList(List<RefBook> refBookList) {
        this.refBookList = refBookList;
    }

    public List<FormStyle> getStyles() {
        return styles;
    }

    public void setStyles(List<FormStyle> styles) {
        this.styles = styles;
    }
}
