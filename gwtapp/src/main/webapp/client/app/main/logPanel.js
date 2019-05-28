(function () {
    'use strict';
    /**
     * @description Модуль панели уведомлений
     */
    angular.module('app.logPanel', ['aplana.splitter', 'ui.router'])
        .factory('$logPanel', ['$compile', '$rootScope', '$filter', 'LogEntryResource', 'APP_CONSTANTS', '$transitions', '$document',
            function ($compile, $rootScope, $filter, LogEntryResource, APP_CONSTANTS, $transitions, $document) {
                var logPanel = {
                    height: 300
                };

                var scope = null;

                //TODO:https://jira.aplana.com/browse/SBRFNDFL-1637
                function createLogPanel(uuid) {
                    return $compile("" +
                        "<div id='log-panel' class='flex-column' style='background: #fff;'>" +
                        "   <div data-aplana-splitter" +
                        "        data-splitter='horizontal'" +
                        "        data-splitter-thick='36'" +
                        "        data-splitter-left-top='app-content'" +
                        "        data-splitter-right-bottom='log-entry-list'" +
                        "        data-splitter-major-side='log-entry-list'" +
                        "        data-splitter-container='.cbr-page-layout__view'" +
                        "        data-splitter-start='" + logPanel.height + "'" +
                        "        data-splitter-resizing='true'" +
                        "        id='resize-button'>" +
                        "       <div id='log-panel-header' style='top: 16px;'>" +
                        "           <div id='log-panel-header-message'></div>" +
                        "           <div style='float: right; margin: -7px 6px 0 4px;'>" +
                        "               <button type='button' class='close' data-ng-click='closeLogPanel()'>×</button>" +
                        "           </div>" +
                        "           <div id='log-panel-header-print' style='float: right; margin: -4px 5px 0 0;'>" +
                        "               <img src='resources/img/unload-white-16.png'>" +
                        "               <a href='controller/actions/logEntry/" + uuid + "' title=\"{{'logPanel.header.unload.title' | translate}}\">{{'logPanel.header.unload' | translate}}</a>" +
                        "           </div>" +
                        "       </div>" +
                        "   </div>" +
                        "   <div id='log-entry-list'>" +
                        "       <div class='flex-grid' style='height: 100%;'" +
                        "            data-aplana-grid" +
                        "            data-grid-fill-space='true' " +
                        "            data-grid-refresh-button='false'  " +
                        "            data-grid-options='logEntryGrid.options' " +
                        "            data-grid-fill-space-container-selector='#log-entry-list' " +
                        "            data-grid-fill-space-container-selector-top='#log-panel-header' " +
                        "            data-grid-ctrl='logEntryGrid.ctrl'" +
                        "            data-ng-model='logEntryGrid.value'" +
                        "            style='float: left; width: 100%;'" +
                        "            id='log-entry-grid' ></div>" +
                        "   </div>" +
                        "</div>"
                    )(scope = $rootScope.$new());
                }

                function updateLogPanelHeaderMessage(total, errors) {
                    var messageText = $filter("translate")('logPanel.header.message');
                    //TODO:https://jira.aplana.com/browse/SBRFNDFL-1637
                    $('#log-panel-header-message').text(messageText.replace('{0}', total).replace('{1}', errors));
                }

                /**
                 * @description Метод отображает панель уведомлений со списком сообщений
                 *
                 * @param rootElementId Идентификатор родительского элемента, внизу которого отображается панель уведомления
                 * @param logId Идентификатор группы сообщений
                 */
                logPanel.open = function (rootElementId, logId) {
                    logPanel.close();
                    if (rootElementId) {
                        //TODO:https://jira.aplana.com/browse/SBRFNDFL-1637
                        $rootScope.logEntryGrid = {
                            ctrl: {},
                            value: [],
                            options: {
                                datatype: "angularResource",
                                angularResource: LogEntryResource,
                                requestParameters: function () {
                                    return {
                                        uuid: logId
                                    };
                                },
                                height: 200,
                                colNames: [
                                    $filter('translate')('logPanel.title.num'),
                                    $filter('translate')('logPanel.title.dateTime'),
                                    $filter('translate')('logPanel.title.messageType'),
                                    $filter('translate')('logPanel.title.message'),
                                    $filter('translate')('logPanel.title.type'),
                                    $filter('translate')('logPanel.title.object')],
                                colModel: [
                                    {
                                        name: 'ord',
                                        index: 'ord',
                                        width: 60,
                                        key: true,
                                        sortable: false,
                                        formatter: $filter('incrementFormatter')
                                    },
                                    {
                                        name: 'date',
                                        index: 'date',
                                        width: 126,
                                        sortable: false,
                                        formatter: $filter('dateTimeFormatter')
                                    },
                                    {
                                        name: 'icon',
                                        index: 'icon',
                                        width: 120,
                                        sortable: false,
                                        formatter: $filter('iconFormatter'),
                                        classes: 'image-column'
                                    },
                                    {
                                        name: 'message',
                                        index: 'message',
                                        width: 1278,
                                        sortable: false,
                                        formatter: $filter('textColorFormatter'),
                                        classes: 'white-space-pre-line'
                                    },
                                    {
                                        name: 'type',
                                        index: 'type',
                                        width: 200,
                                        sortable: false,
                                        formatter: $filter('textColorFormatter'),
                                        classes: 'grid-cell-white-space'
                                    },
                                    {
                                        name: 'object',
                                        index: 'object',
                                        width: 200,
                                        sortable: false,
                                        formatter: $filter('textColorFormatter'),
                                        classes: 'grid-cell-white-space'
                                    }
                                ],
                                rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                                rowList: APP_CONSTANTS.COMMON.PAGINATION,
                                viewrecords: true,
                                hidegrid: false
                            }
                        };

                        var logPanelDiv = createLogPanel(logId);
                        var rootElement = angular.element(document.querySelector('#' + rootElementId));
                        rootElement.append(logPanelDiv);

                        LogEntryResource.query({
                            uuid: logId,
                            projection: 'count'
                        }, function (data) {
                            var totalCount = data.ERROR + data.WARNING + data.INFO;
                            var fatalErrors = data.ERROR;
                            updateLogPanelHeaderMessage(totalCount, fatalErrors);
                        });
                    }
                    $rootScope.$broadcast('UPDATE_GIRD_HEIGHT');
                };

                /**
                 * Метод закрывает панель уведомлений
                 */
                logPanel.close = function () {
                    if (scope) {
                        logPanel.height = angular.element(document.querySelector('#log-entry-list')).height();
                        $rootScope.logEntryGrid = undefined;
                        angular.element(document.querySelector('#log-panel')).remove();
                        angular.element(document.querySelector('#app-content')).css('height', '100%');
                        scope.$destroy();
                        scope = null;
                    }
                };

                /**
                 * Отрабатывает при нажатии на кнопку закрытия панели
                 */
                $rootScope.closeLogPanel = function () {
                    logPanel.close();
                    $rootScope.$broadcast('UPDATE_GIRD_HEIGHT');
                };

                $transitions.onSuccess({}, function () {
                    logPanel.close();
                });

                return logPanel;
            }])

        .filter('iconFormatter', ['$filter', function () {
            return function (value, row, logEntryObject) {
                if (logEntryObject.level === 'ERROR') {
                    return '<img src="resources/img/error-16.png" height="15" width="15">';
                } else {
                    return '';
                }
            };
        }])

        .filter('textColorFormatter', ['$filter', function () {
            return function (value, row, logEntryObject) {
                value = value ? value : '';
                if (logEntryObject.level === 'ERROR') {
                    return '<div class="log-error-message">' + value + '</div>';
                } else if (logEntryObject.level === 'WARNING') {
                    return '<div class="log-warning-message">' + value + '</div>';
                } else {
                    return '<div class="log-normal-message">' + value + '</div>';
                }
            };
        }])

        .filter('incrementFormatter', ['$filter', function () {
            return function (value) {
                if (angular.isNumber(value)) {
                    return value + 1;
                } else {
                    return '';
                }
            };
        }]);
}());