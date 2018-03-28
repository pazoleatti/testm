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

                // Загружает данные о фатальности проверок
                $scope.tab.loadChecks = function () {
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
                    );
                };

                if ($scope.tab.needLoadChecks) {
                    $scope.tab.loadChecks();
                }

                // Инициализация грида
                var init = function (ctrl) {
                    ctrl.beforeSelectRow = function (rowid, e) {
                        var iCol = $.jgrid.getCellIndex($(e.target).closest("td")[0]);
                        if ($scope.templateChecksGrid.options.colModel[iCol].name === "fatal") {
                            // Устанавливаем значение фатальности из checkBox'a
                            $scope.templateChecksGrid.ctrl.grid.getLocalRow(rowid).fatal = $(e.target).is(":checked");
                        }
                        return true; // allow selection
                    };
                };

                // Настройки грида
                $scope.tab.grid = $scope.templateChecksGrid = {
                    init: init,
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
                                formatter: "checkbox",
                                align: "center",
                                formatoptions: {disabled: false}
                            },
                            {
                                name: 'code',
                                index: 'code',
                                width: 200,
                                formatter: $filter('declarationCheckCodeEnumFormatter')
                            },
                            {name: 'checkType', index: 'checkType', width: 500},
                            {name: 'description', index: 'description', width: 500}],
                        rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                        rowList: APP_CONSTANTS.COMMON.PAGINATION,
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