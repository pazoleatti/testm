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
        logicCheck()
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
    def formDataCollection = declarationService.getAcceptedFormDataSources(declarationData)

    !declarationData.isAccepted()

    // TODO Нужно ли проверять, что источник - "Матрица", а не другая форма?
    if (formDataCollection == null || formDataCollection.records.isEmpty()) {
        logger.error('Отсутствует "Матрица" в статусе "Принят". Формирование уведомления невозможно.')
        return
    }

    def departmentId = declarationData.departmentId

    // Параметры подразделения
    // TODO: переделать на версионные справочники (Marat Fayzullin 2013-08-02)
    def departmentParam = departmentService.getDepartmentParam(departmentId)

    if (departmentParam == null){
        throw new Exception("Ошибка при получении настроек обособленного подразделения")
    }

    // Получить параметры по транспортному налогу
    // TODO Параметров для УНП пока нет
    // def departmentParamDeal = departmentService.getDepartmentParamDeal(departmentId)

    def builder = new MarkupBuilder(xml)

    // Тип декларации - Уведомление
    def notificationType = 6

    builder.Файл(
            ИдФайл: declarationService.generateXmlFileId(notificationType, departmentId),
            ВерсПрог: /*departmentParamDeal.appVersion*/'???',      // TODO
            ВерсФорм: /*departmentParamDeal.formatVersion*/'???') { // TODO
        Документ(
                // Код формы отчетности по КНД
                // TODO Константа
                КНД: '1110025',
                // Дата формирования документа
                ДатаДок: (docDate != null ? docDate : new Date()).format("dd.MM.yyyy"),
                // Отчетный год
                ОтчетГод: taxPeriodService.get(reportPeriodService.get(declarationData.reportPeriodId).taxPeriodId).startDate.format('yyyy'),
                // Код налогового органа
                КодНО: departmentParam.taxOrganCode,
                // Номер корректировки
                НомКорр: '0', // TODO Смотреть признак корректирующего периода
                // Код места, по которому представляется документ
                ПоМесту: /*departmentParamDeal.taxPlaceTypeCode*/'????' // TODO
        ) {
            СвНП(
                    ОКАТО: departmentParam.okato,
                    ОКВЭД: departmentParam.okvedCode,
                    Тлф: departmentParam.phone
            ) {
                НПЮЛ(
                        НаимОрг: departmentParam.name,
                        ИННЮЛ: departmentParam.inn,
                        КПП: departmentParam.kpp
                ) {
                    if (departmentParam.reorgFormCode != null && !departmentParam.reorgFormCode.equals('0')) {
                        СвРеоргЮЛ(
                                ФормРеорг: departmentParam.reorgFormCode,
                                ИННЮЛ: departmentParam.reorgInn,
                                КПП: departmentParam.reorgKpp)
                    }
                }
            }
            Подписант(
                    ПрПодп: /*departmentParamDeal.signatoryId*/1
            ) {
                ФИО(
                        Фамилия: /*departmentParamDeal.signatorySurname*/ 'todo', // TODO
                        Имя: /*departmentParamDeal.getSignatoryFirstName*/ 'todo', // TODO
                        Отчество: /*departmentParamDeal.getSignatoryLastName*/ 'todo' // TODO
                )
                // TODO СвПред нужен?
                // СвПред(НаимДок: '', НаимОрг: '')
            }

            // По строкам матрицы
            УвКонтрСд(){

            }
        }
    }
}

boolean checkMatrix() {

}

void logicCheck() {
    // TODO
}

