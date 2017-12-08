(function () {
    'use strict';

    /**
     * @description Модуль для работы с модальным окном 'Информация по налоговой форме'"
     */
    angular.module('app.logBusines', [])
    /**
     * @description Контроллер формы создания/ Информация по налоговой форме
     */
        .controller('logBusinesFormCtrl', ['$scope', '$modalInstance', '$shareData', '$filter', '$http', 'DeclarationDataResource', 'APP_CONSTANTS',
            function ($scope, $modalInstance, $shareData, $filter, $http, DeclarationDataResource, APP_CONSTANTS) {
            /**
             * @description Закрытие окна
             */
            $scope.close = function () {
                $modalInstance.dismiss('Canceled');
            };

            /**
             * @description Создание и заполнение грида
             */
            $scope.logBusinesGrid = {
                ctrl: {},
                options: {
                    datatype: "angularResource",
                    angularResource: DeclarationDataResource,
                    requestParameters: function () {
                        return {
                            projection: "businessLogs",
                            declarationDataId: $shareData.declarationDataId
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
                        $filter('translate')('logBusiness.title.note')

                    ],
                    colModel: [
                        {name: 'id', index: 'id', width: 176, key: true, hidden: true},
                        {name: 'eventName', index: 'event', width: 216},
                        {name: 'logDate', index: 'date', width: 167, formatter: $filter('dateTimeFormatter')},
                        {name: 'userFullName', index: 'user', width: 173},
                        {name: 'roles', index: 'user_role', width: 165},
                        {name: 'departmentName', index: 'department', width: 220},
                        {name: 'note', index: 'note', width: 273}
                    ],
                    rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                    rowList: APP_CONSTANTS.COMMON.PAGINATION,

                    sortname: 'logDate',
                    sortorder: "desc",
                    viewrecords: true,
                    hidegrid: false
                }
            };

        }]);
}());

