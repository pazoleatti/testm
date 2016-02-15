package com.aplana.sbrf.taxaccounting.model;

/**
 * Абстрактный класс для столбцов, где используется фильтр
 *
 * @author Fail Mukhametdinov
 */
public abstract class FilterColumn extends Column {
    private static final long serialVersionUID = -3355084111741345935L;
    protected String filter;

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }
}
