(function () {
    'use strict';

    /**
     * @description Модуль для работы окном 'Общие параметры'
     */

    angular.module('app.commonParams', ['ui.router', 'app.rest', 'app.logPanel', 'app.editParams', 'app.confirmationAction'])

        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('commonParams', {
                url: '/taxes/commonParams/editCommonParams',
                templateUrl: 'client/app/taxes/commonParams/commonParams.html?v=${buildUuid}',
                controller: 'commonParamsCtrl'
            });
        }])

        /**
         * @description Контроллер для общих параметров
         */
        .controller('commonParamsCtrl', ['$scope', 'CommonParams', '$filter', '$http', '$aplanaModal', 'APP_CONSTANTS',
            function ($scope, CommonParams, $filter, $http, $aplanaModal, APP_CONSTANTS) {

                /**
                 * @description Создание и заполнение грида
                 */
                $scope.commonParamsGrid = {
                    ctrl: {},
                    value: [],
                    options: {
                        datatype: "angularResource",
                        angularResource: CommonParams,
                        height: 280,
                        colNames: [
                            "",
                            $filter('translate')('title.comParams.param'),
                            $filter('translate')('title.comParams.paramValue')
                        ],
                        colModel: [
                            {name: 'departmentId', index: 'departmentId', hidden: true, key: true},
                            {name: 'code', index: 'code', width: 500, key: true},
                            {name: 'value', index: 'value', width: 700, key: true}
                        ],

                        rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                        rowList: APP_CONSTANTS.COMMON.PAGINATION,
                        sortname: 'departmentId',
                        viewrecords: true,
                        sortorder: "asc",
                        hidegrid: false,
                        multiselect: false
                    }
                };

                /**
                 * @description Изменение общих параметров на значения по умолчанию
                 */
                $scope.changeToDefault = function () {
                    $aplanaModal.open({
                        title: $filter('translate')('title.commonParams.default'),
                        templateUrl: 'client/app/taxes/commonParams/confirmationAction.html?v=${buildUuid}',
                        controller: 'confirmationActionCtrl',
                        windowClass: 'modal600',
                        resolve: {
                            commonParamsGrid: function () {
                                return $scope.commonParamsGrid;
                            }
                        }
                    });
                };

                /**
                 * @description Редактирование параметра
                 */
                $scope.edit = function () {
                    $scope.valOiu = $scope.commonParamsGrid.value;

                    $aplanaModal.open({
                        title: $filter('translate')('title.redactParametr'),
                        templateUrl: 'client/app/taxes/commonParams/editCommonParams.html?v=${buildUuid}',
                        controller: 'editParamsCtrl',
                        windowClass: 'modal600',
                        resolve: {
                            commonParamsGrid: function () {
                                return $scope.commonParamsGrid;
                            }
                        }
                    });

                };

            }]);
}());


