(function () {
    'use strict';

    /**
     * @description Модуль для назначения оригиналов/дублей для ФЛ
     */
    angular.module('app.originalAndDuplicatesModal', ['ui.router', 'app.rest', 'app.refBookPicker'])

    /**
     * @description Контроллер для назначения оригиналов/дублей для ФЛ
     */
        .controller('originalAndDuplicatesModalCtrl', ['$scope', '$filter', 'APP_CONSTANTS', '$modalInstance', '$shareData',
            '$http', '$logPanel',
            function ($scope, $filter, APP_CONSTANTS, $modalInstance, $shareData, $http, $logPanel) {
                $scope.record = $shareData.record;
                $scope.temp = {};
                $scope.original = null;
                $scope.duplicates = [];
                $scope.newDuplicates = [];
                $scope.deletedDuplicates = [];

                $scope.duplicatesGrid = {
                    ctrl: {},
                    options: {
                        datatype: "local",
                        data: $scope.duplicates,
                        colNames: [
                            '',
                            $filter('translate')('refBook.fl.title.id'),
                            $filter('translate')('title.lastName'),
                            $filter('translate')('title.firstName'),
                            $filter('translate')('title.middleName'),
                            $filter('translate')('refBook.fl.title.inn'),
                            $filter('translate')('title.snils'),
                            $filter('translate')('refBook.fl.title.innForeign'),
                            $filter('translate')('title.dateOfBirth'),
                            $filter('translate')('refBook.fl.title.docNumber')
                        ],
                        colModel: [
                            {name: 'id', index: 'id', width: 100, key: true, hidden: true},
                            {name: 'recordId', index: 'recordId', width: 100, sortable: false},
                            {name: 'lastName', index: 'lastName', width: 100, sortable: false},
                            {name: 'firstName', index: 'firstName', width: 100, sortable: false},
                            {name: 'middleName', index: 'middleName', width: 100, sortable: false},
                            {name: 'inn', index: 'inn', width: 100, sortable: false},
                            {name: 'snils', index: 'snils', width: 100, sortable: false},
                            {name: 'innForeign', index: 'innForeign', width: 100, sortable: false},
                            {
                                name: 'birthDate',
                                index: 'birthDate',
                                width: 120,
                                formatter: $filter('dateFormatter'),
                                sortable: false
                            },
                            {name: 'docNumber', index: 'docNumber', width: 100, sortable: false}
                        ],
                        rowNum: APP_CONSTANTS.COMMON.PAGINATION[0],
                        rowList: APP_CONSTANTS.COMMON.PAGINATION,
                        cellEdit: true,
                        cellsubmit: 'clientArray',
                        viewrecords: true,
                        sortname: 'id',
                        sortorder: "asc",
                        hidegrid: false,
                        multiselect: true

                    }
                };

                /**
                 * Получаем и устанавливаем оригинал для ФЛ
                 * @param personId идентификатор ФЛ
                 */
                function fetchOriginal(personId) {
                    $http({
                        method: "GET",
                        url: "controller/actions/refBookFL/fetchOriginal/" + personId
                    }).then(function (response) {
                        $scope.original = response.data;
                    });
                }

                /**
                 * TODO: по логике тут вместо идентификатора ФЛ должен использоваться record_id, но используется id. На это кажется заведен баг в аналитике еще на старый код
                 * Заполняет номер ДУЛ ФЛ в указанном поле объекта
                 * @param personId идентификатор ФЛ
                 * @param object объект, в который надо поместить номер ДУЛ
                 * @param field поле, в которое надо поместить номер ДУЛ
                 * @param callback функция вызываемая после заполнения номера ДУЛ
                 */
                function fillDocNumber(personId, object, field, callback) {
                    $http({
                        method: "GET",
                        url: "controller/actions/refBookFL/getDocNumber/" + personId
                    }).then(function (response) {
                        object[field] = response.data;
                        if (callback) {
                            callback()
                        }
                    });
                }

                fetchOriginal($scope.record.id);
                fillDocNumber($scope.record.id, $scope.record, 'docNumber');

                /**
                 * Отслеживаем изменение оригинала и если надо подгружаем номер ДУЛ
                 */
                $scope.$watch("original.id", function (newValue, oldValue) {
                    if (newValue !== oldValue && !$scope.original.docNumber) {
                        fillDocNumber($scope.original.id, $scope.original, 'docNumber');
                    }
                });

                /**
                 * Удалить оригинал
                 */
                $scope.deleteOriginal = function () {
                    $scope.original = null;
                };

                /**
                 * Событие выбора нового дубликата для добавления в таблицу
                 */
                $scope.onSelectDuplicate = function (duplicate) {
                    fillDocNumber(duplicate.id, duplicate, 'docNumber', function () {
                        // Добавляем в список дубликатов
                        $scope.duplicates.push(duplicate);
                        // Добавляем в список новых дубликатов
                        $scope.newDuplicates.push(duplicate);
                        $scope.duplicatesGrid.ctrl.refreshGridData($scope.duplicates);
                    });
                };

                /**
                 * Получение списка уже установленных дубликатов
                 */
                $scope.fetchDuplicates = function (ctrl) {
                    var page = ctrl.getGrid().jqGrid('getGridParam', 'page');
                    var rows = ctrl.getGrid().jqGrid('getGridParam', 'rowNum');
                    $http({
                        method: "GET",
                        url: "controller/actions/refBookFL/fetchDuplicates/" + $scope.record.recordId,
                        params: {
                            pagingParams: JSON.stringify({
                                page: page,
                                count: rows,
                                startIndex: page === 1 ? 0 : rows * (page - 1)
                            })
                        }
                    }).then(function (response) {
                        $scope.duplicates = response.data.rows;
                        $scope.duplicatesGrid.ctrl.refreshGridData($scope.duplicates);
                    });
                };

                /**
                 * Удалить дубль
                 */
                $scope.deleteDuplicates = function () {
                    var deletedRecord = null;
                    $scope.duplicatesGrid.value.forEach(function (selected) {
                        $scope.duplicates.forEach(function (record, index) {
                            if (record.id === selected.id) {
                                deletedRecord = record;
                                $scope.duplicates.splice(index, 1);
                            }
                        });
                    });
                    if (deletedRecord) {
                        // Ищем дубликат среди ранее добавленных, если его там нет - значит удалили ранее назначенный и это надо сохранить
                        var found = false;
                        $scope.newDuplicates.forEach(function (record) {
                            if (record.id === deletedRecord.id) {
                                found = true;
                            }
                        });
                        if (!found) {
                            $scope.deletedDuplicates.push(deletedRecord);
                        }
                    }
                    $scope.duplicatesGrid.ctrl.refreshGridData($scope.duplicates);
                };

                /**
                 * Сохранение
                 */
                $scope.save = function () {
                    $http({
                        method: "POST",
                        url: "controller/actions/refBookFL/saveOriginalAndDuplicates",
                        data: {
                            currentPerson: $scope.record,
                            original: $scope.original ? $scope.original : null,
                            newDuplicates: $scope.newDuplicates,
                            deletedDuplicates: $scope.deletedDuplicates
                        }
                    }).then(function (response) {
                        $logPanel.open('log-panel-container', response.data.uuid);
                        $modalInstance.close();
                    });
                };

                /**
                 * @description закрытие модального окна
                 */
                $scope.close = function () {
                    $modalInstance.close();
                };

                /**
                 * Переопределенный метод модуля {aplana.modal}, чтобы по нажатию на крестик
                 * выполнялась логика кнопки "Закрыть"
                 */
                $scope.modalCloseCallback = function () {
                    $scope.close();
                };

                /**
                 * Возвращает фильтр при поиске записей для назначения дубликатами или оригиналами. Дубликат не должен быть текущей или оригинальной записью.
                 * Также и оригинальная запись не может быть текущей или дубликатом.
                 * @returns {string}
                 */
                $scope.filter = function () {
                    var filter = 'OLD_ID is null';
                    if ($scope.original) {
                        filter += ' and id != ' + $scope.original.id;
                    }
                    if ($scope.record) {
                        filter += ' and id != ' + $scope.record.id;
                    }
                    if ($scope.duplicates) {
                        angular.forEach($scope.duplicates, function (value, key) {
                            filter += ' and id != ' + value.id;
                        });
                    }
                    return filter;
                };
            }
        ]);
}());