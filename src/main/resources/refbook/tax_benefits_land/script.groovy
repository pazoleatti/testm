package refbook.tax_benefits_land

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import groovy.transform.Field

/**
 * Cкрипт справочника «Параметры налоговых льгот земельного налога» (id = 705)
 *
 * @author Bulat Kinzyabulatov
 *
 * Код субъекта РФ представителя декларации - DECLARATION_REGION_ID
 * Код ОКТМО                                - OKTMO
 * Код налоговой льготы                     - TAX_BENEFIT_ID
 * Параметры - статья                       - SECTION
 * Параметры - пункт                        - ITEM
 * Параметры - подпункт                     - SUBITEM
 * Необлагаемая налогом сумма, руб.         - REDUCTION_SUM
 * Доля необлагаемой площади                - REDUCTION_SEGMENT
 * Уменьшающий процент, %                   - REDUCTION_PERCENT
 * Пониженная ставка, %                     - REDUCTION_RATE
 * Параметры льготы                         - REDUCTION_PARAMS
 */
switch (formDataEvent) {
    case FormDataEvent.SAVE:
        save()
        break
}

// Поиск записи в справочнике по значению (для расчетов)
def Long getRecordId(def Long refBookId, def String alias, def String value) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            validDateFrom, -1, null, logger, true)
}

@Field
def providerCache = [:]
@Field
def recordCache = [:]

@Field
def refBookId = 705
@Field
def refBookTaxId = 704
@Field
def concatParamsCodes = ["3022100", "3022200", "3022300", "3022400", "3022500"]
@Field
def benefitMap = [['SECTION', 'ITEM', 'SUBITEM'/*, 'REDUCTION_PARAMS'*/]: concatParamsCodes,
                  ['REDUCTION_SUM']                                     : ["3022100", "3021210", "3021220", "3021230", "3021240", "3021250", "3021260", "3021270"],
                  ['REDUCTION_SEGMENT']                                 : ['3022300'],
                  ['REDUCTION_PERCENT']                                 : ['3022200'],
                  ['REDUCTION_RATE']                                    : ['3022500']]

@Field
def recordIdMap = [:]

void save() {
    benefitMap.values().sum().unique().each { code ->
        recordIdMap.put(code, getRecordId(refBookTaxId, 'CODE', code))
    }
    def refBook = refBookFactory.get(refBookId)
    def showMeaning = true
    saveRecords.each {
        // Проверка обязательности заполнения атрибутов в справочнике "Параметры налоговых льгот"
        def benefitId = it.TAX_BENEFIT_ID.referenceValue
        def String ERROR_EMPTY = "Для налоговой льготы «%s» поле «%s» является обязательным!"
        benefitMap.each { aliases, codes ->
            def codeIds = codes.collect { recordIdMap[it] } as List<Long>
            aliases.each { alias ->
                if (it[alias].value == null && codeIds.contains(benefitId)) {
                    def code = recordIdMap.find { it.value.equals(benefitId) }.key
                    logger.error(ERROR_EMPTY, code, refBook.getAttribute(alias).name)
                }
            }
        }
        // Проверка паттерна для графы "Доля необлагаемой площади"
        def segment = it.REDUCTION_SEGMENT.value
        def pattern = /[0-9]{1,10}\/[0-9]{1,10}/
        if (segment && !(segment ==~ pattern)) {
            logger.error("Атрибут \"%s\" заполнен неверно (%s)! Ожидаемый паттерн: \"%s\"", refBook.getAttribute('REDUCTION_SEGMENT').name, segment, pattern)
            if(showMeaning) {
                showMeaning = false
                logger.error("Расшифровка паттерна «%s»: %s.", pattern, "«от 1 до 10 знаков» / «от 1 до 10 знаков»")
            }
        }
        // заполнение графы "Параметры льготы"
        def concatCodeIds = concatParamsCodes.collect { recordIdMap[it] } as List<Long>
        def params = null
        if (concatCodeIds.contains(benefitId)) {
            def section = it.SECTION.value ?: ""
            def item = it.ITEM.value ?: ""
            def subItem = it.SUBITEM.value ?: ""
            params = String.format("%s%s%s", section.padLeft(4, '0'), item.padLeft(4, '0'), subItem.padLeft(4, '0'))
        }
        it.put("REDUCTION_PARAMS", new RefBookValue(RefBookAttributeType.STRING, params))
    }
}