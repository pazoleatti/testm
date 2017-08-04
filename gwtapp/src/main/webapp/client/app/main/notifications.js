(function () {
    'use strict';

    /**
     * @description Модуль для работы со формами ндфл
     */
    angular.module('app.notifications', [])
    /**
     * @description Контроллер формы создания/редактирования ФЛ
     */
        .controller('notificationsFormCtrl', ["$scope", "$http", "$uibModalInstance", "NotificationResource", "$filter", 'dialogs', '$logPanel', '$rootScope',
            function ($scope, $http, $uibModalInstance, NotificationResource, $filter, dialogs, $logPanel, $rootScope) {
                $http({
                    method: "PUT",
                    url: "controller/actions/notification/markAsRead"
                }).success(function () {
                    $rootScope.$broadcast('UPDATE_NOTIF_COUNT');
                });

                $scope.notificationsGrid =
                    {
                        ctrl: {},
                        value: [],
                        options: {
                            datatype: "angularResource",
                            angularResource: NotificationResource,
                            requestParameters: function () {
                                return {
                                    projection: 'notifications'
                                };
                            },
                            height: 250,
                            colNames: [
                                $filter('translate')('notifications.title.createDate'),
                                $filter('translate')('notifications.title.content'),
                                $filter('translate')('notifications.title.link')],
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
                                    formatter: $filter('notificationTextFormatter')
                                },
                                {name: 'reportId', index: 'reportId', width: 175, sortable: false}
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
                 * @description Удаление оповещения
                 */
                $scope.deleteNotification = function () {
                    var buttons = {
                        labelYes: $filter('translate')('common.button.yes'),
                        labelNo: $filter('translate')('common.button.no')
                    };

                    var opts = {
                        size: 'md'
                    };

                    if ($scope.notificationsGrid.value && $scope.notificationsGrid.value.length !== 0) {
                        var dlg = dialogs.confirm($filter('translate')('notifications.title.delete'), $filter('translate')('notifications.title.deleteText'), buttons, opts);
                        dlg.result.then(
                            function () {
                                var ids = [];
                                _.each($scope.notificationsGrid.value, function (element) {
                                    ids.push(element.id);
                                });

                                $http({
                                    method: "POST",
                                    url: "controller/actions/notification/delete",
                                    params: {
                                        ids: ids
                                    }
                                }).success(function () {
                                    $scope.notificationsGrid.ctrl.refreshGrid();
                                });
                            },
                            function () {

                            });
                    }
                };

                /**
                 * @description Закрытие окна
                 */
                $scope.close = function () {
                    $uibModalInstance.dismiss('Canceled');
                };

                $(document).undelegate('#notificationsTable .notification-link', 'click');
                $(document).delegate('#notificationsTable .notification-link', 'click', function () {
                    var logId = $(this).attr('log-id');
                    $logPanel.open('log-panel-container', logId);
                });
            }])

        .filter('notificationTextFormatter', ['$filter', function ($filter) {
            return function (value, row, notificationObject) {
                return '<a class="notification-link" log-id="' + notificationObject.logId + '">' + value + '</a>';
            };
        }]);
}());