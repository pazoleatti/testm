(function () {
    'use strict';

    angular.module('sbrfNdfl.ndflDetailsForms', ['ui.router'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('ndflDetailsForms', {
                url: '/taxes/ndflDetails/{formId}',
                templateUrl: 'client/js/taxes/ndfl/ndflDetails.html',
                controller: 'ndflDetailsFormsCtrl'
            });
        }])
        .controller('ndflDetailsFormsCtrl', [
            '$scope', '$timeout', '$state', '$stateParams', 'dialogs',
            function ($scope, $timeout, $state, $stateParams, dialogs) {
                $scope.refreshGrid = function(page) {
                    $scope.ctrlMyGrid.refreshGrid(page);
                };
                $scope.searchFilter = {
                    ajaxFilter: [],
                    params: {},
                    isClear: false,
                    filterName: 'ndflDetailsFilter'
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
                        }]
                };

                $scope.dataForRequisites = {
                    "list": [
                        {
                            id: 1,
                            inp: "1234567",
                            surname: "Иванов",
                            name: "Иван",
                            patronymic: "Иванович",
                            dateOfBirth: new Date(),
                            snils: "123-345-567-89",
                            statusCode: {
                                id: 1,
                                code: '1',
                                name: 'Налогоплательщик является налоговым резидентом Российской Федерации'
                            },
                            documentCode: {id: 1, code: '21', name: 'Паспорт гражданина Российской Федерации'},
                            documentNumber: "1234 545435",
                            citizenship: {id: 1, name: "Россия", code: "143"},
                            innRF: "765756",
                            innINO: "456466",
                            codeDul: {id: 5, code: "01", number: "123456"},
                            numberDul: {id: 5, code: "01", number: "123456"},
                            status: {id: 2, name: "Новый"},
                            codeSub: "001",
                            index: "079685",
                            subject: {id: 1, code: '52', name: 'Нижегородская область'},
                            area: {id: 1, name: "Ардатовский район"},
                            locality: {id: 1, name: "Ардатов"},
                            street: {id: 1, name: "Ленина"},
                            building: 99,
                            housing: 1,
                            apartment: 102
                        },
                        {
                            id: 2,
                            inp: "75643",
                            surname: "Крапивин",
                            name: "Алексей",
                            patronymic: "Гаврилович",
                            dateOfBirth: new Date(),
                            snils: "098-345-567-89",
                            statusCode: {
                                id: 1,
                                code: '1',
                                name: 'Налогоплательщик является налоговым резидентом Российской Федерации'
                            },
                            documentCode: {id: 1, code: '21', name: 'Паспорт гражданина Российской Федерации'},
                            documentNumber: "1234 455464",
                            citizenship: {id: 2, name: "Украина", code: "167"},
                            innRF: "976543",
                            innINO: "123423",
                            codeDul: {id: 2, code: "23", number: "0987"},
                            numberDul: {id: 2, code: "23", number: "0987"},
                            status: {id: 2, name: "Новый"},
                            codeSub: "023",
                            index: "17753",
                            subject: {id: 1, code: '52', name: 'Нижегородская область'},
                            area: {id: 2, name: "Арзамасский район"},
                            city: {id: 2, name: "Арзамас"},
                            street: {id: 2, name: "Красная"},
                            building: 1,
                            housing: 4,
                            apartment: 54
                        }],
                    "offset": 1,
                    "total": 2,
                    "count": 2
                };

                $scope.requisitesGridOptions =
                {
                    datatype: "local",
                    data: $scope.dataForRequisites.list,
                    value: [],
                    height: 250,
                    colNames: [
                        '№п/п',
                        'Налогоплательщик. ИНП',
                        'Налогоплательщик. Фамилия',
                        'Налогоплательщик. Имя',
                        'Налогоплательщик. Отчество',
                        'Налогоплательщик. Дата рождения',
                        'Налогоплательщик. СНИЛС',
                        'Гражданство (код страны)',
                        'ИНН. В стране гражданства',
                        'ДУЛ. Код',
                        'ДУЛ. Номер',
                        'Статус (код)',
                        'Адрес рег. в РФ. Код субъекта',
                        'Адрес рег. в РФ. Индекс',
                        'Адрес рег. в РФ. Район',
                        'Адрес рег. в РФ. Город',
                        'Адрес рег. в РФ. Населенный пункт',
                        'Адрес рег. в РФ. Улица',
                        'Адрес рег. в РФ. Дом',
                        'Адрес рег. в РФ. Корпус',
                        'Адрес рег. в РФ. Квартира'],
                    colModel: [
                        {name: 'id', index: 'id', width: 60, key: true},
                        {name: 'inp', index: 'inp', width: 170},
                        {name: 'surname', index: 'surname', width: 200},
                        {name: 'name', index: 'name', width: 175},
                        {name: 'patronymic', index: 'patronymic', width: 200},
                        {name: 'dateOfBirth', index: 'dateOfBirth', width: 240, formatter: 'date', formatoptions: { newformat: 'd.m.Y H:m:s'}},
                        {name: 'snils', index: 'snils', width: 190, sortable: false},
                        {name: 'citizenship.code', index: 'citizenship', width: 185},
                        {name: 'innRF', index: 'innRF', width: 95},
                        {name: 'innINO', index: 'innINO', width: 195},
                        {name: 'codeDul.code', index: 'codeDul', width: 85},
                        {name: 'numberDul.number', index: 'numberDul', width: 95},
                        {name: 'status.id', index: 'status', width: 100},
                        {name: 'codeSub', index: 'codeSub', width: 205, sortable: false},
                        {name: 'index', index: 'index', width: 170},
                        {name: 'area.name', index: 'area', width: 155},
                        {name: 'city.name', index: 'city', width: 165},
                        {name: 'locality.name', index: 'locality', width: 240},
                        {name: 'building', index: 'building', width: 145},
                        {name: 'housing', index: 'housing', width: 170},
                        {name: 'apartment', index: 'apartment', width: 205}
                    ],
                    rowNum: 10,
                    rowList: [10, 20, 30],
                    sortname: 'id',
                    viewrecords: true,
                    sortorder: "asc",
                    hidegrid: false,
                    multiselect: true
                };

                /**
                 * инициализирует грид №1
                 * @param ctrl контроллер грида
                 */
                $scope.initOurGrid = function (ctrl) {
                    $scope.ctrlMyGrid = ctrl;
                    var grid = ctrl.getGrid();
                    grid.setGridParam({
                        onSelectRow: function (rowId, status) {
                            $scope.selectedItems = ctrl.getAllSelectedRows();
                            $scope.$apply();
                            setButtonsEnabled($scope.selectedItems);
                        },
                        onSelectAll: function (aRowids, status) {
                            $scope.selectedItems = ctrl.getAllSelectedRows();
                            $scope.$apply();
                            setButtonsEnabled($scope.selectedItems);
                        }
                    });

                };

                var tableData;

                /**
                 * Получение данных с сервера
                 */
                function fetchData() {
                    var data = jQuery.extend({}, $scope.dataForRequisites);
                    data.list = tableData ? tableData.slice(0) : data.list.slice(0);
                    for (var i = 0; i < data.list.length; i++) {
                        var entity = data.list[i];
                        if ($scope.searchFilter.params) {
                            if (($scope.searchFilter.params.inp && entity.inp.indexOf($scope.searchFilter.params.inp) === -1) || ($scope.searchFilter.params.snils && entity.snils.indexOf($scope.searchFilter.params.snils) === -1) || ($scope.searchFilter.params.inn && entity.innRF.indexOf($scope.searchFilter.params.inn) === -1 && entity.innINO.indexOf($scope.searchFilter.params.inn) === -1) || ($scope.searchFilter.params.numberDul && entity.numberDul.number.indexOf($scope.searchFilter.params.numberDul) === -1) || ($scope.searchFilter.params.surname && entity.surname.toLowerCase().indexOf($scope.searchFilter.params.surname.toLowerCase()) === -1) || ($scope.searchFilter.params.name && entity.name.toLowerCase().indexOf($scope.searchFilter.params.name.toLowerCase()) === -1) || ($scope.searchFilter.params.patronymic && entity.patronymic.toLowerCase().indexOf($scope.searchFilter.params.patronymic.toLowerCase()) === -1) || ($scope.searchFilter.params.dateFrom ? ($scope.searchFilter.params.dateTo ? ($scope.searchFilter.params.dateFrom > entity.dateOfBirth || $scope.searchFilter.params.dateTo < entity.dateOfBirth) : $scope.searchFilter.params.dateFrom > entity.dateOfBirth) : ($scope.searchFilter.params.dateTo ? $scope.searchFilter.params.dateTo < entity.dateOfBirth : false))) {
                                data.list.splice(i, 1);
                                i--;
                            }
                        }
                    }
                    $scope.ctrlMyGrid.refreshGridData(data.list);
                }

                /**
                 * Инициализация кнопок
                 */
                function setButtonsEnabled(selectedItems) {
                    //Доступность кнопок над таблице
                    if (selectedItems.length === 0) {
                        $scope.editReqsEnabled = false;
                        $scope.deleteReqsEnabled = false;
                    } else {
                        $scope.editReqsEnabled = selectedItems.length === 1;
                        $scope.deleteReqsEnabled = selectedItems.length > 0;
                    }

                    //Доступности кнопок над вкладками
                    $scope.checkButtonEnabled = true;
                    $scope.calculateButtonEnabled = true;
                    $scope.acceptButtonEnabled = $scope.state === "Создана";
                    $scope.returnButtonEnabled = $scope.state !== "Создана";
                    $scope.editButtonEnabled = true;
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
                //Принять
                $scope.acceptButtonClick = function () {
                    //Имитация действий над родительской сущностью
                    $scope.state = "Принята";
                    setButtonsEnabled();
                };
                //Вернуть в Создана
                $scope.returnButtonClick = function () {
                    //Имитация действий над родительской сущностью
                    $scope.state = "Создана";
                    setButtonsEnabled();
                };
                //Редактировать
                $scope.editRecordClick = function () {
                };
                //Добавить ФЛ
                $scope.createFLClick = function () {
                    var params = {};
                    jQuery.extend(params, $scope.searchFilter);

                    var data = {
                        scope: angular.copy(params),
                        mode: 'create'
                    };
                    var opts = {
                        copy: true,
                        windowClass: 'fl-modal-window'
                    };
                    var dlg = dialogs.create('client/js/taxes/ndfl/createOrEditFlDialog.html', 'createOrEditFLCtrl', data, opts);
                    return dlg.result.then(function (entity) {
                        //Заглушка создания новой записи
                        entity.id = $scope.dataForRequisites.list[$scope.dataForRequisites.list.length - 1].id + 1;
                        $scope.dataForRequisites.list.push(entity);
                        tableData = $scope.dataForRequisites.list;
                        $scope.ctrlMyGrid.refreshGridData($scope.dataForRequisites.list);
                    });
                };

                //Редактировать ФЛ
                $scope.editFLClick = function () {
                    var params = {};
                    jQuery.extend(params, $scope.searchFilter);

                    var data = {
                        scope: angular.copy(params),
                        entity: $scope.selectedItems[0],
                        mode: 'edit'
                    };
                    var opts = {
                        copy: true,
                        windowClass: 'fl-modal-window'
                    };
                    var dlg = dialogs.create('client/js/taxes/ndfl/createOrEditFlDialog.html', 'createOrEditFLCtrl', data, opts);
                    return dlg.result.then(function (entity) {
                        //Заглушка редактирования записи
                        for (var i = 0; i < $scope.requisitesGridOptions.data.length; i++) {
                            if ($scope.requisitesGridOptions.data[i].id === entity.id) {
                                $scope.requisitesGridOptions.data[i] = entity;
                            }
                        }
                        $scope.ctrlMyGrid.refreshGridData($scope.dataForRequisites.list);
                    });
                };
                //Поиск по фильтру
                $scope.searchClick = function () {
                    $scope.searchFilter.fillFilterParams();
                    if ($scope.searchFilter.ajaxFilter.length !== 0){
                        $scope.searchFilter.isClear = true;
                    }
                    fetchData();
                };

                $scope.searchFilter.fillFilterParams = function () {
                    if ($scope.searchFilter.params.inp) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "inp",
                            value: $scope.searchFilter.params.inp
                        });
                    }
                    if ($scope.searchFilter.params.snils) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "snils",
                            value: $scope.searchFilter.params.snils
                        });
                    }
                    if ($scope.searchFilter.params.inn) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "inn",
                            value: $scope.searchFilter.params.inn
                        });
                    }
                    if ($scope.searchFilter.params.numberDul) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "numberDul",
                            value: $scope.searchFilter.params.numberDul
                        });
                    }
                    if ($scope.searchFilter.params.surname) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "surname",
                            value: $scope.searchFilter.params.surname
                        });
                    }
                    if ($scope.searchFilter.params.name) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "name",
                            value: $scope.searchFilter.params.name
                        });
                    }
                    if ($scope.searchFilter.params.patronymic) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "patronymic",
                            value: $scope.searchFilter.params.patronymic
                        });
                    }
                    if ($scope.searchFilter.params.dateFrom) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "dateFrom",
                            value: $scope.searchFilter.params.dateFrom
                        });
                    }
                    if ($scope.searchFilter.params.dateTo) {
                        $scope.searchFilter.ajaxFilter.push({
                            property: "dateTo",
                            value: $scope.searchFilter.params.dateTo
                        });
                    }
                };


                /**
                 * Инициализация первичных данных на странице
                 */
                function initPage() {
                    for (var i = 0; i < dataStub.list.length; i++) {
                        if (parseInt($stateParams.formId) === dataStub.list[i].id) {
                            $scope.department = dataStub.list[i].department.name;
                            $scope.formNumber = $stateParams.formId;
                            $scope.creator = dataStub.list[i].creator;
                            $scope.formType = dataStub.list[i].formType.name;
                            $scope.period = dataStub.list[i].period.name;
                            $scope.state = dataStub.list[i].state.name;
                            $scope.nameAsnu = dataStub.list[i].asnu.name;
                        }
                    }
                    $scope.editReqsEnabled = false;
                    $scope.deleteReqsEnabled = false;
                }

                initPage();
            }])
        /**
         * Контроллер формы создания/редактирования ФЛ
         */
        .controller('createOrEditFLCtrl', ["$scope", "$http", "$uibModalInstance", "$alertService", "$translate", 'data', 'FormLeaveConfirmer',
            function ($scope, $http, $uibModalInstance, $alertService, $translate, data, FormLeaveConfirmer) {
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

                    if (data.mode === 'create') {
                        //Создание нового ФЛ
                        $translate('header.ndfl.fl.create').then(function (header) {
                            $scope.header = header;
                        });
                        $translate('button.add').then(function (title) {
                            $scope.buttonTitle = title;
                        });
                    } else {
                        $translate('header.ndfl.fl.edit').then(function (header) {
                            $scope.header = header;
                        });
                        $translate('button.save').then(function (title) {
                            $scope.buttonTitle = title;
                        });
                        $scope.entity = data.entity;
                        $scope.startEntity = angular.copy(data.entity);
                    }

                    //Статические данные-заглушка
                    $scope.dialogDataStub = {
                        statusList: [{
                            id: 1,
                            code: '1',
                            name: 'Налогоплательщик является налоговым резидентом Российской Федерации'
                        }, {
                            id: 2,
                            code: '2',
                            name: 'Налогоплательщик не является налоговым резидентом Российской Федерации'
                        }],
                        documentCodeList: [{id: 1, code: '21', name: 'Паспорт гражданина Российской Федерации'}, {
                            id: 2,
                            code: '03',
                            name: 'Свидетельство о рождении'
                        }],
                        subjectCodeList: [{id: 1, code: '52', name: 'Нижегородская область'}, {
                            id: 2,
                            code: '77',
                            name: 'Московская область'
                        }],
                        areaList: [{id: 1, name: 'Ардатовский район'}, {id: 2, name: 'Арзамасский район'}, {
                            id: 3,
                            name: 'Бронницы'
                        }, {id: 4, name: 'Дзержинский'}],
                        cityList: [{id: 1, name: 'Ардатов'}, {id: 2, name: 'Арзамас'}, {
                            id: 3,
                            name: 'Бронницы'
                        }, {id: 4, name: 'Дзержинский'}],
                        localityList: [{id: 1, name: 'Простоквашино'}],
                        streetList: [{id: 1, name: 'Ленина'}, {id: 2, name: 'Красная'}]
                    };

                    // Селект выбора квартала
                    $scope.citizenshipSelect = {
                        options: {
                            minimumResultsForSearch: -1,
                            allowClear: false,
                            formatSelection: function (quarter) {
                                return quarter.code;
                            },
                            formatResult: function (quarter) {
                                return quarter.name;
                            },
                            data: {
                                results: [
                                    {id: 1, name: 'Россия', code: 123},
                                    {id: 2, name: 'Сомали', code: 456}
                                ]
                            }
                        }
                    };
                }

                initDialog();

                /**
                 * Обработчики событий
                 */
                    //Сохранение данных
                $scope.save = function () {
                    //TODO: Send request to server for create/update data

                    FormLeaveConfirmer.clearListeners();
                    $uibModalInstance.close($scope.entity);
                };

                // Изменялись ли данные на форме
                $scope.isFormModified = function () {
                    var isModified = false;
                    if (data.mode === "create"){
                        angular.forEach($scope.entity, function (item) {
                            if (item !== undefined) {
                                isModified = true;
                            }
                        });
                    } else if (data.mode === "edit"){
                        if(!_.isEqual($scope.startEntity, $scope.entity)){
                            isModified = true;
                        }
                    }
                    return isModified;
                };

                FormLeaveConfirmer.initializeListeners(
                    $scope.isFormModified,
                    "Отмена операции",
                    "Вы уверены, что хотите отменить создание/редактирование записи",
                    function () {
                        FormLeaveConfirmer.clearListeners();
                        $uibModalInstance.close($scope.entity);
                    },
                    undefined,
                    $uibModalInstance
                );

                //Закрытие окна
                $scope.cancel = function () {
                    FormLeaveConfirmer.askSaveChanges(
                        function () {
                            $uibModalInstance.dismiss('Canceled');
                        }
                    );
                };

                //Проверка значения на число
                $scope.isNumber = function (value) {
                    if (!value || value === "") {
                        return true;
                    }
                    var INTEGER_REGEXP = /^\-?\d+$/;
                    if (INTEGER_REGEXP.test(value)) {
                        return true;
                    }
                    return false;
                };
            }])

// ------------------------------------------------------------------------------------------------------------------------------------
// ------------------------------------------------------------------------------------------------------------------------------------
 ;
}());