    'use strict';
    (function () {

    angular.module('mtsUsim.productionPlanning', ['ui.router'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('productionPlanning', {
                url: '/production/planning',
                templateUrl: 'js/production/planning/planning.html',
                controller: 'planningCtrl'
            })
        }])
        .controller('planningCtrl', [
            '$scope', '$timeout', 'aplanaEntityUtils', '$state', 'aplanaDialogs',
            function ($scope, $timeout, aplanaEntityUtils, $state, aplanaDialogs) {
                $scope.customGridColumnsBuilder = function (dataOptions, gridOptions) {
                    for (var fieldName in dataOptions.metaData) {
                        var field = dataOptions.metaData[fieldName];
                        var columnDef = {};
                        columnDef.name = field.name;
                        columnDef.displayName = field.title;
                        columnDef.enableHiding = false;
                        columnDef.cellTooltip = true;
                        columnDef.visible = field.visible;
                        columnDef.width = field.width || 100;
                        if (field.name == 'planState') {
                            //Вставляем картинку по урлу из displayField
                            columnDef.cellTemplate = '<div title="{{row.entity.planStateId == 0 ? \'В плане\' : row.entity.planStateId == 1 ? \'В производстве\' : \'Заказ на удаление\'}}" ' +
                                'style="text-align: center;" ><img ng-src="{{grid.getCellValue(row, col)}}" /></div>';
                        }
                        columnDef.cellClass = function(grid, row, col, rowRenderIndex, colRenderIndex) {
                            if (row.entity.planStateId == 2) {
                                return 'deletedRow';
                            }
                        };

                        //Если предусмотрены настройки сортировки, устанавливаем их
                        columnDef.enableSorting = field.type !== 'java.util.SortedSet' && field.type !== 'java.util.List' &&
                            (gridOptions.excludeSortColumns && gridOptions.excludeSortColumns.indexOf(field.name) == -1);

                        if (field.type == "java.util.Date") {
                            var format = field.format == "" ? "dd.MM.yyyy" : field.format;
                            columnDef.type = 'date';
                            columnDef.cellFilter = "date:'" + format + "'";
                        }
                        gridOptions.columnDefs.push(columnDef);
                    }

                    aplanaEntityUtils.fitColumnsWidth(gridOptions, $scope.gridApi.grid.gridWidth);
                };

                /**
                 * Получение данных с сервера
                 */
                function fetchData() {
                    var dataStub = {
                        "list":[
                            {"id":75824,"regionProductionOrder":{"id":78106,"region":{"id":59,"macroRegion":{"id":6,"name":"Сибирь","shortName":null,"description":null,"deleted":false,"beginDate":null,"delay":null,"threshold":null},"name":"Барнаул","address":null,"description":null,"trigram":null,"deleted":false},"profile":null,"code":null,"hlr":null,"amount":105,"fileInfo":{"id":413608,"name":null,"attacheDate":"2017-02-03","fileType":null,"size":0,"entityType":null,"entityId":null},"techFiles":null,"status":null,"macroRegionProductionOrder":{"id":16559,"orderNumber":"2179N16","macroRegion":null,"tariff":{"id":509,"code":0,"name":"Комплект МТС-Коннект LTE (роутер)","shortName":null,"techName":null,"personality":true,"planDateUnlimited":false,"dualSim":false,"cardTypeList":null,"prodPeriod":null,"description":null,"deleted":false,"productRep":false,"codeStr":"000"},"cardType":{"id":null,"name":"320K","description":null,"deleted":false},"totalAmount":0,"formationDate":null,"shipmentDate":null,"vendor":{"id":null,"name":"АО НоваКард","shortName":null,"contractNumber":null,"contractDate":null,"description":null,"nds":0,"returnAddress":null,"orderPrefix":null,"orderPostfix":null,"currentNumber":null,"deleted":false,"contractInfo":null,"ndsAsDouble":0},"status":null,"price":null,"description":null,"month":0,"year":0,"reqDeliveryDate":null,"totalPrice":null,"nds":null},"description":null,"orderDetails":null,"fileName":null,"returnedFromPlanning":null,"hasAllFiles":null,"attachFilesStatusDate":null,"macroRegion":null,"tariffCode":"000","orderNumber":"2179N16","vendor":{"id":null,"name":"АО НоваКард","shortName":null,"contractNumber":null,"contractDate":null,"description":null,"nds":0,"returnAddress":null,"orderPrefix":null,"orderPostfix":null,"currentNumber":null,"deleted":false,"contractInfo":null,"ndsAsDouble":0},"tariff":{"id":509,"code":0,"name":"Комплект МТС-Коннект LTE (роутер)","shortName":null,"techName":null,"personality":true,"planDateUnlimited":false,"dualSim":false,"cardTypeList":null,"prodPeriod":null,"description":null,"deleted":false,"productRep":false,"codeStr":"000"},"cardType":{"id":null,"name":"320K","description":null,"deleted":false},"trigram":null,"personality":"Перс.","tariffTechName":null,"fileReceiveDate":"2017-02-03"},"readyDate":null,"reqDeliveryDate":"2017-03-02","planState":{"name":"В плане","id":0,"imgUrl":"img/icon/statePlan.png","entityStatus":{"id":81,"title":"Статус производства: В плане","entityType":null,"finalStatus":false}},"quantity":105,"priorityDate":"2017-02-20","planDeliveryDate":null,"status":{"id":25,"title":"Принят в производство","entityType":{"id":5,"description":"Планирование","tableName":"usim_production_plan","entityClass":"com.mts.usim.model.ProductionPlan"},"finalStatus":false},"planComment":"daysdgausydhiua i a dhasiudh asuydhsauygdsaygdaysdua idiajsdi ahsduysagdyasgdisao ajsod hasd sagydah si djsaoid jasid gastyd gausd","filePostfix":null,"fileName":"BAR99110","fullFileName":"BAR99110","fileDate":null,"deliveryDate":null,"macroRegion":{"id":6,"name":"Сибирь","shortName":null,"description":null,"deleted":false,"beginDate":null,"delay":null,"threshold":null},"orderNumber":"2179N16","vendorName":"АО НоваКард","macroRegionName":"Сибирь","regionName":"Барнаул","tariffPlanDateUnlimited":false,"cardTypeName":"320K","planStateId":0,"tariffName":"Комплект МТС-Коннект LTE (роутер)"},
                            {"id":75889,"regionProductionOrder":{"id":78112,"region":{"id":59,"macroRegion":{"id":6,"name":"Сибирь","shortName":null,"description":null,"deleted":false,"beginDate":null,"delay":null,"threshold":null},"name":"Барнаул","address":null,"description":null,"trigram":null,"deleted":false},"profile":null,"code":null,"hlr":null,"amount":385,"fileInfo":{"id":413634,"name":null,"attacheDate":"2017-02-03","fileType":null,"size":0,"entityType":null,"entityId":null},"techFiles":null,"status":null,"macroRegionProductionOrder":{"id":16561,"orderNumber":"2181N16","macroRegion":null,"tariff":{"id":337,"code":0,"name":"Комплект МТС-Коннект-LTE(модем)","shortName":null,"techName":null,"personality":true,"planDateUnlimited":false,"dualSim":false,"cardTypeList":null,"prodPeriod":null,"description":null,"deleted":false,"productRep":false,"codeStr":"000"},"cardType":{"id":null,"name":"320K","description":null,"deleted":false},"totalAmount":0,"formationDate":null,"shipmentDate":null,"vendor":{"id":null,"name":"АО НоваКард","shortName":null,"contractNumber":null,"contractDate":null,"description":null,"nds":0,"returnAddress":null,"orderPrefix":null,"orderPostfix":null,"currentNumber":null,"deleted":false,"contractInfo":null,"ndsAsDouble":0},"status":null,"price":null,"description":null,"month":0,"year":0,"reqDeliveryDate":null,"totalPrice":null,"nds":null},"description":null,"orderDetails":null,"fileName":null,"returnedFromPlanning":null,"hasAllFiles":null,"attachFilesStatusDate":null,"macroRegion":null,"tariffCode":"000","orderNumber":"2181N16","vendor":{"id":null,"name":"АО НоваКард","shortName":null,"contractNumber":null,"contractDate":null,"description":null,"nds":0,"returnAddress":null,"orderPrefix":null,"orderPostfix":null,"currentNumber":null,"deleted":false,"contractInfo":null,"ndsAsDouble":0},"tariff":{"id":337,"code":0,"name":"Комплект МТС-Коннект-LTE(модем)","shortName":null,"techName":null,"personality":true,"planDateUnlimited":false,"dualSim":false,"cardTypeList":null,"prodPeriod":null,"description":null,"deleted":false,"productRep":false,"codeStr":"000"},"cardType":{"id":null,"name":"320K","description":null,"deleted":false},"trigram":null,"personality":"Перс.","tariffTechName":null,"fileReceiveDate":"2017-02-03"},"readyDate":null,"reqDeliveryDate":"2017-03-02","planState":{"name":"В производстве","id":1,"imgUrl":"img/icon/stateProduction.png","entityStatus":{"id":82,"title":"Статус производства: В производстве","entityType":null,"finalStatus":false}},"quantity":235,"priorityDate":"2017-02-20","planDeliveryDate":null,"status":{"id":25,"title":"Принят в производство","entityType":{"id":5,"description":"Планирование","tableName":"usim_production_plan","entityClass":"com.mts.usim.model.ProductionPlan"},"finalStatus":false},"planComment":null,"filePostfix":"2","fileName":"BAR99116","fullFileName":"BAR99116_2","fileDate":null,"deliveryDate":null,"macroRegion":{"id":6,"name":"Сибирь","shortName":null,"description":null,"deleted":false,"beginDate":null,"delay":null,"threshold":null},"orderNumber":"2181N16","vendorName":"АО НоваКард","macroRegionName":"Сибирь","regionName":"Барнаул","tariffPlanDateUnlimited":false,"cardTypeName":"320K","planStateId":1,"tariffName":"Комплект МТС-Коннект-LTE(модем)"}],
                        "metaData":[
                            {"name":"planState","type":"com.mts.usim.model.PlanState","referenceType":"void","displayField":"imgUrl","title":"","width":40,"order":1,"visible":true,"format":"","precision":0,"maxLength":null,"minLength":null,"max":null,"min":null,"required":false,"readOnly":false,"unique":false,"pattern":"","defaultValue":0,"enableColumnMenu":true,"canSelectAll":false,"uniqueGroup":false},
                            {"name":"regionName","type":"java.lang.String","referenceType":"void","displayField":"","title":"Регион","width":120,"order":3,"visible":true,"format":"","precision":0,"maxLength":null,"minLength":null,"max":null,"min":null,"required":false,"readOnly":false,"unique":false,"pattern":"","defaultValue":0,"enableColumnMenu":true,"canSelectAll":false,"uniqueGroup":false},
                            {"name":"orderNumber","type":"java.lang.String","referenceType":"void","displayField":"","title":"Номер заказа","width":120,"order":5,"visible":true,"format":"","precision":0,"maxLength":null,"minLength":null,"max":null,"min":null,"required":false,"readOnly":false,"unique":false,"pattern":"","defaultValue":0,"enableColumnMenu":true,"canSelectAll":false,"uniqueGroup":false},
                            {"name":"status","type":"com.mts.usim.model.EntityStatus","referenceType":"void","displayField":"title","title":"Статус","width":210,"order":11,"visible":true,"format":"","precision":0,"maxLength":null,"minLength":null,"max":null,"min":null,"required":false,"readOnly":false,"unique":false,"pattern":"","defaultValue":0,"enableColumnMenu":true,"canSelectAll":false,"uniqueGroup":false},
                            {"name":"priorityDate","type":"java.util.Date","referenceType":"void","displayField":"","title":"Приоритет","width":100,"order":9,"visible":true,"format":"","precision":0,"maxLength":null,"minLength":null,"max":null,"min":null,"required":false,"readOnly":false,"unique":false,"pattern":"","defaultValue":0,"enableColumnMenu":true,"canSelectAll":false,"uniqueGroup":false},
                            {"name":"planComment","type":"java.lang.String","referenceType":"void","displayField":"","title":"Комментарий","width":210,"order":12,"visible":true,"format":"","precision":0,"maxLength":null,"minLength":null,"max":null,"min":null,"required":false,"readOnly":false,"unique":false,"pattern":"","defaultValue":0,"enableColumnMenu":true,"canSelectAll":false,"uniqueGroup":false}],
                        "offset":1,
                        "total":2,
                        "count":2};
                    return aplanaEntityUtils.fillGrid($scope, dataStub, $scope.customGridColumnsBuilder)
                        .then(setButtonsEnabled);
                    //return aplanaEntityUtils.fetchData('rest/entity/light/ProductionPlan', $scope)
                    //    .then(setButtonsEnabled);
                }

                /**
                 * Инициализация кнопок
                 */
                function setButtonsEnabled() {
                    $scope.exportButtonEnabled = aplanaEntityUtils.isTableHasValue($scope);
                    $scope.createButtonEnabled = true;
                    $scope.searchButtonEnabled = true;
                    $scope.clearButtonEnabled = true;
                    if (!$scope.gridApi || !$scope.currentEntityView) {
                        $scope.searchButtonEnabled = false;
                        $scope.clearButtonEnabled = false;
                    } else {
                        $scope.selectedItems = aplanaEntityUtils.getSelectedEntities($scope);
                        var hasSelectedItems = $scope.gridApi.grid.selection.selectedCount > 0;
                        var hasSingleSelectedItem = $scope.gridApi.grid.selection.selectedCount == 1;

                        //доступно в статусе «Принят в производство» или «Корректировка» и указан Приоритет
                        //$scope.splitButtonEnabled = hasSingleSelectedItem
                        //    && aplanaEntityUtils.isSelectionHasValue($scope, 'priorityDate')
                        //    && aplanaEntityUtils.isSelectionHasValue($scope, 'status.id', [APP_CONSTANTS.ENTITY_STATUS.PLANNING_TO_PRODUCTION.id, APP_CONSTANTS.ENTITY_STATUS.PLANNING_CORRECTION.id]);
                    }
                }

                /**
                 * Обработчики нажатий на кнопки
                 */

                //Экспорт
                $scope.exportButtonClick = function () {
                    var params = aplanaEntityUtils.getRequestParams($scope.dataOptions);
                    aplanaEntityUtils.getReport($scope.gridApi, 'ProductionPlan', null, 'xlsx', params)
                };
                $scope.createButtonClick = function () {
                    var metaData = [
                        {
                            name: 'name',
                            title: 'Наименование',
                            type: 'java.lang.String',
                            ord: 1,
                            maxLength: 10,
                            readOnly: false,
                            required: true
                        }, {
                            name: 'date',
                            title: 'Дата',
                            type: 'java.util.Date',
                            ord: 2
                        }, {
                            name: 'count',
                            title: 'Количество',
                            min: 100,
                            max: 9999,
                            readOnly: false,
                            required: true,
                            type: 'long',
                            ord: 3
                        }/**, {
                            name: 'macroRegion1',
                            title: 'Макрорегион 1',
                            type: 'com.mts.usim.model.MacroRegion',
                            ord: 4,
                            required: true,
                            displayField: 'name'
                        }**/
                    ];

                    var values = {
                        name: 'тест',
                        date: new Date()
                    };

                    var dlg = aplanaDialogs.editObject('Создание новой записи', null, metaData, values);
                    return dlg.result.then(function (values) {
                        //do something
                    })
                };
                //Поиск по фильтру
                $scope.searchClick = function () {
                    fetchData();
                };
                //Очистка фильтра
                $scope.clearFilterClick = function () {
                    $scope.dataOptions.filter = {};
                    fetchData();
                };


                initPage();

                /**
                 * Инициализация первичных данных на странице
                 */
                function initPage() {
                    //Инициализация грида
                    aplanaEntityUtils.initGrid($scope, fetchData, function(row) {
                        if (row.entity) {
                            $state.go("planningDetails", {
                                planningId: row.entity.id
                            });
                        }
                    });
                    //Инициализация параметров сессии - выбраных ранее строк в гриде + фильтры
                    aplanaEntityUtils.initPageSession($scope, fetchData)
                }

                $scope.$watchCollection('[dataOptions.filter.fulltext]', function () {
                    aplanaEntityUtils.saveFilter($scope);
                });
            }])
    ;
}());