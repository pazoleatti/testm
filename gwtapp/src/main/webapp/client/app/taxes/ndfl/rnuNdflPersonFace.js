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
                 * @description Создание рну ндфл для физ лица
                 */
                $scope.formationRNU = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/declarationData/" + $shareData.declarationDataId + "/rnuDoc",
                        params: {
                            ndflPersonFilter: JSON.stringify({
                                declarationDataId: $shareData.declarationDataId,
                                lastName: $scope.searchFilter.params.lastName,
                                firstName: $scope.searchFilter.params.firstName,
                                middleName: $scope.searchFilter.params.middleName,
                                innNp: $scope.searchFilter.params.innNp,
                                innForeign: $scope.searchFilter.params.innForeign,
                                snils: $scope.searchFilter.params.snils,
                                idDocNumber: $scope.searchFilter.params.idDocNumber,
                                dateFrom: $scope.searchFilter.params.dateFrom,
                                dateTo: $scope.searchFilter.params.dateTo
                            }),
                            ndflPersonId: $scope.rnuNdflGrid.value[0].id
                        }


                    }).then(function (response) {
                        if (response && response.data && response.data.uuid) {
                            $logPanel.open('log-panel-container', response.data.uuid);
                        }
                    });
                    $modalInstance.dismiss('Canceled');
                };

                /**
                 * @description Закрытие окна
                 */
                $scope.close = function () {
                    $modalInstance.dismiss('Canceled');
                };

                /**
                 * Grid для отображения найденных физ лиц в документе
                 */
                $scope.rnuNdflGrid =
                    {
                        ctrl: {},
                        value: [],
                        options: {
                            datatype: "angularResource",
                            angularResource: RnuPerson,
                            requestParameters: function () {
                                if (!$scope.isEmptySearchParams) {
                                    return {
                                        projection: 'rnuPersons',
                                        ndflPersonFilter: JSON.stringify({
                                            declarationDataId: $shareData.declarationDataId,
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
                                {name: 'lastName', index: 'last_name', width: 140},
                                {name: 'firstName', index: 'first_name', width: 140},
                                {name: 'middleName', index: 'middle_name', width: 140},
                                {name: 'snils', index: 'snils', width: 100, sortable: false},
                                {name: 'innNp', index: 'innNp', width: 100},
                                {name: 'innForeign', index: 'innForeign', width: 100},
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
                            sortname: 'createDate',
                            sortorder: "desc",
                            hidegrid: false,
                            multiselect: false
                        }
                    };

                /**
                 * @description Поиск физ лиц для формирования рну
                 */
                $scope.searchPerson = function () {
                    if (validate()) {
                        $scope.infoMessage = "";
                        totalCountResult();
                        $scope.rnuNdflGrid.ctrl.refreshGrid();
                    }

                };

                $scope.searchFilter = {
                    ajaxFilter: [],
                    params: {},
                    isClear: false,
                    filterName: 'rnuNdflPersonFaceFilter'
                };

                var validate = function () {
                    isEmptySearchPararms();
                    if ($scope.isEmptySearchParams) {
                        return false;
                    }
                    if (!checkDateInterval()) {
                        $scope.infoMessage = $filter('translate')('rnuPersonFace.error.dateInterval');
                        return false;
                    }
                    if (!checkDateField()) {
                        $scope.infoMessage = $filter('translate')('rnuPersonFace.error.dateIntervalOutOfBounds');
                        return false;
                    }
                    return true;
                };


                var isEmptySearchPararms = function () {
                    $scope.isEmptySearchParams = !($scope.searchFilter.params.lastName || $scope.searchFilter.params.firstName || $scope.searchFilter.params.middleName ||
                        $scope.searchFilter.params.inp || $scope.searchFilter.params.snils || $scope.searchFilter.params.inn ||
                        $scope.searchFilter.params.idDocNumber || $scope.searchFilter.params.dateFrom || $scope.searchFilter.params.dateTo);
                };

                var totalCountResult = function () {
                    RnuPerson.query({
                        projection: 'rnuPersons',
                        ndflPersonFilter: JSON.stringify({
                            declarationDataId: $shareData.declarationDataId,
                            lastName: (typeof($scope.searchFilter.params.lastName) !== 'undefined') ? '%' + $scope.searchFilter.params.lastName + '%' : $scope.searchFilter.params.lastName,
                            firstName: (typeof($scope.searchFilter.params.firstName) !== 'undefined') ? '%' + $scope.searchFilter.params.firstName + '%' : $scope.searchFilter.params.firstName,
                            middleName: (typeof($scope.searchFilter.params.middleName) !== 'undefined') ? '%' + $scope.searchFilter.params.middleName + '%' : $scope.searchFilter.params.middleName,
                            inp: (typeof($scope.searchFilter.params.inp) !== 'undefined') ? '%' + $scope.searchFilter.params.inp + '%' : $scope.searchFilter.params.inp,
                            snils: (typeof($scope.searchFilter.params.snils) !== 'undefined') ? '%' + $scope.searchFilter.params.snils + '%' : $scope.searchFilter.params.snils,
                            innNp: (typeof($scope.searchFilter.params.inn) !== 'undefined') ? '%' + $scope.searchFilter.params.inn + '%' : $scope.searchFilter.params.inn,
                            idDocNumber: (typeof($scope.searchFilter.params.idDocNumber) !== 'undefined') ? '%' + $scope.searchFilter.params.idDocNumber + '%' : $scope.searchFilter.params.idDocNumber,
                            dateFrom: $scope.searchFilter.params.dateFrom,
                            dateTo: $scope.searchFilter.params.dateTo
                        }),
                        pagingParams: {}

                    }, function (data) {
                        if (data.records > 10) {
                            $scope.infoMessage = $filter('translate')('ndfl.rnuNdflPersonFace.manyRecords', {count: data.records});
                        } else {
                            $scope.infoMessage = $filter('translate')('ndfl.rnuNdflPersonFace.countRecords', {count: data.records});
                        }
                    });
                };

                /**
                 * Валидатор диапазона дат. Проверяет, что стартовая дата не превышает конечную
                 * @returns {boolean} признак корректности диапазона дат
                 */
                var checkDateInterval = function () {
                    return ($scope.searchFilter.params.dateFrom === undefined || $scope.searchFilter.params.dateFrom === null || $scope.searchFilter.params.dateFrom === "") ||
                        ($scope.searchFilter.params.dateTo === undefined || $scope.searchFilter.params.dateTo === null || $scope.searchFilter.params.dateTo === "") ||
                        (truncHMS(new Date($scope.searchFilter.params.dateFrom)) <= truncHMS(new Date($scope.searchFilter.params.dateTo)));
                };

                // Отбрасывает часы, минуты, секунды, миллисекунды
                var truncHMS = function (date) {
                    if (!_.isUndefined(date) && !_.isNull(date) && date !== "") {
                        date.setHours(0);
                        date.setMinutes(0);
                        date.setSeconds(0);
                        date.setMilliseconds(0);
                        return date.getTime();
                    }
                    return date;
                };

                /**
                 * @description Проверяет на наличие ошибок поля формы Дата рождения.
                 */
                var checkDateField = function () {
                    var mindate = new Date();
                    var maxdate = new Date();
                    var dateFrom = $scope.searchFilter.params.dateFrom ? new Date($scope.searchFilter.params.dateFrom) : null;
                    var dateTo = $scope.searchFilter.params.dateTo ? new Date($scope.searchFilter.params.dateTo) : null;
                    mindate.setFullYear(1900, 0, 1);
                    maxdate.setFullYear(2100, 11, 31);

                    return (!dateFrom || mindate < dateFrom && dateFrom < maxdate) &&
                        (!dateTo || mindate < dateTo && dateTo < maxdate);
                };

            }]);
}());

