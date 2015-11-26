package com.aplana.sbrf.taxaccounting.model.refbook;

/**
 * Результат проверки справочных атрибутов в нф
 * @author dloshkarev
 */
public enum CheckResult {
    //Не существует
    NOT_EXISTS(0),
    //Не пересекается с отчетным периодом нф
    NOT_CROSS(1),
    //Не последняя версия в отчетном периоде
    NOT_LAST(2);

    private int result;

    CheckResult(int result) {
        this.result = result;
    }

    public int getResult() {
        return result;
    }

    public static CheckResult getByCode(int code) {
        for (CheckResult result : CheckResult.values()) {
            if (result.getResult() == code) {
                return result;
            }
        }
        return null;
    }
}
