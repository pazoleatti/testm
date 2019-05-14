(function () {
    'use strict';

    /**
     * @description Модуль для работы с модальным окном 'Информация по налоговой форме'"
     */
    angular.module('app.logBusines', [])
    /**
     * @description Контроллер формы создания/ Информация по налоговой форме
     */
        .controller('logBusinesFormCtrl', ['$scope', '$modalInstance', '$shareData', '$filter', '$http', 'LogBusinessResource', 'APP_CONSTANTS', '$logPanel',
            function ($scope, $modalInstance, $shareData, $filter, $http, LogBusinessResource, APP_CONSTANTS, $logPanel) {
                /**
                 * @description Закрытие окна
                 */
                $scope.close = function () {
                    $modalInstance.dismiss();
                };

                /**
                 * @description Создание и заполнение грида
                 */
                $scope.logBusinesGrid = {
                    ctrl: {},
                    options: {
                        datatype: "angularResource",
                        angularResource: LogBusinessResource,
                        requestParameters: function () {
                            return {
                                projection: "declarationBusinessLogs",
                                objectId: $shareData.declarationDataId
                            };
                        },
                        value: [],
                        colNames: [
                            '',
                            $filter('translate')('logBusiness.title.event'),
                            $filter('translate')('logBusiness.title.logDate'),
                            $filter('translate')('logBusiness.title.user'),
                            $filter('translate')('logBusiness.title.rolesUser'),
                            $filter('translate')('logBusiness.title.departmentUser'),
                            $filter('translate')('logBusiness.title.logId'),
                            $filter('translate')('logBusiness.title.note')
                        ],
                        colModel: [
                            {name: 'id', index: 'id', key: true, hidden: true},
                            {name: 'eventName', index: 'event_name', width: 170},
                            {name: 'logDate', index: 'log_date', width: 130, formatter: $filter('dateTimeFormatter')},
                            {name: 'userName', index: 'user_name', width: 170},
                            {name: 'roles', index: 'roles', width: 150},
                            {name: 'userDepartmentName', index: 'user_department_name', width: 150},
                            {name: 'logId', index: 'logId', width: 80, sortable: false, formatter: logIdLinkFormatter},
                            {name: 'note', index: 'note', width: 270}
                        ],
                        rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                        rowList: APP_CONSTANTS.COMMON.PAGINATION,
                        sortname: 'log_date',
                        sortorder: "desc",
                        hidegrid: false
                    }
                };

                function logIdLinkFormatter(value, options, row) {
                    if (row.logId) {
                        return '<a class="notification-link" data-log-id="' + row.logId + '">' + $filter('translate')('logBusiness.title.logId') + '</a>';
                    } else {
                        return '';
                    }
                }

                // Открытие панели уведомлений при клике по ссылке в оповещении
                $(document).undelegate('#logBusinesGrid .notification-link', 'click');
                $(document).delegate('#logBusinesGrid .notification-link', 'click', function () {
                    var logId = $(this).attr('data-log-id');
                    $logPanel.open('log-panel-container', logId);
                });

            }]);
}());

