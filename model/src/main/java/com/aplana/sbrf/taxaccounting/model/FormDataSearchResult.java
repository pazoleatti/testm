package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * Модель возвращаемых данных при поиске данных
 * внутри налоговой формы
 * @author auldanov
 *
 * 28.03.2014
 */
public class FormDataSearchResult implements Serializable, Comparable {

    private static final long serialVersionUID = -8906124879515002415L;

    private Long index;
    private Long columnIndex;
    private Long rowIndex;
    private String stringFound;

    public Long getIndex() {
        return index;
    }

    public void setIndex(Long index) {
        this.index = index;
    }

    public Long getColumnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(Long columnIndex) {
        this.columnIndex = columnIndex;
    }

    public Long getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(Long rowIndex) {
        this.rowIndex = rowIndex;
    }

    public String getStringFound() {
        return stringFound;
    }

    public void setStringFound(String stringFound) {
        this.stringFound = stringFound;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FormDataSearchResult)) return false;

        FormDataSearchResult that = (FormDataSearchResult) o;

        if (columnIndex != null ? !columnIndex.equals(that.columnIndex) : that.columnIndex != null) return false;
        if (index != null ? !index.equals(that.index) : that.index != null) return false;
        if (rowIndex != null ? !rowIndex.equals(that.rowIndex) : that.rowIndex != null) return false;
        if (stringFound != null ? !stringFound.equals(that.stringFound) : that.stringFound != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = index != null ? index.hashCode() : 0;
        result = 31 * result + (columnIndex != null ? columnIndex.hashCode() : 0);
        result = 31 * result + (rowIndex != null ? rowIndex.hashCode() : 0);
        result = 31 * result + (stringFound != null ? stringFound.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(Object o) {
        FormDataSearchResult o1 = (FormDataSearchResult)o;
        if (o1.getRowIndex() != this.getRowIndex())
            return this.getRowIndex().compareTo(o1.getRowIndex());
        return this.getColumnIndex().compareTo(o1.getColumnIndex());
    }
}
