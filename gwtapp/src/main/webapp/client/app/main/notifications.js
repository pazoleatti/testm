(function () {
    'use strict';
    /**
     * @description Модуль модального окна оповещений
     */
    angular.module('app.notifications', [])
    /**
     * @description Контроллер модального окна оповещений
     */
        .controller('notificationsCtrl', ['$scope', '$http', '$httpParamSerializer', '$modalInstance', 'NotificationResource', '$filter', '$logPanel', '$dialogs', '$rootScope', 'APP_CONSTANTS',
            function ($scope, $http, $httpParamSerializer, $modalInstance, NotificationResource, $filter, $logPanel, $dialogs, $rootScope, APP_CONSTANTS) {

                $scope.searchFilter = {
                    text: null,
                    timeFrom: null,
                    timeTo: null
                };

                // Пометим все оповещения как прочтённые
                $http({
                    method: "POST",
                    url: "controller/actions/notification/markAsRead"
                }).success(function () {
                    $rootScope.$broadcast('UPDATE_NOTIFICATION_COUNT');
                });

                /**
                 * @description форматтер для поля 'Ссылка' для получения файла
                 * @param row строка таблицы
                 * @param cellValue значение ячейки
                 * @param options данные таблицы
                 * без cellValue и options ссылка формируется некорректно
                 */
                function linkFileFormatter(cellValue, options, row) {
                    if (row.reportId) {
                        return "<a target='_self' href='controller/actions/notification/" + row.reportId + "/download'>" + $filter('translate')('title.link.download') + "</a>";
                    } else {
                        return "";
                    }
                }

                // Грид оповещений
                $scope.notificationsGrid = {
                    ctrl: {},
                    value: [],
                    options: {
                        datatype: "angularResource",
                        angularResource: NotificationResource,
                        requestParameters: function () {
                            return {
                                text: $scope.searchFilter.text,
                                timeFrom: $scope.searchFilter.timeFrom ? $scope.searchFilter.timeFrom.format("dd.mm.yyyy'T'HH:MM") : null,
                                timeTo: $scope.searchFilter.timeTo ? $scope.searchFilter.timeTo.format("dd.mm.yyyy'T'HH:MM") : null
                            };
                        },
                        height: 250,
                        colNames: [
                            $filter('translate')('notifications.title.createDate'),
                            $filter('translate')('notifications.title.content'),
                            $filter('translate')('notifications.title.link')
                        ],
                        colModel: [
                            {
                                name: 'createDate',
                                index: 'create_date',
                                width: 140,
                                formatter: $filter('dateTimeFormatter')
                            },
                            {
                                name: 'text',
                                index: 'text',
                                width: 700,
                                formatter: $filter('notificationTextFormatter'),
                                classes: 'grid-cell-white-space'
                            },
                            {
                                name: 'link',
                                index: 'link',
                                width: 80,
                                sortable: false,
                                formatter: linkFileFormatter
                            }

                        ],
                        rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                        rowList: APP_CONSTANTS.COMMON.PAGINATION,
                        viewrecords: true,
                        sortname: 'create_date',
                        sortorder: "desc",
                        hidegrid: false,
                        multiselect: true
                    }
                };

                $scope.refreshGrid = function (page) {
                    $scope.notificationsGrid.ctrl.refreshGrid(page);
                };

                /**
                 * @description Выбрана ли хотя бы одна запись в таблице оповещений.
                 * @returns {boolean}
                 */
                $scope.anyChecked = function () {
                    return ($scope.notificationsGrid.value && $scope.notificationsGrid.value.length > 0)
                };

                /**
                 * @description Удаление выбранных в гриде оповещений
                 */
                $scope.delete = function () {
                    if ($scope.anyChecked()) {
                        $dialogs.confirmDialog({
                            title: $filter('translate')('notifications.title.delete'),
                            content: $filter('translate')('notifications.title.deleteText'),
                            okBtnCaption: $filter('translate')('common.button.yes'),
                            cancelBtnCaption: $filter('translate')('common.button.no'),
                            okBtnClick: function () {
                                $http({
                                    method: "POST",
                                    url: "controller/actions/notification/delete",
                                    data: $filter('idExtractor')($scope.notificationsGrid.value)
                                }).success(function () {
                                    $scope.notificationsGrid.ctrl.refreshGrid();
                                });
                            }
                        });
                    }
                };

                /**
                 * Формирование адреса для выгрузки выбранных оповещений.
                 * @returns {string} адрес
                 */
                $scope.downloadSelectedLink = function () {
                    var address = "controller/actions/notification/downloadCsv?";
                    var idsParam = $httpParamSerializer({
                        ids: $filter('idExtractor')($scope.notificationsGrid.value)
                    });
                    return address + idsParam;
                };

                /**
                 * @description Закрытие окна
                 */
                $scope.close = function () {
                    $modalInstance.dismiss();
                };

                // Открытие панели уведомлений при клике по ссылке в оповещении
                $(document).undelegate('#notificationsTable .notification-link', 'click');
                $(document).delegate('#notificationsTable .notification-link', 'click', function () {
                    var logId = $(this).attr('data-log-id');
                    $logPanel.open('log-panel-container', logId);
                });
            }])
        /**
         * @description Фильтр для формирования ссылки на оповещение
         */
        .filter('notificationTextFormatter', [function () {
            return function (value, row, notification) {
                if (notification.logId) {
                    return '<a class="notification-link" data-log-id="' + notification.logId + '">' + value + '</a>';
                } else {
                    return value;
                }
            };
        }]);
}());
