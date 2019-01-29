(function () {
    'use strict';

    /**
     * @description Модуль для работы с модальным окном 'Информация по налоговой форме'"
     */
    angular.module('app.rnuNdflPersonFace', [])
    /**
     * @description Контроллер формы создания/ Информация по налоговой форме
     */
        .controller('rnuNdflPersonFaceFormCtrl', ['$scope', '$modalInstance', '$shareData', '$filter', '$http', 'RnuPerson', 'APP_CONSTANTS', '$logPanel',
            function ($scope, $modalInstance, $shareData, $filter, $http, RnuPerson, APP_CONSTANTS, $logPanel) {

                $scope.isEmptySearchParams = true;

                /**
                 * @description хранит список сообщений об ошибках
                 * @type {Array}
                 */
                var errorList = [];


                /**
                 * @description список назавния стилей для биндинга с ngStyle
                 * @type {{}}
                 */
                $scope.fieldStyles = {};

                /**
                 * @description Проверяет все ли поля на форме пустые
                 * @returns {boolean}
                 */
                var fieldsEmpty = function () {
                    if (typeof($scope.searchFilter) !== 'undefined' && typeof($scope.searchFilter.params) !== 'undefined') {
                        for (var param in $scope.searchFilter.params) {
                            var paramValue = $scope.searchFilter.params[param];
                            if (paramValue != null && paramValue !== '') {
                                return false;
                            }
                        }
                        return true;
                    } else {
                        return true;
                    }
                };

                /**
                 * @description Проверяет на наличие ошибок строкового поля формы. Текст ошибки добавляетс в errorList
                 * @param string строка для проверки
                 * @param requiredLength максимальная длина
                 * @param fieldName название поля
                 * @param styleName назания стиля для биндинга с ngStyle
                 */
                var checkStringField = function (string, requiredLength, fieldName, styleName) {
                    if (string.length > requiredLength) {
                        var msg = $filter('translate')('reportPersonFace.error.attr') + fieldName +
                            $filter('translate')('reportPersonFace.error.symbolsQuantity') + requiredLength;
                        errorList.push(msg.split(" ").join("\u00a0"));
                        $scope.fieldStyles[styleName] = {"background-color": "#FFCCD2"};
                    }
                };

                /**
                 * @description Проверяет на наличие ошибок строкового поля формы, которое должно состоять из цифр.
                 * Текст ошибки добавляетс в errorList
                 * @param number строка для проверки
                 * @param requiredLength максимальная длина
                 * @param fieldName название поля
                 * @param styleName назания стиля для биндинга с ngStyle
                 */
                var checkNumberField = function (number, requiredLength, fieldName, styleName) {
                    if (number.length > requiredLength || /[^\d]/.test(number)) {
                        var msg = $filter('translate')('reportPersonFace.error.attr') + fieldName +
                            $filter('translate')('reportPersonFace.error.symbolsQuantityAndNotDigits') + requiredLength;
                        errorList.push(msg.split(" ").join("\u00a0"));
                        $scope.fieldStyles[styleName] = {"background-color": "#FFCCD2"};
                    }
                };

                /**
                 * @description Проверяет на наличие ошибок поля формы типа дата. Текст ошибки добавляетс в errorList
                 * @param date дата
                 * @param fieldName название поля
                 */
                var checkDateField = function (date, fieldName) {
                    if (date) {
                        date = new Date(date);
                        date.setHours(0);
                        var mindate = new Date(1900, 0, 1);
                        var maxdate = new Date(2099, 11, 31);

                        if (date < mindate || date > maxdate) {
                            var msg = $filter('translate')('reportPersonFace.error.attr') + fieldName +
                                $filter('translate')('reportPersonFace.error.dateInterval');
                            errorList.push(msg.split(" ").join("\u00a0"));
                        }
                    }
                };

                /**
                 * @description Создание рну ндфл для физ лица
                 */
                $scope.formationRNU = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/" + $shareData.declarationDataId + "/rnuDoc",
                        params: {
                            ndflPersonId: $scope.rnuNdflGrid.value[0].id
                        }
                    }).then(function (response) {
                        if (response.data) {
                            $logPanel.open('log-panel-container', response.data);
                        }
                    });
                    $modalInstance.dismiss();
                };

                /**
                 * @description Закрытие окна
                 */
                $scope.close = function () {
                    $modalInstance.dismiss();
                };

                /**
                 * Grid для отображения найденных физ лиц в документе
                 */
                $scope.rnuNdflGrid = {
                    init: function (ctrl) {
                        ctrl.gridComplete = function () {
                            $scope.showInfo = true;
                            if ($scope.rnuNdflGrid.ctrl.getCountRecords() > 10) {
                                $scope.infoMessage = $filter('translate')('ndfl.rnuNdflPersonFace.manyRecords', {count: $scope.rnuNdflGrid.ctrl.getCountRecords()});
                            } else {
                                $scope.infoMessage = $filter('translate')('ndfl.rnuNdflPersonFace.countRecords', {count: $scope.rnuNdflGrid.ctrl.getCountRecords()});
                            }
                        };
                    },
                    ctrl: {},
                    value: [],
                    options: {
                        datatype: "angularResource",
                        angularResource: RnuPerson,
                        requestParameters: function () {
                            if (!$scope.isEmptySearchParams) {
                                return {
                                    projection: 'rnuPersons',
                                    declarationDataId: $shareData.declarationDataId,
                                    ndflPersonFilter: JSON.stringify({
                                        lastName: (typeof($scope.searchFilter.params.lastName) !== 'undefined') ? '%' + $scope.searchFilter.params.lastName + '%' : $scope.searchFilter.params.lastName,
                                        firstName: (typeof($scope.searchFilter.params.firstName) !== 'undefined') ? '%' + $scope.searchFilter.params.firstName + '%' : $scope.searchFilter.params.firstName,
                                        middleName: (typeof($scope.searchFilter.params.middleName) !== 'undefined') ? '%' + $scope.searchFilter.params.middleName + '%' : $scope.searchFilter.params.middleName,
                                        inp: (typeof($scope.searchFilter.params.inp) !== 'undefined') ? '%' + $scope.searchFilter.params.inp + '%' : $scope.searchFilter.params.inp,
                                        snils: (typeof($scope.searchFilter.params.snils) !== 'undefined') ? '%' + $scope.searchFilter.params.snils + '%' : $scope.searchFilter.params.snils,
                                        innNp: (typeof($scope.searchFilter.params.inn) !== 'undefined') ? '%' + $scope.searchFilter.params.inn + '%' : $scope.searchFilter.params.inn,
                                        idDocNumber: (typeof($scope.searchFilter.params.idDocNumber) !== 'undefined') ? '%' + $scope.searchFilter.params.idDocNumber + '%' : $scope.searchFilter.params.idDocNumber,
                                        dateFrom: $scope.searchFilter.params.dateFrom,
                                        dateTo: $scope.searchFilter.params.dateTo
                                    })
                                };
                            } else {
                                return {
                                    projection: 'rnuPersons',
                                    declarationDataId: 0,
                                    ndflPersonFilter: {}
                                };
                            }
                        },
                        height: 220,
                        colNames: [
                            '',
                            $filter('translate')('title.lastName'),
                            $filter('translate')('title.firstName'),
                            $filter('translate')('title.middleName'),
                            $filter('translate')('title.snils'),
                            $filter('translate')('title.innNp'),
                            $filter('translate')('title.innForeign'),
                            $filter('translate')('title.inp'),
                            $filter('translate')('title.dateOfBirth'),
                            $filter('translate')('title.idDocNumber'),
                            $filter('translate')('title.status.taxpayer')

                        ],
                        colModel: [
                            {name: 'id', index: 'id', width: 100, key: true, hidden: true},
                            {name: 'lastName', index: 'last_name', width: 130},
                            {name: 'firstName', index: 'first_name', width: 130},
                            {name: 'middleName', index: 'middle_name', width: 130},
                            {name: 'snils', index: 'snils', width: 100},
                            {name: 'innNp', index: 'inn_np', width: 100},
                            {name: 'innForeign', index: 'inn_foreign', width: 100},
                            {name: 'inp', index: 'inp', width: 100},
                            {
                                name: 'birthDay',
                                index: 'birth_day',
                                width: 100,
                                formatter: $filter('dateFormatter')
                            },
                            {name: 'idDocNumber', index: 'id_doc_number', width: 95},
                            {name: 'status', index: 'status', width: 160}
                        ],
                        rowNum: 10,
                        viewrecords: true,
                        sortname: 'last_name',
                        sortorder: "asc",
                        disableAutoLoad: true,
                        hidegrid: false,
                        multiselect: false
                    }
                };

                /**
                 * @description Поиск физ лиц для формирования рну
                 */
                $scope.searchPerson = function () {
                    // очищаем список ошибок
                    errorList = [];
                    // очищаем поля от стиля ошибок
                    for (var fieldStyle in $scope.fieldStyles) {
                        $scope.fieldStyles[fieldStyle] = {};
                    }
                    // Проверяем что заполнено хотя бы одно поле
                    if (fieldsEmpty()) {
                        $scope.infoMessage = $filter('translate')('reportPersonFace.error.fieldsAreEmpty');
                        $scope.showInfo = true;
                        return;
                    } else {
                        $scope.showInfo = false;
                    }

                    for (var param in $scope.searchFilter.params) {
                        for (var field in APP_CONSTANTS.PERSON_SEARCH_FIELDS_RNU) {
                            var fieldProps = APP_CONSTANTS.PERSON_SEARCH_FIELDS_RNU[field];
                            if (fieldProps.alias === param) {
                                if (fieldProps === APP_CONSTANTS.PERSON_SEARCH_FIELDS_RNU.SNILS) {
                                    checkNumberField($scope.searchFilter.params[param], fieldProps.length, fieldProps.label, param);
                                } else {
                                    checkStringField($scope.searchFilter.params[param], fieldProps.length, fieldProps.label, param);
                                }
                            }
                        }
                    }

                    checkDateField($scope.searchFilter.params.dateFrom, $filter('translate')('title.dateOfBirthFrom'));
                    checkDateField($scope.searchFilter.params.dateTo, $filter('translate')('title.dateOfBirthTo'));

                    if (errorList.length > 0) {
                        $scope.infoMessage = errorList.join("\n");
                        $scope.showInfo = true;
                        return;
                    }

                    $scope.showInfo = false;
                    $scope.isEmptySearchParams = false;
                    $scope.infoMessage = "";

                    $scope.rnuNdflGrid.ctrl.refreshGrid();
                };

                $scope.searchFilter = {
                    ajaxFilter: [],
                    params: {},
                    isClear: false,
                    filterName: 'rnuNdflPersonFaceFilter'
                };

            }]);
}());

