(function () {
    'use strict';

    /**
     * @description Модуль для работы с формой 'Налоги - Общие параметры'
     */

    angular.module('app.commonParams', ['ui.router', 'app.rest', 'app.editParams'])

        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('commonParams', {
                url: '/taxes/commonParams/editCommonParams',
                templateUrl: 'client/app/taxes/commonParams/commonParams.html?v=${buildUuid}',
                controller: 'commonParamsCtrl',
                onEnter: ['$state', 'PermissionChecker', 'APP_CONSTANTS', '$rootScope',
                    function ($state, PermissionChecker, APP_CONSTANTS, $rootScope) {
                        if (!PermissionChecker.check($rootScope.user, APP_CONSTANTS.USER_PERMISSION.VIEW_TAXES_GENERAL)) {
                            $state.go("/");
                        }
                    }]
            });
        }])

        /**
         * @description Контроллер для общих параметров
         */
        .controller('commonParamsCtrl', ['$scope', 'CommonParamResource', '$filter', '$http', '$aplanaModal', 'APP_CONSTANTS',
            '$dialogs', 'LogEntryResource', '$logPanel',
            function ($scope, CommonParamResource, $filter, $http, $aplanaModal, APP_CONSTANTS, $dialogs) {

                /**
                 * @description Создание и заполнение грида
                 */
                $scope.commonParamsGrid = {
                    ctrl: {},
                    value: [],
                    options: {
                        datatype: "angularResource",
                        angularResource: CommonParamResource,
                        requestParameters: function () {
                            return {
                                projection: 'taxes'
                            };
                        },
                        height: 280,
                        colNames: [
                            "",
                            $filter('translate')('title.comParams.param'),
                            $filter('translate')('title.comParams.paramValue')
                        ],
                        colModel: [
                            {name: 'departmentId', index: 'departmentId', hidden: true},
                            {name: 'description', index: 'code', width: 500, key: true},
                            {name: 'value', index: 'value', width: 700}
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
                    $dialogs.confirmDialog({
                        content: $filter('translate')('title.acceptCommonParamsDefault'),
                        okBtnCaption: $filter('translate')('common.button.yes'),
                        cancelBtnCaption: $filter('translate')('DIALOGS_CLOSE'),
                        okBtnClick: function () {
                            $http({
                                method: "POST",
                                url: "controller/actions/resetCommonParams"
                            }).then(function () {
                                $scope.commonParamsGrid.ctrl.refreshGrid();
                            });
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
                            parameter: function () {
                                return $scope.commonParamsGrid.value[0];
                            }
                        }
                    }).result.then(function () {
                        $scope.commonParamsGrid.ctrl.refreshGrid();
                    });

                };

            }]);
}());


