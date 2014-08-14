package com.aplana.sbrf.taxaccounting.model;

import java.util.LinkedList;
import java.util.List;

/**
 * Результат загрузки ТФ в каталог
 */
public class UploadResult {
    int successCounter;
    int failCounter;
    List<String> diasoftFileNameList = new LinkedList<String>();
    List<String> formDataFileNameList = new LinkedList<String>();
    List<Integer> formDataDepartmentList = new LinkedList<Integer>();

    public int getFailCounter() {
        return failCounter;
    }

    public void setFailCounter(int failCounter) {
        this.failCounter = failCounter;
    }

    public int getSuccessCounter() {
        return successCounter;
    }

    public void setSuccessCounter(int successCounter) {
        this.successCounter = successCounter;
    }

    public List<String> getDiasoftFileNameList() {
        return diasoftFileNameList;
    }

    public List<String> getFormDataFileNameList() {
        return formDataFileNameList;
    }

    public List<Integer> getFormDataDepartmentList() {
        return formDataDepartmentList;
    }
}
