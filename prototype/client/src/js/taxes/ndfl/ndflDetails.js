'use strict';
(function () {

    angular.module('sbrfNdfl.ndflDetailsForms', ['ui.router'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('ndflDetailsForms', {
                url: '/taxes/ndfl/forms/{formId}',
                templateUrl: 'js/taxes/ndfl/ndflDetails.html',
                controller: 'ndflDetailsFormsCtrl'
            })
        }])
        .controller('ndflDetailsFormsCtrl', [
            '$scope', '$timeout', 'aplanaEntityUtils', '$state', '$stateParams', 'aplanaDialogs',
            function ($scope, $timeout, aplanaEntityUtils, $state, $stateParams, aplanaDialogs) {
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
                        if (field.name == 'formType') {
                            columnDef.cellTemplate = '<div class="ui-grid-cell-contents ng-binding ng-scope" ' +
                                'title="{{grid.appScope.getRowValue(row.entity)}}">' +
                                '<a href="ndflDetails/{{row.entity.id}}">{{grid.getCellValue(row, col)}}</a></div>';
                        }
                        if (field.type == "java.util.Date") {
                            var format = field.format == "" ? "dd.MM.yyyy" : field.format;
                            columnDef.type = 'date';
                            columnDef.cellFilter = "date:'" + format + "'";
                        }
                        gridOptions.columnDefs.push(columnDef);
                    }

                    aplanaEntityUtils.fitColumnsWidth(gridOptions, $scope.gridApi.grid.gridWidth);
                };


                var dataStub = {
                    "list": [
                        {
                            id: 1,
                            formType: {id: 1, name: "Первичная"},
                            formKind: {id: 1, name: "РНУ НДФЛ (первичная)"},
                            department: {id: 1, name: "Иркутское отделение №8586 ПАО Сбербанк"},
                            asnu: {id: 1, name: "АС \"SAP\""},
                            period: {id: 1, name: "2017; 1 квартал"},
                            state: {id: 1, name: "Создана"},
                            fileTF: "99_6100_01200021201728042017000000000000000000015000.xml",
                            creationDate: new Date(),
                            creator: "Хазиев Ленар"
                        },
                        {
                            id: 2,
                            formType: {id: 2, name: "Консолидированная"},
                            formKind: {id: 2, name: "РНУ НДФЛ (консолидированная)"},
                            department: {id: 1, name: "Иркутское отделение №8586 ПАО Сбербанк"},
                            asnu: {id: 2, name: 'АС \"Депозитарий\"'},
                            period: {id: 1, name: "2017; 1 квартал"},
                            state: {id: 2, name: "Подготовлена"},
                            fileTF: "98_6100_01200021201728042017000000000000000000015000.xml",
                            creationDate: new Date(),
                            creator: "Хазиев Ленар"
                        },
                        {
                            id: 3,
                            formType: {id: 1, name: "Первичная"},
                            formKind: {id: 3, name: "6-НДФЛ"},
                            department: {id: 2, name: "Читинское отделение №8600 ПАО Сбербанк"},
                            asnu: {id: 1, name: "АС \"SAP\""},
                            period: {id: 2, name: "2017; 2 квартал"},
                            state: {id: 3, name: "Принята"},
                            fileTF: "97_6100_01200021201728042017000000000000000000015000.xml",
                            creationDate: new Date(),
                            creator: "Хазиев Ленар"
                        },
                        {
                            id: 4,
                            formType: {id: 1, name: "Первичная"},
                            formKind: {id: 1, name: "РНУ НДФЛ (первичная)"},
                            department: {id: 1, name: "Иркутское отделение №8586 ПАО Сбербанк"},
                            asnu: {id: 1, name: "АС \"SAP\""},
                            period: {id: 3, name: "2017; 3 квартал"},
                            state: {id: 1, name: "Создана"},
                            fileTF: "96_6100_01200021201728042017000000000000000000015000.xml",
                            creationDate: new Date(),
                            creator: "Хазиев Ленар"
                        },
                        {
                            id: 5,
                            formType: {id: 1, name: "Первичная"},
                            formKind: {id: 1, name: "РНУ НДФЛ (первичная)"},
                            department: {id: 4, name: "ПЦП Многофункциональный сервисный центр \"Ладья\" (ПВБ, г.Самара) ПЦП Многофункциональный сервисный центр \"Ладья\" (ПВБ, г.Самара) ПЦП Многофункциональный сервисный центр \"Ладья\" (ПВБ, г.Самара)"},
                            asnu: {id: 2, name: 'АС \"Депозитарий\"'},
                            period: {id: 3, name: "2017; 3 квартал"},
                            state: {id: 1, name: "Создана"},
                            fileTF: "95_6100_01200021201728042017000000000000000000015000.xml",
                            creationDate: new Date(),
                            creator: "Хазиев Ленар"
                        }],
                    "metaData": [
                        {
                            "name": "id",
                            "type": "java.lang.String",
                            "title": "Номер формы",
                            "width": 20,
                            "order": 1,
                            "visible": true
                        },
                        {
                            "name": "formType",
                            "type": "com.ndlf.model.FormType",
                            "displayField": "name",
                            "title": "Тип налоговой формы",
                            "width": 30,
                            "order": 2,
                            "visible": true
                        },
                        {
                            "name": "formKind",
                            "type": "com.ndlf.model.FormKind",
                            "displayField": "name",
                            "title": "Вид налоговой формы",
                            "width": 35,
                            "order": 3,
                            "visible": true
                        },
                        {
                            "name": "department",
                            "type": "com.ndlf.model.Department",
                            "displayField": "name",
                            "title": "Подразделение",
                            "width": 40,
                            "order": 4,
                            "visible": true
                        },
                        {
                            "name": "asnu",
                            "type": "com.ndlf.model.Asnu",
                            "displayField": "name",
                            "title": "Наименование АСНУ",
                            "width": 40,
                            "order": 5,
                            "visible": true
                        },
                        {
                            "name": "period",
                            "type": "com.ndlf.model.Period",
                            "displayField": "name",
                            "title": "Период",
                            "width": 20,
                            "order": 6,
                            "visible": true
                        },
                        {
                            "name": "state",
                            "type": "com.ndlf.model.State",
                            "displayField": "name",
                            "title": "Состояние",
                            "width": 20,
                            "order": 7,
                            "visible": true
                        },
                        {
                            "name": "fileTF",
                            "type": "java.lang.String",
                            "title": "Файл ТФ",
                            "width": 40,
                            "order": 8,
                            "visible": true
                        },
                        {
                            "name": "creationDate",
                            "type": "java.util.Date",
                            "title": "Дата и время создания формы",
                            "width": 45,
                            "order": 9,
                            "visible": true,
                            "format": "dd.MM.yyyy HH:mm:ss"
                        },
                        {
                            "name": "creator",
                            "type": "java.lang.String",
                            "title": "Создал",
                            "width": 40,
                            "order": 10,
                            "visible": true
                        }],
                    "offset": 1,
                    "total": 5,
                    "count": 5
                };

                var tableData;

                /**
                 * Получение данных с сервера
                 */
                function fetchData() {
                    var data = jQuery.extend({}, dataStub);
                    data.list = tableData ? tableData.slice(0) : data.list.slice(0);
                    for (var i = 0; i < data.list.length; i++) {
                        var entity = data.list[i];
                        if ($scope.dataOptions.filter) {
                            if (($scope.dataOptions.filter.period && entity.period.id != $scope.dataOptions.filter.period.id) ||
                                ($scope.dataOptions.filter.department && entity.department.id != $scope.dataOptions.filter.department.id) ||
                                ($scope.dataOptions.filter.formNumber && entity.id != $scope.dataOptions.filter.formNumber) ||
                                ($scope.dataOptions.filter.formType && entity.formType.id != $scope.dataOptions.filter.formType.id) ||
                                ($scope.dataOptions.filter.formKind && entity.formKind.id != $scope.dataOptions.filter.formKind.id) ||
                                ($scope.dataOptions.filter.asnu && entity.asnu.id != $scope.dataOptions.filter.asnu.id) ||
                                ($scope.dataOptions.filter.state && entity.state.id != $scope.dataOptions.filter.state.id) ||
                                ($scope.dataOptions.filter.file && entity.fileTF.indexOf($scope.dataOptions.filter.file) == -1)) {
                                data.list.splice(i, 1);
                                i--;
                            }
                        }
                    }

                    for (var i = 0; i < dataStub.list.length - 1; i ++){
                        if (parseInt($stateParams.formId) === dataStub.list[i].id){
                            $scope.department = dataStub.list[i].department.name;
                            $scope.formNumber = $stateParams.formId;
                            $scope.creator = dataStub.list[i].creator;
                            $scope.formType = dataStub.list[i].formType.name;
                            $scope.period = dataStub.list[i].period.name;
                            $scope.state = dataStub.list[i].state.name;
                            $scope.nameAsnu = dataStub.list[i].asnu.name;
                        }
                    }

                    return aplanaEntityUtils.fillGrid($scope, data, $scope.customGridColumnsBuilder)
                        .then(setButtonsEnabled);
                }

                /**
                 * Инициализация кнопок
                 */
                function setButtonsEnabled() {
                    $scope.exportButtonEnabled = aplanaEntityUtils.isTableHasValue($scope);
                    $scope.searchButtonEnabled = true;
                    $scope.clearButtonEnabled = true;
                    if (!$scope.gridApi || !$scope.currentEntityView) {
                        $scope.searchButtonEnabled = false;
                        $scope.clearButtonEnabled = false;
                        $scope.checkButtonEnabled = false;
                        $scope.calculateButtonEnabled = false;
                        $scope.deleteButtonEnabled = false;
                        $scope.acceptButtonEnabled = false;
                        $scope.returnButtonEnabled = false;
                    } else {
                        $scope.selectedItems = aplanaEntityUtils.getSelectedEntities($scope);
                        var hasSelectedItems = $scope.gridApi.grid.selection.selectedCount > 0;
                        var hasSingleSelectedItem = $scope.gridApi.grid.selection.selectedCount == 1;

                        $scope.checkButtonEnabled = hasSelectedItems;
                        $scope.calculateButtonEnabled = hasSelectedItems;
                        $scope.deleteButtonEnabled = hasSelectedItems;
                        $scope.acceptButtonEnabled = hasSelectedItems;
                        $scope.returnButtonEnabled = hasSelectedItems;
                    }
                }

                /**
                 * Обработчики нажатий на кнопки
                 */
                //Проверить
                $scope.checkButtonClick = function () {
                    //Do check
                };
                //Рассчитать
                $scope.calculateButtonClick = function () {
                    //Do calculate
                };
                //Удалить
                $scope.deleteButtonClick = function () {
                    //Клиентская заглушка
                    aplanaEntityUtils.processSelectedEntities($scope, function (entity) {
                        $scope.dataOptions.data.splice($scope.dataOptions.data.lastIndexOf(entity), 1);
                    });
                    aplanaEntityUtils.updateViewData($scope);
                    tableData = $scope.dataOptions.data;
                };
                //Принять
                $scope.acceptButtonClick = function () {
                    //Клиентская заглушка
                    aplanaEntityUtils.processSelectedEntities($scope, function (entity) {
                        entity.state = {id: 3, name: "Принята"};
                        aplanaEntityUtils.updateViewData($scope)
                    });
                };
                //Вернуть в Создана
                $scope.returnButtonClick = function () {
                    //Клиентская заглушка
                    aplanaEntityUtils.processSelectedEntities($scope, function (entity) {
                        entity.state = {id: 1, name: "Создана"};
                        aplanaEntityUtils.updateViewData($scope)
                    });
                    tableData = $scope.dataOptions.data;
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
                    aplanaEntityUtils.initGrid($scope, fetchData, setButtonsEnabled);
                    //Инициалиация фильтра (заглушка)
                    $scope.dataOptions.filterList = {
                        periodList: [{id: 1, name: '2017; 1 квартал'}, {id: 2, name: '2017; 2 квартал'}, {
                            id: 3,
                            name: '2017; 3 квартал'
                        }, {id: 4, name: '2017; 4 квартал'}],
                        departmentList: [{id: 1, name: 'Иркутское отделение №8586 ПАО Сбербанк'}, {
                            id: 2,
                            name: 'Читинское отделение №8600 ПАО Сбербанк'
                        }, {id: 3, name: 'Якутское отделение №8603 ПАО Сбербанк'}, {
                            id: 4,
                            name: 'ПЦП Многофункциональный сервисный центр "Ладья" (ПВБ, г.Самара) ПЦП Многофункциональный сервисный центр "Ладья" (ПВБ, г.Самара) ПЦП Многофункциональный сервисный центр "Ладья" (ПВБ, г.Самара)'
                        }],
                        formTypeList: [{id: 1, name: 'Первичная'}, {id: 2, name: 'Консолидированная'}],
                        formKindList: [{id: 1, name: 'РНУ НДФЛ (первичная)'}, {id: 2, name: 'РНУ НДФЛ (консолидированная)'}, {id: 3, name: '6-НДФЛ'}],
                        asnuList: [{id: 1, name: 'АС \"SAP\"'}, {id: 2, name: 'АС \"Депозитарий\"'}],
                        stateList: [{id: 1, name: 'Создана'}, {id: 2, name: 'Подготовлена'}, {id: 3, name: 'Принята'}]
                    };
                    //Инициализация параметров сессии - выбраных ранее строк в гриде + фильтры
                    aplanaEntityUtils.initPageSession($scope, fetchData)
                }

                $scope.$watchCollection('[dataOptions.filter.fulltext]', function () {
                    aplanaEntityUtils.saveFilter($scope);
                });
            }])
    ;
}());