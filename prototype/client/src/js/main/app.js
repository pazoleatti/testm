(function() {
    'use strict'

    var translateDictionary = {}

    angular
        .module('mtsUsim', [
            'mtsUsim.header',
            'mtsUsim.refBook',
            'mtsUsim.productionPlanning',
            'mtsUsim.comments',
            'mtsUsim.filterDirectives',
            'pascalprecht.translate',
            'aplana.overlay',
            'aplana.alert',
            'aplana.modal',
            'aplana.utils',
            'aplana.entity-utils',
            'aplana.submitValid',
            'aplana.dropdownMenu',
            'aplana.collapse',
            'ui.router',
            'ui.validate',
            'dialogs.main',
            'aplana.dialogs',
            'ngMessages',
            'angularFileUpload',
            'mtsUsim.Constants'
        ])
        .config(['$stateProvider', '$urlRouterProvider', '$translateProvider',
            function($stateProvider, $urlRouterProvider, $translateProvider) {
                // Указание страницы по умолчанию
                $urlRouterProvider.otherwise('/')
                // Настройка обработчика главной страницы
                $stateProvider
                    .state('/', {
                        url: '/',
                        templateUrl: 'js/main/app.html'
                    })

                // Настройка источника локализованных сообщений
                $translateProvider.translations('ru', translateDictionary)
                $translateProvider.preferredLanguage('ru')
                $translateProvider.useSanitizeValueStrategy('sanitizeParameters')
                // Добавляем форматирование дат
                Date.prototype.format = function (mask, utc) {
                    return dateFormat(this, mask, utc);
                };
                // Добавляем возможность прибавления дней к дате
                Date.prototype.addDays = function(days)
                {
                    var dat = new Date(this.valueOf());
                    dat.setDate(dat.getDate() + days);
                    return dat;
                }
            }
        ])

    // Получение информации о текущем пользователе и запуск приложения
    var initInjector = angular.injector(['ng']);
    var $http = initInjector.get('$http')

    //TODO: (dloshkarev) временная заглушка
    var userDataStub = {
        user: {id: 6, uid: "mkoposov", firstName: "Михаил", lastName: "Копосов", roles: [{role: {name: 'Администратор'}}], fullShortName: 'Михаил Копосов'},
        role: {id: 1168, name: "Менеджер МР", active: true},
        authorities: ['PRODUCTION_PLAN_READ', 'REF_BOOK_GUI', 'PRODUCTION_PLAN_CREATE'],
        macroRegion: null,
        paging: "20"
    };
    translateDictionary = {"0":"Удаленный сервер недоступен","400":"Сервер обнаружил в запросе клиента синтаксическую ошибку (400)","401":"Неавторизованный доступ (401)","403":"Доступ к ресурсу запрещен (403)","404":"Не найдено. Сервер не может найти данные (404)","500":"Внутренняя серверная ошибка (500)","503":"Сервис недоступен (503)","12029":"Удаленный сервер недоступен","common.button.send":"Направить","iccidPrefixLabel":"ICCID (начало)","waybillNumberLabel":"Номер накладной","common.button.unloadTechFiles":"Выгрузить тех.файлы","common.button.onDelete":"На удаление","profileFilterLabel":"Электронный профиль:","error.import.incorrectPattern":"Импортируемый файл не соответствует шаблону","searchData":"Найти","error.form.string.maxLength":"Количество символов не более","editRecord":"Редактировать","monthFilterLabel":"Месяц:","admin.user.middleName":"Отчество:","common.button.continue":"Продолжить","common.button.unloadMR":"Выгрузить МР","editApprovedOrder":"Редактирование утвержденного заказа-исполнения региона","addNewRecord":"Добавить","entity.ForecastDetails":"Детализация прогнозов по МР","prefixLabel":"Начало","orderExecRegion":"Заказы-исполнения регионов","Implementation-Version":"2.1.8","!":"Для локализации на другой язык создать копию файла и переименовать","forecastGeneral":"Сводные прогнозы","entity.MacroRegion":"Макрорегионы","error.form.number.min":"Значение не менее","common.button.backToList":"К общему списку","tariffTypeFilterLabel":"Тип тарифного плана:","admin.role.section":"Раздел:","saveRecord":"Сохранить","macroRegionFilterLabel":"Макрорегион:","iccidPostfixLabel":"ICCID (конец)","DIALOGS_ERROR_MSG":"Произошла непредвиденная ошибка.","filterPanelLabel":"Фильтр","weekFilterLabel":"Неделя:","common.button.upload":"Загрузить","estimateIncomingDateFilter":"План. дата прихода с:","numberOrder":"№ Заказа:","entity.CardType":"Типы карт","admin.user.lastName":"Фамилия:","admin.notifications.user":"Пользователь:","commentLabel":"Комментарий:","filter.label.tariffType":"Тип тарифного плана","shipmentActualDateFilterLabel":"Дата отгрузки c:","DIALOGS_CONFIRMATION":"Подтверждение","DIALOGS_REJECTED":"Отказать","forecastsChooseExcelFile":"Выберите Excel-файл для загрузки","DIALOGS_NOTIFICATION_MSG":"Неопределенное уведомление.","deliveryWeekFilterLabel":"Неделя поставки:","hlrFilterLabel":"HLRId:","sync":"Синхронизировать","publish":"Опубликовать","common.button.import.xls":"Импортировать","common.button.edit":"Редактировать","userManagement":"Управление пользователями","exportData":"Экспортировать","entity.Price":"Цена комплекта","admin.user.firstName":"Имя:","common.button.default":"Восстановить значения по умолчанию","production.planning.deliveryDate":"Фактическая:","DIALOGS_OK":"Применить","orderExec":"Заказы-исполнения макрорегионов","editRegionProductionOrder":"Редактирование HLR и электронного профиля","reportForecastTitle":"Остатки по прогнозам","error.form.deleted":"Заблокированная запись","entity.Admin":"Администрирование","common.button.notified":"Оповещен","common.button.approveMR":"Согласовать МР","macroRegionProductionOrders":"Заказы на производство макрорегионов","common.button.import":"Загрузить","filter.label.year":"Год","commentRecord":"Комментировать","common.input.date.current":"Сегодня","filterLabel":"Поиск:","common.button.done":"Выполнен","сorrectionLabel":"Корректирован:","filter.label.orderNumber":"№ заказа","entity.Code":"Коды","DIALOGS_PERCENT_COMPLETE":"% завершено","DIALOGS_PLEASE_WAIT_MSG":"Ожидание выполнения операции.","actualDate":"Фактическая","Git-Revision":"48394e5","filter.label.price":"Цена за один комплект","admin.user.mobileNumber":"Мобильный:","filter.label.profile":"Электронные профили","settings":"Настройки","DIALOGS_CLOSE":"Закрыть","error.form.length":"В значении поля ввода введено некорректное количество символов (не более 255)","production.planning.date":"Дата отгрузки","error.form.pattern":"Значение содержит некорректные символы","DIALOGS_NO":"Нет","amount":"Количество","historyDateFromFilterLabel":"Дата изменения с:","common.button.rejectMR":"Отказать МР","reportTechFiles":"Отчет по генерации тех. файлов","approvedOrderExec":"Утвержденные заказы-исполнения регионов","entity.Region":"Регионы и адреса доставки","shipmentDateLabel":"Дата отгрузки","DIALOGS_CONTINUE":"Продолжить","Manifest-Version":"1.0","common.button.delivered":"Доставлен","production.macroRegionOrderDetails.formationDate":"Дата формирования:","postfixLabel":"Конец","Created-By":"1.7.0_09 (Oracle Corporation)","remainsDetails":"Детализация отчета по остаткам","distributorFilterLabel":"Поставщик:","actualIncomingDateFilter":"Факт. дата прихода с:","common.button.export":"Экспортировать","theNumberOfSets":"Количество комплектов:","common.button.claim":"Претензия","fileManagementDetails":"Управление файлами заказа на производство региона","statusChangesFilterLabel":"Изменение статусов:","DIALOGS_PLEASE_WAIT":"Пожалуйста, подождите","common.dialog.comment.header":"Добавление комментария","refuse":"Отказать","admin.user.status":"Статус:","filter.label.dateFrom.attach":"Дата прикрепления с:","production.macroRegionOrderDetails.title":"Заказ на производство","common.button.copy":"Скопировать","consolidatedRemains":"Сводные остатки по макрорегионам","common.button.deleteLNK":"Удалить LNK","common.button.production":"В производство","common.button.open":"Открыть","common.button.no":"Нет","DIALOGS_ERROR":"Ошибка","statusFilterLabel":"Статус:","quartalFilterLabel":"Квартал:","shipmentEstimateDateFilterLabel":"План. дата отгрузки с:","common.button.uploadLNK":"Загрузить LNK","admin.user.phoneNumber":"Телефон:","regionForecastsHeaderCreate":"Создание прогноза региона","amountFilterLabel":"Количество:","common.button.comment":"Комментировать","common.button.point":"Указать","filter.label.transitionDate":"Дата перехода","createRole":"Создание роли","common.button.approveBER":"Согласовать КЦ","cardFilterLabel":"Тип карты:","entity.AuthAlgorythm":"Алгоритмы аутентификации","Git-Date":"04.05.2017 19:17:19","reportOverdue":"Отчет по просрочкам","production.planning.button.pointReadyDate":"Указать готовность","common.button.confirm":"Подтвердить","theNumberOfSelectedItems":"Выбрано значений:","estimateDate":"Планируемая","editUser":"Редактирование пользователя","filter.label.status":"Статус","filter.label.cardType":"Тип карты","production.planning.commonInfo":"Общая информация","common.button.forwardMR":"Отправить МР","planDateFromFilterLabel":"План. дата с:","common.button.add":"Добавить","DIALOGS_YES":"Да","error.form.string.minLength":"Количество символов не менее","entity.Specification":"Спецификации продуктов","productionShipmentEdit":"Редактирование параметров реестра отгрузок","admin.user.email":"E-mail:","common.button.reject":"Отказать","yearsFilterLabel":"Год:","statusToFilterLabel":"Конечный статус:","algoFilterLabel":"Алгоритм аутентификации:","admin.role.hasMacroregion":"Возможен выбор макрорегиона:","common.input.date.close":"Выбрать","entity.Tariff":"Тарифные планы","entity.Forecast":"Прогнозы","vendorFilterLabel":"Производитель:","common.button.onRoute":"На маршруте","common.button.delete":"Удалить","DIALOGS_PUBLISH":"Опубликовать","forecastsChooseFile":"Выберите файл для загрузки","fileNameLabel":"Файл:","inputFilterHint":"Введите строку","error.planning.noReqDate":"Невозможно добавить плановую дату отгрузки т.к. отсутствует требуемая дата отгрузки","tariffFilterLabel":"Тарифный план:","filter.label.tariff":"Тарифный план","orderNumberFilterLabel":"№ заказа:","common.button.change":"Изменить","common.button.prev":"◀","refbookTitle":"Справочник","editRole":"Редактирование роли","DIALOGS_CONFIRMATION_MSG":"Требуется подтвеждение.","queryToCorrection":"Запросить","filter.label.dateFrom":"с","filter.label.fileNumber":"№ файла","common.button.split":"Разделить","admin.user.type":"Пользователь:","orderMR":"Заказ-исполнения макрорегиона","error.import.incorrectFormat":"Импортируемый файл не соответствует формату","orderFilterLabel":"Заказы:","common.input.date.clear":"Очистить","lockRecord":"Заблокировать","remainsReports":"Отчеты по остаткам","error.import":"Ошибка импорта","forecastGeneralHeader":"Сводная таблица прогнозов","common.button.save":"Сохранить","entity.Order":"Заказы-исполнения","attachFile":"Прикрепить файл","statusFromFilterLabel":"Начальный статус:","historyDateChangesFilterLabel":"Дата изменения:","deletedStatus":"Включая заблокированные","reportFiles":"Отчет по работе с файлами","productionShipment":"Реестр отгрузок","error.form.number.integer":"Значение поля должно быть целым числом","entity.Production":"Производство","common.button.run":"Выполнить","error.form.number.max":"Значение не более","entity.Vendor":"Поставщики","regionFilterLabel":"Регион:","DIALOGS_NOTIFICATION":"Уведомление","productionPlanning":"Планирование","production.planning.reqDeliveryDate":"Срок сдачи готовой продукции:","common.button.attach":"Прикрепить","production.planning.readyDate":"Дата готовности комплекта:","entity.RemainsDetails":"Детализация остатков по МР","common.error.message.caption.stackTrace":"Ошибка","clearFilter":"Сбросить","cancelRecord":"Отменить","productionLogging":"Логирование групповой загрузки","reconcile":"Согласовать","importData":"Импортировать","DIALOGS_CANCEL":"Отменить","reportForecast":"Отчет по прогнозам","common.button.further":"Далее","return":"Вернуть","userFilterLabel":"Пользователь:","common.button.attachLNK":"Прикрепить LNK","unknown":"Неизвестная ошибка (error)","common.button.choose":"Выбрать","Build-Date":"10.05.2017 11:45:01","common.button.redefine":"Переформировать","unlockRecord":"Разблокировать","filter.label.storageType":"Тип склада","error.planning.planDate":"Недопустимое значение поля поля \"Планируемая дата отгрузки\"","common.dialog.comment.textarea.label":"Добавить комментарий","productionOrderCreate":"Создание заказа на производство","common.button.yes":"Да","filter.label.region":"Регион","entity.TransportCompany":"Транспортные компании","production.macroRegionOrderDetails.price":"Цена за комплект:","admin.role.name":"Название роли:","editMacroRegionProductionOrder":"Редактирование заказа на производство","common.button.redistribute":"Перераспределить","DIALOGS_QUERY":"Запросить","consolidatedForecastsHeaderSplit":"Разделение прогноза региона","consolidatedForecastsHeaderCreate":"Назначение поставщика","common.button.next":"▶","transportCompanyFilterLabel":"Транспортная компания","entity.RefBooks":"Справочники","filter.label.dateTo":"по","filter.label.dateFrom.change":"Дата перехода в статус Прикрепление файлов","error.form.date":"Неправильно указана дата","entity.Reports":"Отчеты","filter.label.month":"Месяц","filter.label.fileName":"Имя файла","admin.user.vendor":"Поставщик:","reportProduct":"Отчет по продукту","common.button.repeal":"Аннулировать","common.error.message.seemore":"Подробнее","admin.user.uid":"UID:","filter.label.hlr":"HLR:","common.button.create":"Создать","entity.Delivery":"Сроки доставки","DIALOGS_RETURN":"Вернуть","reportProductionDate":"Срок исполнения процесса","vendorDistribution":"Распределение по поставщикам","DIALOGS_PLEASE_WAIT_ELIPS":"Подождите...","createUser":"Создание пользователя","notifications":"Рассылка уведомлений","rolesFilterLabel":"Роль:","reconcileKC":"Согласовать КЦ","entity.Profile":"Электронные профили","common.button.assign":"Назначить","Implementation-Title":"MTS USIM","admin.user.jobTitle":"Должность:","common.button.close":"Закрыть","forecastMR":"Прогнозы макрорегионов","common.button.restore":"Восстановить","common.button.query":"Запросить","estimateIncomingDate":"Планируемая дата прихода","error.form.required":"Обязательное поле","forecastsHeaderCreate":"Создание прогноза макрорегиона","admin.notifications.tab":"Раздел настроек","common.button.unload":"Выгрузить","iccidLabel":"ICCID (база)","commentsLabel":"Комментарии:","id":"ID:","Implementation-Vendor":"Aplana (www.aplanadc.ru)","error.form.pattern.format":"Значение не соответствует формату","DIALOGS_APPROVE":"Согласовать","distribute":"Распределить","DIALOGS_DELETE":"Удалить","numberOfPiecesLabel":"Количество мест","admin.role.filter":"Фильтр по-умолчанию:","common.button.makeFnsFiles":"Сформировать","common.button.return":"Вернуть","dateToFilterLabel":"по:","entity.Remains":"Остатки","production.planning.priorityDate":"Приоритет (дата):","admin.role.status":"Статус:","filter.label.vendor":"Поставщик","DataDSA":"Данные DSA","filter.label.search":"Поиск","productionOrder":"Заказы на производство","DIALOGS_SAVE":"Сохранить","admin.settings.tab":"Тип настроек","admin.user.label.addRole":"Добавить права","fileManagement":"Управление файлами заказов на производство регионов","fileNameFilterLabel":"Имя файла:","filter.placeholder.search":"Введите строку","production.macroRegionOrderDetails.shipmentDate":"Требуемая дата отгрузки:","forecastDetails":"Прогноз макрорегиона:","common.button.search":"Найти","common.button.forward":"Отправить","reconcileMR":"Согласовать МР","baseLabel":"База","common.button.back":"Назад","common.button.cancel":"Отменить","readUser":"Просмотр информации пользователя","entity.Hlr":"HLR","exceedPagingSize":"Количество полученных строк с сервера превышает ограничение количества строк на странице","production.planning.planDeliveryDate":"Планируемая:","filter.label.week":"Неделя","roleManagement":"Управление ролями пользователей","common.button.clear":"Сбросить","filter.placeholder.all":"-Все-","statusLabel":"Статус:","orderDetailsSplit":"Разделение заказа-исполнения региона","filter.label.macroRegion":"Макрорегион","returnCorrection":"На корректировку"};
    angular.module('userData', []).constant('USER_DATA', userDataStub);
    angular.element(document).ready(function() {
        angular.bootstrap(document, ['mtsUsim'])
    });
    /*$http.get('rest/service/configService/getConfig').then(
         function (response) {
             translateDictionary = response.data.translate
             angular.module('userData', []).constant('USER_DATA', response.data.user_data)
             angular.element(document).ready(function() {
             angular.bootstrap(document, ['mtsUsim'])
         })
         }
     )*/

    /**
     * Поиск по нажатию на enter
     */
    window.addEventListener("keydown", function(event) {
        if (event.keyCode == 13){
            event.preventDefault();
            event.stopPropagation();
            var doc = $(event.target).closest(".grid-filter");

            if (doc){
                var buttonSearch = doc.find("#searchButton");
                if (buttonSearch){
                    buttonSearch.click();
                }
            }
        }
    });
}());

