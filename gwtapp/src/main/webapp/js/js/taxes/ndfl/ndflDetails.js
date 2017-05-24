'use strict';
(function () {

    angular.module('sbrfNdfl.ndflDetailsForms', ['ui.router', 'sbrfNdfl.widgets', 'ui.grid', 'ui.grid.edit'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('ndflDetailsForms', {
                url: '/taxes/ndflDetails/{formId}',
                templateUrl: 'js/js/taxes/ndfl/ndflDetails.html',
                controller: 'ndflDetailsFormsCtrl'
            })
        }])
        .controller('ndflDetailsFormsCtrl', [
            '$scope', '$timeout', 'aplanaEntityUtils', '$state', '$stateParams', 'dialogs',
            function ($scope, $timeout, aplanaEntityUtils, $state, $stateParams, dialogs) {
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

                var dataForRequisites = {
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
                    "metaData": [
                        {
                            "name": "id",
                            "type": "java.lang.String",
                            "title": "№п/п",
                            "width": 60,
                            "order": 1,
                            "visible": true
                        },
                        {
                            "name": "inp",
                            "type": "java.lang.String",
                            "title": "Налогоплательщик. ИНП",
                            "width": 170,
                            "order": 2,
                            "visible": true
                        },
                        {
                            "name": "surname",
                            "type": "java.lang.String",
                            "title": "Налогоплательщик. Фамилия",
                            "width": 200,
                            "order": 3,
                            "visible": true
                        },
                        {
                            "name": "name",
                            "type": "java.lang.String",
                            "title": "Налогоплательщик. Имя",
                            "width": 175,
                            "order": 4,
                            "visible": true
                        },
                        {
                            "name": "patronymic",
                            "type": "java.lang.String",
                            "title": "Налогоплательщик. Отчество",
                            "width": 200,
                            "order": 5,
                            "visible": true
                        },
                        {
                            "name": "dateOfBirth",
                            "type": "java.util.Date",
                            "title": "Налогоплательщик. Дата рождения",
                            "width": 240,
                            "order": 6,
                            "visible": true,
                            "format": "dd.MM.yyyy HH:mm:ss"
                        },
                        {
                            "name": "snils",
                            "type": "java.lang.String",
                            "title": "Налогоплательщик. СНИЛС",
                            "width": 190,
                            "order": 7,
                            "visible": true
                        },
                        {
                            "name": "citizenship",
                            "type": "com.ndlf.model.Citizenship",
                            "displayField": "code",
                            "title": "Гражданство (код страны)",
                            "width": 185,
                            "order": 8,
                            "visible": true
                        },
                        {
                            "name": "innRF",
                            "type": "java.lang.String",
                            "title": "ИНН. В РФ",
                            "width": 95,
                            "order": 9,
                            "visible": true
                        },
                        {
                            "name": "innINO",
                            "type": "java.lang.String",
                            "title": "ИНН. В стране гражданства",
                            "width": 195,
                            "order": 10,
                            "visible": true
                        },
                        {
                            "name": "codeDul",
                            "type": "com.ndlf.model.Dul",
                            "displayField": "code",
                            "title": "ДУЛ. Код",
                            "width": 85,
                            "order": 11,
                            "visible": true
                        },
                        {
                            "name": "numberDul",
                            "type": "com.ndlf.model.Dul",
                            "displayField": "number",
                            "title": "ДУЛ. Номер",
                            "width": 95,
                            "order": 12,
                            "visible": true
                        },
                        {
                            "name": "status",
                            "type": "com.ndlf.model.Status",
                            "displayField": "id",
                            "title": "Статус (код)",
                            "width": 100,
                            "order": 13,
                            "visible": true
                        },
                        {
                            "name": "codeSub",
                            "type": "java.lang.String",
                            "title": "Адрес рег. в РФ. Код субъекта",
                            "width": 205,
                            "order": 14,
                            "visible": true
                        },
                        {
                            "name": "index",
                            "type": "java.lang.String",
                            "title": "Адрес рег. в РФ. Индекс",
                            "width": 170,
                            "order": 15,
                            "visible": true
                        },
                        {
                            "name": "area",
                            "type": "com.ndlf.model.Area",
                            "displayField": "name",
                            "title": "Адрес рег. в РФ. Район",
                            "width": 155,
                            "order": 16,
                            "visible": true
                        },
                        {
                            "name": "city",
                            "type": "com.ndlf.model.City",
                            "displayField": "name",
                            "title": "Адрес рег. в РФ. Город",
                            "width": 165,
                            "order": 17,
                            "visible": true
                        },
                        {
                            "name": "locality",
                            "type": "com.ndlf.model.Locality",
                            "displayField": "name",
                            "title": "Адрес рег. в РФ. Населенный пункт",
                            "width": 240,
                            "order": 18,
                            "visible": true
                        },
                        {
                            "name": "street",
                            "type": "com.ndlf.model.Street",
                            "displayField": "name",
                            "title": "Адрес рег. в РФ. Улица",
                            "width": 160,
                            "order": 19,
                            "visible": true
                        },
                        {
                            "name": "building",
                            "type": "com.ndlf.model.Address",
                            "displayField": "building",
                            "title": "Адрес рег. в РФ. Дом",
                            "width": 145,
                            "order": 20,
                            "visible": true
                        },
                        {
                            "name": "housing",
                            "type": "number",
                            "title": "Адрес рег. в РФ. Корпус",
                            "width": 170,
                            "order": 21,
                            "visible": true
                        },
                        {
                            "name": "apartment",
                            "type": "number",
                            "title": "Адрес рег. в РФ. Квартира",
                            "width": 205,
                            "order": 22,
                            "visible": true
                        }],
                    "offset": 1,
                    "total": 2,
                    "count": 2
                };

                var tableData;

                /**
                 * Получение данных с сервера
                 */
                function fetchData() {
                    var data = jQuery.extend({}, dataForRequisites);
                    data.list = tableData ? tableData.slice(0) : data.list.slice(0);
                    for (var i = 0; i < data.list.length; i++) {
                        var entity = data.list[i];
                        if ($scope.dataOptions.filter) {
                            if (($scope.dataOptions.filter.inp && entity.inp.indexOf($scope.dataOptions.filter.inp) == -1)
                                || ($scope.dataOptions.filter.snils && entity.snils.indexOf($scope.dataOptions.filter.snils) == -1)
                                || ($scope.dataOptions.filter.inn && entity.innRF.indexOf($scope.dataOptions.filter.inn) == -1 && entity.innINO.indexOf($scope.dataOptions.filter.inn) == -1)
                                || ($scope.dataOptions.filter.numberDul && entity.numberDul.number.indexOf($scope.dataOptions.filter.numberDul) == -1)
                                || ($scope.dataOptions.filter.surname && entity.surname.toLowerCase().indexOf($scope.dataOptions.filter.surname.toLowerCase()) == -1)
                                || ($scope.dataOptions.filter.name && entity.name.toLowerCase().indexOf($scope.dataOptions.filter.name.toLowerCase()) == -1)
                                || ($scope.dataOptions.filter.patronymic && entity.patronymic.toLowerCase().indexOf($scope.dataOptions.filter.patronymic.toLowerCase()) == -1)
                                || ($scope.dataOptions.filter.dateFrom ? ($scope.dataOptions.filter.dateTo ? ($scope.dataOptions.filter.dateFrom > entity.dateOfBirth || $scope.dataOptions.filter.dateTo < entity.dateOfBirth) : $scope.dataOptions.filter.dateFrom > entity.dateOfBirth) : ($scope.dataOptions.filter.dateTo ? $scope.dataOptions.filter.dateTo < entity.dateOfBirth : false))) {
                                data.list.splice(i, 1);
                                i--;
                            }
                        }
                    }

                    return aplanaEntityUtils.fillGrid($scope, data, $scope.customGridColumnsBuilder)
                        .then(setButtonsEnabled);
                }

                /**
                 * Инициализация кнопок
                 */
                function setButtonsEnabled() {
                    //Доступность кнопок над таблицей
                    $scope.createReqsEnabled = true;
                    if (!$scope.gridApi || !$scope.currentEntityView) {
                        $scope.editReqEnabled = false;
                        $scope.deleteReqsEnabled = false;
                    } else {
                        $scope.editReqsEnabled = $scope.gridApi.grid.selection.selectedCount == 1;
                        $scope.deleteReqsEnabled = $scope.gridApi.grid.selection.selectedCount > 0;
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
                    jQuery.extend(params, $scope.dataOptions);

                    var data = {
                        scope: angular.copy(params),
                        mode: 'create'
                    };
                    var opts = {
                        copy: true,
                        windowClass: 'fl-modal-window'
                    };
                    var dlg = dialogs.create('js/taxes/ndfl/createOrEditFlDialog.html', 'createOrEditFLCtrl', data, opts);
                    return dlg.result.then(function (entity) {
                        //Заглушка создания новой записи
                        entity.id = $scope.dataOptions.data[$scope.dataOptions.data.length - 1].id + 1;
                        $scope.dataOptions.data.push(entity);
                        aplanaEntityUtils.updateViewData($scope);
                        tableData = $scope.dataOptions.data;
                    })
                };

                $scope.editInformationData = function() {
                    var params = {};
                    jQuery.extend(params, $scope.dataOptions);
                    var data = {

                    };
                    var opts = {
                        copy: true,
                        windowClass: 'fl-modal-window'
                    };
                    dialogs.create('js/js/taxes/ndfl/informationDialog.html', 'createOrEditFLCtrl', data, opts);
                }

                $scope.addInformationData = function() {
                    var data = {
                    };
                    var opts = {
                        copy: true,
                        windowClass: 'fl-modal-window'
                    };
                    dialogs.create('js/js/taxes/ndfl/informationDialog.html', 'createOrEditFLCtrl', data, opts);
                }

                //Редактировать ФЛ
                $scope.editFLClick = function () {
                    var params = {};
                    jQuery.extend(params, $scope.dataOptions);

                    var data = {
                        scope: angular.copy(params),
                        entity: aplanaEntityUtils.getSelectedEntities($scope)[0],
                        mode: 'edit'
                    };
                    var opts = {
                        copy: true,
                        windowClass: 'fl-modal-window'
                    };
                    var dlg = dialogs.create('js/taxes/ndfl/createOrEditFlDialog.html', 'createOrEditFLCtrl', data, opts);
                    return dlg.result.then(function (entity) {
                        //Заглушка редактирования записи
                        for (var i = 0; i < $scope.dataOptions.data.length; i++) {
                            if ($scope.dataOptions.data[i].id == entity.id) {
                                $scope.dataOptions.data[i] = entity
                            }
                        }
                        aplanaEntityUtils.updateViewData($scope);
                        tableData = $scope.dataOptions.data;
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
                    //Инициализация грида
                    aplanaEntityUtils.initGrid($scope, fetchData, setButtonsEnabled);
                    //Инициалиация фильтра (заглушка)

                    //Инициализация параметров сессии - выбраных ранее строк в гриде + фильтры
                    aplanaEntityUtils.initPageSession($scope, fetchData)
                }

                $scope.$watchCollection('[dataOptions.filter.fulltext]', function () {
                    aplanaEntityUtils.saveFilter($scope);
                });
            }])
        /**
         * Контроллер формы создания/редактирования ФЛ
         */
        .controller('createOrEditFLCtrl', ["$scope", "$http", "$uibModalInstance", "$alertService", "$translate", 'data',
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

                    if (data.mode == 'create') {
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
                    }

                    //Статические данные-заглушка
                    $scope.dialogDataStub = {
                        citizenshipList: [{id: 1, name: 'Россия', code: 123}, {id: 2, name: 'Сомали', code: 456}],
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
                    }
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

// ------------------------------------------------------------------------------------------------------------------------------------
// ------------------------------------------------------------------------------------------------------------------------------------



        .controller('informationDetailsFormsCtrl', ['$scope', '$timeout', 'aplanaEntityUtils', '$state', '$stateParams',
            function ($scope, $timeout, aplanaEntityUtils, $state, $stateParams) {

                $scope.informationOptions = {
                    columnDefs: [
                        {field: 'number', displayName: '№ пп'},
                        {field: 'inp', displayName: 'ИНП'},
                        {field: 'code', displayName: 'Код вычета'},
                        {field: 'documentType', displayName: 'Тип'},
                        {field: 'documentDate', displayName: 'Дата'},
                        {field: 'documentNumber', displayName: 'Номер'},
                        {field: 'documentSourceCode', displayName: 'Код источника'},
                        {field: 'documentSum', displayName: 'Сумма'},
                        {field: 'ndflId', displayName: 'ID операции'},
                        {field: 'ndflDate', displayName: 'Дата'},
                        {field: 'ndflCode', displayName: 'Код дохода'},
                        {field: 'ndflSum', displayName: 'Сумма'},
                        {field: 'previousDate', displayName: 'Дата'},
                        {field: 'previousSum', displayName: 'Сумма'},
                        {field: 'currentDate', displayName: 'Дата'},
                        {field: 'currentSum', displayName: 'Сумма'}
                    ]
                };

                //$scope.myData = [{
                //var myData = [{
                $scope.informationOptions.data = [{
                    'number' : '112233',
                    'inp' : '112233',
                    'code' : '112233',
                    'documentType' : '33443',
                    'documentDate' : '221212',
                    'documentNumber' : '121212',
                    'documentSourceCode' : '121212',
                    'documentSum' : '1212121',
                    'ndflId' : '1212121',
                    'ndflDate' : '121212',
                    'ndflCode' : '343434',
                    'ndflSum' : '544545',
                    'previousDate' : '343434',
                    'previousSum' : '232323',
                    'currentDate' : '23232323',
                    'currentSum' : '878787878'
                }];

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
                        if (field.type == "java.util.Date") {
                            var format = field.format == "" ? "dd.MM.yyyy" : field.format;
                            columnDef.type = 'date';
                            columnDef.cellFilter = "date:'" + format + "'";
                        }
                        gridOptions.columnDefs.push(columnDef);
                    }
                    aplanaEntityUtils.fitColumnsWidth(gridOptions, $scope.gridApi.grid.gridWidth);
                };

                var dataForRequisites = {
                    "list": [
                        {
                            id: 1,
                            inp: "1234567",
                            surname: "Иванов",
                            name: "Иван",
                            patronymic: "Иванович",
                            dateOfBirth: new Date(),
                            snils: "123-345-567-89",
                            citizenship: {id: 1, name: "Россия", code: "143"},
                            innRf: {id: 3, innRf: "1274146979", innSitizenship: "1274146979"},
                            innSitizenship: {id: 3, innRf: "1274146979", innSitizenship: "1274146979"},
                            codeDul: {id: 5, code: "01", number: "123456"},
                            numberDul: {id: 5, code: "01", number: "123456"},
                            status: {id: 2, name: "Новый"},
                            codeSub: "001",
                            index: "079685",
                            area: "Тюменский",
                            city: "Октябрьский",
                            locality: "Октябрьский",
                            street: "Ленина",
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
                            citizenship: {id: 2, name: "Украина", code: "167"},
                            innRf: {id: 87, innRf: "976543", innSitizenship: "123423"},
                            innSitizenship: {id: 87, innRf: "976543", innSitizenship: "123423"},
                            codeDul: {id: 2, code: "23", number: "0987"},
                            numberDul: {id: 2, code: "23", number: "0987"},
                            status: {id: 2, name: "Новый"},
                            codeSub: "023",
                            index: "17753",
                            area: "Львовская",
                            city: "Львов",
                            locality: "Львов",
                            street: "Красная",
                            building: 1,
                            housing: 4,
                            apartment: 54
                        }],
                    "metaData": [
                        {
                            "name": "id",
                            "type": "java.lang.String",
                            "title": "№п/п",
                            "width": 60,
                            "order": 1,
                            "visible": true
                        },
                        {
                            "name": "inp",
                            "type": "java.lang.String",
                            "title": "Налогоплательщик. ИНП",
                            "width": 170,
                            "order": 2,
                            "visible": true
                        }
                    ],
                    "offset": 1,
                    "total": 2,
                    "count": 2
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
                        }]
                };

                var tableData;

                function setButtonsEnabledInformation() {

                }

                function fetchDataInformation() {
                    var data = jQuery.extend({}, dataForRequisites);
                    data.list = tableData ? tableData.slice(0) : data.list.slice(0);
                    for (var i = 0; i < data.list.length; i++) {
                        var entity = data.list[i];
                        if ($scope.dataOptions.filter) {
                            if (($scope.dataOptions.filter.inp && entity.inp != $scope.dataOptions.filter.inp)
                                || ($scope.dataOptions.filter.snils && entity.snils != $scope.dataOptions.filter.snils)
                                || ($scope.dataOptions.filter.inn && entity.innRf.innRf != $scope.dataOptions.filter.inn && entity.innSitizenship.innSitizenship != $scope.dataOptions.filter.inn)
                                || ($scope.dataOptions.filter.numberDul && entity.numberDul.number != $scope.dataOptions.filter.numberDul)
                                || ($scope.dataOptions.filter.surname && entity.surname != $scope.dataOptions.filter.surname)
                                || ($scope.dataOptions.filter.name && entity.name != $scope.dataOptions.filter.name)
                                || ($scope.dataOptions.filter.patronymic && entity.patronymic != $scope.dataOptions.filter.patronymic)
                                || ($scope.dataOptions.filter.dateFrom ? ($scope.dataOptions.filter.dateTo ? ($scope.dataOptions.filter.dateFrom > entity.dateOfBirth || $scope.dataOptions.filter.dateTo < entity.dateOfBirth) : $scope.dataOptions.filter.dateFrom > entity.dateOfBirth) : ($scope.dataOptions.filter.dateTo ? $scope.dataOptions.filter.dateTo < entity.dateOfBirth : false))) {
                                data.list.splice(i, 1);
                                i--;
                            }
                        }
                    }

                    $scope.infoGridOptions = {};
                    return aplanaEntityUtils.fillGrid($scope, data, $scope.customGridColumnsBuilder, $scope.infoGridOptions)
                        .then(setButtonsEnabledInformation);
                }

                initPageInformation();

                function initPageInformation() {
                    for (var i = 0; i < dataStub.list.length; i ++){
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




                    //Инициализация грида
                    aplanaEntityUtils.initGrid($scope, fetchDataInformation, setButtonsEnabledInformation);
                    //Инициалиация фильтра (заглушка)

                    //Инициализация параметров сессии - выбраных ранее строк в гриде + фильтры
                    aplanaEntityUtils.initPageSession($scope, fetchDataInformation)
                }


            }])

// ---------------------------------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------------------------------

    ;
}());