package form_template.deal.notification

import com.aplana.sbrf.taxaccounting.model.FormDataEvent

import groovy.xml.MarkupBuilder

/**
 * Уведомление. Генератор XML.
 *
 * @author Dmitriy Levykin
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        generateXML()
        break
    case FormDataEvent.DELETE:
        delete()
        break
    default:
        return
}

/**
 * Удаление уведомления
 */
void delete() {
    // TODO реализовать удаление
}

/**
 * Запуск генерации XML
 */
void generateXML() {

}

boolean checkMatrix() {
    def formDataCollection = declarationService.getAcceptedFormDataSources(declarationData)

    !declarationData.isAccepted()

    // TODO Нужно ли проверять, что источник - "Матрица", а не другая форма?
    if (formDataCollection == null || formDataCollection.records.isEmpty()) {
        logger.error('Отсутствует "Матрица" в статусе "Принят". Формирование уведомления невозможно.')
        return
    }

    def departmentId = declarationData.departmentId

    // Параметры подразделения
    def departmentParam = departmentService.getDepartmentParam(departmentId)

    if (departmentParam == null){
        throw new Exception("Ошибка при получении настроек обособленного подразделения")
    }

    // Получить параметры по транспортному налогу
    def departmentParamTransport = departmentService.getDepartmentParamTransport(departmentId)

    def builder = new MarkupBuilder(xml)

    // Тип декларации - Уведомление
    def notificationType = 6

    builder.Файл(ИдФайл: declarationService.generateXmlFileId(notificationType, departmentId), ВерсПрог: departmentParamTransport.appVersion, ВерсФорм:departmentParamTransport.formatVersion) {
        Документ(
                // Код формы отчетности по КНД
                // TODO Константа
                КНД: "1110025",
                // Дата формирования документа
                ДатаДок: (docDate != null ? docDate : new Date()).format("dd.MM.yyyy"),
                // Отчетный год
                ОтчетГод: taxPeriodService.get(reportPeriodService.get(declarationData.reportPeriodId).taxPeriodId).startDate.format('yyyy'),
                // Код налогового органа
                КодНО: departmentParam.taxOrganCode,
                // Номер корректировки
                // TODO разобраться
                НомКорр: "0",
                // Код места, по которому представляется документ
                ПоМесту: departmentParamTransport.taxPlaceTypeCode
        )
    }
}

void logicCheck() {
    // TODO
}

