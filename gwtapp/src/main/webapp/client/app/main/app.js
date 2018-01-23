(function () {
    'use strict';
    // Локализация текстовых ресурсов
    var translateDictionary = {
        "mainUN": "Автоматизированная система «Учет налогов»",
        "mainNDFL": "функциональная подсистема «НДФЛ»",
        "main.menu.nsi": "НСИ",
        "main.menu.administration": "Администрирование",
        "main.menu.notifications": "Нет оповещений",
        "main.menu.exit": "Выход",
        "main.footer.version": "Версия",
        "main.footer.revision": "Ревизия",
        "main.footer.server": "Сервер",
        "main.footer.browser": "Браузер",

        "0": "Удаленный сервер недоступен",
        "400": "Сервер обнаружил в запросе клиента синтаксическую ошибку (400)",
        "401": "Неавторизованный доступ (401)",
        "403": "Доступ к ресурсу запрещен (403)",
        "404": "Не найдено. Сервер не может найти данные (404)",
        "500": "Внутренняя серверная ошибка (500)",
        "503": "Сервис недоступен (503)",
        "12029": "Удаленный сервер недоступен",
        "DIALOGS_SAVE": "Сохранить",
        "DIALOGS_CANCEL": "Отменить",
        "DIALOGS_ERROR_MSG": "Произошла непредвиденная ошибка.",
        "DIALOGS_CONFIRMATION": "Подтверждение",
        "DIALOGS_REJECTED": "Отказать",
        "DIALOGS_NOTIFICATION_MSG": "Неопределенное уведомление.",
        "DIALOGS_OK": "Применить",
        "DIALOGS_PERCENT_COMPLETE": "% завершено",
        "DIALOGS_PLEASE_WAIT_MSG": "Ожидание выполнения операции.",
        "DIALOGS_CLOSE": "Закрыть",
        "DIALOGS_YES": "Да",
        "DIALOGS_NO": "Нет",
        "DIALOGS_PLEASE_WAIT": "Пожалуйста, подождите",
        "DIALOGS_ERROR": "Ошибка",
        "DIALOGS_CONFIRMATION_MSG": "Требуется подтвеждение.",
        "DIALOGS_NOTIFICATION": "Уведомление",
        "DIALOGS_CONTINUE": "Продолжить",
        "DIALOGS_CREATE": "Создать",
        "DIALOGS_CANCELLATION": "Отмена",

        "yes": "Да",
        "no": "Нет",
        "common.select.formatSearching": "Идет поиск...",
        "common.select.formatNoMatches": "По вашему запросу ничего не найдено",
        "common.select.formatLoadMore" : "Подождите, идет загрузка данных...",
        "common.placeholder.notSelected": "Значение не выбрано",
        "common.validation.required": "Необходимо заполнить поле",

        "common.undefined": "Не задано",
        "common.hint": "Заполните обязательные поля (отмечены желтым)",
        "common.error.message.caption.stackTrace": "Ошибка",
        "common.error.message.seemore": "Смотреть подробности",

        "common.button.search": "Найти",
        "common.button.formation": "Сформировать",
        "common.button.clear": "Сбросить",
        "common.button.add": "Добавить",
        "common.button.cancel": "Отменить",
        "common.button.save": "Сохранить",
        "common.button.no": "Нет",
        "common.button.yes": "Да",
        "button.create": "Создать",
        "button.check": "Проверить",
        "button.identifyPersons": "Идентифицировать ФЛ",
        "button.consolidate": "Консолидировать",
        "button.delete": "Удалить",
        "button.accept": "Принять",
        "button.return": "Вернуть в Создана",
        "button.add": "Добавить",
        "button.save": "Сохранить",
        "button.edit": "Редактировать",
        "button.close": "Закрыть",
        "button.today": "Сегодня",
        "button.clear": "Очистить",
        "button.select": "Выбрать",
        "button.open": "Открыть",
        "button.cancel": "Отмена",
        "button.updateFLData": "Обновить данные ФЛ",
        "button.fillTransferTerm": "Заполнить \"Срок перечисления в бюджет\"",
        "button.fillIncomeAttr": "Заполнить \"Признак дохода\"",
        "button.show": "Показать",
        "button.next": ">",
        "button.prev": "<",
        "messageDialog.toDo.title": "Информация",
        "messageDialog.toDo.message": "Функционал находится в разработке",

        "common.validation.snils": "Значение поля должно соответствовать формату \"ХХХ-ХХХ-ХХХ ХХ\"",
        "common.validation.number": "В поле могут вводиться только цифры",
        "common.validation.min.value": "Число должно быть не меньше {{minValue}}",
        "common.validation.max.value": "Число должно быть не больше {{maxValue}}",
        "common.validation.interval.year" : "Интервал периода поиска указан неверно!",
        "common.validation.year": "Значение поля \"Год\" должно соответствовать формату \"ХХХХ\"",
        "common.validation.dateInterval": "Значение поля должно входить в интервал дат от 01.01.1900 до 31.12.2099.",

        "pager.firstPage": "Первая страница",
        "pager.prevPage": "Предыдущая страница",
        "pager.nextPage": "Следующая страница",
        "pager.lastPage": "Последняя страница",
        "pager.page": "Стр.",
        "pager.of": "из {{pagesTotal}}",

        "resizer.scale": "Масштаб",
        "resizer.scaleDown": "Уменьшить масштаб",
        "resizer.scaleUp": "Увеличить масштаб",
        "resizer.fitToWidth": "По ширине",

        "filter.placeholder.all": "-Все-",
        "filterPanelLabel": "Фильтр",
        "infoPanelLabel": "Информация",
        "filter.placeholder.search": "Введите строку",
        "filter.placeholder.empty": "",
        "theNumberOfSelectedItems": "Выбрано значений:",
        "title.redactParametr": "Редактировать параметр",
        "commonParams.default": "Восстановить значения по умолчанию",
        "title.commonParams.default": "Восстановление значений по умолчанию",
        "commonParams.notSavedData": "На форме общих параметров системы имеются несохраненные данные. Выйти без сохранения?",
        "menu.taxes": "Налоги",
        "menu.taxes.ndfl": "НДФЛ",
        "menu.taxes.ndfl.details": "НДФЛ детализация",
        "menu.taxes.ndfl.forms": "Формы",
        "menu.taxes.ndfl.maintenanceOfPeriods": "Ведение периодов",
        "menu.taxes.ndfl.settingsUnits": "Настройки подразделений",
        "menu.taxes.ndfl.declarationTypeAssignment": "Назначение форм",
        "menu.taxes.ndfl.accountability": "Отчетность",
        "menu.taxes.service": "Сервис",
        "menu.taxes.service.loadFiles": "Загрузить файлы",
        "menu.taxes.commonParameters": "Общие параметры",
        "menu.nsi.refbooks": "Справочники",
        "menu.administration.blockList": "Список блокировок",
        "menu.administration.userList": "Список пользователей",
        "menu.administration.asyncTaskList": "Асинхронные задачи",
        "menu.administration.configParams": "Конфигурационные параметры",
        "menu.administration.schedulerTaskList": "Планировщик задач",
        "menu.administration.settings": "Настройки",
        "menu.administration.settings.mockOfTaxForms": "Макеты налоговых форм",
        "menu.administration.settings.refbooks": "Справочники",
        "menu.administration.settings.resetCache": "Сбросить кэш",
        "menu.administration.settings.exportLayouts": "Экспорт макетов",
        "menu.administration.settings.importingScripts": "Импорт скриптов",
        "menu.manuals.manualUser": "Руководство пользователя",
        "menu.manuals.manualLayoutDesigner": "Руководство настройщика макетов",
        "move_back": "Назад",
        "header.ndflJournal.forms": "НДФЛ - Список налоговых форм",
        "header.ndflReportJournal.forms": "НДФЛ - Отчетность",
        "header.configParams": "Администрирование - Конфигурационные параметры",
        "header.ndfl.form.create": "Создание новой записи",
        "header.ndfl.fl.create": "Добавить запись ФЛ",
        "header.ndfl.fl.edit": "Редактировать запись ФЛ",
        "header.declarationTypeAssignment": "НДФЛ - Назначение налоговых форм",
        "header.CommonParams": "Налоги - Общие параметры",

        "tab.ndfl.requisites": "1. Реквизиты",
        "tab.ndfl.informationOnIncomesAndNdfl": "2. Сведения о доходах и НДФЛ",
        "tab.ndfl.informationOnDeductions": "3. Сведения о вычетах",
        "tab.ndfl.informationOnAdvancePayments": "4. Сведения о доходах в виде авансовых платежей",

        "link.ndfl.reporting": "Формирование отчетов",
        "link.ndfl.reporting.uploadXml": "Выгрузить в XML",
        "link.ndfl.reporting.formXLSX": "Сформировать в XLSX",
        "link.ndfl.reporting.formTemplateExcel": "Сформировать \"Шаблон ТФ (Excel)\"",
        "link.ndfl.reporting.formForFL": "Сформировать \"РНУ НДФЛ по физическому лицу\"",
        "link.ndfl.reporting.formForFLs": "Сформировать \"РНУ НДФЛ по всем ФЛ\"",
        "link.ndfl.reporting.formForKppOktmo": "Сформировать \"Реестр сформированной отчетности\"",
        "link.ndfl.reporting.download": "(Скачать)",
        "link.ndfl.historyOfChange": "История изменений",
        "link.ndfl.filesAndComment": "Файлы и комментарии",
        "link.ndfl.sources": "Источники и приемники",
        "link.ndflReport.reporting.formForFL21": "Сформировать \"2-НДФЛ (1) по физическому лицу\"",
        "link.ndflReport.reporting.formForFL22": "Сформировать \"2-НДФЛ (2) по физическому лицу\"",

        "ndfl.button.importExcel": "Загрузить из ТФ (Excel)",

        "ndflReportJournal.button.createReport": "Создать отчетность",
        "ndflReportJournal.button.downloadReport": "Выгрузить",
        "ndflReportJournal.message.emptyFilterFields": "Заполнены не все поля отчетности",
        "ndflReportJournal.filter.documentState": "Состояние ЭД",
        "ndflReportJournal.filter.note": "Примечание",
        "ndflReportJournal.filter.kpp": "КПП",
        "ndflReportJournal.filter.oktmo": "ОКТМО",
        "ndflReportJournal.filter.codeNO": "Код НО",

        "declarationTypeAssignment.grid.columnName.department": "Подразделение",
        "declarationTypeAssignment.grid.columnName.declarationType": "Макет",
        "declarationTypeAssignment.grid.columnName.performer": "Исполнитель",
        "declarationTypeAssignment.filter.department": "Подразделение",

        "ndflReport.label.page": "Стр.",
        "ndflReport.label.from": "из",

        "field.ndfl.tooLongMessage": "Количество символов для комментария превысило допустимое значение",
        "field.ndfl.requiredField": "Обязательное поле",
        "field.ndfl.isNumber": "В поле должны вводиться только цифры",
        "title.comParams.param": "Параметр",
        "title.comParams.paramValue": "Значение параметра",

        "title.cancelChanges": "Отмена операции",
        "title.period": "Период",
        "title.period.value": "{{year}}, {{periodName}}{{correctionString}}",
        "title.period.value.correctionString": ", кор. ({{correctionDate}})",
        "title.department": "Подразделение",
        "title.formNumber": "Номер формы",
        "title.formType": "Тип налоговой формы",
        "title.formKind": "Вид налоговой формы",
        "title.asnu": "Наименование АСНУ",
        "title.nameAsnu": "АСНУ",
        "title.dateAndTimeCreate": "Дата и время создания формы",
        "title.state": "Состояние",
        "title.stateDoc": "Состояние ЭД",
        "title.file": "Файл",
        "title.creator": "Создал",
        "title.manuallyCreated": "Создана вручную",
        "title.numberpp": "№п/п",
        "title.numberDeclaration": "Номер формы",
        "title.inp": "ИНП",
        "title.idOperation": "ID операции",
        "title.kpp": "КПП",
        "title.oktmo": "ОКТМО",
        "title.snils": "СНИЛС",
        "title.idDocNumber": "№ ДУЛ",
        "title.refNumber": "Номер справки",
        "title.lastName": "Фамилия",
        "title.firstName": "Имя",
        "title.middleName": "Отчество",
        "title.dateOfBirthFrom": "Дата рождения c",
        "title.dateOfBirthTo": "Дата рождения по",
        "title.dateFromFilterLabel": "Дата изменения c",
        "title.dateToFilterLabel": "по",
        "title.taxpayer": "Налогоплательщик",
        "title.dateOfBirth": "Дата рождения",
        "title.citizenship": "Гражданство (код страны)",
        "title.statusCode": "Статус (код)",
        "title.document": "Документ",
        "title.document.code": "Код",
        "title.document.number": "Номер",
        "title.status.taxpayer": "Статус налогоплательщика",
        "title.codeDul": "ДУЛ Код",
        "title.numberDul": "ДУЛ Номер",
        "title.registrationAddress": "Адрес регистрации в РФ",
        "title.inn": "ИНН",
        "title.innNp": "ИНН РФ",
        "title.innForeign": "ИНН Ино",
        "title.subject": "Субьект",
        "title.subjectCode": "Код субьекта",
        "title.area": "Район",
        "title.city": "Город",
        "title.locality": "Населенный пункт",
        "title.street": "Улица",
        "title.index": "Индекс",
        "title.house": "Дом",
        "title.building": "Корпус",
        "title.flat": "Квартира",
        "title.deductionCode": "Код вычета",
        "title.documentOnTheRightToDeduct": "Документ о праве на вычет",
        "title.type": "Тип",
        "title.documentDate": "Дата документа",
        "title.number": "Номер",
        "title.sourceCode": "Код источника",
        "title.documentAmount": "Сумма документа",
        "title.income": "Доход",
        "title.incomeCode": "Код дохода",
        "title.incomeDate": "Дата дохода",
        "title.incomeAmount": "Сумма дохода",
        "title.incomeAttr": "Признак дохода",
        "title.dateCalcIncome": "Дата начисления дохода",
        "title.dateCalcIncomeFrom": "Дата начисления дохода с",
        "title.datePaymentIncome": "Дата выплаты дохода",
        "title.taxRate": "Процентная ставка",
        "title.taxRateWithPercent": "Процентная ставка, %",
        "title.numberPaymentOrder": "Номер платежного поручения",
        "title.applicationOfDeduction": "Применение вычета",
        "title.previousPeriod": "Предыдущий период",
        "title.previousDeductionDate": "Дата предыдущего вычета",
        "title.previousDeductionAmount": "Сумма предыдущего вычета",
        "title.currentPeriod": "Текущий период",
        "title.currentDeductionDate": "Дата текущего вычета",
        "title.currentDeductionDateFrom": "Дата текущего вычета с",
        "title.currentDeductionAmount": "Сумма текущего вычета",
        "title.transferDateFrom": "Срок перечисления в бюджет с",
        "title.calculationDateFrom": "Дата расчета НДФЛ с",
        "title.paymentOrderDateFrom": "Дата платежного поручения с",
        "title.codeNO": "Код НО",
        "title.docState": "Состояние ЭД",
        "title.xmlFile": "XML-файл формы",
        "title.note": "Примечание",
        "title.reasonForReturn": "Причина возврата:",
        "title.indicateReasonForReturn": "Укажите причину возврата",
        "title.creatingReport": "Создание отчетности по НДФЛ",

        "title.amountCalcIncome": "Сумма начисленного дохода",
        "title.amountPaymentIncome": "Сумма выплаченного дохода",
        "title.amountDeduction": "Сумма вычета",
        "title.taxBase": "Налоговая база",
        "title.taxDate": "Дата НДФЛ",
        "title.calculatedTax": "НДФЛ исчисленный",
        "title.withholdingTax": "НДФЛ удержанный",
        "title.notHoldingTax": "НДФЛ не удержанный",
        "title.overholdingTax": "НДФЛ излишне удержанный",
        "title.refoundTax": "НДФЛ возвращенный НП",
        "title.taxTransferDate": "Срок перечисления в бюджет",
        "title.paymentDate": "Дата платежного поручения",
        "title.paymentNumber": "Номер платежного поручения",
        "title.taxSumm": "Сумма платежного поручения",
        "title.confirm": "Подтверждение",
        "title.deleteDeclaration": "Вы уверены, что хотите удалить форму?",
        "title.deleteDeclarations": "Вы уверены, что хотите удалить формы?",
        "title.importDeclaration.confirm": "Запуск операции приведет к удалению текущих данных формы и замене их новыми. Продолжить?",
        "title.returnToCreatedDeclaration": "Вы действительно хотите вернуть в статус \"Создана\" формы?",
        "title.returnExistTask": "Запуск операции приведет к отмене некоторых ранее запущенных операций. Операции, уже выполняемые Системой, выполнятся до конца, но результат их выполнения не будет сохранен. Продолжить?",
        "title.checkImpossible": "Для текущего экземпляра налоговой формы не выполнен расчет. Проверка данных невозможна",
        "title.noCalculationPerformed": "Для текущего экземпляра налоговой формы не выполнен расчет",
        "title.acceptImpossible": "Для текущего экземпляра налоговой формы не выполнен расчет. Принятие налоговой формы невозможно",
        "title.acceptCommonParamsDefault": "Вы уверены, что хотите вернуть значения параметров по умолчанию? Все текущие значения будут заменены.",
        "title.confirmation": "Восстановление значений по умолчанию",
        "title.notifType": "Подтверждающий документ. Тип",
        "title.notifDate": "Подтверждающий документ. Дата",
        "title.notifNum": "Подтверждающий документ. Номер",
        "title.notifSource": "Подтверждающий документ. Код источника",
        "title.notifSumm": "Подтверждающий документ. Сумма",
        "title.incomeIdOperation": "Доход. ID операции",
        "title.incomeAccrued": "Доход. Дата",
        "title.income.incomeCode": "Доход. Код дохода",
        "title.incomeSumm": "Доход. Сумма",
        "title.periodPrevDate": "Вычет. Предыдущий период. Дата",
        "title.periodPrevSumm": "Вычет. Предыдущий период. Сумма",
        "title.periodCurrDate": "Вычет. Текущий период. Дата",
        "title.periodCurrSumm": "Вычет. Текущий период. Сумма",

        "title.summ": "Сумма фиксированного авансового платежа",
        "title.prepayment.notifNum": "Номер уведомления",
        "title.prepayment.notifDate": "Дата выдачи уведомления",
        "title.prepayment.notifDateFrom": "Дата выдачи уведомления c",
        "title.prepayment.notifSource": "Код налогового органа, выдавшего уведомление",
        "title.prepayment.filter.notifSource": "Код НО, выдавшего уведомление",

        "logBusiness.title": "Информация по налоговой форме",
        "logBusiness.title.event": "Событие",
        "logBusiness.title.logDate": "Дата-время",
        "logBusiness.title.user": "Пользователь",
        "logBusiness.title.rolesUser": "Роли пользователя",
        "logBusiness.title.departmentUser": "Подразделение пользователя",
        "logBusiness.title.note": "Текст события",
        "rnuPersonFace.title": "РНУ НДФЛ по физическому лицу",
        "rnuPersonFace.": "РНУ НДФЛ по физическому лицу",
        "rnuPersonFace.error.dateInterval":"Дата начала интервала должна быть меньше даты конца",
        "rnuPersonFace.error.dateIntervalOutOfBounds":"Значение поля \"Дата рождения\" должно входить в интервал дат \"от 01.01.1900 до 31.12.2099\".",
        "reportPersonFace.title": "2-НДФЛ (1) по физическому лицу",
        "reportPersonFace.title2": "2-НДФЛ (2) по физическому лицу",
        "reportPersonFace.error.fieldsAreEmpty": "Для поиска физического лица необходимо выбрать хотя бы один критерий поиска",
        "reportPersonFace.info.numberOfFoundEntries": "Найдено записей: ",
        "reportPersonFace.info.found": "Найдено ",
        "reportPersonFace.info.entriesShowed": " записей. Отображено записей ",
        "reportPersonFace.info.needSearchClarify": ". Уточните критерии поиска.",
        "reportPersonFace.error.attr": "Атрибут \"",
        "reportPersonFace.error.symbolsQuantity": "\": Количество символов в значении не должно превышать ",
        "reportPersonFace.error.symbolsQuantityAndNotDigits": "\": Значение должно состоять из цифр. Количество символов в значении не должно превышать ",
        "reportPersonFace.error.dateInterval": "\": значение поля должно входить в интервал дат 'от 01.01.1900 до 31.12.2099'.",

        "notifications.title.listNotifications": "Список оповещений",
        "notifications.title.createDate": "Дата оповещения",
        "notifications.title.content": "Содержание",
        "notifications.title.link": "Ссылка",
        "notifications.title.delete": "Подтверждение удаления оповещений",
        "notifications.title.deleteText": "Вы действительно хотите удалить выбранные оповещения?",
        "notifications.title.deleteLink": "Удалить оповещение",

        "logPanel.title.num": "№ п/п",
        "logPanel.title.dateTime": "Дата-время",
        "logPanel.title.messageType": "Тип сообщения",
        "logPanel.title.message": "Текст сообщения",
        "logPanel.title.type": "Тип",
        "logPanel.title.object": "Объект",
        "logPanel.header.message": "Уведомления (всего: {0}; фатальных ошибок: {1})",
        "logPanel.header.unload": "Выгрузить",
        "logPanel.header.unload.title": "Выгрузить в файл MS Excel",
        "title.link.download": "Скачать",
        "title.link.reportDelete": "Отчет удален",

        "sources.title.sourcesList": "Источники и приемники формы",
        "sources.checkbox.sources": "Источники",
        "sources.checkbox.destinations": "Приемники",
        "sources.checkbox.uncreated": "Несозданные",
        "sources.tableColumn.index": "№",
        "sources.tableColumn.tax": "Налог",
        "sources.tableColumn.srcOrDest": "Источник / Приёмник",
        "sources.tableColumn.declarationId": "Номер формы",
        "sources.tableColumn.department": "Подразделение",
        "sources.tableColumn.correctionDate": "Дата сдачи корректировки",
        "sources.tableColumn.declarationKind": "Тип формы",
        "sources.tableColumn.declarationType": "Вид формы",
        "sources.tableColumn.year": "Год",
        "sources.tableColumn.period": "Период",
        "sources.tableColumn.declarationState": "Состояние формы",

        "uploadTransportData.title": "Загрузка файлов",
        "uploadTransportData.button.upload": "Загрузить файл",
        "uploadTransportData.button.loadAll": "Обработать файлы из каталога загрузки",
        "uploadTransportData.description.upload": "Загружаются только те файлы, которые пользователь предоставляет системе по нажатию этой кнопки.",
        "uploadTransportData.description.loadAll": "Загружаются файлы из каталогов загрузки.",

        "filesComment.header": "Файлы и комментарии",
        "filesComment.title.fileName": "Имя файла",
        "filesComment.title.fileType": "Тип файла",
        "filesComment.title.comment": "Комментарий",
        "filesComment.title.dateTime": "Дата-Время",
        "filesComment.title.user": "Пользователь",
        "filesComment.title.userDepartment": "Подразделение пользователя",
        "filesComment.delete.header": "Подтверждение удаления файлов",
        "filesComment.delete.text": "Вы действительно хотите удалить выбранные файлы?",
        "filesComment.comment": "Комментарий:",
        "filesComment.files": "Файлы:",
        "filesComment.button.add": "Добавить",
        "filesComment.button.remove": "Удалить",
        "filesComment.system": "Система",
        "filesComment.tooLongNoteMessage": "Количество символов для комментария превысило допустимое значение 512.",
        "filesComment.close.saveChange": "Первоначальные данные изменились, применить изменения?",

        "ndfl.removedDeclarationDataBegin": "Налоговая форма с номером = ",
        "ndfl.removedDeclarationDataEnd": " не существует либо была удалена. Вы будете перенаправлены на главную страницу",
        "ndfl.notPersonalOrConsolidatedDeclarationDataBegin": "Налоговая форма с номером = ",
        "ndfl.notPersonalOrConsolidatedDeclarationDataEnd": " не является ПНФ или КНФ. Вы будете перенаправлены на главную страницу",
        "ndfl.notReportDeclarationDataBegin": "Налоговая форма с номером = ",
        "ndfl.notReportDeclarationDataEnd": " не является отчетной. Вы будете перенаправлены на главную страницу",

        "ndfl.rnuNdflPersonFace.manyRecords": "Найдено записей: {{count}}. Отображено записей: 10. Уточните критерии поиска",
        "ndfl.rnuNdflPersonFace.countRecords": "Найдено записей: {{count}}.",
        "ndfl.rnuNdflPersonFace.emptySearchFilter": "Для выбора физического лица необходимо задать один из критериев.",

        "ndflJournal.filter.periodType": "Тип периода",
        "ndflJournal.filter.period": "Период",
        "ndflJournal.filter.department": "Подразделение",
        "ndflJournal.filter.declarationNumber": "Номер формы",
        "ndflJournal.filter.declarationKind": "Тип налоговой формы",
        "ndflJournal.filter.declarationType": "Вид налоговой формы",
        "ndflJournal.filter.asnu": "Наименование АСНУ",
        "ndflJournal.filter.state": "Состояние",
        "ndflJournal.filter.file": "Файл",
        "ndflJournal.button.create": "Создать налоговую форму",
        "ndflJournal.grid.columnName.declarationNumber": "Номер формы",
        "ndflJournal.grid.columnName.declarationKind": "Тип налоговой формы",
        "ndflJournal.grid.columnName.declarationType": "Вид налоговой формы",
        "ndflJournal.grid.columnName.department": "Подразделение",
        "ndflJournal.grid.columnName.asnu": "Наименование АСНУ",
        "ndflJournal.grid.columnName.period": "Период",
        "ndflJournal.grid.columnName.state": "Состояние",
        "ndflJournal.grid.columnName.tfFile": "Файл ТФ",
        "ndflJournal.grid.columnName.creationDateTime": "Дата и время создания формы",
        "ndflJournal.grid.columnName.creator": "Создал",
        "ndflJournal.table.titleYear": "Календарный год ",

        "reportPeriod.title": "Ведение периодов",
        "reportPeriod.deadline.title":"Срок сдачи отчетности для периода: ",
        "reportPeriod.department": "Подразделение",
        "reportPeriod.pils.openPeriod": "Открыть период",
        "reportPeriod.pils.closePeriod": "Закрыть период",
        "reportPeriod.pils.editPeriod": "Редактировать период",
        "reportPeriod.pils.correctPeriod": "Открыть корректирующий период",
        "reportPeriod.pils.deletePeriod": "Удалить период",
        "reportPeriod.pils.deadline":"Назначить срок сдачи отчетности",
        "reportPeriod.filter.periodFrom": "Период, с",
        "reportPeriod.filter.periodTo": "по",
        "reportPeriod.grid.deadline": "Срок сдачи отчетности",
        "reportPeriod.grid.period": "Период",
        "reportPeriod.grid.state": "Состояние",
        "reportPeriod.grid.correctionDate": "Период сдачи корректировки",
        "reportPeriod.grid.status.open": "Открыт",
        "reportPeriod.grid.status.close": "Закрыт",
        "reportPeriod.correctionPeriod": "Период корректировки",
        "reportPeriod.modal.year": "Год",
        "reportPeriod.modal.deadline": "Дата сдачи отчетности",
        "reportPeriod.modal.grid.name": "Наименование",
        "reportPeriod.modal.grid.startDate": "Дата начала периода",
        "reportPeriod.modal.grid.endDate": "Дата окончания периода",
        "reportPeriod.modal.grid.calendarDate": "Календарная дата начала периода",
        "reportPeriod.confirm.text": "Назначить нижестоящим подразделениям?",
        "reportPeriod.confirm.closePeriod.hasNotAccepted.title": "Подтверждение закрытия периода",
        "reportPeriod.confirm.closePeriod.hasNotAccepted.text": "Вы действительно хотите закрыть период?",
        "reportPeriod.confirm.deletePeriod.title": "Удаление периода",
        "reportPeriod.confirm.deletePeriod.text": "Вы уверены, что хотите удалить период?",
        "reportPeriod.confirm.openPeriod.reopenPeriod.text": "Выбранный период закрыт. Выполнить переоткрытие?",
        "reportPeriod.confirm.openPeriod.text": "Отменить операцию открытия периода?",
        "reportPeriod.confirm.openPeriod.title": "Отмена операции открытия периода",
        "reportPeriod.confirm.editPeriod.text": "Отменить операцию редактирования периода?",
        "reportPeriod.confirm.editPeriod.title": "Отмена операции редактирования периода",
        "reportPeriod.confirm.deadline.title": "Отмена установки срока сдачи отчетности",
        "reportPeriod.confirm.deadline.text": "Вы уверены, что хотите отменить изменения?",
        "reportPeriod.error.editPeriod.text": "Редактирование периода невозможно!",
        "reportPeriod.error.closePeriod.hasBlocked.text": "Период не может быть закрыт, пока выполняется редактирование форм, относящихся к этому периоду!",
        "reportPeriod.error.deletePeriod.hasLaterCorPeriod.text": "Удаление периода невозможно, т.к. существует более поздний корректирующий период!",
        "reportPeriod.error.deletePeriod.hasCorPeriod.text": "Удаление периода невозможно, т.к. для него существует корректирующий период!",
        "reportPeriod.error.openPeriod.alreadyOpen": "Период уже открыт!",
        "reportPeriod.error.openPeriod.hasCorrectionPeriod": "Для указанного периода существуют корректирующие периоды, его переоткрытие невозможно!",
        "reportPeriod.error.openCorrectionPeriod.smallCorrectionYear": "Календарный год периода сдачи корректировки не должен быть меньше календарного года корректируемого периода!",
        "reportPeriod.error.editPeriod.alreadyClose.text": "Закрытый период не может быть отредактирован!",
        "reportPeriod.error.editPeriod.noChange.text": "Ни одни параметр не был изменен!",
        "reportPeriod.error.editPeriod.hasCorPeriod.text":"Перед изменением периода необходимо удалить все связанные корректирующие периоды!",
        "reportPeriod.error.editPeriod.alreadyExist.text": "Указанный период уже заведён в Системе!",



        "ndflFL.title.numberpp": "Гр. 1 №п/п",
        "ndflFL.title.inp": "Гр. 2 ИНП",
        "ndflFL.title.lastName": "Гр. 3 Фамилия",
        "ndflFL.title.firstName": "Гр. 4 Имя",
        "ndflFL.title.middleName": "Гр. 5 Отчество",
        "ndflFL.title.dateOfBirth": "Гр. 6 Дата рождения",
        "ndflFL.title.citizenship": "Гр. 7 Гражданство (код страны)",
        "ndflFL.title.innNp": "Гр. 8 ИНН РФ",
        "ndflFL.title.innForeign": "Гр. 9 ИНН Ино",
        "ndflFL.title.codeDul": "Гр. 10 ДУЛ Код",
        "ndflFL.title.numberDul": "Гр. 11 ДУЛ Номер",
        "ndflFL.title.statusCode": "Гр. 12 Статус (код)",
        "ndflFL.title.subjectCode": "Гр. 13 Код субьекта",
        "ndflFL.title.index": "Гр. 14 Индекс",
        "ndflFL.title.area": "Гр. 15 Район",
        "ndflFL.title.city": "Гр. 16 Город",
        "ndflFL.title.locality": "Гр. 17 Населенный пункт",
        "ndflFL.title.street": "Гр. 18 Улица",
        "ndflFL.title.house": "Гр. 19 Дом",
        "ndflFL.title.building": "Гр. 20 Корпус",
        "ndflFL.title.flat": "Гр. 21 Квартира",
        "ndflFL.title.snils": "Гр. 22 СНИЛС",

        "incomesAndTax.title.numberpp": "Гр. 1 №п/п",
        "incomesAndTax.title.inp": "Гр. 2 ИНП",
        "incomesAndTax.title.idOperation": "Гр. 3 ID операции",
        "incomesAndTax.title.incomeCode": "Гр. 4 Код дохода",
        "incomesAndTax.title.incomeAttr": "Гр. 5 Признак дохода",
        "incomesAndTax.title.dateCalcIncome": "Гр. 6 Дата начисления дохода",
        "incomesAndTax.title.datePaymentIncome": "Гр. 7 Дата выплаты дохода",
        "incomesAndTax.title.kpp": "Гр. 8 КПП",
        "incomesAndTax.title.oktmo": "Гр. 9 ОКТМО",
        "incomesAndTax.title.amountCalcIncome": "Гр. 10 Сумма начисленного дохода",
        "incomesAndTax.title.amountPaymentIncome": "Гр. 11 Сумма выплаченного дохода",
        "incomesAndTax.title.amountDeduction": "Гр. 12 Сумма вычета",
        "incomesAndTax.title.taxBase": "Гр. 13 Налоговая база",
        "incomesAndTax.title.taxRateWithPercent": "Гр. 14 Процентная ставка, %",
        "incomesAndTax.title.taxDate": "Гр. 15 Дата НДФЛ",
        "incomesAndTax.title.calculatedTax": "Гр. 16 НДФЛ исчисленный",
        "incomesAndTax.title.withholdingTax": "Гр. 17 НДФЛ удержанный",
        "incomesAndTax.title.notHoldingTax": "Гр. 18 НДФЛ не удержанный",
        "incomesAndTax.title.overholdingTax": "Гр. 19 НДФЛ излишне удержанный",
        "incomesAndTax.title.refoundTax": "Гр. 20 НДФЛ возвращенный НП",
        "incomesAndTax.title.taxTransferDate": "Гр. 21 Срок перечисления в бюджет",
        "incomesAndTax.title.paymentDate": "Гр. 22 Дата платежного поручения",
        "incomesAndTax.title.paymentNumber": "Гр. 23 Номер платежного поручения",
        "incomesAndTax.title.taxSumm": "Гр. 24 Сумма платежного поручения",

        "ndflDeduction.title.numberpp": "Гр. 1 №п/п",
        "ndflDeduction.title.inp": "Гр. 2 ИНП",
        "ndflDeduction.title.deductionCode": "Гр. 3 Код вычета",
        "ndflDeduction.title.notifType": "Гр. 4 Подтверждающий документ. Тип",
        "ndflDeduction.title.notifDate": "Гр. 5 Подтверждающий документ. Дата",
        "ndflDeduction.title.notifNum": "Гр. 6 Подтверждающий документ. Номер",
        "ndflDeduction.title.notifSource": "Гр. 7 Подтверждающий документ. Код источника",
        "ndflDeduction.title.notifSumm": "Гр. 8 Подтверждающий документ. Сумма",
        "ndflDeduction.title.incomeIdOperation": "Гр. 9 Доход. ID операции",
        "ndflDeduction.title.incomeAccrued": "Гр. 10 Доход. Дата",
        "ndflDeduction.title.income.incomeCode": "Гр. 11 Доход. Код дохода",
        "ndflDeduction.title.incomeSumm": "Гр. 12 Доход. Сумма",
        "ndflDeduction.title.periodPrevDate": "Гр. 13 Вычет. Предыдущий период. Дата",
        "ndflDeduction.title.periodPrevSumm": "Гр. 14 Вычет. Предыдущий период. Сумма",
        "ndflDeduction.title.periodCurrDate": "Гр. 15 Вычет. Текущий период. Дата",
        "ndflDeduction.title.periodCurrSumm": "Гр. 16 Вычет. Текущий период. Сумма",

        "ndlfPrepayment.title.numberpp": "Гр. 1 №п/п",
        "ndlfPrepayment.title.inp": "Гр. 2 ИНП",
        "ndlfPrepayment.title.idOperation": "Гр. 3 ID операции",
        "ndlfPrepayment.title.summ": "Гр. 4 Сумма фиксированного авансового платежа",
        "ndlfPrepayment.title.notifNum": "Гр. 5 Номер уведомления",
        "ndlfPrepayment.title.notifDate": "Гр. 6 Дата выдачи уведомления",
        "ndlfPrepayment.title.notifSource": "Гр. 7 Код налогового органа, выдавшего уведомление",

        "createReport.period": "Период",
        "createReport.department": "Подразделение",
        "createReport.reportType": "Вид отчетности",

        "createDeclaration.title": "Создание налоговой формы",
        "createDeclaration.period": "Период",
        "createDeclaration.department": "Подразделение",
        "createDeclaration.declarationType": "Вид налоговой формы",
        "createDeclaration.asnu": "АСНУ",
        "createDeclaration.periodMessage": "Налоговая форма будет создана в корректирующем периоде, дата сдачи корректировки: ",
        "createDeclaration.errorMessage": "Заполнены не все поля отчетности",
        "createDeclaration.cancel.header": "Отмена создания",
        "createDeclaration.cancel.text": "Отменить создание?",

        "returnToCreated.error.noReasonMessage": "Необходимо указать причину возврата",

        "taskList.title.taskManager": "Планировщик задач",
        "taskList.title.taskChange": "Изменить задачу",
        "taskList.title.listTasks": "Список задач",
        "taskList.title.number": "№",
        "taskList.title.name": "Название",
        "taskList.title.status": "Статус",
        "taskList.title.schedule": "Расписание",
        "taskList.title.editDate": "Дата редактирования",
        "taskList.title.lastStartDate": "Дата последнего запуска",
        "taskList.title.nextStartDate": "Дата следующего запуска",
        "taskList.title.lifetimeLock": "Время жизни блокировки (секунд)",
        "taskList.button.runTasks": "Запустить выполнение по расписанию",
        "taskList.button.stopTasks": "Остановить выполнение по расписанию",
        "taskList.button.update": "Обновить",
        "taskScheduleHelpUpdateParam": "Расписание задается в в формате CRON: \\n секунда минута час число месяц день недели \\n День недели или месяц не могут быть указаны одновременно. \\n Один из них может быть определен символом '?'. \\n Примеры: \\n0 10 * * * ? выполняется каждый час в 10 минут, т.е 0:10, 1:10 \\n 0 0/5 * * * ? выполняется каждые 5 минут \\n 0 0 21 * * ? выполняется каждый день в 21:00 \\n 0 0 18 ? SEP MON-FRI выполняется в 18:00 с понедельника по пятницу весь сентябрь",

        "locks.title": "Список блокировок",
        "locks.title.dateLock": "Дата установки блокировки",
        "locks.title.key": "Ключ блокировки",
        "locks.title.description": "Описание",
        "locks.title.user": "Пользователь",
        "locks.button.delete": "Удалить блокировку",
        "locks.button.search": "Найти",

        "async.title": "Асинхронные задачи",
        "async.button.interrupt": "Остановить задачу",
        "async.button.search": "Найти",
        "async.title.number": "№",
        "async.title.createDate": "Дата создания",
        "async.title.node": "Сервер",
        "async.title.description": "Описание",
        "async.title.user": "Пользователь",
        "async.title.queue": "Тип очереди",
        "async.title.state": "Состояние",
        "async.title.stateDate": "Дата изменения состояния"

    };
    /**
     * @description Основной модуль приложения
     */
    angular
        .module('app', [
            // Стандартные/внешние модули, плагины, компоненты
            'ui.router',
            'ui.validate',
            'ui.select2',
            'ngMessages',
            'angularFileUpload',
            'pascalprecht.translate',
            // Наши компоненты
            'aplana.overlay',
            'aplana.alert',
            'aplana.utils',
            'aplana.grid',
            'mgcrea.ngStrap.dropdown',
            'aplana.dropdown',
            'aplana.dropdownMenu',
            'aplana.tabs',
            'aplana.tooltip',
            'aplana.submitValid',
            'aplana.collapse',
            'aplana.field',
            'aplana.datepicker',
            'aplana.popover',
            'aplana.outerWidth',
            'aplana.select.universal',
            'aplana.timepicker',
            'aplana.datepickerTimepicker',
            'aplana.dateFromToFilter',
            'aplana.formLeaveConfirmer',
            'aplana.link',
            'aplana.modal',
            'aplana.modal.dialogs',
            // Модули приложения
            'app.treeMenu',
            'app.header',
            'app.logPanel',
            'app.validationUtils',
            'app.ndfl',
            'app.ndflJournal',
            'app.ndflReport',
            'app.ndflReportJournal',
            'app.schedulerTaskList',
            'app.lockDataList',
            'app.asyncTaskList',
            'app.filterUtils',
            'app.rest',
            'app.formatters',
            'app.reportPeriod',
            'app.reportPeriodModal',
            'app.deadlinePeriodModal',
            'app.declarationTypeAssignment'
        ])
        /**
         * @description Отображение модального окна с сообщением "Функционал находится в разработке".
         */
        .factory('ShowToDoDialog', ['$dialogs', '$filter', function ($dialogs, $filter) {
            return function () {
                $dialogs.messageDialog({
                    title: $filter('translate')('messageDialog.toDo.title'),
                    content: $filter('translate')('messageDialog.toDo.message')
                });
            };
        }])

        /**
         * @description Конфигурирование роутера и локализации сообщений для приложения
         */
        .config(['$stateProvider', '$urlRouterProvider', '$translateProvider',
            function ($stateProvider, $urlRouterProvider, $translateProvider) {
                // Указание страницы по умолчанию
                $urlRouterProvider.otherwise('/');
                // Настройка обработчика главной страницы
                $stateProvider
                    .state('/', {
                        url: '/',
                        templateUrl: 'client/app/main/app.html?v=${buildUuid}'
                    });

                // Настройка источника локализованных сообщений
                $translateProvider.translations('ru', translateDictionary);
                $translateProvider.preferredLanguage('ru');
                $translateProvider.useSanitizeValueStrategy('sanitizeParameters');
            }
        ])

        .run(['$rootScope', 'ValidationUtils','$overlayService', '$alertService', '$filter', 'uiSelect2Config', 'PermissionChecker', '$window',
            function ($rootScope, ValidationUtils, $overlayService, $alertService, $filter, uiSelect2Config, PermissionChecker, $window) {

                //Регистрируем глобальные сервисы
                $rootScope.validationUtils = ValidationUtils;
                $rootScope.permissionChecker = PermissionChecker;

                angular.element($window).scroll(function () {
                    $rootScope.$broadcast('WINDOW_SCROLLED_MSG');
                });

                //В осле есть баг: http://stackoverflow.com/questions/1852751/window-resize-event-firing-in-internet-explorer
                //он кастует ресайз окна на каждый чих, даже если онкно свой размер не меняло, нужно самим следить за его размерами
                var lastWindow = {};
                angular.element($window).resize(function () {
                    if (angular.isUndefined(lastWindow.width) || angular.isUndefined(lastWindow.height) ||
                        (lastWindow.width !== $window.innerWidth) || (lastWindow.height !== $window.innerHeight)) {

                        $rootScope.$broadcast('WINDOW_RESIZED_MSG');

                        lastWindow = {
                            width: $window.innerWidth,
                            height: $window.innerHeight
                        };
                    }
                });

                // глобальные настройки для select2
                angular.extend(uiSelect2Config, {
                    openOnEnter: false,
                    formatLoadMore: function () {
                        return $filter('translate')('common.select.formatLoadMore');
                    },
                    formatSearching: function () {
                        return $filter('translate')('common.select.formatSearching');
                    },
                    formatNoMatches: function () {
                        return $filter('translate')('common.select.formatNoMatches');
                    },
                    formatSelection: $filter('nameFormatter'),
                    formatResult: $filter('nameFormatter'),
                    placeholder: $filter('translate')('common.placeholder.notSelected'),
                    allowClear: true,
                    initSelection: function (element) {
                        return $(element).data('$ngModelController') ? $(element).data('$ngModelController').$modelValue : null;
                    }
                });

            }
        ]);

    var UserDataResource = angular.injector(['app.rest']).get('UserDataResource');
    UserDataResource.query({
            projection: "user"
        },
        function (data) {
            angular.element(document).ready(function () {
                var $inj = angular.bootstrap(document, ['app']);
                var $rootScope = $inj.get("$rootScope");

                $rootScope.user = {
                    name: data.taUserInfo.user.name,
                    login: data.taUserInfo.user.login,
                    department: data.department,
                    permissions: data.taUserInfo.user.permissions,
                    roles: data.taUserInfo.user.roles,
                    hasRole: function (role) {
                        var roleAliasList = data.taUserInfo.user.roles.map(function (userRole) {
                            return userRole.alias;
                        });
                        return roleAliasList.indexOf(role) >= 0;
                    }
                };
                $rootScope.permissionChecker = angular.injector(['app.permissionUtils']).get('PermissionChecker');
                $rootScope.validationUtils = angular.injector(['app.validationUtils']).get('ValidationUtils');
                $rootScope.APP_CONSTANTS = angular.injector(['app.constants']).get('APP_CONSTANTS');
            });
        });

    /**
     * @description Поиск по нажатию на enter
     */
    window.addEventListener("keydown", function (event) {
        if (event.keyCode === 13) {
            //TODO: (dloshkarev) Нужна нормальная реализация не завязанная на CSS-селекторы
            var tableFilter = $(event.target).closest(".grid-filter");
            var isModal = $(event.target).closest(".modal-body").length != 0;
            var isTable = tableFilter.length != 0;

            if (!isModal && isTable) {
                var buttonSearch = tableFilter.find("#searchButton");
                if (buttonSearch) {
                    event.preventDefault();
                    event.stopPropagation();
                    buttonSearch.click();
                }
            }
        }
    });
}());