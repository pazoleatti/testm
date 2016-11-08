package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.gwtplatform.dispatch.shared.Result;

import java.util.Map;

/**
 * User: lhaziev
 */
public class AddRowRefBookResult implements Result {
    Map<String, RefBookValueSerializable> record;

    public Map<String, RefBookValueSerializable> getRecord() {
        return record;
    }

    public void setRecord(Map<String, RefBookValueSerializable> record) {
        this.record = record;
    }
}
