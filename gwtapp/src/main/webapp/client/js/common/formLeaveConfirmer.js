(function () {
    "use strict";
    /**
     * Модуль для работы с блокировками выхода из режима редактирования при наличии несохраненных измененеий
     * Запросы будут выдавать при попытке смены состояния и при попытке закрыть/обновить страницу
     * Если при смене режима просмотр/редактирование не происходит смена состояни - запрос не отобразиться
     * Его можно вызвать вручную методом сервиса askSaveChanges
     */
    angular.module('app.formLeaveConfirmer', ['app.formUtils'])
        .factory('FormLeaveConfirmer', ['$rootScope', '$state', '$window', '$filter', 'dialogs', 'FormUtils', function ($rootScope, $state, $window, $filter, dialogs, FormUtils) {
            var getParameterValue = function (parameter) {
                return angular.isFunction(parameter) ? parameter() : parameter;
            };

            //Обработчик события window beforeunload
            var unloadHandler = function () {
                if (service.isModified()) {
                    return service.message();
                } else {
                    return undefined;
                }
            };
            var stateChangeStartListener, logoutListener;
            var listenersStack = [];

            var service = {
                $locationChangeConfirmed: false,
                /**
                 * Метод инициализации сервиса.
                 * Добавляет обработчики событий и инициализирует свой контекст
                 * @param isModified        метод для определения наличия изменений на форме
                 * @param title             строка или метод для заголовка диалогового окна
                 * @param message           строка или метод для получнеия содержимого окна подтверждения
                 * @param successCallBack   ф-ия, которая будет выполнена при смене состояния.
                 * @param unlockAction      ф-ия, которая должны быть выполнена до совершения перехода
                 *                          (например асинхранная разблокировка документа)
                 * @param modalInstance     экземпляр модального окна, на котором расположена форма
                 */
                initializeListeners: function (isModified, title, message, successCallBack, unlockAction, modalInstance) {
                    //кладем настройки в стэк
                    listenersStack.push({
                        isModified: isModified,
                        title: title,
                        message: message,
                        successCallBack: successCallBack,
                        unlockAction: unlockAction,
                        modalInstance: modalInstance
                    });

                    service.$locationChangeConfirmed = false;

                    service.modalInstance = function() {
                        return getParameterValue(modalInstance);
                    };
                    service.isModified = function () {
                        return getParameterValue(isModified);
                    };
                    service.title = function () {
                        return getParameterValue(title);
                    };
                    service.message = function () {
                        return getParameterValue(message);
                    };
                    service.successCallBack = function () {
                        return getParameterValue(successCallBack);
                    };
                    service.unlockAction = function () {
                        return getParameterValue(unlockAction);
                    };

                    if (modalInstance) {
                        modalInstance.clearFormLeaveListeners = service.clearListeners;
                    }

                    $(window).bind('beforeunload', unloadHandler);

                    $window.onunload = function () {
                        service.unlockAction();
                    };

                    //Подчищаем листнеры, если нужно
                    if (logoutListener) {
                        logoutListener();
                    }
                    logoutListener = $rootScope.$on('LOGOUT_MSG', function (event) {
                        service.unlockAction();
                        $window.onunload = null;
                    });

                    if (stateChangeStartListener) {
                        stateChangeStartListener();
                    }
                    stateChangeStartListener = $rootScope.$on('$stateChangeStart', function (event, toState, toParams) {
                        if (!service.$locationChangeConfirmed) {
                            event.preventDefault();

                            service.askSaveChanges(function () {
                                if (successCallBack) {
                                    successCallBack();
                                }
                                $state.go(toState, toParams);
                            }, unlockAction);
                        }
                    });
                },
                /**
                 * Метод вызова диалога с подтверждением перехода
                 * @param callback      ф-ия, которая будет вызвана при подтверждении перехода
                 * @param unlockAction  ф-ия, которая должна быть выполнена до совершения перехода (должна иметь один
                 * параметр - callback, например асинхранная разблокировка документа, сначала нужно разблокировать, и
                 * если все хорошо - остановить работу сервиса + выполнить callback из первого параметра)
                 * @param cancelCallback  ф-ия, которая будет вызвана при отказе от совершения перехода
                 */
                askSaveChanges: function (callback, unlockAction, cancelCallback) {
                    var successHandler = function () {
                        var handler = function () {
                            service.$locationChangeConfirmed = true;
                            service.clearListeners();

                            if (callback) {
                                callback();
                            }
                        };

                        if (unlockAction) {
                            unlockAction(handler);
                        } else {
                            handler();
                        }
                    };

                    if (service.isModified()) {
                        var buttons = {
                            labelYes: $filter('translate')('common.button.yes'),
                            labelNo: $filter('translate')('common.button.no')
                        };
                        var dlg = dialogs.confirm(service.title(), service.message(), buttons);
                        dlg.result.then(
                            function () {
                                successHandler();
                            },
                            function () {
                                if (cancelCallback) {
                                    cancelCallback();
                                }
                            });
                    } else {
                        successHandler();
                    }
                },
                /**
                 * Проверяет наличие неактуальных полей на форме и выдает сообщение при необходимости
                 * @param form      форма для проверки
                 * @param title     заголовок информационного сообщения
                 * @param message   текст информационного сообщения
                 * @param callback  действие, которое нужно выполнить при отсутсвии ошибок или подтверждении пользователя
                 */
                checkOutdated: function (form, title, message, callback) {
                    var errorInfo = FormUtils.hasOnlyActualizationErrors(form);

                    if (errorInfo.hasErrors && errorInfo.onlyThisKeyErrors) {
                        var buttons = {
                            labelYes: $filter('translate')('common.button.save'),
                            labelNo: $filter('translate')('common.button.no')
                        };
                        var dlg = dialogs.confirm(title, message, buttons);
                        dlg.result.then(
                            function () {
                                if (angular.isFunction(callback)) {
                                    callback();
                                }
                            },
                            function () {
                            });
                    }
                },
                /**
                 * Проверяет наличие ошибок связаных с ФИАС на форме и выдает сообщение при необходимости
                 * @param form      форма для проверки
                 * @param title     заголовок информационного сообщения
                 * @param message   текст информационного сообщения
                 * @param callback  действие, которое нужно выполнить при отсутсвии ошибок или подтверждении пользователя
                 */
                checkFiasAddrObj: function (form, title, message, callback) {
                    var errorInfo = FormUtils.hasOnlyFiasErrors(form);

                    if (errorInfo.hasErrors && errorInfo.onlyThisKeyErrors) {
                        var buttons = {
                            labelYes: $filter('translate')('common.button.save'),
                            labelNo: $filter('translate')('common.button.cancel')
                        };
                        var dlg = dialogs.confirm(title, message, buttons);
                        dlg.result.then(
                            function () {
                                if (angular.isFunction(callback)) {
                                    callback();
                                }
                            },
                            function () {
                            });
                    }
                },
                /**
                 * Проверяет наличие ошибок перечисленных в @param warningKeys
                 * @param form        форма для проверки
                 * @param title       заголовок информационного сообщения
                 * @param message     текст информационного сообщения
                 * @param callback    действие, которое нужно выполнить при отсутсвии ошибок или подтверждении пользователя
                 * @param warningKeys алиас или алиасы предупреждений
                 */
                checkWarning: function (form, title, message, callback, warningKeys) {
                    var errorInfo = FormUtils.hasOnlyKeyErrors(form, warningKeys);

                    if (errorInfo.hasErrors && errorInfo.onlyThisKeyErrors) {

                        var buttons = {
                            labelYes: $filter('translate')('common.button.save'),
                            labelNo: $filter('translate')('common.button.cancel')
                        };
                        var dlg = dialogs.confirm(title, message, buttons);
                        dlg.result.then(
                            function () {
                                if (angular.isFunction(callback)) {
                                    callback();
                                }
                            },
                            function () {
                            });
                    }
                },
                /**
                 * Метод для очищения контекста сервиса
                 * Нужно вызывать при нажатии на кнопку "Сохранить"
                 */
                clearListeners: function () {
                    service.$locationChangeConfirmed = false;

                    service.isModified = undefined;
                    service.title = undefined;
                    service.message = undefined;
                    service.successCallBack = undefined;

                    $(window).unbind('beforeunload', unloadHandler);
                    stateChangeStartListener();
                    logoutListener();

                    //удаляем свои настройки из стэка
                    listenersStack.pop();
                    //востанавливаем старые, если есть
                    if (listenersStack.length !== 0) {
                        var settings = listenersStack.pop();
                        service.initializeListeners(settings.isModified, settings.title, settings.message,
                            settings.successCallBack, settings.unlockAction);
                    }
                },
                /**
                 * Отсылает запрос на /. Нужно выполнять после синхронно запроса, чтобы при потере сессии он перешел на
                 * на корневую страницу, а не на адрес этого запроса
                 */
                logoutRedirect: function () {
                    $.ajax({
                        async: false,
                        type: "POST",
                        url: ""
                    });
                }
            };

            return service;
        }]);
}());