(function () {
    'use strict';

    /**
     * @description Модуль для работы с модальным окном 'Информация по налоговой форме'"
     */
    angular.module('app.rnuNdflPersonFace', [])
    /**
     * @description Контроллер формы создания/ Информация по налоговой форме
     */
        .controller('rnuNdflPersonFaceFormCtrl', ['$scope', '$modalInstance', '$shareData', '$filter', '$http', 'RnuPerson', 'APP_CONSTANTS',
            function ($scope, $modalInstance, $shareData, $filter, $http, RnuPerson, APP_CONSTANTS) {

            //Доступгость грида
            $scope.enabledGrid = false;

            //Доступность кнопки сформировать
            $scope.enabledCreateReport = false;

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
                        personId: $scope.rnuNdflGrid.value[0].personId
                    }


                }).then(function (response) {

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
                                    lastName: (typeof($scope.searchFilter.params.lastName) !== 'undefined') ? '%' + $scope.searchFilter.params.lastName + '%' :  $scope.searchFilter.params.lastName ,
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
                        height: 200,
                        colNames: [
                            '',
                            $filter('translate')('title.lastName'),
                            $filter('translate')('title.firstName'),
                            $filter('translate')('title.middleName'),
                            $filter('translate')('title.snils'),
                            $filter('translate')('title.innNp'),
                            $filter('translate')('title.inp'),
                            $filter('translate')('title.dateOfBirth'),
                            $filter('translate')('title.idDocNumber'),
                            $filter('translate')('title.status.taxpayer')

                        ],
                        colModel: [
                            {name: 'id', index: 'id', width: 100, hide: false},
                            {name: 'lastName', index: 'last_name', width: 140},
                            {name: 'firstName', index: 'first_name', width: 140},
                            {name: 'middleName', index: 'middle_name', width: 140},
                            {name: 'snils', index: 'snils', width: 100, sortable: false},
                            {name: 'innNp', index: 'innNp', width: 100},
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
                        rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                        rowList: APP_CONSTANTS.COMMON.PAGINATION,
                        viewrecords: true,
                        sortname: 'createDate',
                        sortorder: "desc",
                        hidegrid: false,
                        multiselect: false
                    }
                };

            /**
             * @description Отвечает за доступность недоступность кнопки 'сформировать'
             */
            $scope.chekRow = function () {
                if ($scope.rnuNdflGrid.value.length !== null) {
                    $scope.enabledCreateReport = true;
                }
            };

            /**
             * @description Поиск физ лиц для формирования рну
             */
            $scope.searchPerson = function () {
                $scope.enabledGrid = true;
                $scope.rnuNdflGrid.ctrl.refreshGrid();
                $scope.enabledCreateReport = $scope.rnuNdflGrid;
                $scope.enabledCreateReport = false;

            };

            $scope.searchFilter = {
                ajaxFilter: [],
                params: {},
                isClear: false,
                filterName: 'incomesAndTaxFilter'
            };


        }]);
}());

