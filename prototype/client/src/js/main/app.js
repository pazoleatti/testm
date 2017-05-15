(function () {
    'use strict'

    var translateDictionary = {}

    angular
        .module('sbrfNdfl', [
            'app.header',
            'sbrfNdfl.refBook',
            'sbrfNdfl.ndflForms',
            'sbrfNdfl.ndflDetailsForms',
            'sbrfNdfl.filterDirectives',
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
            'sbrfNdfl.Constants'
        ])
        .config(['$stateProvider', '$urlRouterProvider', '$translateProvider',
            function ($stateProvider, $urlRouterProvider, $translateProvider) {
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
                Date.prototype.addDays = function (days) {
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
        user: {
            id: 6,
            uid: "mkoposov",
            firstName: "Михаил",
            lastName: "Копосов",
            roles: [{role: {name: 'Администратор'}}],
            fullShortName: 'Михаил Копосов'
        },
        role: {id: 1168, name: "Менеджер МР", active: true},
        authorities: ['PRODUCTION_PLAN_READ', 'REF_BOOK_GUI', 'PRODUCTION_PLAN_CREATE'],
        macroRegion: null,
        paging: "20"
    };
    translateDictionary = {
        "0": "Удаленный сервер недоступен",
        "400": "Сервер обнаружил в запросе клиента синтаксическую ошибку (400)",
        "401": "Неавторизованный доступ (401)",
        "403": "Доступ к ресурсу запрещен (403)",
        "404": "Не найдено. Сервер не может найти данные (404)",
        "500": "Внутренняя серверная ошибка (500)",
        "503": "Сервис недоступен (503)",
        "12029": "Удаленный сервер недоступен",
        "common.button.search": "Найти",
        "common.button.clear": "Сбросить",
        "filter.placeholder.all": "-Все-",
        "filterPanelLabel": "Фильтр",
        "filter.placeholder.search": "Введите строку",
        "theNumberOfSelectedItems": "Выбрано значений:",
        "common.button.add": "Добавить",
        "common.button.cancel": "Отменить",
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

        "menu.taxes": "Налоги",
        "menu.taxes.ndfl": "НДФЛ",
        "menu.taxes.ndfl.details": "НДФЛ детализация",
        "menu.taxes.ndfl.forms": "Формы",

        "header.ndfl.forms": "НДФЛ - Список налоговых форм",
        "header.ndflDetails.forms": "РНУ НДФЛ",
        "header.ndfl.fl.create": "Добавить запись ФЛ",
        "header.ndfl.fl.edit": "Редактировать запись ФЛ",

        "title.period": "Период",
        "title.department": "Подразделение",
        "title.formNumber": "Номер формы",
        "title.formType": "Тип налоговой формы",
        "title.formKind": "Вид налоговой формы",
        "title.asnu": "Наименование АСНУ",
        "title.nameAsnu" : "АСНУ",
        "title.state": "Состояние",
        "title.file": "Файл",
        "title.creator": "Создал",
        "title.inp": "ИНП",
        "title.idOperation": "ID операции",
        "title.kpp": "КПП",
        "title.oktmo": "ОКТМО",
        "title.snils": "СНИЛС",
        "title.inn": "ИНН",
        "title.numberDul": "№ ДУЛ",
        "title.surname": "Фамилия",
        "title.name": "Имя",
        "title.patronymic": "Отчество",
        "title.dateOfBirthFrom": "Дата рождения c",
        "title.dateFromFilterLabel": "Дата изменения c",
        "title.dateToFilterLabel": "по",
        "title.taxpayer": "Налогоплательщик",
        "title.dateOfBirth": "Дата рождения",
        "title.citizenship": "Гражданство (код страны)",
        "title.statusCode": "Статус (код)",
        "title.document": "Документ, удостоверяющий личность",
        "title.document.code": "Код",
        "title.document.number": "Номер",
        "title.registrationAddress": "Адрес регистрации в РФ",
        "title.innRF": "ИНН в РФ",
        "title.innINO": "ИНН в ИНО",
        "title.subject": "Субьект",
        "title.area": "Район",
        "title.city": "Город",
        "title.locality": "Населенный пункт",
        "title.street": "Улица",
        "title.index": "Индекс",
        "title.building": "Дом",
        "title.housing": "Корпус",
        "title.apartment": "Квартира",

        "button.create": "Создать",
        "button.check": "Проверить",
        "button.calculate": "Расчитать",
        "button.delete": "Удалить",
        "button.accept": "Принять",
        "button.return": "Вернуть в создана",
        "button.add": "Добавить",
        "button.save": "Сохранить",
        "button.edit": "Редактировать"
    };
    angular.module('userData', []).constant('USER_DATA', userDataStub);
    angular.element(document).ready(function () {
        angular.bootstrap(document, ['sbrfNdfl'])
    });
    /*$http.get('rest/service/configService/getConfig').then(
     function (response) {
     translateDictionary = response.data.translate
     angular.module('userData', []).constant('USER_DATA', response.data.user_data)
     angular.element(document).ready(function() {
     angular.bootstrap(document, ['sbrfNdfl'])
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