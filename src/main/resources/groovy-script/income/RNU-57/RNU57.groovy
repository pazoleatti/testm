/**
 * Скрипт для РНУ-57 (rnu57.groovy).
 * (РНУ-57) Регистр налогового учёта финансового результата от реализации (погашения) векселей сторонних эмитентов
 *
 * Версия ЧТЗ: 64
 *
 * Вопросы аналитикам: http://jira.aplana.com/browse/SBRFACCTAX-2662
 *
 * TODO:
 *      не сделано ничего, т.к. по аналитике очень много вопросов. точнее один вопрос, но он про все.
 *
 * @author vsergeev
 *
 * Графы:
 *
 * 1    number                  № пп
 * 2    bill                    Вексель
 * 3    purchaseDate            Дата приобретения
 * 4    purchasePrice           Цена приобретения, руб.
 * 5    purchaseOutcome         Расходы, связанные с приобретением,  руб.
 * 6    implementationDate      Дата реализации (погашения)
 * 7    implementationPrice     Цена реализации (погашения), руб.
 * 8    implementationOutcome   Расходы, связанные с реализацией,  руб.
 * 9    price                   Расчётная цена, руб.
 * 10   percent                 Процентный доход, учтённый в целях налогообложения  (для дисконтных векселей), руб.
 * 11   implementationpPriceTax Цена реализации (погашения) для целей налогообложения
 *                              (для дисконтных векселей без процентного дохода),  руб.
 * 12   allIncome               Всего расходы по реализации (погашению), руб.
 * 13   implementationPriceUp   Превышение цены реализации для целей налогообложения над ценой реализации, руб.
 * 14   income                  Прибыль (убыток) от реализации (погашения) руб.
 *
 */
switch (formDataEvent) {
    case FormDataEvent.CHECK :
        logicalCheck()
        break
    case FormDataEvent.CALCULATE :
        if (logicalCheck(false)) {
            calc()
        }
        break
    case FormDataEvent.ADD_ROW :
        addNewRowAction()
        break
    case FormDataEvent.DELETE_ROW :
        deleteRow()
        break
}

def addNewRowAction() {
    logger.warn('asdasdasd')
    def newRow = formData.createDataRow()
    makeCellsEditable(newRow)
    addRowBeforeTotal(newRow)
}

def makeCellsEditable(def row) {
    getEditableColsAliases().each {
        row.getCell(it).editable = true
    }
}

def getEditableColsAliases() {
    return ['bill', 'purchaseDate', 'implementationDate', 'implementationPrice', 'implementationOutcome']
}

def getTotalRowIndex() {
    return formData.getDataRowIndex(getTotalRowAlias())
}

def getTotalRowAlias() {
    return 'total'
}

def addRowBeforeTotal(def dataRow) {
    formData.getDataRows().add(getTotalRowIndex(), dataRow)
}

def calc() {

}