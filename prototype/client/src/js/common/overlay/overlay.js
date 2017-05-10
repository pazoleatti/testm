/**
 * aplana-overlay (Индикатор загрузки)
 * Модуль предназначени для показа индикатора загрузки
 * http://localhost:8080/#/aplana_overlay
 */
(function () {
    'use strict'
    angular.module('aplana.overlay', ['aplana.utils'])
        .provider('Overlay', [function () {
            //Задержка перед закрытием overlay. Необходим для объединения нескольких запросов в один показ индикатора загрузки
            var waiting_response_delay = 250
            //Задержка перед показом overlay. Отсеиваются "короткие" запросы.
            var show_overlay_delay = 200
            //задержка показа/скрытия затенения с индикатором загрузки
            var fade_delay = 0

            // ERR_ADDRESS_UNREACHABLE, для chrome/firefox и ie
            var err_address_unreachable = ['-1', '0', '12029']
            //очередь запросов
            var queue = 0
            var showTimeout = null, hideTimeout = null
            // использовать отложенную остановку
            var isHideDeferred = false

            //кастомные обработчики ответа сервера
            //должны иметь метод handle, который вернет true, если они обработали ответ сервера и больше ничего делать не нужно
            var responseHandlers = []

            //Добавляем один обработчик для статуса 403
            responseHandlers.push({
                handle: function (status, detailMessage, ExceptionStorage){
                    if(status != 403){
                        return false;
                    }

                    var messages = ExceptionStorage.getByStatus(status);
                    return messages.total > 0;
                }
            });

            //Добавляем обработчик для остальных статусов
            responseHandlers.push({
                handle: function (status, detailMessage, ExceptionStorage) {
                    //Провереям, возможна ли работа, отсекаем ошибки импорта
                    if(!ExceptionStorage || !detailMessage || detailMessage.text === "Ошибка импорта" || detailMessage.text === "Ошибка редактирования"){
                        return false;
                    }
                    var messages = ExceptionStorage.getByStatus(status);
                    if(messages.total != 0){
                        for(var key in messages){
                            if(messages.hasOwnProperty(key)){
                                var msg = messages[key];
                                if(msg && detailMessage.text === msg.text && detailMessage.details === msg.details){
                                    return true;
                                }
                            }
                        }
                    }
                    return false;
                }
            });

            //для прелоада изображений
            jQuery.preloadImages = function () {
                for (var i = 0; i < arguments.length; i++) {
                    jQuery("<img>").attr("src", arguments[i])
                }
            }

            var parentOverlayElement = null

            function tryGetParentOverlayElement() {
                if (parentOverlayElement) {
                    return parentOverlayElement
                } else if (document.getElementById('overlay-parent') != null) {
                    parentOverlayElement = $("#overlay-parent")
                    return parentOverlayElement
                } else {
                    return null
                }
            }

            return {
                //настраиваем в конфигурационной функции
                setWaitingResponseDelay: function (delay) {
                    waiting_response_delay = delay
                },

                setShowOverlayDelay: function (delay) {
                    show_overlay_delay = delay
                },

                setFadeDelay: function (delay) {
                    fade_delay = delay
                },

                setParentOverlayElement: function (parentOverlay) {
                    parentOverlayElement = parentOverlay
                },

                $get: ['$rootScope', '$injector', '$timeout', '$filter', 'ExceptionStorage', 'AplanaUtils', '$location', '$window',
                    function ($rootScope, $injector, $timeout, $filter, ExceptionStorage, AplanaUtils, $location, $window) {
                    return {
                        showAlertText: function (status, detailMessage) {

                            // При ошибке доступа возвращаем пользователя на главную страницу
                            if (status == '401') {
                                $injector.invoke(['aplanaDialogs', function (aplanaDialogs) {
                                    aplanaDialogs
                                        .notify('Уведомление', 'Пользователь не авторизован, либо истекла по времени сессия. Вы будете перенаправлены на главную страницу')
                                        .result.then(function(){
                                            $location.url('/')
                                            $window.location.reload()
                                        })
                                }])
                            }

                            //Вариант решения проблемы с circular dependency
                            var messageType
                            var message
                            //500 это значит во время обработки запроса на стороне сервера проихошла ошибка
                            //Сообщения в таком случае формируются в классе ControllerExceptionHandler
                            //Определение - что выводить ошибку или предупреждение
                            if (/^(4..)$/.test(status) || /^(5..)$/.test(status) || status in err_address_unreachable) {
                                messageType = 'error'
                            }
                            else {
                                messageType = 'warning'
                            }
                            message = '<p>' + (detailMessage != null && detailMessage.text != null ? detailMessage.text : $filter('translate')(status)) + '</p>'
                            var handled = false

                            //ключ для хранения стека серверной ошибки
                            var unique = _.uniqueId(status)

                            for (var idx = 0; idx < responseHandlers.length; idx++) {
                                if (responseHandlers[idx].handle(status, detailMessage, ExceptionStorage)) {
                                    handled = true
                                    break
                                }
                            }

                            // Слово "Подробнее" в сообщение
                            if (detailMessage && detailMessage.details){
                                message += '<p class="btn-link">' + $filter('translate')('common.error.message.seemore') + '</p>'
                                ExceptionStorage.set({id: unique, value: detailMessage})
                            }

                            if (!handled) {
                                if (status in err_address_unreachable) {
                                    alert(message)
                                } else {
                                    $injector.invoke(['$alertService', function ($alertService) {
                                        if (detailMessage && detailMessage.details) {
                                            var header = ExceptionStorage.get(unique).text;
                                            var details = ExceptionStorage.get(unique).details;
                                            $alertService.showClickableDetails(messageType, header, details)
                                        } else {
                                            $alertService.show(message, messageType)
                                        }
                                    }])
                                }
                            }
                        },

                        showAlertByStatus: function (status, addMessage) {
                            if (!status) {
                                status = "unknown"
                            }
                            this.showAlertText(status, addMessage)
                        },

                        getOverlayQueueLength: function () {
                            return queue
                        },

                        increaseOverlayQueue: function (num) {
                            queue = queue + num
                        },

                        decreaseOverlayQueue: function (num) {
                            queue = queue - num
                        },

                        setHideDeferred: function (_isHideDeferred) {
                            isHideDeferred = _isHideDeferred
                        },

                        printOverlayQueue: function (startMsg) {
                            var msg = null
                            if (startMsg) {
                                msg = startMsg + " "
                            }
                            msg = msg + "queue: " + queue
                            $log.warn(msg)
                        },

                        /**
                         * Флаг isMemorize используется для указания запоминания старта.
                         * Флаг необходим для запуска оверлея в ручном режиме для того чтобы оверлей
                         * смог автоматом отключится при http или jQuery.ajax запросе
                         */
                        startOverlay: function (_fadeDelay, _showOverlayDelay, isMemorize) {
                            var overlayElement = tryGetParentOverlayElement()
                            if (overlayElement == null) {
                                $log.warn("overlayElement is null")
                                return
                            }

                            if (isMemorize) {
                                queue++
                            }
                            //$log.log('в очереди ' + queue + ' запросов')
                            if (overlayElement.is(":hidden")) {
                                if (hideTimeout) {
                                    $timeout.cancel(hideTimeout)
                                }
                                if (_showOverlayDelay) {
                                    showTimeout = $timeout(function () {
                                        if (queue > 0) {
                                            overlayElement.show(fade_delay)
                                        }
                                    }, _showOverlayDelay)
                                } else {
                                    overlayElement.show(fade_delay)
                                }

                            }
                        },
                        stopOverlay: function (_fadeDelay, _hideOverlayDelay, isMemorize) {
                            var overlayElement = tryGetParentOverlayElement()
                            if (overlayElement == null) {
                                return
                            }

                            if (isMemorize && (queue > 0)) {
                                queue--
                            }

                            //$log.log('в очереди ' + queue + ' запросов')
                            //оставим, чтобы не засорять таймерами
                            if (queue <= 0) {
                                if (showTimeout) {
                                    $timeout.cancel(showTimeout)
                                }
                                if (_hideOverlayDelay) {
                                    hideTimeout = $timeout(function () {
                                        if (queue <= 0) {
                                            overlayElement.hide(_fadeDelay)
                                            isHideDeferred = false
                                        }
                                    }, _hideOverlayDelay)
                                } else {
                                    overlayElement.hide(_fadeDelay)
                                    isHideDeferred = false
                                }
                            }
                        },

                        processRequest: function () {
                            this.startOverlay(fade_delay, show_overlay_delay, true)
                        },

                        processResponse: function () {
                            var self = this
                            if (isHideDeferred) {
                                // запускаем остановку (там выполняется проверка на остановку)
                                // вне текущего стека выполнения javascript,
                                // для того предотвратить мерцание оверлея.
                                $timeout(function () {
                                    self.stopOverlay(fade_delay, waiting_response_delay, true)
                                }, 0)
                            } else {
                                self.stopOverlay(fade_delay, waiting_response_delay, true)
                            }
                        },

                        $registerResponseHandler: function (handler) {
                            if (angular.isUndefined(handler) || !angular.isFunction(handler.handle)) {
                                throw new Error('Обработчик не определен или не имеет метода handle')
                            }

                            responseHandlers.push(handler)

                            return function () {
                                responseHandlers = _.without(responseHandlers, handler)
                            }
                        },

                        $unregisterResponseHandler: function (name) {
                            responseHandlers = _.without(responseHandlers, _.findWhere(responseHandlers, {name: name}))
                        }
                    }
                }]
            }
        }])
        .factory('_OverlayHttpInterceptor', [
            '$q', 'Overlay', '$log',
            function ($q, Overlay, $log) {
                //notice:
                //Если использовать $http в этом interceptor, то для того, чтобы
                //избежать circular dependency используем $inject.invoke
                //и для избежания infinite intercept, нужно добавить параметр в
                //$http.get('', {nointercept: true}) и после отработки логики
                //проверять его здесь и выставлять значение для того чтобы ветка с $http не исполнялась
                return {
                    // On request success
                    request: function (config) {
                        if (config.url.startsWith('rest/')) {
                            $log.debug(JSON.stringify(config))
                        }
                        if (config && angular.isDefined(config.params)) {
                            //Если в запросе нет параметра nooverlay, то показываем overlay
                            if (true !== config.params.nooverlay) {
                                Overlay.processRequest()
                            }
                        } else {
                            Overlay.processRequest()
                        }
                        return config || $q.when(config)
                    },

                    // On request failure
                    requestError: function (rejection) {
                        Overlay.processRequest()
                        Overlay.showAlertByStatus('unknown', rejection.data)
                        return $q.reject(rejection)
                    },

                    // On response success
                    response: function (response) {
                        if (response.config && angular.isDefined(response.config.params)) {
                            //Если в запросе нет параметра nooverlay, то показываем overlay
                            if (true !== response.config.params.nooverlay) {
                                Overlay.processResponse()
                            }
                        } else {
                            Overlay.processResponse()
                        }

                        return response || $q.when(response)
                    },

                    // On response failure
                    responseError: function (rejection) {
                        Overlay.processResponse()
                        // для 0 надо возвращать строку, для null и undefined - undefined
                        Overlay.showAlertByStatus(rejection.status != undefined ? rejection.status.toString() : undefined, rejection.data)
                        return $q.reject(rejection)
                    }
                }
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
                                    Overlay.processRequest()
                                }
                            })

                            $(document).ajaxComplete(function (event, request, settings) {
                                if (!settings.nooverlay) {
                                    Overlay.processResponse()
                                }

                                if ((request.status !== 0) && (request.status !== 200) && (request.readyState !== 0)) {
                                    Overlay.showAlertByStatus(request.status, request.responseJSON)
                                }
                            })

                            $(document).ajaxError(function (event, jqXHR, ajaxSettings, thrownError) {
                                Overlay.processResponse()
                            })
                        }
                    }
                }
            }])
        .factory("$overlayService", ['$compile', '$rootScope', 'Overlay', function ($compile, $rootScope, Overlay) {
            var overlayService = {}

            /**
             *
             * @param isMemorize
             */
            overlayService.isActive = function () {
                return Overlay.getOverlayQueueLength() > 0
            }

            /**
             * Начать Overlay
             */
            overlayService.startOverlay = function (isMemorize) {
                Overlay.startOverlay(0, null, !!isMemorize)
            }

            /**
             * Закончить Overlay
             */
            overlayService.stopOverlay = function (isMemorize) {
                Overlay.stopOverlay(0, 250, !!isMemorize)
            }

            /**
             * Запуск overlay, с дополнительной инициализацией очереди
             */
            function startAddtionalQueueOverlay(additionalQueueCount) {
                if (additionalQueueCount) {
                    Overlay.increaseOverlayQueue(additionalQueueCount)
                }
                Overlay.setHideDeferred(true) // процесс закрытия запустить вне текущего потока
                Overlay.startOverlay(0, null, false)
            }

            /**
             * Показать overlay, который будет скрыт после того как последний запрос будет отработан.
             * В случае если несколько Ajax запросов разнесены по времени может возникнуть
             * моргание overlay (покажется скроется и опять покажется скроется),
             * тогда желательно использовать связку функций: overlayService.showUntilAllProcessesCompleted и
             * overlayService.completeOneProcess.
             */
            overlayService.show = function () {
                startAddtionalQueueOverlay(null)
            }

            /**
             * Показывать overlay до тех пор пока не завершится столько процессов сколько указано в
             * expectedProcessCompletedCount. Для того чтобы отметить что один процесс завершился
             * используется функция overlayService.completeOneProcess()
             */
            overlayService.showUntilAllProcessesCompleted = function (expectedProcessCompletedCount) {
                startAddtionalQueueOverlay(expectedProcessCompletedCount)
            }

            /**
             * Помечает что один из ожидаемых процессов завершился
             */
            overlayService.completeOneProcess = function () {
                Overlay.decreaseOverlayQueue(1)
            }

            /**
             * Добавляет обработчие ответа сервера
             *
             * @param handler обработчик
             * @returns {*} метод удаления обработчика
             */
            overlayService.registerResponseHandler = function (handler) {
                return Overlay.$registerResponseHandler(handler)
            }

            /**
             * Удаляет обработчик по его имени
             *
             * @param name имя обработчика
             */
            overlayService.unregisterResponseHandler = function (name) {
                Overlay.$unregisterResponseHandler(name)
            }

            return  overlayService
        }])
        // сервис для хранения серверной ошибки
        .service('ExceptionStorage', function () {
                var exeptionObject = {}
                this.set = function (value) {
                    exeptionObject[value.id] = value.value
                }
                this.get = function (index) {
                    return exeptionObject[index]
                }
                this.getByStatus = function (status) {
                    var result = {
                        total: 0
                    };
                    for (var key in exeptionObject) {
                        if (exeptionObject.hasOwnProperty(key) && key.indexOf(status) == 0) {
                            result[key] = exeptionObject[key];
                            result.total++;
                        }
                    }
                    return result;
                }
            }
        )
        .config([
            '$httpProvider',
            function ($httpProvider) {
                // Add the interceptor to the $httpProvider.
                $httpProvider.interceptors.push('_OverlayHttpInterceptor')
            }])
        .run([
            '$templateCache',
            '$http',
            '_OverlayJqueryInterceptor',
            function ($templateCache, $http, _OverlayJqueryInterceptor) {
                /**
                 * Добавляем в body html код, который отвечает за затенение и отображение анимации загрузки.
                 * Инициализация html элемента без AJAX подгрузки необходима
                 * т.к. в случае первичной загрузки страницы оверлей должен быть создан, иначе он не сможет появиться.
                 */
                function createOverlayElement() {
                    if (document.getElementById('overlay-parent') == null) {
                        var _overlayElement =
                            "<div id='overlay-parent' style='display: none'>" +
                            "   <div></div>"+
                            "</div>"
                        $('body').prepend(_overlayElement)
                    }
                }

                createOverlayElement()
                _OverlayJqueryInterceptor.attach()
            }])
}())