'use strict';
(function () {

    angular.module('sbrfNdfl.ndflForms', ['ui.router'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('ndflForms', {
                url: '/taxes/ndfl/forms',
                templateUrl: 'js/js/taxes/ndfl/ndflForms.html',
                controller: 'ndflFormsCtrl'
            })
        }])
        .controller('ndflFormsCtrl', [
            '$scope', '$timeout', 'aplanaEntityUtils', '$state', 'dialogs',
            function ($scope, $timeout, aplanaEntityUtils, $state, dialogs) {
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
                                '<a href="index.html#/taxes/ndflDetails/{{row.entity.id}}">{{grid.getCellValue(row, col)}}</a></div>';
                        // index.html# нкужно будет убрать
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
                    return aplanaEntityUtils.fillGrid($scope, data, $scope.customGridColumnsBuilder)
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

                        //доступно в статусе «Принят в производство» или «Корректировка» и указан Приоритет
                        //$scope.splitButtonEnabled = hasSingleSelectedItem
                        //    && aplanaEntityUtils.isSelectionHasValue($scope, 'priorityDate')
                        //    && aplanaEntityUtils.isSelectionHasValue($scope, 'status.id', [APP_CONSTANTS.ENTITY_STATUS.PLANNING_TO_PRODUCTION.id, APP_CONSTANTS.ENTITY_STATUS.PLANNING_CORRECTION.id]);
                    }
                }

                /**
                 * Обработчики нажатий на кнопки
                 */
                    //Создать
                $scope.createButtonClick = function () {
                    var params = {};
                    jQuery.extend(params, $scope.dataOptions);

                    var data = {
                        scope: angular.copy(params)
                    };
                    var opts = {
                        copy: true,
                        windowClass: 'fl-modal-window'
                    };
                    var dlg = dialogs.create('js/taxes/ndfl/createFormDialog.html', 'createFormCtrl', data, opts);
                    return dlg.result.then(function (entity) {
                        //Заглушка создания новой записи
                        entity.id = $scope.dataOptions.data[$scope.dataOptions.data.length - 1].id + 1;
                        entity.formType = {id: 1, name: "Первичная"};
                        entity.asnu = {id: 1, name: "АС \"SAP\""};
                        entity.state = {id: 1, name: "Создана"};
                        entity.creationDate = new Date();
                        entity.creator = "Хазиев Ленар";
                        $scope.dataOptions.data.push(entity);
                        aplanaEntityUtils.updateViewData($scope);
                        tableData = $scope.dataOptions.data;
                    })
                };
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

                function dblClick(row) {
                    if (row.entity) {
                        $state.go("ndflDetailsForms", {
                            formId: row.entity.id,
                        })
                    }
                }


                initPage();

                /**
                 * Инициализация первичных данных на странице
                 */
                function initPage() {
                    //Инициализация грида
                    aplanaEntityUtils.initGrid($scope, fetchData, setButtonsEnabled, dblClick);
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

                //Имитация ajax-запроса
                $scope.searchItems = function (searchText) {
                    $timeout(function () {
                        if (searchText == ""){
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
                        } else {
                            for (var i = 0; i < $scope.dataOptions.filterList.departmentList.length; i++) {
                                var entity = $scope.dataOptions.filterList.departmentList[i];
                                if (entity.name.toLowerCase().indexOf(searchText.toLowerCase()) == -1) {
                                    $scope.dataOptions.filterList.departmentList.splice(i, 1);
                                    i--;
                                }
                            }
                        }
                    }, 250);
                };
            }])

        /**
         * Контроллер формы создания/редактирования ФЛ
         */
        .controller('createFormCtrl', ["$scope", "$http", "$uibModalInstance", "$alertService", "$translate", 'data',
            function ($scope, $http, $uibModalInstance, $alertService, $translate, data) {
                initDialog();

                /**
                 * Инициализация первичных данных
                 */
                function initDialog() {
                    //Получаем scope из главного окна
                    $scope.parentScope = undefined;
                    try {
                        $scope.parentScope = $scope.$resolve.data.scope;
                    } catch (ex) {
                    }

                    $scope.entity = {
                        period: $scope.parentScope.filterList.periodList[0],
                        department: $scope.parentScope.filterList.departmentList[0],
                        formKind: $scope.parentScope.filterList.formKindList[0]
                    };

                    $translate('header.ndfl.form.create').then(function (header) {
                        $scope.header = header;
                    });
                }

                /**
                 * Обработчики событий
                 */
                    //Сохранение данных
                $scope.save = function () {
                    //TODO: Send request to server for create/update data
                    $uibModalInstance.close($scope.entity)
                };

                //Закрытие окна
                $scope.cancel = function () {
                    $uibModalInstance.dismiss('Canceled');
                }
            }])
}());