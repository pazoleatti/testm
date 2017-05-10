/**
 * $logPanel (панель логов)
 * Сервис aplana-log-panel служит для отображения списка сообщений
 * http://localhost:8080/#/aplana_log_panel
 */
(function () {
    'use strict';

    angular.module("aplana.logPanel", ['aplana.utils'])
        .factory("$logPanel", ['$compile', '$rootScope', '$alertService', function ($compile, $rootScope, $alertService) {
            var logPanel = {};
            var rootElement;
            var PANEL_ADDITIONAL_HEIGHT = 195; //высота, необходимая для добавления панели в родительский элемент (px)

            $rootScope.hideLogPanel = function () {
                //Очищаем и скрываем панель
                angular.element($('#logPanelContainer')).css('visibility', 'hidden');
                $('.log-entry').remove();
                if (rootElement) {
                    //если был задан родительский элемент, необходимо уменьшить его высоту на высоту панели
                    rootElement.height(rootElement.height()-PANEL_ADDITIONAL_HEIGHT);
                }
            };

            function getOrCreateLogPanelContainer() {
                if (document.getElementById('logPanelContainer') == null) {
                    var _logPanelContainer = $compile("" +
                        "<div id='logPanelContainer' class='log-panel-container'> " +
                        "   <div class='log-panel-header' id='logPanelHeaderContainer'>" +
                        "       <span id ='logPanelHeader'></span>" +
                        "       <span><button type='button' class='close log-panel-hide-button' " +
                                            "data-ng-click='hideLogPanel()'>&times;</button></span>" +
                        "   </div>" +
                        "   <div id='logEntriesContainer'></div>" +
                        "</div>"
                    )($rootScope);
                    angular.element(document.body).append(_logPanelContainer);
                    return _logPanelContainer;
                } else {
                    return $('#logPanelContainer');
                }
            }

            function initLogPanelContainer(_logPanelContainer) {
                // отступ сверху, например для того чтобы не перекрывать верхнее меню
                var topOffest = 60;
                $(window).resize(function () {
                    _logPanelContainer.css("max-height", $(window).height() - topOffest);
                });
                _logPanelContainer.css("max-height", $(window).height() - topOffest);
            }

            function updateLogPanelHeader(total, errors) {
                $('#logPanelHeader').text("Сообщения (всего: {0}; фатальных ошибок: {1})"
                    .replace('{0}', total).replace('{1}', errors));
            }

            var logPanelContainer = getOrCreateLogPanelContainer();
            initLogPanelContainer(logPanelContainer);
            var logEntriesContainer = $('#logEntriesContainer');
            var logPanelHeader = $('#logPanelHeader');

            /**
             * Метод отображает список сообщений в панели увдомления
             * @param logger            объект со списком сообщений + основное сообщение для диалогового окна.
             *      Каждый объект-сообщение в списке должен содержать поля:
             *      message - текст сообщения
             *      level - уровень логгирования (по-умолчанию - зеленый, WARNING - оранжевый, ERROR - красный)
             * @param rootElementId     id родительского элемента, внизу которого отображается панель уведомления
             */
            logPanel.show = function(logger, rootElementId) {
                logPanel.clear();
                if (rootElementId) {
                    //если задан id родительского элемента
                    if (logPanelContainer.parent()[0]!==$('#'+rootElementId)[0]) {
                        // если родительский элемент найденной на странице панели
                        // несовпадает с родительским элементом, заданным в данный момент
                        // в этом случае необходимо пересоздать js-объект панели
                        logPanelContainer = getOrCreateLogPanelContainer();
                        logEntriesContainer = $('#logEntriesContainer');
                        logPanelHeader = $('#logPanelHeader');
                        rootElement = $('#'+rootElementId);
                        //расширяем родительский элемент на высоту панели
                        rootElement.height(rootElement.height()+PANEL_ADDITIONAL_HEIGHT);
                        //меняем css-классы оформления заголовка, сообщений и панели для вставки в родительский элемент
                        angular.element($('#logPanelHeaderContainer'))
                            .removeClass("log-panel-header").addClass("inner-log-panel-header");
                        angular.element(logEntriesContainer).css({
                            "height": "150px",
                            "overflow-y": "auto"
                        });
                        angular.element(logPanelContainer).removeClass("log-panel-container")
                            .addClass("inner-log-panel-container");
                        rootElement.append(logPanelContainer);
                    } else if (angular.element(logPanelContainer).css("visibility")==="hidden") {
                        //если родительские элементы совпадают, но панель не отображается (была закрыта)
                        rootElement = $('#'+rootElementId);
                        rootElement.height(rootElement.height()+PANEL_ADDITIONAL_HEIGHT);
                    }
                } else if (!logPanelContainer.parent().is("body")){
                    //если на одной и той же странице открываем панель сначала в модальном окне, а потом внизу страницы
                    //то надо проверять - а не была ли уже создана панель в другом месте (не в body)
                    //в этом случае необходимо пересоздать js-объект панели
                    logPanelContainer = getOrCreateLogPanelContainer();
                    initLogPanelContainer(logPanelContainer);
                    logEntriesContainer = $('#logEntriesContainer');
                    logPanelHeader = $('#logPanelHeader');
                }
                var total = 0;
                var errors = 0;
                var elements = "";
                logger.entries.forEach(function(entry) {
                    var levelClass;
                    total++;
                    switch (entry.level) {
                        case 'WARNING':
                            levelClass = "log-panel-warning";
                            break;
                        case 'ERROR':
                            levelClass = "log-panel-danger";
                            errors++;
                            break;
                        default:
                            levelClass = "log-panel-info";
                    }
                    elements += "<div class='log-entry log-panel " + levelClass + "'><span>{{"+ entry.date +" | date : 'dd.MM.yyyy HH:mm:ss'}}</span>&nbsp;<span>" + entry.message + "</span></div>";
                });
                angular.element(logEntriesContainer).append($compile(elements)($rootScope));
                updateLogPanelHeader(total, errors);
                angular.element(logPanelContainer).css("visibility", "visible");
                if (logger.hasException) {
                    $alertService.error(logger.mainMsg);
                }
            };

            /**
             * Метод очищает панель сообщений
             */
            logPanel.clear = function () {
                $('.log-entry').remove();
                updateLogPanelHeader(0, 0);
            };

            return logPanel;
        }]);
}());

