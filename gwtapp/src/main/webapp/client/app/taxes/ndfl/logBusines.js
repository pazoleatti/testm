/**
 * Created by AFateev on 18.07.2017.
 */

(function () {
    'use strict';
    angular.module('sbrfNdfl.logBusines', [])
        .controller('logBusinesFormCtrl', ['$scope', '$uibModalInstance',  "$filter", '$http','data',  function ($scope, $uibModalInstance, $filter, $http ) {

            $scope.close = function () {
                $uibModalInstance.dismiss('Canceled');
            };

            $scope.logBusinesGrid = {
                options: {
                    datatype: "local",
                    height: 250,
                    colNames: [
                        'id',
                        'Событие',
                        'Дата-время',
                        'Пользователь',
                        'Роли пользователя',
                        'Подразделение пользователя',
                        'Текст события'
                    ],
                    colModel: [
                        {name: 'id', index: 'id', width: 176, key: true, hidden: true},
                        {name: 'eventName', index: 'eventName', width: 100},
                        {name: 'logDate', index: 'logDate', width: 100, formatter: $filter('dateFormatter')},
                        {name: 'userFullName', index: 'userFullName', width: 120},
                        {name: 'roles', index: 'roles', width: 155},
                        {name: 'departmentName', index: 'departmentName', width: 220},
                        {name: 'note', index: 'note', width: 135}


                    ]
                }
            };

            $http.get('controller/rest/logBusines/'+$scope.$resolve.data.declarationId)//15953'
                .then(
                    function (response) {
                        $scope.logBusinesGrid.ctrl.refreshGridData(response.data);
                    }
                );
        }]);
}());

