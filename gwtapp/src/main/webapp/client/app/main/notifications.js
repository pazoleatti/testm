(function () {
    'use strict';
    /**
     * @description Модуль модального окна оповещений
     */
    angular.module('app.notifications', ['app.modals'])
    /**
     * @description Контроллер модального окна оповещений
     */
        .controller('notificationsCtrl', ['$scope', '$http', '$uibModalInstance', 'NotificationResource', '$filter', '$logPanel', 'appModals', '$rootScope',
            function ($scope, $http, $uibModalInstance, NotificationResource, $filter, $logPanel, appModals, $rootScope) {
                // Пометим все оповещения как прочтённые
                $http({
                    method: "POST",
                    url: "controller/actions/notification/markAsRead"
                }).success(function () {
                    $rootScope.$broadcast('UPDATE_NOTIFICATION_COUNT');
                });

                // Грид оповещений
                $scope.notificationsGrid = {
                    ctrl: {},
                    value: [],
                    options: {
                        datatype: "angularResource",
                        angularResource: NotificationResource,
                        height: 250,
                        colNames: [
                            $filter('translate')('notifications.title.createDate'),
                            $filter('translate')('notifications.title.content'),
                            $filter('translate')('notifications.title.link'),
                            ""
                        ],
                        colModel: [
                            {
                                name: 'createDate',
                                index: 'createDate',
                                width: 135,
                                formatter: $filter('dateTimeFormatter')
                            },
                            {
                                name: 'text',
                                index: 'text',
                                width: 520,
                                formatter: $filter('notificationTextFormatter'),
                                classes: 'grid-cell-white-space'
                            },
                            {
                                name: 'link',
                                index: 'link',
                                width: 175,
                                sortable: false,
                                formatter: linkFileFormatter
                            },
                            {
                                name: 'reportId',
                                index: 'reportId',
                                hidden: true,
                                width: 175,
                                sortable: false
                            }

                        ],
                        rowNum: 10,
                        rowList: [10, 20, 30],
                        viewrecords: true,
                        sortname: 'createDate',
                        sortorder: "desc",
                        hidegrid: false,
                        multiselect: true
                    }
                };


                /**
                 * @description форматтер для поля 'Ссылка' для получения файла
                 * @param value значение столбца
                 * @param row строка таблицы
                 */
                function linkFileFormatter(value, row) {
                    if (row.reportId && row.reportId !== undefined) {
                        return "<a target='_blank' href='controller/rest/blobData/" + row.reportId + "/conf'>" + $filter('translate')('title.link.download') + " </a>";

                    } else {
                        return "";
                    }
                }


                /**
                 * @description Удаление выбранных в гриде оповещений
                 */
                $scope.delete = function () {
                    if ($scope.notificationsGrid.value && $scope.notificationsGrid.value.length !== 0) {
                        appModals.confirm($filter('translate')('notifications.title.delete'), $filter('translate')('notifications.title.deleteText'))
                            .result.then(
                            function () {
                                $http({
                                    method: "POST",
                                    url: "controller/actions/notification/delete",
                                    params: {
                                        ids: $filter('idExtractor')($scope.notificationsGrid.value)
                                    }
                                }).success(function () {
                                    $scope.notificationsGrid.ctrl.refreshGrid();
                                });
                            });
                    }
                };

                // TODO: Убрать после https://jira.aplana.com/browse/SBRFNDFL-1671
                /**
                 * @description Закрытие окна
                 */
                $scope.close = function () {
                    $uibModalInstance.dismiss('Canceled');
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
        .filter('notificationTextFormatter', ['$filter', function ($filter) {
            return function (value, row, notification) {
                return '<a class="notification-link" data-log-id="' + notification.logId + '">' + value + '</a>';

            };
        }]);


}());