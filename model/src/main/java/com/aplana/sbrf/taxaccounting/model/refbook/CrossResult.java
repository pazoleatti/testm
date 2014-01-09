package com.aplana.sbrf.taxaccounting.model.refbook;

/**
 * Результат проверки пересечения версий представляющий из себя коды операций, которые необходимо выполнить с версиями, в которых присутствуют пересечения
 * @author dloshkarev
 */
public enum CrossResult {
    /** Нет пересечения */
    OK(0),
    /** Фатальная ошибка */
    FATAL_ERROR(1),
    /** Необходима проверка использования */
    NEED_CHECK_USAGES(2),
    /** Необходимо изменение даты актуальности версии */
    NEED_CHANGE(3),
    /** Необходимо удаление версии */
    NEED_DELETE(4);

    private int id;

    private CrossResult(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static CrossResult getResultById(int id) {
        for (CrossResult item : CrossResult.values()) {
            if (item.getId() == id) return item;
        }
        throw new RuntimeException("Результат с указанным идентификатором не найден");
    }
}
