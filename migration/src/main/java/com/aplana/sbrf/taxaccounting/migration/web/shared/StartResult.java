package com.aplana.sbrf.taxaccounting.migration.web.shared;

import com.aplana.sbrf.taxaccounting.model.migration.Exemplar;
import com.gwtplatform.dispatch.shared.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StartResult implements Result {

    private List<Exemplar> exemplarList;
    private Map<String, String> files;

    public StartResult() {
        exemplarList = new ArrayList<Exemplar>();
    }

    public List<Exemplar> getExemplarList() {
        return exemplarList;
    }

    public void setExemplarList(List<Exemplar> exemplarList) {
        this.exemplarList = exemplarList;
    }

    public Map<String, String> getFiles() {
        return files;
    }

    public void setFiles(Map<String, String> files) {
        this.files = files;
    }
}
