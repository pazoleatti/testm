(function () {
    'use strict';

    /**
     * @description Модуль для работы с модальным окном 'Отчет 2-НДФЛ по физическому лицу'"
     */
    angular.module('app.reportNdflPersonFace', [])
    /**
     * @description Контроллер формы создания отчета 2-НДФЛ по физическому лицу
     */
        .controller('reportNdflPersonFaceFormCtrl', ['$scope', '$modalInstance', '$shareData', '$filter',
            '$dialogs', 'APP_CONSTANTS', '$logPanel', 'prepareSpecificReport', 'createReport',
            function ($scope, $modalInstance, $shareData, $filter, $dialogs, APP_CONSTANTS, $logPanel,
                      prepareSpecificReport, createReport) {


                $scope.searchFilter = {
                    ajaxFilter: [],
                    params: {},
                    isClear: false,
                    filterName: 'reportNdflPersonFaceFilter'
                };

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
                 * @description хранит список сообщений об ошибках
                 * @type {Array}
                 */
                var errorList = [];

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
                 * @description Проверяет на наличие ошибок поля формы типа дата. Текст ошибки добавляетс в errorList
                 * @param date дата
                 * @param fieldName название поля
                 */
                var checkDateField = function (date, fieldName) {
                    var mindate = new Date();
                    var maxdate = new Date();
                    mindate.setFullYear(1900, 0, 1);
                    maxdate.setFullYear(2100, 11, 31);
                    if (date && !date instanceof Date) {
                        date = new Date(date);
                    }

                    if (date < mindate || date > maxdate) {
                        var msg = $filter('translate')('reportPersonFace.error.attr') + fieldName +
                            $filter('translate')('reportPersonFace.error.dateInterval');
                        errorList.push(msg.split(" ").join("\u00a0"));
                    }
                };

                /**
                 * @description видимость грида
                 * @type {boolean}
                 */
                $scope.enabledGrid = false;

                /**
                 * @description Наименование столбцов
                 * @type {Array}
                 */
                $scope.colNames = [];

                /**
                 * @description Характеристики столбцов
                 * @type {Array}
                 */
                $scope.colModel = [];

                /**
                 * @description список назавния стилей для биндинга с ngStyle
                 * @type {{}}
                 */
                $scope.fieldStyles = {};

                /**
                 *
                 * @description грид для вывода результата по найденным физлицам
                 */
                $scope.reportNdflGrid = {
                    ctrl: {},
                    value: [],
                    options: {
                        datatype: "local",
                        height: 'auto',
                        colNames: $scope.colNames,
                        data: [],
                        colModel: $scope.colModel,
                        multiselect: false
                    }
                };

                $scope.searchPerson = function () {
                    // очищаем список ошибок
                    errorList = [];
                    // очищаем поля от стиля ошибок
                    for (var fieldStyle in $scope.fieldStyles) {
                        $scope.fieldStyles[fieldStyle] = {};
                    }
                    // Проверяем что заполнено хотя бы одно поле
                    if (fieldsEmpty()) {
                        $scope.infoText = $filter('translate')('reportPersonFace.error.fieldsAreEmpty');
                        $scope.showInfo = true;
                        return;
                    } else {
                        $scope.showInfo = false;
                    }

                    for (var param in $scope.searchFilter.params) {
                        for (var field in APP_CONSTANTS.PERSON_SEARCH_FIELDS) {
                            var fieldProps = APP_CONSTANTS.PERSON_SEARCH_FIELDS[field];
                            if (fieldProps.alias === param) {
                                checkStringField($scope.searchFilter.params[param], fieldProps.length, fieldProps.label, param);
                            }
                        }
                    }

                    checkDateField($scope.searchFilter.params.dateFrom, $filter('translate')('title.dateOfBirthFrom'));
                    checkDateField($scope.searchFilter.params.dateTo, $filter('translate')('title.dateOfBirthTo'));

                    if (errorList.length > 0) {
                        $scope.infoText = errorList.join("\n");
                        $scope.showInfo = true;
                        return;
                    }

                    $scope.showInfo = false;

                    prepareSpecificReport.doOperation({
                            declarationDataId: $shareData.declarationDataId,
                            taxType: 'NDFL',
                            type: 'report_2ndfl',
                            subreportParamValues: {
                                pNumSpravka: $scope.searchFilter.params.refNumber,
                                lastName: $scope.searchFilter.params.lastName,
                                firstName: $scope.searchFilter.params.firstName,
                                middleName: $scope.searchFilter.params.middleName,
                                inn: $scope.searchFilter.params.inn,
                                idDocNumber: $scope.searchFilter.params.idDocNumber,
                                fromBirthDay: $scope.searchFilter.params.dateFrom,
                                toBirthDay: $scope.searchFilter.params.dateTo
                            }
                        },
                        function (response) {
                            // Очищаем грид от данных
                            $scope.reportNdflGrid.ctrl.getGrid().jqGrid('clearGridData');
                            var availableDataRows = response.prepareSpecificReportResult.countAvailableDataRows;
                            $scope.infoText = $filter('translate')('reportPersonFace.info.numberOfFoundEntries') + availableDataRows + ".";
                            $scope.showInfo = true;
                            var resultSize = response.prepareSpecificReportResult.dataRows.length;
                            if (resultSize === 0) {
                                var messageParts = [];
                                for (var param in $scope.searchFilter.params) {
                                    switch (param) {
                                        case 'dateTo':
                                            messageParts.push($scope.searchFilter.params[param].format("dd.mm.yyyy"));
                                            break;
                                        case 'dateFrom':
                                            messageParts.push($scope.searchFilter.params[param].format("dd.mm.yyyy"));
                                            break;
                                        default:
                                            messageParts.push($scope.searchFilter.params[param]);
                                    }
                                }
                                return;
                            }
                            if (resultSize < availableDataRows) {
                                $scope.infoText = $filter('translate')('reportPersonFace.info.found') +
                                    availableDataRows + $filter('translate')('reportPersonFace.info.entriesShowed') +
                                    resultSize + $filter('translate')('reportPersonFace.info.needSearchClarify');
                                $scope.showInfo = true;
                            }
                            var columns = response.prepareSpecificReportResult.tableColumns;
                            var i = 0;
                            for (; i < columns.length; i++) {
                                $scope.colNames.push(columns[i].name);
                                if (columns[i].alias === 'pNumSpravka') {
                                    $scope.colModel.push({
                                        name: columns[i].alias,
                                        index: columns[i].alias,
                                        width: columns[i].width * 10,
                                        key: true
                                    });
                                } else {
                                    $scope.colModel.push({
                                        name: columns[i].alias,
                                        index: columns[i].alias,
                                        width: columns[i].width * 10
                                    });
                                }

                            }

                            $scope.reportNdflGrid.ctrl.rebuildGrid();

                            $scope.reportNdflGrid.ctrl.refreshGridData(response.prepareSpecificReportResult.dataRows);

                            $scope.enabledGrid = true;
                        });
                };

                /**
                 * @description Сформировать спецотчет
                 * @param force
                 * @param create
                 */
                $scope.createSubreport = function (force, create) {
                    {
                        force = typeof force !== 'undefined' ? force : false;
                        create = typeof create !== 'undefined' ? create : false;
                    }

                    createReport.query({
                            declarationDataId: $shareData.declarationDataId,
                            isForce: force,
                            taxType: 'NDFL',
                            type: APP_CONSTANTS.SUBREPORT_ALIAS_CONSTANTS.REPORT_2NDFL,
                            create: create,
                            selectedRow: $scope.reportNdflGrid.value[0],
                            subreportParamValues: {
                                pNumSpravka: $scope.searchFilter.params.refNumber,
                                lastName: $scope.searchFilter.params.lastName,
                                firstName: $scope.searchFilter.params.firstName,
                                middleName: $scope.searchFilter.params.middleName,
                                inn: $scope.searchFilter.params.inn,
                                idDocNumber: $scope.searchFilter.params.idDocNumber,
                                fromBirthDay: $scope.searchFilter.params.dateFrom,
                                toBirthDay: $scope.searchFilter.params.dateTo
                            }
                        },
                        function (response) {
                            if (response.uuid && response.uuid !== null) {
                                $logPanel.open('log-panel-container', response.uuid);
                            }
                            $modalInstance.close();
                        }
                    );
                };

                $scope.close = function () {
                    $modalInstance.dismiss('Canceled');
                };


            }]);


}());

