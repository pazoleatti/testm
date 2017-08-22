package com.aplana.sbrf.taxaccounting.model.refbook;

/**
 * Справочник с версией
 * Created by aokunev on 08.08.2017.
 */
public class RefBookVersionedObject<IdType extends Number> extends RefBookSimple<IdType> {
    private byte versionStatusId;

    public byte getVersionStatusId() {
        return versionStatusId;
    }

    public void setVersionStatusId(byte versionStatusId) {
        this.versionStatusId = versionStatusId;
    }
}
