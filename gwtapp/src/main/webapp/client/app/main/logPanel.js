(function () {
    'use strict';
    /**
     * @description Модуль панели уведомлений
     */
    angular.module('app.logPanel', ['aplana.splitter', 'ui.router'])
        .factory('$logPanel', ['$compile', '$rootScope', '$filter', 'LogEntryResource', 'APP_CONSTANTS', '$transitions',
            function ($compile, $rootScope, $filter, LogEntryResource, APP_CONSTANTS, $transitions) {
                var logPanel = {};

                //TODO:https://jira.aplana.com/browse/SBRFNDFL-1637
                function createLogPanel(uuid) {
                    return $compile("" +
                        "<div id='log-panel' style=' background: #fff;height: 300px; width: 97.7%; left: 21px;min-height: 296px;'>" +
                        "    <div data-aplana-splitter" +
                        "         data-splitter='horizontal'" +
                        "         data-splitter-thick='30'" +
                        "         data-splitter-left-top='app-content'" +
                        "         data-splitter-right-bottom='log-entry-list'" +
                        "         data-splitter-max='994' data-splitter-min='12'" +
                        "         data-splitter-start='540'" +
                        "         data-splitter-resizing='true'" +
                        "         id='resize-button'"+
                        "    ></div>" +
                        "    <div id='log-entry-list'>" +
                        "        <div id='log-panel-header'>" +
                        "            <div id='log-panel-header-message'></div>" +
                        "            <div style='float: right; margin: -7px 6px 0 4px;'>" +
                        "                <button type='button' class='close' data-ng-click='closeLogPanel()'>×</button>" +
                        "            </div>" +
                        "            <div id='log-panel-header-print' style='float: right; margin: -4px 5px 0 0;'>" +
                        "                <img src='resources/img/unload-white-16.png'>" +
                        "                <a href='controller/actions/logEntry/" + uuid + "' title=\"{{'logPanel.header.unload.title' | translate}}\">{{'logPanel.header.unload' | translate}}</a>" +
                        "            </div>" +
                        "        </div>" +
                        "        <div data-aplana-grid" +
                        "             data-grid-fill-space='true' " +
                        "             data-grid-options='logEntryGrid.options' " +
                        "              data-grid-fill-space-container-selector='#log-panel' " +
                        "             data-grid-ctrl='logEntryGrid.ctrl'" +
                        "             data-ng-model='logEntryGrid.value'" +
                        "             style='float: left; width: 100%;'" +
                        "             id='log-entry-grid' ></div>" +
                        "    </div>" +
                        "</div>"
                    )($rootScope);
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
                                        width: 50,
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
                                        width: 110,
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
                                        classes: 'grid-cell-white-space'
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

                        var appContainers = angular.element(document.querySelector('#app-content')).height();
                       angular.element(document.querySelector('#resize-button')).on('mousedown', function (e) {
                           var $dragable = angular.element(document.querySelector('#log-panel')),
                               startHeight = $dragable.height(),
                               pY = e.pageY,
                               wrapper = angular.element('.cbr-page-layout__view');

                           angular.element(document.querySelector('.cbr-page')).on('mouseup', function () {
                               wrapper.off('mouseup').off('mousemove');
                           });
                           angular.element(wrapper).on('mousemove', function (me) {
                               var my = (me.pageY - pY);
                               $dragable.find('.ui-jqgrid-bdiv').css({
                                   height: startHeight - my - 136,
                                   maxHeight: appContainers - 172
                               });

                               $dragable.css({
                                   height: startHeight - my,
                                   maxHeight: appContainers - 34
                               });

                               if(me.pageY  < 60 ){
                                   wrapper.off('mouseup').off('mousemove');
                                   console.log(me.pageY)
                               }
                           });
                       });
                    }
                };

                /**
                 * Метод закрывает панель уведомлений
                 */
                logPanel.close = function () {
                    $rootScope.logEntryGrid = undefined;
                    angular.element(document.querySelector('#log-panel')).remove();
                    angular.element(document.querySelector('#app-content')).css('height', '100%');
                };

                $rootScope.closeLogPanel = function () {
                    logPanel.close();
                };

                $transitions.onSuccess({}, function() {
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