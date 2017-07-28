(function () {
    'use strict';

    /**
     * @description Модуль для работы с модальным окном 'Информация по налоговой форме'"
     */
    angular.module('app.logBusines', [])
    /**
     * @description Контроллер формы создания/ Информация по налоговой форме
     */
        .controller('logBusinesFormCtrl', ['$scope', '$uibModalInstance', '$filter', '$http', 'LogBusinesResource', function ($scope, $uibModalInstance, $filter, $http, LogBusinesResource) {
            // Инициализация данных по изменению налоговой формы
            LogBusinesResource.query({
                formId: $scope.$resolve.data.declarationId
            }, function (data) {
                $scope.logBusinesGrid.ctrl.refreshGridData(data);
            });

            /**
             * @description Закрытие окна
             */
            $scope.close = function () {
                $uibModalInstance.dismiss('Canceled');
            };

            /**
             * @description Создание и заполнение грида
             */
            $scope.logBusinesGrid = {
                options: {
                    datatype: "local",
                    height: 250,
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
                        {name: 'eventName', index: 'eventName', width: 100},
                        {name: 'logDate', index: 'logDate', width: 100, formatter: $filter('dateFormatter')},
                        {name: 'userFullName', index: 'userFullName', width: 140},
                        {name: 'roles', index: 'roles', width: 165},
                        {name: 'departmentName', index: 'departmentName', width: 220},
                        {name: 'note', index: 'note', width: 140}
                    ]
                }
            };

        }]);
}());

