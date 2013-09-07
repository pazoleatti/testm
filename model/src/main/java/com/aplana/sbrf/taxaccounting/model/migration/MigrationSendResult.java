package com.aplana.sbrf.taxaccounting.model.migration;

import java.io.Serializable;
import java.util.List;

/**
 * Результат отправки файлов миграции
 * @author Dmitriy Levykin
 */
public class MigrationSendResult implements Serializable {
    private int sendFilesCount = 0;
    private List<Exemplar> exemplarList;

    public int getSendFilesCount() {
        return sendFilesCount;
    }

    public void setSendFilesCount(int sendFilesCount) {
        this.sendFilesCount = sendFilesCount;
    }

    public List<Exemplar> getExemplarList() {
        return exemplarList;
    }

    public void setExemplarList(List<Exemplar> exemplarList) {
        this.exemplarList = exemplarList;
    }
}
