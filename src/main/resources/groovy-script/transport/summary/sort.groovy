/**
 * Скрипт для сортировки.
 * Форма "Расчет суммы налога по каждому транспортному средству".
 */

// сортировка
formData.dataRows.sort {a, b ->
    int val = (a.okato ?: "").compareTo(b.okato ?: "")
    if (val == 0) {
        val = (a.tsTypeCode?: "").compareTo(b.tsTypeCode ?: "")
    }
    return val;
}

// обновление поля порядка
formData.dataRows.eachWithIndex {row, i ->
    row.order = i;
}