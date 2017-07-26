(function () {
    'use strict';

    /**
     * @description Модуль для работы со формами ндфл
     */
    angular.module('app.ndflJournal', ['ui.router', 'app.createFormDialog'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('ndflJournal', {
                url: '/taxes/ndflJournal',
                templateUrl: 'client/app/taxes/ndfl/ndflJournal.html',
                controller: 'ndflJournalCtrl'
            });
        }])

        /**
         * @description Контроллер списка форм
         */
        .controller('ndflJournalCtrl', [
            '$scope', '$timeout', '$state', 'dialogs', 'ShowToDoDialog',
            function ($scope, $timeout, $state, dialogs, $showToDoDialog) {
                $scope.$parent.$broadcast('UPDATE_NOTIF_COUNT');
                /**
                 * @description Обновление грида
                 * @param page
                 */
                $scope.refreshGrid = function (page) {
                    $scope.ctrlMyGrid.refreshGrid(page);
                };
                $scope.searchFilter = {
                    ajaxFilter: [],
                    params: {},
                    isClear: false,
                    filterName: 'ndflJournalFilter'
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
                            department: {
                                id: 4,
                                name: "ПЦП Многофункциональный сервисный центр \"Ладья\" (ПВБ, г.Самара) ПЦП Многофункциональный сервисный центр \"Ладья\" (ПВБ, г.Самара) ПЦП Многофункциональный сервисный центр \"Ладья\" (ПВБ, г.Самара)"
                            },
                            asnu: {id: 2, name: 'АС \"Депозитарий\"'},
                            period: {id: 3, name: "2017; 3 квартал"},
                            state: {id: 1, name: "Создана"},
                            fileTF: "95_6100_01200021201728042017000000000000000000015000.xml",
                            creationDate: new Date(),
                            creator: "Хазиев Ленар"
                        }],
                    "offset": 1,
                    "total": 5,
                    "count": 5
                };

                $scope.ndflJournalGridOptions =
                {
                    datatype: "local",
                    data: dataStub.list,
                    value: [],
                    height: 250,
                    colNames: [
                        'Номер формы',
                        'Тип налоговой формы',
                        'Вид налоговой формы',
                        'Подразделение',
                        'Наименование АСНУ',
                        'Период',
                        'Состояние',
                        'Файл ТФ',
                        'Дата и время создания формы',
                        'Создал'],
                    colModel: [
                        {name: 'id', index: 'id', width: 135, key: true},
                        {name: 'formType.name', index: 'formType.name', width: 175, formatter: linkformatter},
                        {name: 'formKind.name', index: 'formKind.name', width: 175},
                        {name: 'department.name', index: 'department.name', width: 150},
                        {name: 'asnu.name', index: 'asnu.name', width: 180},
                        {name: 'period.name', index: 'period.name', width: 110},
                        {name: 'state.name', index: 'state.name', width: 100},
                        {name: 'fileTF', index: 'fileTF', width: 400},
                        {
                            name: 'creationDate',
                            index: 'creationDate',
                            width: 230,
                            formatter: 'date',
                            formatoptions: {newformat: 'd.m.Y H:m:s'}
                        },
                        {name: 'creator', index: 'creator', width: 175}
                    ],
                    rowNum: 10,
                    rowList: [10, 20, 30],
                    sortname: 'id',
                    viewrecords: true,
                    sortorder: "asc",
                    hidegrid: false,
                    multiselect: true,
                    ondblClickRow: function (rowId) {
                        $state.go("ndfl", {
                            formId: rowId
                        })
                    }
                };

                function linkformatter(cellValue, options) {
                    return "<a href='index.html#/taxes/ndfl/" + options.rowId + "'>" + cellValue + "</a>";
                }

                /**
                 * @description инициализирует грид №1
                 * @param ctrl контроллер грида
                 */
                $scope.initOurGrid = function (ctrl) {
                    $scope.ctrlMyGrid = ctrl;
                    var grid = ctrl.getGrid();
                    grid.setGridParam({
                        onSelectRow: function () {
                            $scope.selectedItems = ctrl.getAllSelectedRows();
                            $scope.$apply();
                            setButtonsEnabled();
                        },
                        onSelectAll: function () {
                            $scope.selectedItems = ctrl.getAllSelectedRows();
                            $scope.$apply();
                            setButtonsEnabled();
                        }
                    });
                    setButtonsEnabled();
                };

                var tableData;

                /**
                 * @description Получение данных с сервера
                 */
                function fetchData() {
                    var data = jQuery.extend({}, dataStub);
                    data.list = tableData ? tableData.slice(0) : data.list.slice(0);
                    for (var i = 0; i < data.list.length; i++) {
                        var entity = data.list[i];
                        if ($scope.searchFilter.params) {
                            if (($scope.searchFilter.params.period && entity.period.id !== $scope.searchFilter.params.period.id) ||
                                ($scope.searchFilter.params.department && entity.department.id !== $scope.searchFilter.params.department.id) ||
                                ($scope.searchFilter.params.formNumber && entity.id !== $scope.searchFilter.params.formNumber) ||
                                ($scope.searchFilter.params.formType && entity.formType.id !== $scope.searchFilter.params.formType.id) ||
                                ($scope.searchFilter.params.formKind && entity.formKind.id !== $scope.searchFilter.params.formKind.id) ||
                                ($scope.searchFilter.params.asnu && entity.asnu.id !== $scope.searchFilter.params.asnu.id) ||
                                ($scope.searchFilter.params.state && entity.state.id !== $scope.searchFilter.params.state.id) ||
                                ($scope.searchFilter.params.file && entity.fileTF.indexOf($scope.searchFilter.params.file) === -1)) {
                                data.list.splice(i, 1);
                                i--;
                            }
                        }
                    }
                    $scope.ctrlMyGrid.refreshGridData(data.list);
                }

                /**
                 * @description Инициализация кнопок
                 */
                function setButtonsEnabled() {
                    $scope.createButtonEnabled = true;
                    if (!$scope.selectedItems) {
                        $scope.checkButtonEnabled = false;
                        $scope.calculateButtonEnabled = false;
                        $scope.deleteButtonEnabled = false;
                        $scope.acceptButtonEnabled = false;
                        $scope.returnButtonEnabled = false;
                    } else {
                        var hasSelectedItems = $scope.selectedItems.length > 0;
                        $scope.checkButtonEnabled = hasSelectedItems;
                        $scope.calculateButtonEnabled = hasSelectedItems;
                        $scope.deleteButtonEnabled = hasSelectedItems;
                        $scope.acceptButtonEnabled = hasSelectedItems;
                        $scope.returnButtonEnabled = hasSelectedItems;

                    }
                }

                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Создать"
                 */
                $scope.createButtonClick = function () {
                    var params = {};
                    jQuery.extend(params, $scope.selects);

                    var data = {
                        scope: angular.copy(params)
                    };
                    var opts = {
                        copy: true,
                        windowClass: 'fl-modal-window'
                    };
                    var dlg = dialogs.create('client/app/taxes/ndfl/createFormDialog.html', 'createFormCtrl', data, opts);
                    return dlg.result.then(function (entity) {
                        //Заглушка создания новой записи
                        entity.id = dataStub.list[dataStub.list.length - 1].id + 1;
                        entity.formType = {id: 1, name: "Первичная"};
                        entity.asnu = {id: 1, name: "АС \"SAP\""};
                        entity.state = {id: 1, name: "Создана"};
                        entity.creationDate = new Date();
                        entity.creator = "Хазиев Ленар";
                        dataStub.list.push(entity);
                        tableData = dataStub.list;
                        $scope.ctrlMyGrid.refreshGridData(dataStub.list);
                    });
                };
                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Проверить"
                 */
                $scope.checkButtonClick = function () {
                    $showToDoDialog();
                };
                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Рассчитать"
                 */
                $scope.calculateButtonClick = function () {
                    $showToDoDialog();
                };
                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Удалить"
                 */
                $scope.deleteButtonClick = function () {
                    $showToDoDialog();
                };
                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Принять"
                 */
                $scope.acceptButtonClick = function () {
                    $showToDoDialog();
                };
                /**
                 * @description Событие, которое возникает по нажатию на кнопку "Вернуть в создана"
                 */
                $scope.returnButtonClick = function () {
                    $showToDoDialog();
                };
                /**
                 * @description Поиск по фильтру
                 */
                $scope.searchClick = function () {
                    $scope.searchFilter.fillFilterParams();
                    if ($scope.searchFilter.ajaxFilter.length !== 0) {
                        $scope.searchFilter.isClear = true;
                    }
                    fetchData();
                };

                /**
                 * @description Заполнение ajaxFilter
                 */
                $scope.searchFilter.fillFilterParams = function () {
                    if ($scope.searchFilter.params.period) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "period",
                            value: $scope.searchFilter.params.period
                        });
                    }
                    if ($scope.searchFilter.params.department) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "department",
                            value: $scope.searchFilter.params.department
                        });
                    }
                    if ($scope.searchFilter.params.formNumber) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "formNumber",
                            value: $scope.searchFilter.params.formNumber
                        });
                    }
                    if ($scope.searchFilter.params.formType) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "formType",
                            value: $scope.searchFilter.params.formType
                        });
                    }
                    if ($scope.searchFilter.params.formKind) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "formKind",
                            value: $scope.searchFilter.params.formKind
                        });
                    }
                    if ($scope.searchFilter.params.asnu) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "asnu",
                            value: $scope.searchFilter.params.asnu
                        });
                    }
                    if ($scope.searchFilter.params.state) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "state",
                            value: $scope.searchFilter.params.state
                        });
                    }
                    if ($scope.searchFilter.params.file) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "file",
                            value: $scope.searchFilter.params.file
                        });
                    }
                };

                /**
                 * @description Инициализация первичных данных на странице
                 */
                function initPage() {
                    $scope.selects = {};
                    //Инициалиация фильтра (заглушка)
                    // Селект выбора квартала
                    $scope.selects.periodSelect = {
                        options: {
                            minimumResultsForSearch: -1,
                            allowClear: false,
                            formatSelection: function (period) {
                                return period.name;
                            },
                            formatResult: function (period) {
                                return period.name;
                            },
                            data: {
                                results: [
                                    {id: 1, name: '2017; 1 квартал'},
                                    {id: 2, name: '2017; 2 квартал'},
                                    {id: 3, name: '2017; 3 квартал'},
                                    {id: 4, name: '2017; 4 квартал'}
                                ]
                            }
                        }
                    };
                    $scope.selects.departmentSelect = {
                        options: {
                            minimumResultsForSearch: -1,
                            allowClear: false,
                            formatSelection: function (department) {
                                return department.name;
                            },
                            formatResult: function (department) {
                                return department.name;
                            },
                            data: {
                                results: [
                                    {id: 1, name: 'Иркутское отделение №8586 ПАО Сбербанк'},
                                    {id: 2, name: 'Читинское отделение №8600 ПАО Сбербанк'},
                                    {id: 3, name: 'Якутское отделение №8603 ПАО Сбербанк'},
                                    {
                                        id: 4,
                                        name: 'ПЦП Многофункциональный сервисный центр "Ладья" (ПВБ, г.Самара) ПЦП Многофункциональный сервисный центр "Ладья" (ПВБ, г.Самара) ПЦП Многофункциональный сервисный центр "Ладья" (ПВБ, г.Самара)'
                                    }
                                ]
                            }
                        }
                    };
                    $scope.selects.formTypeSelect = {
                        options: {
                            minimumResultsForSearch: -1,
                            allowClear: false,
                            formatSelection: function (formType) {
                                return formType.name;
                            },
                            formatResult: function (formType) {
                                return formType.name;
                            },
                            data: {
                                results: [
                                    {id: 1, name: 'Первичная'},
                                    {id: 2, name: 'Консолидированная'}
                                ]
                            }
                        }
                    };
                    $scope.selects.formKindSelect = {
                        options: {
                            minimumResultsForSearch: -1,
                            allowClear: false,
                            formatSelection: function (formKind) {
                                return formKind.name;
                            },
                            formatResult: function (formKind) {
                                return formKind.name;
                            },
                            data: {
                                results: [
                                    {id: 1, name: 'РНУ НДФЛ (первичная)'},
                                    {id: 2, name: 'РНУ НДФЛ (консолидированная)'},
                                    {id: 3, name: '6-НДФЛ'}
                                ]
                            }
                        }
                    };
                    $scope.selects.asnuSelect = {
                        options: {
                            minimumResultsForSearch: -1,
                            allowClear: false,
                            formatSelection: function (asnu) {
                                return asnu.name;
                            },
                            formatResult: function (asnu) {
                                return asnu.name;
                            },
                            data: {
                                results: [
                                    {id: 1, name: 'АС \"SAP\"'},
                                    {id: 2, name: 'АС \"Депозитарий\"'}
                                ]
                            }
                        }
                    };
                    $scope.selects.stateSelect = {
                        options: {
                            minimumResultsForSearch: -1,
                            allowClear: false,
                            formatSelection: function (state) {
                                return state.name;
                            },
                            formatResult: function (state) {
                                return state.name;
                            },
                            data: {
                                results: [
                                    {id: 1, name: 'Создана'},
                                    {id: 2, name: 'Подготовлена'},
                                    {id: 3, name: 'Принята'}
                                ]
                            }
                        }
                    };
                }

                initPage();
            }]);
}());