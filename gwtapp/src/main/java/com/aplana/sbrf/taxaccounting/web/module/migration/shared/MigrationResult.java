package com.aplana.sbrf.taxaccounting.web.module.migration.shared;

import com.aplana.sbrf.taxaccounting.model.migration.Exemplar;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * @author Dmitriy Levykin
 */
public class MigrationResult implements Result {

    private List<Exemplar> exemplarList;
    private int senFilesCount;

    public List<Exemplar> getExemplarList() {
        return exemplarList;
    }

    public void setExemplarList(List<Exemplar> exemplarList) {
        this.exemplarList = exemplarList;
    }

    public int getSenFilesCount() {
        return senFilesCount;
    }

    public void setSenFilesCount(int senFilesCount) {
        this.senFilesCount = senFilesCount;
    }
}
