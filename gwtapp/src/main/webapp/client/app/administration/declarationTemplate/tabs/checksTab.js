(function () {
    'use strict';
    /**
     * @description Модуль для работы со вкладкой "Проверки" на форме макета
     */
    angular.module('app.templateChecksTab', [])
        .controller('TemplateChecksTabCtrl', [
            '$scope', '$stateParams', '$filter', 'APP_CONSTANTS', 'DeclarationTemplateResource',
            function ($scope, $stateParams, $filter, APP_CONSTANTS, DeclarationTemplateResource) {

                $scope.tab = $scope.checksTab;

                // При переключению на вкладку загружаем данные
                $scope.$watch("checksTab.active", function (newValue, oldValue) {
                    if (newValue && !oldValue) {
                        DeclarationTemplateResource.querySource({
                                declarationTypeId: $scope.declarationTemplate.type.id,
                                declarationTemplateId: $scope.declarationTemplate.id,
                                projection: "fetchChecks"
                            },
                            function (data) {
                                if (data) {
                                    $scope.templateChecksGrid.ctrl.refreshGridData(data);
                                }
                            }
                        )
                    }
                });

                $scope.templateChecksGrid = {
                    ctrl: {},
                    value: [],
                    options: {
                        datatype: "local",
                        data: [],
                        colNames: [
                            '',
                            $filter('translate')('declarationTemplate.tabs.checks.fatality'),
                            $filter('translate')('declarationTemplate.tabs.checks.code'),
                            $filter('translate')('declarationTemplate.tabs.checks.type'),
                            $filter('translate')('declarationTemplate.tabs.checks.description')],
                        colModel: [
                            {name: 'id', index: 'id', key: true, hidden: true},
                            {
                                name: 'fatal',
                                index: 'fatal',
                                width: 150,
                                editable: true,
                                edittype: 'checkbox',
                                editoptions: {value: "True:False"},
                                formatter: "checkbox",
                                align: "center",
                                formatoptions: {disabled: false}
                            },
                            {name: 'code.code', index: 'code.code', width: 200},
                            {name: 'checkType', index: 'checkType', width: 500},
                            {name: 'description', index: 'description', width: 500}],
                        rowNum: 5,
                        rowList: [3, 5, 7],
                        cellEdit: true,
                        cellsubmit: 'clientArray',
                        sortname: 'id',
                        viewrecords: true,
                        sortorder: "asc",
                        hidegrid: false
                    }
                };
            }]);
}());