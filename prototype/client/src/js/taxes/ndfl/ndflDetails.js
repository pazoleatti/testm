'use strict';
(function () {

    angular.module('sbrfNdfl.ndflDetailsForms', ['ui.router'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('ndflDetailsForms', {
                url: '/taxes/ndflDetails/{formId}',
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
                        if (field.type == "java.util.Date") {
                            var format = field.format == "" ? "dd.MM.yyyy" : field.format;
                            columnDef.type = 'date';
                            columnDef.cellFilter = "date:'" + format + "'";
                        }
                        gridOptions.columnDefs.push(columnDef);
                    }

                    //aplanaEntityUtils.fitColumnsWidth(gridOptions, $scope.gridApi.grid.gridWidth);
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
                            codeSub: {id: 7, codeSub:"001", index: "079685", area: "Тюменский", city: "Октябрьский", locality: "Октябрьский", street: "Ленина", building: "99", housing: "1", apartment: "102"},
                            index: {id: 7, codeSub:"001", index: "079685", area: "Тюменский", city: "Октябрьский", locality: "Октябрьский", street: "Ленина", building: "99", housing: "1", apartment: "102"},
                            area: {id: 7, codeSub:"001", index: "079685", area: "Тюменский", city: "Октябрьский", locality: "Октябрьский", street: "Ленина", building: "99", housing: "1", apartment: "102"},
                            city: {id: 7, codeSub:"001", index: "079685", area: "Тюменский", city: "Октябрьский", locality: "Октябрьский", street: "Ленина", building: "99", housing: "1", apartment: "102"},
                            locality: {id: 7, codeSub:"001", index: "079685", area: "Тюменский", city: "Октябрьский", locality: "Октябрьский", street: "Ленина", building: "99", housing: "1", apartment: "102"},
                            street: {id: 7, codeSub:"001", index: "079685", area: "Тюменский", city: "Октябрьский", locality: "Октябрьский", street: "Ленина", building: "99", housing: "1", apartment: "102"},
                            building: {id: 7, codeSub:"001", index: "079685", area: "Тюменский", city: "Октябрьский", locality: "Октябрьский", street: "Ленина", building: "99", housing: "1", apartment: "102"},
                            housing: {id: 7, codeSub:"001", index: "079685", area: "Тюменский", city: "Октябрьский", locality: "Октябрьский", street: "Ленина", building: "99", housing: "1", apartment: "102"},
                            apartment: {id: 7, codeSub:"001", index: "079685", area: "Тюменский", city: "Октябрьский", locality: "Октябрьский", street: "Ленина", building: "99", housing: "1", apartment: "102"}
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
                            codeSub: {id: 1, codeSub:"023", index: "17753", area: "Львовская", city: "Львов", locality: "Львов", street: "Красная", building: "1", housing: "4", apartment: "54"},
                            index: {id: 1, codeSub:"023", index: "17753", area: "Львовская", city: "Львов", locality: "Львов", street: "Красная", building: "1", housing: "4", apartment: "54"},
                            area: {id: 1, codeSub:"023", index: "17753", area: "Львовская", city: "Львов", locality: "Львов", street: "Красная", building: "1", housing: "4", apartment: "54"},
                            city: {id: 1, codeSub:"023", index: "17753", area: "Львовская", city: "Львов", locality: "Львов", street: "Красная", building: "1", housing: "4", apartment: "54"},
                            locality: {id: 1, codeSub:"023", index: "17753", area: "Львовская", city: "Львов", locality: "Львов", street: "Красная", building: "1", housing: "4", apartment: "54"},
                            street: {id: 1, codeSub:"023", index: "17753", area: "Львовская", city: "Львов", locality: "Львов", street: "Красная", building: "1", housing: "4", apartment: "54"},
                            building: {id: 1, codeSub:"023", index: "17753", area: "Львовская", city: "Львов", locality: "Львов", street: "Красная", building: "1", housing: "4", apartment: "54"},
                            housing: {id: 1, codeSub:"023", index: "17753", area: "Львовская", city: "Львов", locality: "Львов", street: "Красная", building: "1", housing: "4", apartment: "54"},
                            apartment: {id: 1, codeSub:"023", index: "17753", area: "Львовская", city: "Львов", locality: "Львов", street: "Красная", building: "1", housing: "4", apartment: "54"}
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
                            "name": "innRf",
                            "type": "com.ndlf.model.Inn",
                            "displayField": "innRf",
                            "title": "ИНН. В РФ",
                            "width": 95,
                            "order": 9,
                            "visible": true
                        },
                        {
                            "name": "innSitizenship",
                            "type": "com.ndlf.model.Inn",
                            "displayField": "innSitizenship",
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
                            "width": 20,
                            "order": 11,
                            "visible": true
                        },
                        {
                            "name": "numberDul",
                            "type": "com.ndlf.model.Dul",
                            "displayField": "number",
                            "title": "ДУЛ. Номер",
                            "width": 20,
                            "order": 12,
                            "visible": true
                        },
                        {
                            "name": "status",
                            "type": "com.ndlf.model.Status",
                            "displayField": "id",
                            "title": "Статус (код)",
                            "width": 20,
                            "order": 13,
                            "visible": true
                        },
                        {
                            "name": "codeSub",
                            "type": "com.ndlf.model.Address",
                            "displayField": "codeSub",
                            "title": "Адрес рег. в РФ. Код субъекта",
                            "width": 20,
                            "order": 14,
                            "visible": true
                        },
                        {
                            "name": "index",
                            "type": "com.ndlf.model.Address",
                            "displayField": "index",
                            "title": "Адрес рег. в РФ. Индекс",
                            "width": 20,
                            "order": 15,
                            "visible": true
                        },
                        {
                            "name": "area",
                            "type": "com.ndlf.model.Address",
                            "displayField": "area",
                            "title": "Адрес рег. в РФ. Район",
                            "width": 20,
                            "order": 16,
                            "visible": true
                        },
                        {
                            "name": "city",
                            "type": "com.ndlf.model.Address",
                            "displayField": "city",
                            "title": "Адрес рег. в РФ. Город",
                            "width": 20,
                            "order": 17,
                            "visible": true
                        },
                        {
                            "name": "locality",
                            "type": "com.ndlf.model.Address",
                            "displayField": "locality",
                            "title": "Адрес рег. в РФ. Населенный пункт",
                            "width": 20,
                            "order": 18,
                            "visible": true
                        },
                        {
                            "name": "street",
                            "type": "com.ndlf.model.Address",
                            "displayField": "street",
                            "title": "Адрес рег. в РФ. Улица",
                            "width": 20,
                            "order": 19,
                            "visible": true
                        },
                        {
                            "name": "building",
                            "type": "com.ndlf.model.Address",
                            "displayField": "building",
                            "title": "Адрес рег. в РФ. Дом",
                            "width": 20,
                            "order": 20,
                            "visible": true
                        },
                        {
                            "name": "housing",
                            "type": "com.ndlf.model.Address",
                            "displayField": "housing",
                            "title": "Адрес рег. в РФ. Корпус",
                            "width": 20,
                            "order": 21,
                            "visible": true
                        },
                        {
                            "name": "apartment",
                            "type": "com.ndlf.model.Address",
                            "displayField": "apartment",
                            "title": "Адрес рег. в РФ. Квартира",
                            "width": 20,
                            "order": 22,
                            "visible": true
                        }],
                    "offset": 1,
                    "total": 1,
                    "count": 1
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
                            //Доступности кнопок над вкладками
                            $scope.checkButtonEnabled = true;
                            $scope.calculateButtonEnabled = true;
                            $scope.acceptButtonEnabled = dataStub.list[i].state.id === 1;
                            $scope.returnButtonEnabled = dataStub.list[i].state.id !== 1;
                            $scope.editButtonEnabled = true;
                        }
                    }

                    return aplanaEntityUtils.fillGrid($scope, data, $scope.customGridColumnsBuilder)
                        .then(setButtonsEnabled);
                }

                /**
                 * Инициализация кнопок
                 */
                function setButtonsEnabled() {
                    $scope.createReqsEnabled = true;
                    if (!$scope.gridApi || !$scope.currentEntityView) {
                        $scope.editReqEnabled = false;
                        $scope.deleteReqsEnabled = false;
                    } else {
                        $scope.editReqsEnabled = $scope.gridApi.grid.selection.selectedCount == 1;
                        $scope.deleteReqsEnabled = $scope.gridApi.grid.selection.selectedCount > 0;
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

                    //Инициализация параметров сессии - выбраных ранее строк в гриде + фильтры
                    aplanaEntityUtils.initPageSession($scope, fetchData)
                }

                $scope.$watchCollection('[dataOptions.filter.fulltext]', function () {
                    aplanaEntityUtils.saveFilter($scope);
                });
            }])
    ;
}());