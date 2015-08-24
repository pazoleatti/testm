package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Date;
import java.util.Map;

/**
 * Запрос на изменение наименования подразделения в печатных формах.
 * Только для подразделений
 * User: avanteev
 */
public class UnitEditingAction extends UnsecuredActionImpl<UnitEditingResult> {
    private Date versionFrom;
    private Date versionTo;
    private int depId;
    private String depName;
    private boolean isChangeType;
    private Map<String, RefBookValueSerializable> valueToSave;

    public Map<String, RefBookValueSerializable> getValueToSave() {
        return valueToSave;
    }

    public void setValueToSave(Map<String, RefBookValueSerializable> valueToSave) {
        this.valueToSave = valueToSave;
    }

    public boolean isChangeType() {
        return isChangeType;
    }

    public void setChangeType(boolean isChangeType) {
        this.isChangeType = isChangeType;
    }

    public int getDepId() {
        return depId;
    }

    public void setDepId(int depId) {
        this.depId = depId;
    }

    public String getDepName() {
        return depName;
    }

    public void setDepName(String depName) {
        this.depName = depName;
    }

    public Date getVersionFrom() {
        return versionFrom;
    }

    public void setVersionFrom(Date versionFrom) {
        this.versionFrom = versionFrom;
    }

    public Date getVersionTo() {
        return versionTo;
    }

    public void setVersionTo(Date versionTo) {
        this.versionTo = versionTo;
    }
}
