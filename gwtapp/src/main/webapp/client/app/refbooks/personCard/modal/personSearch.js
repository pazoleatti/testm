(function () {
    'use strict';

    angular.module('app.personSearch', [])

        .controller('personSearchCtrl', ['$scope', '$rootScope', '$modalInstance', '$shareData', '$filter', 'APP_CONSTANTS', 'RefBookFLResource',
            function ($scope, $rootScope, $modalInstance, $shareData, $filter, APP_CONSTANTS, RefBookFLResource) {


                $scope.isEmptySearchParams = true;

                var errorList = [];

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
                 * @description Проверяет на наличие ошибок строкового поля формы. Текст ошибки добавляетс в errorList
                 * @param string строка для проверки
                 * @param requiredLength максимальная длина
                 * @param fieldName название поля
                 */
                var checkStringField = function (string, requiredLength, fieldName) {
                    if (string.length > requiredLength) {
                        var msg = $filter('translate')('reportPersonFace.error.attr') + fieldName +
                            $filter('translate')('reportPersonFace.error.symbolsQuantity') + requiredLength;
                        errorList.push(msg.split(" ").join("\u00a0"));
                    }
                };

                $scope.searchFilter = {
                    ajaxFilter: [],
                    params: {}
                };

                /**
                 * Строковое представление содержимого фильтра.
                 * @returns {string} содержимое фильтра в виде JSON-строки
                 */
                $scope.filterRequestParam = function () {
                    return JSON.stringify({
                        recordId: $shareData.recordId,
                        lastName: $scope.searchFilter.params.lastName,
                        firstName: $scope.searchFilter.params.firstName,
                        middleName: $scope.searchFilter.params.middleName,
                        id: $scope.searchFilter.params.recordId,
                        inn: $scope.searchFilter.params.inn,
                        innForeign: $scope.searchFilter.params.inn,
                        birthDateFrom: $scope.searchFilter.params.birthDateFrom,
                        birthDateTo: $scope.searchFilter.params.birthDateTo,
                        documentNumber: $scope.searchFilter.params.idDocNumber,
                        snils: $scope.searchFilter.params.snils,
                        allVersions: false,
                        versionDate: new Date().format("yyyy-mm-dd")
                    });
                };

                $scope.flGrid = {
                    ctrl: {},
                    value: [],
                    options: {
                        datatype: "angularResource",
                        angularResource: RefBookFLResource,
                        requestParameters: function () {
                            if (!$scope.isEmptySearchParams) {
                                return {
                                    filter: $scope.filterRequestParam(),
                                    projection: 'originalAndDuplicates'
                                };
                            } else {
                                return {
                                    projection: 'originalAndDuplicates'
                                };
                            }

                        },
                        colNames: [
                            '',
                            $filter('translate')('refBook.fl.card.tabs.idDoc.tabColumnHeader.personId'),
                            $filter('translate')('title.lastName'),
                            $filter('translate')('title.firstName'),
                            $filter('translate')('title.middleName'),
                            $filter('translate')('title.dateOfBirth'),
                            $filter('translate')('refBook.fl.table.title.documentType'),
                            $filter('translate')('refBook.fl.table.title.documentNumber'),
                            $filter('translate')('refBook.fl.table.title.inn'),
                            $filter('translate')('refBook.fl.table.title.innForeign'),
                            $filter('translate')('refBook.fl.table.title.snils')
                        ],
                        colModel: [
                            {name: 'id', hidden: true},
                            {
                                name: 'oldId',
                                width: 70
                            },
                            {
                                name: 'lastName',
                                formatter: $filter('personLinkSeparateTabFormatter'),
                                width: 110
                            },
                            {
                                name: 'firstName',
                                width: 110
                            },
                            {
                                name: 'middleName',
                                width: 110
                            },
                            {
                                name: 'birthDate',
                                formatter: $filter('dateFormatter'),
                                width: 110
                            },
                            {
                                name: 'docType',
                                formatter: $filter('docTypeFormatter'),
                                width: 217
                            },
                            {
                                name: 'docNumber',
                                formatter: $filter('permissiveFormatter'),
                                width: 110
                            },
                            {
                                name: 'inn',
                                formatter: $filter('permissiveFormatter'),
                                width: 110
                            },
                            {
                                name: 'innForeign',
                                formatter: $filter('permissiveFormatter'),
                                width: 110
                            },
                            {
                                name: 'snils',
                                formatter: $filter('permissiveFormatter'),
                                width: 110
                            }
                        ],
                        rowNum: 10,
                        viewrecords: true
                    }
                };

                /**
                 * @description запускает поиск по списку
                 */
                $scope.submitSearch = function () {
                    // очищаем список ошибок
                    errorList = [];

                    // Проверяем что заполнено хотя бы одно поле
                    if (fieldsEmpty()) {
                        $scope.infoMessage = $filter('translate')('reportPersonFace.error.fieldsAreEmpty');
                        $scope.showInfo = true;
                        return;
                    } else {
                        $scope.showInfo = false;
                    }

                    for (var param in $scope.searchFilter.params) {
                        for (var field in APP_CONSTANTS.PERSON_SEARCH_FIELDS_ORIGINAL_DUPLICATES) {
                            var fieldProps = APP_CONSTANTS.PERSON_SEARCH_FIELDS_ORIGINAL_DUPLICATES[field];
                            if (fieldProps.alias === param) {
                                checkStringField($scope.searchFilter.params[param], fieldProps.length, fieldProps.label);
                            }
                        }
                    }

                    checkDateField($scope.searchFilter.params.birthDateFrom, $filter('translate')('title.dateOfBirthFrom'));
                    checkDateField($scope.searchFilter.params.birthDateTo, $filter('translate')('title.dateOfBirthTo'));

                    if (errorList.length > 0) {
                        $scope.infoMessage = errorList.join("\n");
                        $scope.showInfo = true;
                        return;
                    }

                    $scope.showInfo = false;
                    $scope.isEmptySearchParams = false;
                    $scope.infoMessage = "";
                    $scope.flGrid.ctrl.refreshGrid();
                };

                $scope.complete = function () {
                    $modalInstance.close($scope.flGrid.value[0]);
                };

                $scope.close = function () {
                    $modalInstance.close();
                };
            }]);
}());

