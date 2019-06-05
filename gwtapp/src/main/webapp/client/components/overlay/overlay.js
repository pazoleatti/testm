/**
 * aplana-overlay (Индикатор загрузки)
 * Модуль предназначени для показа индикатора загрузки
 * http://localhost:8080/#/aplana_overlay
 */
(function () {
    'use strict';
    angular.module('aplana.overlay', ['aplana.utils'])
        .provider('Overlay', [function () {
            //Задержка перед закрытием overlay. Необходим для объединения нескольких запросов в один показ индикатора загрузки
            var waiting_response_delay = 250;
            //Задержка перед показом overlay. Отсеиваются "короткие" запросы.
            var show_overlay_delay = 200;
            //задержка показа/скрытия затенения с индикатором загрузки
            var fade_delay = 0;

            // ERR_ADDRESS_UNREACHABLE, для chrome/firefox и ie
            var err_address_unreachable = ['0', '12029'];
            //очередь запросов
            var queue = 0;
            var showTimeout = null, hideTimeout = null;
            // использовать отложенную остановку
            var isHideDeferred = false;

            //кастомные обработчики ответа сервера
            //должны иметь метод handle, который вернет true, если они обработали ответ сервера и больше ничего делать не нужно
            var responseHandlers = [];
            //была ли уже открыто окно с ошибкой. Предотвращяет показ нескольких окон с ошибками
            var errorDialogOpened = false;

            //для прелоада изображений
            jQuery.preloadImages = function () {
                for (var i = 0; i < arguments.length; i++) {
                    jQuery("<img>").attr("src", arguments[i]);
                }
            };

            var parentOverlayElement = null;

            function tryGetParentOverlayElement() {
                if (parentOverlayElement) {
                    return parentOverlayElement;
                } else if (document.getElementById('overlay-parent') !== null) {
                    parentOverlayElement = $("#overlay-parent");
                    return parentOverlayElement;
                } else {
                    return null;
                }
            }

            //noinspection JSValidateJSDoc
            return {
                //настраиваем в конфигурационной функции
                setWaitingResponseDelay: function (delay) {
                    waiting_response_delay = delay;
                },

                setShowOverlayDelay: function (delay) {
                    show_overlay_delay = delay;
                },

                setFadeDelay: function (delay) {
                    fade_delay = delay;
                },

                setParentOverlayElement: function (parentOverlay) {
                    parentOverlayElement = parentOverlay;
                },

                $get: ['$rootScope', '$injector', '$timeout', '$filter', 'AplanaUtils', function ($rootScope, $injector, $timeout, $filter, AplanaUtils) {
                    return {
                        showAlertText: function (status, addMessage) {
                            //Вариант решения проблемы с circular dependency

                            var messageType;
                            //500 это значит во время обработки запроса на стороне сервера проихошла ошибка
                            //Сообщения в таком случае формируются в классе GlobalControllerExceptionHandler

                            //Определение - что выводить ошибку или предупреждение
                            if (/^(4..)$/.test(status) || /^(5..)$/.test(status) || status in err_address_unreachable) {
                                messageType = 'error';
                            }
                            else {
                                messageType = 'warning';
                            }

                            function errorDialog(message) {
                                if (!errorDialogOpened) {
                                    $injector.invoke(['$dialogs', function ($dialogs) {
                                        errorDialogOpened = true;
                                        $dialogs.errorDialog({
                                            content: message,
                                            closeBtnClick: function () {
                                                errorDialogOpened = false;
                                            }
                                        });
                                    }]);
                                }
                            }

                            function errorWithStack(message, addMessage) {
                                if (!errorDialogOpened) {
                                    $injector.invoke(['$dialogs', function ($dialogs) {
                                        errorDialogOpened = true;
                                        $dialogs.errorWithStack({
                                            message: message,
                                            addMessage: addMessage,
                                            windowClass: 'modal1000',
                                            closeBtnClick: function () {
                                                errorDialogOpened = false;
                                            }
                                        });
                                    }]);
                                }
                            }

                            if (addMessage && typeof addMessage === "string") {
                                errorDialog(addMessage);
                            } else if (status === '500' || status === '403') {
                                messageType = addMessage.messageType;
                                var message;
                                if (status === '403') {
                                    message = $filter('translate')(status);
                                } else if (addMessage.exceptionCause && addMessage.exceptionCause.length > 0) {
                                    //Достаем корневое сообщение из стека исключений
                                    message = addMessage.exceptionCause[0].message;
                                } else {
                                    message = $filter('translate')(addMessage.messageCode);
                                }
                                if (messageType === 'MULTI_ERROR' && addMessage.additionInfo.uuid) {
                                    //Отображаем уведомления по uuid и ошибку в окне
                                    $injector.invoke(['$logPanel', function ($logPanel) {
                                        $logPanel.open('log-panel-container', addMessage.additionInfo.uuid);
                                    }]);
                                    errorDialog(message);
                                } else if (status === '403' || messageType === "BUSINESS_ERROR") {
                                    // Бизнес-ошибка, стектрейс не нужен
                                    errorDialog(message);
                                } else {
                                    errorWithStack(message, addMessage);
                                }
                            }
                        },

                        showAlertByStatus: function (status, addMessage) {
                            if (!status) {
                                status = "unknown";
                            }
                            this.showAlertText(status, addMessage);
                        },

                        getOverlayQueueLength: function () {
                            return queue;
                        },

                        increaseOverlayQueue: function (num) {
                            queue = queue + num;
                        },

                        decreaseOverlayQueue: function (num) {
                            queue = queue - num;
                        },

                        setHideDeferred: function (_isHideDeferred) {
                            isHideDeferred = _isHideDeferred;
                        },

                        printOverlayQueue: function (startMsg) {
                            var msg = null;
                            if (startMsg) {
                                msg = startMsg + " ";
                            }
                            msg = msg + "queue: " + queue;
                            console.warn(msg);
                        },

                        /**
                         * Флаг isMemorize используется для указания запоминания старта.
                         * Флаг необходим для запуска оверлея в ручном режиме для того чтобы оверлей
                         * смог автоматом отключится при http или jQuery.ajax запросе
                         */
                        startOverlay: function (_fadeDelay, _showOverlayDelay, isMemorize) {
                            var overlayElement = tryGetParentOverlayElement();
                            if (overlayElement === null) {
                                console.warn("overlayElement is null");
                                return;
                            }

                            if (isMemorize) {
                                queue++;
                            }
                            //console.log('в очереди ' + queue + ' запросов');
                            if (overlayElement.is(":hidden")) {
                                if (hideTimeout) {
                                    $timeout.cancel(hideTimeout);
                                }
                                if (_showOverlayDelay) {
                                    showTimeout = $timeout(function () {
                                        if (queue > 0) {
                                            overlayElement.show(fade_delay);
                                        }
                                    }, _showOverlayDelay);
                                } else {
                                    overlayElement.show(fade_delay);
                                }

                            }
                        },
                        stopOverlay: function (_fadeDelay, _hideOverlayDelay, isMemorize) {
                            var overlayElement = tryGetParentOverlayElement();
                            if (overlayElement === null) {
                                return;
                            }

                            if (isMemorize && (queue > 0)) {
                                queue--;
                            }

                            //console.log('в очереди ' + queue + ' запросов');
                            //оставим, чтобы не засорять таймерами
                            if (queue <= 0) {
                                if (showTimeout) {
                                    $timeout.cancel(showTimeout);
                                }
                                if (_hideOverlayDelay) {
                                    hideTimeout = $timeout(function () {
                                        if (queue <= 0) {
                                            overlayElement.hide(_fadeDelay);
                                            isHideDeferred = false;
                                        }
                                    }, _hideOverlayDelay);
                                } else {
                                    overlayElement.hide(_fadeDelay);
                                    isHideDeferred = false;
                                }
                            }
                        },

                        processRequest: function () {
                            this.startOverlay(fade_delay, show_overlay_delay, true);
                        },

                        processResponse: function () {
                            var self = this;
                            if (isHideDeferred) {
                                // запускаем остановку (там выполняется проверка на остановку)
                                // вне текущего стека выполнения javascript,
                                // для того предотвратить мерцание оверлея.
                                $timeout(function () {
                                    self.stopOverlay(fade_delay, waiting_response_delay, true);
                                }, 0);
                            } else {
                                self.stopOverlay(fade_delay, waiting_response_delay, true);
                            }

                        },

                        $registerResponseHandler: function (handler) {
                            if (angular.isUndefined(handler) || !angular.isFunction(handler.handle)) {
                                throw new Error('Обработчие не определен или не имеет метода handle');
                            }

                            responseHandlers.push(handler);

                            return function () {
                                responseHandlers = _.without(responseHandlers, handler);
                            };
                        },

                        $unregisterResponseHandler: function (name) {
                            responseHandlers = _.without(responseHandlers, _.findWhere(responseHandlers, {name: name}));
                        }
                    };
                }]
            };
        }])
        .factory('_OverlayHttpInterceptor', ['$q', 'Overlay', '$window', '$injector', '$filter',
            function ($q, Overlay, $window, $injector, $filter) {
                var expireDialogOpened = false;

                //notice:
                //Если использовать $http в этом interceptor, то для того, чтобы
                //избежать circular dependency используем $inject.invoke
                //и для избежания infinite intercept, нужно добавить параметр в
                //$http.get('', {nointercept: true}) и после отработки логики
                //проверять его здесь и выставлять значение для того чтобы ветка с $http не исполнялась
                return {
                    // On request success
                    request: function (config) {
                        if (config && angular.isDefined(config.params)) {
                            //Если в запросе нет параметра nooverlay, то показываем overlay
                            if (true !== config.params.nooverlay) {
                                Overlay.processRequest();
                            }
                        } else {
                            Overlay.processRequest();
                        }
                        return config || $q.when(config);
                    },

                    // On request failure
                    requestError: function (rejection) {
                        Overlay.processRequest();
                        Overlay.showAlertByStatus('unknown', rejection.data);
                        return $q.reject(rejection);
                    },

                    // On response success
                    response: function (response) {
                        // признак того что запрос обработан самим приложением, а не СУДИР или чем-то другим
                        var isCustomPage = ('true' !== response.headers('isCustomPage'));
                        // ответ пришел с сервера, а не взят из кэша
                        var cached = angular.isDefined(response.config.cache) &&
                            angular.isObject(response.config.cache) && angular.isDefined(response.config.cache.info());
                        // contentType = 'text/html'
                        var isHtmlContent = response.headers()['content-type'] && response.headers()['content-type'].indexOf("text/html") !== -1;
                        // Определяем что сессия не истекла, и если истекла, то выдаём окно с информацией об этом.
                        // тут правильней словить статус код 401 (не авторизован), но СУДИР этого делать не умеет, возвращяет 400,
                        // поэтому ловим ответ с типом 'text/html', который сформировала не само приложение, а СУДИР или др., и чтобы он был взят не из кэша
                        if (isHtmlContent && isCustomPage && !cached) {
                            if (!expireDialogOpened) {
                                $injector.invoke(['$dialogs', function ($dialogs) {
                                    expireDialogOpened = true;

                                    $dialogs.confirmDialog({
                                        title: $filter('translate')('authorization.expire.dialog.title'),
                                        content: $filter('translate')('authorization.expire.dialog.message'),
                                        okBtnCaption: $filter('translate')('authorization.expire.dialog.reload'),
                                        cancelBtnCaption: $filter('translate')('button.close'),
                                        okBtnClick: function () {
                                            $window.location.reload();
                                        },
                                        cancelBtnClick: function () {
                                            expireDialogOpened = false;
                                            return false;
                                        }
                                    });
                                }]);
                            }
                            $injector.invoke(['$rootScope', function ($rootScope) {
                                $rootScope && $rootScope.$broadcast('AUTHORIZATION_EXPIRED');;
                            }]);
                            Overlay.processResponse();
                            return $q.reject();
                        }
                        if (response.config && angular.isDefined(response.config.params)) {
                            //Если в запросе нет параметра nooverlay, то показываем overlay
                            if (true !== response.config.params.nooverlay) {
                                Overlay.processResponse();
                            }
                        } else {
                            Overlay.processResponse();
                        }

                        return response || $q.when(response);
                    },

                    // On response failure
                    responseError: function (rejection) {
                        Overlay.processResponse();
                        // для 0 надо возвращать строку, для null и undefined - undefined
                        Overlay.showAlertByStatus(rejection.status !== undefined ? rejection.status.toString() : undefined, rejection.data);
                        return $q.reject(rejection);
                    }
                };
            }])
        .factory('_OverlayJqueryInterceptor', [
            'Overlay',
            function (Overlay) {
                return {
                    attach: function () {
                        //Intercept jQuery
                        if (window.jQuery) {
                            $(document).ajaxSend(function (event, jqxhr, settings) {
                                if (!settings.nooverlay) {
                                    Overlay.processRequest();
                                }
                            });

                            $(document).ajaxComplete(function (event, request, settings) {
                                if (!settings.nooverlay) {
                                    Overlay.processResponse();
                                }

                                if ((request.status !== 0) && (request.status !== 200) && (request.readyState !== 0)) {
                                    //noinspection JSUnresolvedVariable
                                    Overlay.showAlertByStatus(request.status, request.responseJSON);
                                }
                            });

                            $(document).ajaxError(function () {
                                Overlay.processResponse();
                            });
                        }
                    }
                };
            }])
        .factory("$overlayService", ['$compile', '$rootScope', 'Overlay', function ($compile, $rootScope, Overlay) {
            var overlayService = {};


            overlayService.isActive = function () {
                return Overlay.getOverlayQueueLength() > 0;
            };

            /**
             * Начать Overlay
             */
            overlayService.startOverlay = function (isMemorize) {
                Overlay.startOverlay(0, null, !!isMemorize);
            };

            /**
             * Закончить Overlay
             */
            overlayService.stopOverlay = function (isMemorize) {
                Overlay.stopOverlay(0, 250, !!isMemorize);
            };

            /**
             * Запуск overlay, с дополнительной инициализацией очереди
             */
            function startAddtionalQueueOverlay(additionalQueueCount) {
                if (additionalQueueCount) {
                    Overlay.increaseOverlayQueue(additionalQueueCount);
                }
                Overlay.setHideDeferred(true); // процесс закрытия запустить вне текущего потока
                Overlay.startOverlay(0, null, false);
            }

            /**
             * Показать overlay, который будет скрыт после того как последний запрос будет отработан.
             * В случае если несколько Ajax запросов разнесены по времени может возникнуть
             * моргание overlay (покажется скроется и опять покажется скроется),
             * тогда желательно использовать связку функций: overlayService.showUntilAllProcessesCompleted и
             * overlayService.completeOneProcess.
             */
            overlayService.show = function () {
                startAddtionalQueueOverlay(null);
            };

            /**
             * Показывать overlay до тех пор пока не завершится столько процессов сколько указано в
             * expectedProcessCompletedCount. Для того чтобы отметить что один процесс завершился
             * используется функция overlayService.completeOneProcess()
             */
            overlayService.showUntilAllProcessesCompleted = function (expectedProcessCompletedCount) {
                startAddtionalQueueOverlay(expectedProcessCompletedCount);
            };

            /**
             * Помечает что один из ожидаемых процессов завершился
             */
            overlayService.completeOneProcess = function () {
                Overlay.decreaseOverlayQueue(1);
            };

            /**
             * Добавляет обработчие ответа сервера
             *
             * @param handler обработчик
             * @returns {*} метод удаления обработчика
             */
            overlayService.registerResponseHandler = function (handler) {
                return Overlay.$registerResponseHandler(handler);
            };

            /**
             * Удаляет обработчик по его имени
             *
             * @param name имя обработчика
             */
            overlayService.unregisterResponseHandler = function (name) {
                Overlay.$unregisterResponseHandler(name);
            };

            return  overlayService;
        }])
        .config([
            '$httpProvider',
            function ($httpProvider) {
                // Add the interceptor to the $httpProvider.
                //noinspection JSUnresolvedVariable
                $httpProvider.interceptors.push('_OverlayHttpInterceptor');
            }])
        .run([
            '$templateCache',
            '$http',
            '_OverlayJqueryInterceptor',
            'AplanaUtils',
            function ($templateCache, $http, _OverlayJqueryInterceptor) {
                /**
                 * Добавляем в body html код, который отвечает за затенение и отображение анимации загрузки.
                 * Инициализация html элемента без AJAX подгрузки необходима
                 * т.к. в случае первичной загрузки страницы оверлей должен быть создан иначе он не сможет появится.
                 */
                function createOverlayElement() {
                    if (document.getElementById('overlay-parent') === null) {
                        var _overlayElement = "<div id='overlay-preloader'></div>"+
                             "<div id='overlay-parent' style='display: none;'>"+
                             "<div id='overlay-child'>"+
                             "<div id='overlay-right' class='overlay-content'>"+
                             "<div class='overlay-container-img'></div>"+
                             "</div>"+
                             "<div id='overlay-left' class='overlay-content'>"+
                             "</div>"+
                             "</div>"+
                             "<div id='overlay-helper'></div>"+
                             "</div>";
                        $('body').prepend(_overlayElement);
                    }
                }

                createOverlayElement();
                _OverlayJqueryInterceptor.attach();
            }]);
}());