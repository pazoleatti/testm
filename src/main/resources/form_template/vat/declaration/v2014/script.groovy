package form_template.vat.declaration.v2014

import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import groovy.transform.Field
import groovy.xml.MarkupBuilder
/**
 * Декларация НДС. Генератор XML.
 * http://jira.aplana.com/browse/SBRFACCTAX-6453
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        //checkDeparmentParams(LogLevel.WARNING)
        generateXML()
        break
    case FormDataEvent.CHECK:
        //checkDeparmentParams(LogLevel.ERROR)
        break
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:
        //checkDeparmentParams(LogLevel.ERROR)
        break
    default:
        return
}

//// Кэш провайдеров
//@Field
//def providerCache = [:]
//// Кэш значений справочника
//@Field
//def refBookCache = [:]

//void checkDeparmentParams(LogLevel logLevel) {
//    def date = reportPeriodService.getStartDate(declarationData.reportPeriodId).getTime()
//
//    def departmentId = declarationData.departmentId
//
//    // Параметры подразделения
//    def departmentParamList = getProvider(37).getRecords(date, null, "DEPARTMENT_ID = $departmentId", null)
//
//    if (departmentParamList == null || departmentParamList.size() == 0 || departmentParamList.get(0) == null) {
//        throw new Exception("Ошибка при получении настроек обособленного подразделения")
//    }
//
//    def departmentParam = departmentParamList?.get(0)
//
//    // Проверки подразделения
//    def List<String> errorList = getErrorDepartment(departmentParam)
//    for (String error : errorList) {
//        logger.log(logLevel, String.format("Для данного подразделения на форме настроек подразделений отсутствует значение атрибута %s", error))
//    }
//    errorList = getErrorVersion(departmentParam)
//    for (String error : errorList) {
//        logger.log(logLevel, String.format("Неверно указано значение атрибута %s на форме настроек подразделений для %s", error, departmentParam.NAME.stringValue))
//    }
//}

/**
 * Запуск генерации XML
 */
