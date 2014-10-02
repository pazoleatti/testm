package com.aplana.sbrf.taxaccounting.model;

/**
 * Изменение с указанием номеров строк
 *
 * @author Levykin
 */
public class Diff {
    Integer originalRowNumber;
    Integer revisedRowNumber;
    DiffType diffType;

    /**
     * Изменение
     * @param originalRowNumber Номер строки в исходном файле (null для добавленной)
     * @param revisedRowNumber Номер строки в измененном файле (null для удаленной)
     * @param diffType Характер изменения
     */
    public Diff(Integer originalRowNumber, Integer revisedRowNumber, DiffType diffType) {
        this.originalRowNumber = originalRowNumber;
        this.revisedRowNumber = revisedRowNumber;
        this.diffType = diffType;
    }

    public Integer getOriginalRowNumber() {
        return originalRowNumber;
    }

    public Integer getRevisedRowNumber() {
        return revisedRowNumber;
    }

    public DiffType getDiffType() {
        return diffType;
    }
}
