package com.aplana.sbrf.taxaccounting.model;

/**
 * Счетчики загрузки ТФ
 *
 * @author Dmitriy Levykin
 */
public class ImportCounter {
    private int successCounter = 0;
    private int skipCounter = 0;
    private int failCounter = 0;

    public ImportCounter() {}

    public ImportCounter(int successCounter, int skipCounter, int failCounter) {
        this.successCounter = successCounter;
        this.skipCounter = skipCounter;
        this.failCounter = failCounter;
    }

    public int getSuccessCounter() {
        return successCounter;
    }
    public int getSkipCounter() {
        return skipCounter;
    }
    public int getFailCounter() {
        return failCounter;
    }

    public void add(ImportCounter importCounter) {
        successCounter += importCounter.successCounter;
        skipCounter += importCounter.skipCounter;
        failCounter += importCounter.failCounter;
    }
}