void generateXML() {
    // def date = reportPeriodService.getStartDate(declarationData.reportPeriodId).getTime()

    def departmentId = declarationData.departmentId

    // Параметры подразделения
    // TODO Пока настроек нет
    def departmentParam = null//getProvider(-1).getRecords(date, null, "DEPARTMENT_ID = $departmentId", null).get(0)

    def formDataCollection = declarationService.getAcceptedFormDataSources(declarationData)

    def builder = new MarkupBuilder(xml)

    // Тип декларации
    def declarationType = 5
    // Код формы отчетности по КНД
    def String KND = '1151001'

    // ОКАТО
    def okato = departmentParam?.OKTMO?.referenceValue != null ? getRefBookValue(96, departmentParam.OKTMO?.referenceValue).CODE.stringValue : null
    // ОКВЭД
    def okvedCode = departmentParam?.OKVED_CODE?.referenceValue != null ? getRefBookValue(34, departmentParam?.OKVED_CODE?.referenceValue).CODE.stringValue : null
    // По месту
    def taxPlaceTypeCode = departmentParam?.TAX_PLACE_TYPE_CODE?.referenceValue != null ? getRefBookValue(2, departmentParam?.TAX_PLACE_TYPE_CODE?.referenceValue).CODE.stringValue : null
    // Список данных форм-источников
    def formDataList = formDataCollection.getRecords()
    // Тип формы → Данные формы
    def formDataRecordsMap = [:]
    for (def formData : formDataList) {
        formDataRecordsMap.put(formData.formType.id, formData)
    }

    builder.Файл(
            ИдФайл: declarationService.generateXmlFileId(declarationType, departmentId, declarationData.reportPeriodId),
            ВерсПрог: departmentParam?.APP_VERSION?.stringValue,
            ВерсФорм: departmentParam?.FORMAT_VERSION?.stringValue) {
        Документ(
                // ТИТУЛЬНЫЙ ЛИСТ
                // Код формы отчетности по КНД
                КНД: KND,
                // Дата формирования документа
                ДатаДок: (docDate != null ? docDate : new Date()).format("dd.MM.yyyy"),
                // Отчетный год
                ОтчетГод: reportPeriodService.get(declarationData.reportPeriodId).taxPeriod.year,
                // Код налогового органа
                КодНО: departmentParam?.TAX_ORGAN_CODE?.stringValue,
                // Номер корректировки
                НомКорр: reportPeriodService.getCorrectionPeriodNumber(declarationData.reportPeriodId, declarationData.departmentId),
                // Код места, по которому представляется документ
                ПоМесту: taxPlaceTypeCode
        ) {
            // ТИТУЛЬНЫЙ ЛИСТ
            СвНП(
                    ОКВЭД: okvedCode,
                    Тлф: departmentParam?.PHONE?.stringValue
            ) {
                НПЮЛ(
                        НаимОрг: departmentParam?.NAME?.stringValue,
                        ИННЮЛ: departmentParam?.INN?.stringValue,
                        КПП: departmentParam?.KPP?.stringValue
                )
            }
            def prPodp = 1
            if (departmentParam?.SIGNATORY_ID?.referenceValue != null) {
                prPodp = getRefBookValue(35, departmentParam?.SIGNATORY_ID?.referenceValue)?.CODE?.numberValue
            }
            Подписант(
                    ПрПодп: prPodp
            ) {
                def String surname = departmentParam?.SIGNATORY_SURNAME?.stringValue
                def String firstname = departmentParam?.SIGNATORY_FIRSTNAME?.stringValue
                def String lastname = departmentParam?.SIGNATORY_LASTNAME?.stringValue
                ФИО(
                        [Фамилия: surname] +
                                [Имя: firstname] +
                                (lastname != null && !lastname.isEmpty() ? [Отчество: lastname] : [:])
                )
                if (prPodp == 2) {
                    СвПред(
                            НаимДок: departmentParam?.APPROVE_DOC_NAME?.stringValue,
                            НаимОрг: departmentParam?.APPROVE_ORG_NAME?.stringValue
                    )
                }
            }

            НДС() {
                // РАЗДЕЛ 1
                СумУплНП(
                        ОКАТО: okato,
                        КБК: '18210301000011000110',
                        'СумПУ_173.5': 0,
                        'СумПУ_173.1': '' // TODO Вычисление
                )

                // РАЗДЕЛ 2
                def rows = [] // TODO Строки 724.6, 724.7
                for (def row : rows) {
                    СумУплНА(
                            // КППИно: '',
                            КБК: '18210301000011000110',
                            ОКАТО: okato,
                            СумИсчисл: '', // TODO Вычисление из row
                            КодОпер: '', // TODO Вычисление из row
                            СумИсчислОтгр: 0,
                            СумИсчислОпл: 0,
                            СумИсчислНА: 0
                    ) {
                        СведПродЮЛ(
                                НаимПрод: '', // TODO Вычисление из row
                                ИННЮЛПрод: '', // TODO Вычисление из row
                        )
                    }
                }

                // РАЗДЕЛ 3
                СумУпл164(
                        НалПУ164: null // TODO Не описан в ТЗ
                ) {
                    СумНалОб(
                            НалВосстОбщ: null // TODO Вычисление
                    ) {
                        РеалТов18(
                                НалБаза: null, // TODO Вычисление из 724.1
                                СумНал: null // TODO Вычисление из 724.1
                        )
                        РеалТов10(
                                НалБаза: null, // TODO Вычисление из 724.1
                                СумНал: null // TODO Вычисление из 724.1
                        )
                        РеалТов118(
                                НалБаза: null, // TODO Вычисление из 724.1
                                СумНал: null // TODO Вычисление из 724.1
                        )
                        РеалТов110(
                                НалБаза: null, // TODO Вычисление из 724.1
                                СумНал: null // TODO Вычисление из 724.1
                        )
                        РеалПредИК(
                                НалБаза: 0,
                                СумНал: 0
                        )
                        ВыпСМРСоб(
                                НалБаза: 0,
                                СумНал: 0
                        )
                        ОплПредПост(
                                НалБаза: null, // TODO Вычисление из 724.1
                                СумНал: null // TODO Вычисление из 724.1
                        )
                        ОплНОТовар(
                                НалБаза: null, // TODO Вычисление из 724.1
                                СумНал: null // TODO Вычисление из 724.1
                        )
                        СумНалВосст(
                                СумНалВс: 0,
                                СумНалСтав0: 0,
                                СумНал170: 0
                        )
                    }
                    СумНалВыч(
                            НалВычОбщ: null, // TODO Вычисление из 724.1
                            НалПредНППок: 0,
                            НалИсчСМР: null, // Нет в ТЗ
                            НалИсчПрод: null, // TODO Вычисление из 724.1
                            НалУплПокНА: null // TODO Вычисление из 724.1
                    ) {
                        НалВыч171(
                                НалВыч171Общ: null, // TODO Вычисление из 724.4
                                НалВычКапСтр: 0
                        )
                        НалВычТамож(
                                НалВычВс: 0,
                                НалУплТО: 0,
                                НалУплНО: 0
                        )
                    }
                }

                // РАЗДЕЛ 4
                НалПодтв0(
                        СумУменИтог: 0
                ) {
                    СумОпер4(
                            КодОпер: null, // TODO Вычисление из 724.2.2
                            НалБаза: null, // TODO Вычисление из 724.2.2
                            НалВычПод: 0,
                            НалНеПод: 0,
                            НалВосст: 0
                    )
                }

                // РАЗДЕЛ 7
                ОперНеНал(
                        ОплПостСв6Мес: 0
                ) {
                    rows = [] // TODO Строки 724.2.1
                    for (def row : rows) {
                        СумОпер7(
                                КодОпер: null, // TODO Вычисление из row
                                СтРеалТов: null, // TODO Вычисление из row
                                СтПриобТов: null, // TODO Вычисление из row
                                НалНеВыч: 0
                        )
                    }
                }
            }
        }
    }
}
