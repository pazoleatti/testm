package form_template.property.property_945_3.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

/**
 * Расчёт налога на имущество по средней/среднегодовой стоимости
 *
 * @author Bulat Kinzyabulatov
 */

// графа 1	rowNum	    	        № пп
//          fix
// графа 2	subject	    	        Код субъекта
// графа 3	taxAuthority	        Код НО
// графа 4	kpp	                    КПП
// графа 5	oktmo	                Код ОКТМО
// графа 6	priceAverage	        Средняя/среднегодовая стоимость имущества
// графа 7	taxBenefitCode	        Код налоговой льготы
// графа 8	priceAverageTaxFree	    Средняя/Среднегодовая стоимость необлагаемого имущества
// графа 9	taxBase                 Налоговая база
// графа 10	taxBenefitCodeReduction Код налоговой льготы (понижение налоговой ставки)
// графа 11	taxRate	                Налоговая ставка
// графа 12	taxSum	                Сумма налога (авансового платежа)
// графа 13	sumPayment	            Сумма авансовых платежей, исчисленная за отчетные периоды
// графа 14	taxBenefitCodeDecrease	Код налоговой льготы (в виде уменьшения суммы налога)
// графа 15	sumDecrease	            Сумма уменьшения платежа
// графа 16	residualValue	        Остаточная стоимость основных средств

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow != null && currentDataRow.getAlias() == null) {
            formDataService.getDataRowHelper(formData).delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        consolidation()
        calc()
        logicCheck()
        break
}

@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

// все атрибуты
@Field
def allColumn = ["rowNum", "fix", "subject", "taxAuthority", "kpp", "oktmo", "priceAverage", "taxBenefitCode", "priceAverageTaxFree", "taxBase", "taxBenefitCodeReduction", "taxRate", "taxSum", "sumPayment", "taxBenefitCodeDecrease", "sumDecrease", "residualValue"]

// Редактируемые атрибуты
@Field
def editableColumns = []

@Field
def autoFillColumns = []

@Field
def nonEmptyColumns = []

@Field
def sortColumns = []

@Field
def groupColumns = []

@Field
def totalColumns = []

@Field
def startDate = null

@Field
def endDate = null

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // TODO

}

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // TODO
}

void consolidation() {
    // TODO собирается из 945.5 и 945.3
}

