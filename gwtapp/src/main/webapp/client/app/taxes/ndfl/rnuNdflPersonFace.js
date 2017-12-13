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

                //Доступгость грида
                $scope.enabledGrid = false;
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
                    toManyResult();
                    isEmptyResult();
                    if(!$scope.isEmptySearchParams) {
                        $scope.enabledGrid = true;
                        $scope.rnuNdflGrid.ctrl.refreshGrid();
                    }else {
                        $scope.enabledGrid = false;
                    }

                };

                $scope.searchFilter = {
                    ajaxFilter: [],
                    params: {},
                    isClear: false,
                    filterName: 'incomesAndTaxFilter'
                };

                var isEmptyResult =  function () {
                    $scope.isEmptySearchParams = !($scope.searchFilter.params.lastName || $scope.searchFilter.params.firstName || $scope.searchFilter.params.middleName ||
                        $scope.searchFilter.params.inp || $scope.searchFilter.params.snils || $scope.searchFilter.params.inn ||
                        $scope.searchFilter.params.idDocNumber || $scope.searchFilter.params.dateFrom || $scope.searchFilter.params.dateTo);
                };

                var toManyResult = function () {
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
                        pagingParams:{}

                    }, function (data) {
                        $scope.records = data.records;
                        $scope.isManyResult = data.records > 10;
                    });
                };

            }]);
}());

