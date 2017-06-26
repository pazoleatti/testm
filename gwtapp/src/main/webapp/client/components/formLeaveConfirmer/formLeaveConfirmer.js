(function () {
    "use strict";
    /**
     * @description Модуль для работы с блокировками выхода из режима редактирования при наличии несохраненных измененеий
     * Запросы будут выдавать при попытке смены состояния и при попытке закрыть/обновить страницу
     * Если при смене режима просмотр/редактирование не происходит смена состояни - запрос не отобразиться
     * Его можно вызвать вручную методом сервиса askSaveChanges
     */
    angular.module('aplana.formLeaveConfirmer', [])
        .factory('FormLeaveConfirmer', ['$rootScope', '$state', '$window', '$filter', 'dialogs', function ($rootScope, $state, $window, $filter, dialogs) {
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
                 * @description Метод инициализации сервиса.
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

                    /**
                     * @description Чтобы это срабатывало, необходимо создавать сообщение броадкастом $rootScope.$broadcast('LOGOUT_MSG');
                     */
                    logoutListener = $rootScope.$on('LOGOUT_MSG', function () {
                        service.unlockAction();
                        $window.onunload = null;
                    });

                    if (stateChangeStartListener) {
                        stateChangeStartListener();
                    }
                    stateChangeStartListener = $rootScope.$on('$stateChangeStart', function (event, toState, toParams) {
                        if (!service.$locationChangeConfirmed) {
                            //noinspection JSUnresolvedFunction
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
                 * @description Метод вызова диалога с подтверждением перехода
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

                        var opts = {
                            size: 'md'
                        };

                        var dlg = dialogs.confirm(service.title(), service.message(), buttons, opts);
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
                 * @description Метод для очищения контекста сервиса
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
                }
            };

            return service;
        }]);
}());