(function () {
    'use strict';
    /**
     * @description Модуль реализует обмен с сервером по стандартам REST.
     * Требуется jsGrid для запроса данных через options.angularResource,
     * а также используются методы ресурса для создания/изменения сущностей.
     *
     * Каждый ресурс по умолчанию содержит следующие HTTP-методы:
     * { 'get':   {method:'GET'},
     *  'save':   {method:'POST'},
     *  'query':  {method:'GET', isArray:true},
     *  'remove': {method:'DELETE'},
     *  'delete': {method:'DELETE'} };
     */
    angular.module('app.rest', ['ngResource'])
    /**
     * @description Конфигурация
     */
        .factory('ConfigResource', ['$resource', function ($resource) {
            return $resource('controller/rest/config', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])
        /**
         * @description Данные о пользователе
         */
        .factory('UserDataResource', ['$resource', function ($resource) {
            return $resource('controller/rest/userData', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])
        /**
         * @description Оповещения
         */
        .factory('NotificationResource', ['$resource', function ($resource) {
            return $resource('controller/rest/notification?projection=:projection', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])
        /**
         * @description Данные НДФЛ по физлицу
         */
        .factory('NdflPersonResource', ['$resource', function ($resource) {
            return $resource('controller/rest/ndflPerson?projection=:projection', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])
        /**
         * @description Декларация
         */
        .factory('DeclarationDataResource', ['$resource', function ($resource) {
            return $resource('controller/rest/declarationData/:declarationDataId?projection=:projection', {}, {
                query: {method: 'GET', isArray: false, cache: false},
                querySource: {method: 'GET', isArray: true, cache: false}
            });
        }])
        /**
         * @description Уведомления
         */
        .factory('LogEntryResource', ['$resource', function ($resource) {
            return $resource('controller/rest/logEntry/:uuid?projection=:projection', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])
        /**
         * @description Файлы и комментарии
         */
        .factory('FilesCommentsResource', ['$resource', function ($resource) {
            return $resource('controller/rest/declaration/filesComments?projection=:projection', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])

        /**
         * @description Справочники
         */
        .factory('RefBookValuesResource', ['$resource', function ($resource) {
            return $resource('controller/rest/refBookValues/:refBookId', {}, {
                query: {method: 'GET', isArray: true, cache: false}
            });
        }])

        /**
         * @description Поиск лиц для формирования персонального РНУ НДФЛ по физическому лицу
         */
        .factory('RnuPerson', ['$resource', function ($resource) {
            return $resource('controller/rest/getListPerson/rnuPerson/:rnuPersons?projection=:projection', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])


        /**
         * @description Формирование рну ндфл
         */
        .factory('RnuPersonDocument', ['$resource', function ($resource) {
            return $resource('/actions/declarationData/declarationDataId}/rnuDoc?projection=:projection', {}, {
                query: {method: 'GET', isArray: false, cache: false},
                querySource: {method: 'GET', isArray: true, cache: false}
            });
        }])

        /**
         * @description Поиск видов налоговых форм для создания формы. Ищутся назначенные подразделению виды форм
         * с действующей на момент начала периода версией шаблона формы указанного типа.
         */
        .factory('DeclarationTypeForCreateResource', ['$resource', function ($resource) {
            return $resource('controller/rest/refBook/207/activeAndAssigned', {}, {
                query: {
                    method: 'GET', isArray: true, cache: false
                }
            });
        }])

        /**
         * @description Поиск лиц для формирования персонального РНУ НДФЛ по физическому лицу
         */
        .factory('RnuPerson', ['$resource', function ($resource) {
            return $resource('controller/rest/getListPerson/rnuPerson/:rnuPersons?projection=:projection', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])

        /**
         * @description Формирование рну ндфл
         */
        .factory('RnuPersonDocument', ['$resource', function ($resource) {
            return $resource('/actions/declarationData/declarationDataId}/rnuDoc?projection=:projection', {}, {
                query: {method: 'GET', isArray: false, cache: false},
                querySource: {method: 'GET', isArray: true, cache: false}
            });
        }])

        /**
         * @description Планировщик задач - Список задач
         */
        .factory('schedulerTaskResource', ['$resource', function ($resource) {
            return $resource('controller/rest/schedulerTask', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])
        /**
         * @description Общие параметры
         */
        .factory('CommonParams', ['$resource', function ($resource) {
            return $resource('controller/rest/fetchCommonParams', {}, {
                query: {method: 'GET', isArray: true, cache: false}
            });
        }])

        /**
         * @description Блокировки
         */
        .factory('lockDataResource', ['$resource', function ($resource) {
            return $resource('controller/rest/locks', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])

      /**
       *  @description Редактируемая задача планировщик
       * */
      .factory('updateScheduleTask', ['$resource', function ($resource) {
        return $resource('controller/rest/updateSchedulerTask?idTaskScheduler=:idTaskScheduler', {}, {
            query: {method: 'GET', isArray: false, cache: false}
        });
      }])

        /**
         * @description Асинхронные задачи
         */
        .factory('asyncTaskResource', ['$resource', function ($resource) {
            return $resource('controller/rest/async', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])


        .factory('ReportPeriodResource', ['$resource', function ($resource) {
            return $resource('controller/rest/reportPeriods?projection=:projection', {}, {
                query: {method: 'GET', isArray: false, cache: false},
                delete: {method: 'DELETE', isArray: false, cache: false}
            });
        }])

        .factory('DepartmentResource', ['$resource', function ($resource) {
            return $resource('controller/rest/getBankDepartment', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])

        .factory('DepartmentGridResource', ['$resource', function ($resource) {
            return $resource('controller/rest/getDepartments', {}, {
                query: {method: 'GET', isArray: true, cache: false}
            });
        }])

        .factory('ReportPeriodTypeResource', ['$resource', function ($resource) {
            return $resource('controller/rest/refBookValues/reportPeriodTypeById', {}, {
                query: {method: 'GET', isArray: false, cache: false}
            });
        }])

        /**
         * @description Принятие НФ
         */
        .factory('acceptDeclarationData', ['$resource', function ($resource) {
            return $resource('controller/rest/declarationData/:declarationDataId/accept', {declarationDataId: '@declarationDataId'}, {
                query: {method: 'POST'}
            });
        }])

        /**
         * @description Запуск асинхронной задачи по созданию отчетности
         */
        .factory('createReport', ['$resource', function ($resource) {
            return $resource('controller/rest/createReport', {}, {
                query: {
                    method: 'POST'
                }
            });
        }])

        /**
         * @description Получить изображение страницы pdf отчета
         */
        .factory('getPageImage', ['$resource', function ($resource) {
            return $resource('controller/rest/declarationData/:declarationDataId/pageImage/:pageId/ndfl',
                {declarationDataId: '@declarationDataId', pageId: '@pageId'}, {
                    query: {
                        method: 'GET', isArray: false,
                        interceptor: {
                            response: function (response) {
                                response.requestUrl = response.config.url;
                                return response;
                            }
                        }
                    }
                });
        }])

        /**
         * @description ПРоверить НФ
         */
        .factory('checkDeclarationData', ['$resource', function ($resource) {
            return $resource('controller/rest/declarationData/:declarationDataId/check', {declarationDataId: '@declarationDataId'}, {
                query: {method: 'POST'}
            });
        }])

        /**
         * @description Вернуть в создана
         */
        .factory('moveToCreatedDeclarationData', ['$resource', function ($resource) {
            return $resource('controller/rest/declarationData/:declarationDataId/moveToCreated', {declarationDataId: '@declarationDataId'}, {
                query: {method: 'POST'}
            });
        }])

        /**
         * @description Подготовить данные для спецотчета
         */
        .factory('prepareSpecificReport', ['$resource', function ($resource) {
            return $resource('controller/rest/declarationData/prepareSpecificReport', {}, {
                doOperation: {method: 'POST'}
            });
        }])
    ;
}());