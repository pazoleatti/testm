package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model;

import java.util.*;

/**
 * Модель для хранения предыдущего значения сосотояния виджета
 * По кнопке отмена должно устанавливаться этот объект
 *
 * @author aivanov
 */
public class PickerState{

    private Long refBookAttrId;     // can't be null
    private Date versionDate;       // can't be null
    private Boolean multiSelect;    // can't be null

    private String filter;
    private String searchPattern;
    private List<Long> setIds = new LinkedList<Long>();
    private boolean needReload;
    private boolean exactSearch;

    private PickerContext pickerContext;

    public PickerState() {
    }

    public PickerState(Long refBookAttrId, String filter, String searchPattern, Date versionDate, Boolean multiSelect, boolean exactSearch) {
        this.refBookAttrId = refBookAttrId;
        this.filter = filter;
        this.searchPattern = searchPattern;
        this.versionDate = versionDate;
        this.multiSelect = multiSelect;
        this.exactSearch = exactSearch;
    }

    public PickerState(Long refBookAttrId, String filter, String searchPattern, Date versionDate, Boolean multiSelect, boolean exactSearch, List<Long> longList) {
        this.refBookAttrId = refBookAttrId;
        this.filter = filter;
        this.searchPattern = searchPattern;
        this.versionDate = versionDate;
        this.multiSelect = multiSelect;
        this.exactSearch = exactSearch;
        this.setIds = new LinkedList<Long>(longList);
    }

    public void setValues(PickerState newState){
        this.refBookAttrId = newState.getRefBookAttrId();
        this.filter = newState.getFilter();
        this.searchPattern = newState.getSearchPattern();
        this.versionDate = newState.getVersionDate();
        this.multiSelect = newState.isMultiSelect();
        this.pickerContext = newState.getPickerContext();
        this.exactSearch = newState.isExactSearch();

        this.setIds = newState.getSetIds() != null ? new LinkedList<Long>(newState.getSetIds()) : null;
    }

    public Long getRefBookAttrId() {
        return refBookAttrId;
    }

    public void setRefBookAttrId(Long refBookAttrId) {
        this.refBookAttrId = refBookAttrId;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getSearchPattern() {
        return searchPattern;
    }

    public void setSearchPattern(String searchPattern) {
        this.searchPattern = searchPattern;
    }

    public Date getVersionDate() {
        return versionDate;
    }

    public void setVersionDate(Date versionDate) {
        this.versionDate = versionDate;
    }

    public Boolean isMultiSelect() {
        return multiSelect;
    }

    public void setMultiSelect(Boolean multiSelect) {
        this.multiSelect = multiSelect;
    }

    public List<Long> getSetIds() {
        return setIds;
    }

    public void setSetIds(List<Long> setIds) {
        this.setIds = setIds;
    }

    public boolean isNeedReload() {
        return needReload;
    }

    public void setNeedReload(boolean needReload) {
        this.needReload = needReload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PickerState)) return false;

        PickerState that = (PickerState) o;

        if (filter != null ? !filter.equals(that.filter) : that.filter != null) return false;
        if (!multiSelect.equals(that.multiSelect)) return false;
        if (!refBookAttrId.equals(that.refBookAttrId)) return false;
        if (searchPattern != null ? !searchPattern.equals(that.searchPattern) : that.searchPattern != null)
            return false;
        if (!setIds.equals(that.setIds)) return false;
        if (!versionDate.equals(that.versionDate)) return false;
        if (exactSearch != that.exactSearch) return false;

        return true;
    }

    public PickerContext getPickerContext() {
        return pickerContext;
    }

    public void setPickerContext(PickerContext pickerContext) {
        this.pickerContext = pickerContext;
    }

    public boolean isExactSearch() {
        return exactSearch;
    }

    public void setExactSearch(boolean exactSearch) {
        this.exactSearch = exactSearch;
    }

    @Override
    public int hashCode() {
        int result = refBookAttrId.hashCode();
        result = 31 * result + versionDate.hashCode();
        result = 31 * result + multiSelect.hashCode();
        result = 31 * result + (filter != null ? filter.hashCode() : 0);
        result = 31 * result + (searchPattern != null ? searchPattern.hashCode() : 0);
        result = 31 * result + setIds.hashCode();
        result = 31 * result + (exactSearch ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PickerState{" +
                "refBookAttrId=" + refBookAttrId +
                ", filter='" + filter + '\'' +
                ", searchPattern='" + searchPattern + '\'' +
                ", versionDate=" + versionDate +
                ", multiSelect=" + multiSelect +
                ", setIds=" + setIds +
                ", exactSearch=" + exactSearch +
                '}';
    }

}
