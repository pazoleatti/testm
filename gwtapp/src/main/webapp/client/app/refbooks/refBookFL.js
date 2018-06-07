(function () {
    'use strict';

    /**
     * @description Модуль для отображения справочника физических лиц
     */
    angular.module('app.refBookFL', ['app.refBookRecordModal', 'app.originalAndDuplicatesModal'])
        .config(['$stateProvider', function ($stateProvider) {
            $stateProvider.state('refBookFL', {
                url: '/refBooks/linearRefBook/904?recordId',
                templateUrl: 'client/app/refbooks/refBookFL.html?v=${buildUuid}',
                controller: 'refBookFLCtrl',
                onEnter: ['$state', 'PermissionChecker', 'APP_CONSTANTS', '$rootScope',
                    function ($state, PermissionChecker, APP_CONSTANTS, $rootScope) {
                        if (!PermissionChecker.check($rootScope.user, APP_CONSTANTS.USER_PERMISSION.VIEW_NSI)) {
                            $state.go("/");
                        }
                    }]
            });
        }])
        .controller('refBookFLCtrl', ['$scope', "$stateParams", "$injector", "$compile", "APP_CONSTANTS",
            "RefBookResource", "RefBookFLResource", "$aplanaModal", '$filter', '$window', "$http", "$logPanel", "$dialogs",
            function ($scope, $stateParams, $injector, $compile, APP_CONSTANTS, RefBookResource, RefBookFLResource,
                      $aplanaModal, $filter, $window, $http, $logPanel, $dialogs) {
                $scope.refBookId = APP_CONSTANTS.REFBOOK.PERSON;
                $scope.columnNames = [];
                $scope.columnModel = [];
                $scope.data = {};
                $scope.data.recordVersion = new Date();
                $scope.versionMode = $stateParams.recordId && $stateParams.recordId !== 'undefined';
                $scope.search = {
                    text: "",
                    precise: false
                };

                // Получаем данные справочника
                RefBookResource.query({
                    id: $scope.refBookId
                }, function (data) {
                    $scope.refBook = data;
                    // Определяем атрибут для сортировки по умолчанию в справочнике
                    if ($scope.refBook.sortAttribute) {
                        // Если в настройках справочника указан атрибут для сортировки - берем его
                        $scope.sortAttribute =  $scope.refBook.sortAttribute.alias;
                    } else {
                        // Иначе первый атрибут в списке
                        $scope.sortAttribute =  $scope.refBook.attributes[0].alias;
                    }

                    // Добавляем информацию о периоде действия записей
                    $scope.refBook.attributes.unshift({
                        name: $filter('translate')('refBook.versionTo'),
                        alias: APP_CONSTANTS.REFBOOK_ALIAS.RECORD_VERSION_TO_ALIAS,
                        attributeType: 'DATE',
                        required: true,
                        visible: true,
                        width: 5
                    });
                    $scope.refBook.attributes.unshift({
                        name: $filter('translate')('refBook.versionFrom'),
                        alias: APP_CONSTANTS.REFBOOK_ALIAS.RECORD_VERSION_FROM_ALIAS,
                        attributeType: 'DATE',
                        required: true,
                        visible: true,
                        width: 5
                    });
                });

                $scope.personGrid = {
                    ctrl: {},
                    value: [],
                    options: {
                        datatype: "angularResource",
                        angularResource: RefBookFLResource,
                        requestParameters: function () {
                            return {
                                recordId: $stateParams.recordId,
                                version: $scope.data.recordVersion,
                                firstName: $scope.search.firstName,
                                lastName: $scope.search.lastName,
                                searchPattern: $scope.search.text,
                                exactSearch: $scope.search.precise
                            };
                        },
                        value: [],
                        colNames: [
                            '',
                            $filter('translate')('refBook.versionFrom'),
                            $filter('translate')('refBook.versionTo'),
                            $filter('translate')('refBook.fl.title.id'),
                            $filter('translate')('title.lastName'),
                            $filter('translate')('title.firstName'),
                            $filter('translate')('title.middleName'),
                            $filter('translate')('refBook.fl.title.inn'),
                            $filter('translate')('refBook.fl.title.innForeign'),
                            $filter('translate')('title.snils'),
                            $filter('translate')('title.status.taxpayer'),
                            $filter('translate')('title.dateOfBirth'),
                            $filter('translate')('refBook.fl.title.birthPlace'),
                            $filter('translate')('refBook.fl.title.citizenship'),
                            $filter('translate')('refBook.fl.title.address'),
                            $filter('translate')('refBook.fl.title.employee'),
                            $filter('translate')('refBook.fl.title.sourceId'),
                            $filter('translate')('refBook.fl.title.oldId')
                        ],
                        colModel: [
                            {name: 'id', index: 'id', width: 10, key: true, hidden: true},
                            {
                                name: 'version',
                                index: 'version',
                                width: 120,
                                formatter: $filter('dateFormatter'),
                                sortable: false
                            },
                            {
                                name: 'versionEnd',
                                index: 'versionEnd',
                                width: 120,
                                formatter: $filter('dateFormatter'),
                                sortable: false
                            },
                            {name: 'recordId', index: 'RECORD_ID', width: 120, sortable: !$scope.versionMode},
                            {name: 'lastName', index: 'LAST_NAME', width: 150, sortable: !$scope.versionMode},
                            {name: 'firstName', index: 'FIRST_NAME', width: 150, sortable: !$scope.versionMode},
                            {name: 'middleName', index: 'MIDDLE_NAME', width: 150, sortable: !$scope.versionMode},
                            {name: 'inn', index: 'INN', width: 100, sortable: !$scope.versionMode},
                            {name: 'innForeign', index: 'INN_FOREIGN', width: 100, sortable: !$scope.versionMode},
                            {name: 'snils', index: 'SNILS', width: 100, sortable: !$scope.versionMode},
                            {
                                name: 'taxpayerState',
                                index: 'TAXPAYER_STATE',
                                width: 135,
                                formatter: $filter('codeFormatter'),
                                sortable: !$scope.versionMode
                            },
                            {
                                name: 'birthDate',
                                index: 'BIRTH_DATE',
                                width: 100,
                                formatter: $filter('dateFormatter'),
                                sortable: !$scope.versionMode
                            },
                            {name: 'birthPlace', index: 'BIRTH_PLACE', width: 150},
                            {
                                name: 'citizenship',
                                index: 'CITIZENSHIP',
                                width: 100,
                                formatter: $filter('codeFormatter'),
                                sortable: !$scope.versionMode
                            },
                            {name: 'addressAsText', index: 'ADDRESS', width: 400, sortable: !$scope.versionMode},
                            {name: 'employee', index: 'EMPLOYEE', width: 100, sortable: !$scope.versionMode},
                            {
                                name: 'source',
                                index: 'SOURCE_ID',
                                width: 100,
                                formatter: $filter('codeFormatter'),
                                sortable: !$scope.versionMode
                            },
                            {name: 'oldId', index: 'OLD_ID', width: 100, sortable: !$scope.versionMode}
                        ],
                        rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                        rowList: APP_CONSTANTS.COMMON.PAGINATION,
                        sortname: $scope.sortAttribute,
                        sortorder: "asc",
                        viewrecords: true,
                        hidegrid: false,
                        multiselect: true,
                        ondblClickRow: function (rowId) {
                            $scope.showRecord($scope.personGrid.ctrl.getRawData(rowId))
                        }
                    }
                };

                /**
                 * Преобразует запись в формат, который может воспринимать общий функционал справочников
                 */
                $scope.transformRecord = function (record) {
                    return {
                        id: {value: record.id},
                        record_version_from: {value: record.version},
                        record_version_to: {value: record.versionEnd},
                        RECORD_ID: {value: record.recordId},
                        LAST_NAME: {value: record.lastName},
                        FIRST_NAME: {value: record.firstName},
                        MIDDLE_NAME: {value: record.middleName},
                        INN: {value: record.inn},
                        INN_FOREIGN: {value: record.innForeign},
                        SNILS: {value: record.snils},
                        TAXPAYER_STATE: {
                            referenceObject: {
                                id: {value: record.taxpayerState.id},
                                CODE: {value: record.taxpayerState.code}
                            }
                        },
                        BIRTH_DATE: {value: record.birthDate},
                        BIRTH_PLACE: {value: record.birthPlace},
                        CITIZENSHIP: {
                            referenceObject: {
                                id: {value: record.citizenship.id},
                                CODE: {value: record.citizenship.code}
                            }
                        },
                        ADDRESS: {
                            referenceObject: {
                                id: {value: record.address.id},
                                ADDRESS_FULL: {value: record.addressAsText}
                            }
                        },
                        EMPLOYEE: {value: record.employee},
                        SOURCE_ID: {
                            referenceObject: {
                                id: {value: record.source.id},
                                CODE: {value: record.source.code}
                            }
                        },
                        OLD_ID: {value: record.oldId}
                    }
                };

                /**
                 * Обработка события изменения даты актуальности для отбора записей справочника
                 */
                $scope.onChangeVersion = function () {
                    if (!$scope.data.recordVersion) {
                        // Запрет на установку пустой версии
                        $scope.data.recordVersion = $scope.data.prevVersion
                    } else {
                        $scope.data.prevVersion = $scope.data.recordVersion;
                        if ($scope.personGrid && $scope.personGrid.ctrl.refreshGrid) {
                            $scope.personGrid.ctrl.refreshGrid(1);
                        }
                    }
                };

                /**
                 * Отображает диалог для создания новой записи справочника
                 */
                $scope.createRecord = function () {
                    $aplanaModal.open({
                        title: $filter('translate')('refBooks.createRecord'),
                        templateUrl: 'client/app/refbooks/modal/refBookRecordModal.html?v=${buildUuid}',
                        controller: 'refBookRecordModalCtrl',
                        windowClass: 'modal800',
                        resolve: {
                            $shareData: function () {
                                return {
                                    mode: "CREATE",
                                    refBook: $scope.refBook,
                                    recordId: $stateParams.recordId,
                                    record: $scope.personGrid.value.length === 1 ? $scope.transformRecord($scope.personGrid.value[0]) : null
                                };
                            }
                        }
                    }).result.then(function () {
                        $scope.personGrid.ctrl.refreshGrid(1);
                    });
                };

                /**
                 * Отображает диалог для просмотра записи справочника
                 */
                $scope.showRecord = function (record) {
                    var transformedRecord = $scope.transformRecord(record ? record : $scope.personGrid.value[0]);
                    var transformedGridData = [];
                    $scope.personGrid.ctrl.getRawData().forEach(function (person) {
                        transformedGridData.push($scope.transformRecord(person))
                    });
                    $aplanaModal.open({
                        title: $filter('translate')('refBooks.showRecord'),
                        templateUrl: 'client/app/refbooks/modal/refBookRecordModal.html?v=${buildUuid}',
                        controller: 'refBookRecordModalCtrl',
                        windowClass: 'modal800',
                        resolve: {
                            $shareData: function () {
                                return {
                                    mode: "VIEW",
                                    refBook: $scope.refBook,
                                    record: transformedRecord,
                                    gridData: transformedGridData
                                };
                            }
                        }
                    });
                };

                /**
                 * Отображает диалог для редактирования записи справочника
                 */
                $scope.editRecord = function () {
                    var transformedRecord = $scope.transformRecord($scope.personGrid.value[0]);
                    $aplanaModal.open({
                        title: $filter('translate')('refBooks.editRecord'),
                        templateUrl: 'client/app/refbooks/modal/refBookRecordModal.html?v=${buildUuid}',
                        controller: 'refBookRecordModalCtrl',
                        windowClass: 'modal800',
                        resolve: {
                            $shareData: function () {
                                return {
                                    mode: "EDIT",
                                    refBook: $scope.refBook,
                                    record: transformedRecord
                                };
                            }
                        }
                    }).result.then(function (needToRefresh) {
                        if (needToRefresh) {
                            $scope.personGrid.ctrl.refreshGrid(1);
                        }
                    });
                };

                /**
                 * Удаляет записи справочника, выбранные в таблице
                 * Если форма в режиме версий - удаляются выбранные версии, иначе удаляются все версии выбранных записей
                 */
                $scope.deleteRecords = function () {
                    $dialogs.confirmDialog({
                        content: $scope.versionMode ?
                            $filter('translate')('refBook.confirm.versions.delete') :
                            $filter('translate')('refBook.confirm.delete'),
                        okBtnCaption: $filter('translate')('common.button.yes'),
                        cancelBtnCaption: $filter('translate')('common.button.no'),
                        okBtnClick: function () {
                            var ids = [];
                            $scope.personGrid.value.forEach(function (record) {
                                ids.push(record.id)
                            });

                            $http({
                                method: "POST",
                                url: "controller/actions/refBook/" + $scope.refBook.id + ($scope.versionMode ? "/deleteVersions" : "/deleteRecords"),
                                data: ids
                            }).then(function (response) {
                                if (response.data && response.uuid && response.uuid !== null) {
                                    $logPanel.open('log-panel-container', response.uuid);
                                } else {
                                    $scope.personGrid.ctrl.refreshGrid(1);
                                }
                            });
                        }
                    });
                };

                /**
                 * Отображает список версий записи справочника
                 */
                $scope.showVersions = function () {
                    $window.location = "index.html#/refBooks/linearRefBook/" + $scope.refBook.id + "?recordId=" + $scope.personGrid.value[0].recordId;
                };

                /**
                 * Формирование XLSX выгрузки записей справочника
                 */
                $scope.createReportXlsx = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/refBook/" + $scope.refBookId + "/reportXlsx",
                        params: {
                            version: $scope.data.recordVersion,
                            pagingParams: JSON.stringify({
                                property: $scope.personGrid.ctrl.getGrid().jqGrid('getGridParam', 'sortname'),
                                direction: $scope.personGrid.ctrl.getGrid().jqGrid('getGridParam', 'sortorder')
                            }),
                            searchPattern: $scope.search.text,
                            exactSearch: $scope.search.precise
                        }
                    }).success(function (response) {
                        $logPanel.open('log-panel-container', response.uuid);
                    });
                };

                /**
                 * Формирование CSV выгрузки записей справочника
                 */
                $scope.createReportCsv = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/refBook/" + $scope.refBookId + "/reportCsv",
                        params: {
                            version: $scope.data.recordVersion,
                            pagingParams: JSON.stringify({
                                property: $scope.personGrid.ctrl.getGrid().jqGrid('getGridParam', 'sortname'),
                                direction: $scope.personGrid.ctrl.getGrid().jqGrid('getGridParam', 'sortorder')
                            }),
                            searchPattern: $scope.search.text,
                            exactSearch: $scope.search.precise
                        }
                    }).success(function (response) {
                        $logPanel.open('log-panel-container', response.uuid);
                    });
                };

                /**
                 * Назначить оригинал/дубли
                 */
                $scope.setOriginalAndDuplicate = function () {
                    $aplanaModal.open({
                        title: $filter('translate')('refBook.fl.title.originalAndDuplicate'),
                        templateUrl: 'client/app/refbooks/modal/originalAndDuplicates.html?v=${buildUuid}',
                        controller: 'originalAndDuplicatesModalCtrl',
                        windowClass: 'modal1200',
                        resolve: {
                            $shareData: function () {
                                return {
                                    record: $scope.personGrid.value[0]
                                };
                            }
                        }
                    }).result.then(function (needToRefresh) {
                        if (needToRefresh) {
                            $scope.personGrid.ctrl.refreshGrid(1);
                        }
                    });
                };

                /**
                 * Поиск по справочнику
                 */
                $scope.searchRecords = function () {
                    $scope.personGrid.ctrl.refreshGrid(1);
                }
            }]);
}());