(function () {
    'use strict';

    /**
     * @description Модуль для панели уведомлений
     */

    angular.module('app.logPanel', ['aplana.splitter', 'ui.router'])
        .factory("$logPanel", ['$compile', '$rootScope', '$filter', 'LogEntryResource',
            function ($compile, $rootScope, $filter, LogEntryResource) {
                var logPanel = {};

                function createLogPanel(uuid) {
                    return $compile("" +
                        "<div id='log-panel'>" +
                        "    <div data-aplana-splitter" +
                        "         data-splitter='horizontal'" +
                        "         data-splitter-thick='20'" +
                        "         data-splitter-left-top='app-content'" +
                        "         data-splitter-right-bottom='log-entry-list'" +
                        "         data-splitter-max='800'" +
                        "         data-splitter-min='600'" +
                        "         data-splitter-start='790'" +
                        "         data-splitter-resizing='true'" +
                        "    ></div>" +
                        "    <div id='log-entry-list'>" +
                        "        <div id='log-panel-header'>" +
                        "            <div id='log-panel-header-message'></div>" +
                        "            <div id='log-panel-header-print'>" +
                        "                <img src='resources/img/printer-black-16.png'>" +
                        "                <a href='/controller/actions/logEntry/" + uuid + "'>{{'logPanel.header.print' | translate}}</a>" +
                        "            </div>" +
                        "            <div style='float: right'>" +
                        "                <button type='button' class='close' data-ng-click='closeLogPanel()'>×</button>" +
                        "            </div>" +
                        "        </div>" +
                        "        <div id='log-entry-grid' data-aplana-grid" +
                        "             data-grid-options='logEntryGrid.options'" +
                        "             data-grid-ctrl='logEntryGrid.ctrl'" +
                        "             data-ng-model='logEntryGrid.value'" +
                        "             style='float: left; width: 100%;'></div>" +
                        "    </div>" +
                        "</div>"
                    )($rootScope);
                }

                function updateLogPanelHeaderMessage(total, errors) {
                    var messageText = $filter("translate")('logPanel.header.message');
                    $('#log-panel-header-message').text(messageText.replace('{0}', total).replace('{1}', errors));
                }

                /**
                 * Метод отображает панель уведомлений со списком сообщений
                 * @param rootElementId Идентификатор родительского элемента, внизу которого отображается панель уведомления
                 * @param logId Идентификатор группы сообщений
                 */
                logPanel.open = function (rootElementId, logId) {
                    logPanel.close();
                    if (rootElementId) {
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
                                    '',
                                    $filter('translate')('logPanel.title.dateTime'),
                                    '',
                                    $filter('translate')('logPanel.title.message')],
                                colModel: [
                                    {
                                        name: 'ord',
                                        index: 'ord',
                                        width: 30,
                                        key: true,
                                        formatter: $filter('incrementFormatter')
                                    },
                                    {name: 'date', index: 'date', width: 140, formatter: $filter('dateTimeFormatter')},
                                    {name: 'icon', index: 'icon', width: 30, formatter: $filter('iconFormatter')},
                                    {
                                        name: 'message',
                                        index: 'message',
                                        width: 800,
                                        formatter: $filter('textColorFormatter')
                                    }
                                ],
                                rowNum: 10,
                                rowList: [10, 20, 30],
                                viewrecords: true,
                                sortname: 'date',
                                sortorder: "asc",
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
                };

                /**
                 * Метод закрывает панель уведомлений
                 */
                logPanel.close = function () {
                    $rootScope.logEntryGrid = undefined;
                    angular.element(document.querySelector('#log-panel')).remove();
                };

                $rootScope.closeLogPanel = function () {
                    logPanel.close();
                };

                return logPanel;
            }])

        .filter('iconFormatter', ['$filter', function ($filter) {
            return function (value, row, logEntryObject) {
                if (logEntryObject.level === 'ERROR') {
                    return '<img src="resources/img/error-16.png" height="15" width="15">';
                } else {
                    return '';
                }
            };
        }])

        .filter('textColorFormatter', ['$filter', function ($filter) {
            return function (value, row, logEntryObject) {
                if (logEntryObject.level === 'ERROR') {
                    return '<div class="log-error-message">' + value + '</div>';
                } else {
                    return '<div class="log-normal-message">' + value + '</div>';
                }
            };
        }])

        .filter('incrementFormatter', ['$filter', function ($filter) {
            return function (value) {
                if (angular.isNumber(value)) {
                    return value + 1;
                } else {
                    return '';
                }
            };
        }]);
}());