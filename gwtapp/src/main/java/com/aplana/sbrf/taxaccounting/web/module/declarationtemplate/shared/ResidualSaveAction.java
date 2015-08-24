package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Сохранение zip-архива макета после jrxml-проверок
 * User: avanteev
 */
public class ResidualSaveAction extends UnsecuredActionImpl<ResidualSaveResult> {
    //Uuid архива в базе
    private String uploadUuid;
    private int dtId;
    //Показывает будем ли jrxml в архиве или просто файл.
    private boolean isArchive;

    public boolean isArchive() {
        return isArchive;
    }

    public void setIsArchive(boolean isArchive) {
        this.isArchive = isArchive;
    }

    public int getDtId() {
        return dtId;
    }

    public void setDtId(int dtId) {
        this.dtId = dtId;
    }

    public String getUploadUuid() {
        return uploadUuid;
    }

    public void setUploadUuid(String uploadUuid) {
        this.uploadUuid = uploadUuid;
    }
}
