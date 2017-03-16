package com.aplana.sbrf.taxaccounting.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Счетчики загрузки ТФ
 *
 * @author Dmitriy Levykin
 */
public class ImportCounter {
    private int successCounter = 0;
    private int failCounter = 0;
    private List<String> msgList;

    public ImportCounter() {
        this.msgList = new ArrayList<String>();
    }

    public ImportCounter(int successCounter, int failCounter, List<String> msgList) {
        this.successCounter = successCounter;
        this.failCounter = failCounter;
        this.msgList = msgList;
    }

    public ImportCounter(int successCounter, int failCounter) {
        this.successCounter = successCounter;
        this.failCounter = failCounter;
        this.msgList = new ArrayList<String>();
    }

    public int getSuccessCounter() {
        return successCounter;
    }

    public int getFailCounter() {
        return failCounter;
    }

    public List<String> getMsgList() {
        return msgList;
    }

    public void add(ImportCounter importCounter) {
        successCounter += importCounter.successCounter;
        failCounter += importCounter.failCounter;
        msgList.addAll(importCounter.msgList);
    }
}
