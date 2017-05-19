(function () {
    'use strict'

    angular
        .module('aplana.entity-utils', [
            'ui.grid',
            'ngAnimate',
            'aplana.dialogs'
        ])
        .factory('aplanaEntityUtils', ['$log', '$window', '$http', 'aplanaDialogs', 'USER_DATA', 'APP_CONSTANTS', '$q', '$location', '$timeout', 'Overlay',
            function ($log, $window, $http, aplanaDialogs, USER_DATA, APP_CONSTANTS, $q, $location, $timeout, Overlay) {
                var commonRestUrl = "rest/entity/"; //базовая ссылка на CommonRestController
                /**
                 * Служебная функция для отправки запроса на получения списков сущностей с сервера и заполнения им переданного масива.
                 * @param url адрес REST-сервиса для получения списка
                 * @param scope объект, к которому нужно добавить массив полученных сущностей
                 * @param list строка, содержащая название массива сущностей
                 * @param params дополнительные параметры в запрос (не обязательный)
                 * @returns {promise} полученный в ходе работы метод промис. Нужен, чтобы можно было организовывать цепочки вызовов.
                 */
                function getEntityList(url, scope, list, params) {
                    var promise;
                    if (params && params != null) {
                        promise = $http.get(url, {params: params});
                    } else {
                        promise = $http.get(url);
                    }
                    var result = promise
                        .then(function (response) {
                            if (response.data) {
                                scope[list] = response.data.list || response.data || [];
                            }
                        });
                    return result;
                }

                /**
                 * Функция возвращает из БД один экземпляр сущности
                 * @param url адрес REST-сервиса для получения списка
                 * @param scope объект, к которому нужно добавить массив полученных сущностей
                 * @param entity строка, содержащая название атрибута, в который вернётся сущность
                 * @param params дополнительные параметры в запрос (не обязательный)
                 * @returns {promise} полученный в ходе работы метод промис. Нужен, чтобы можно было организовывать цепочки вызовов.
                 */
                function getEntity(url, scope, entity, params) {
                    var promise;
                    if (params && params != null) {
                        promise = $http.get(url, {params: params});
                    } else {
                        promise = $http.get(url);
                    }
                    var result = promise
                        .then(function (response) {
                            if (response.data) {
                                scope[entity] = response.data || [];
                            }
                        });
                    return result;
                }

                /**
                 * Функция для получения параметров сортировки в запросах на извлечение данных с сервера
                 * @param scope объект, содержащиё параметры сортировки. Как правило - $scope.dataOptions
                 * @returns {string}
                 */
                function getSort(scope) {
                    // Собираем параметры сортировки
                    var sort = "";
                    if (scope.sort && scope.sort.length != 0) {
                        sort = scope.sort.reduce(function (str, column) {
                            str += column.field + '-' + column.sort.direction + ';';
                            return str;
                        }, "");
                    }
                    return sort;
                }

                /**
                 * Функция для построения базовых параметров запроса на выбюорку данных
                 * @param scope объект, содержащиё параметры запроса. Как правило - $scope.dataOptions
                 * @returns {{paging: string, sort: string}}
                 */
                function getRequestParams(dataOptions) {
                    var params = {};
                    //Формируем параметры
                    if (dataOptions.paging) {
                        // Параметры пейджинга
                        var from = dataOptions.paging.pageSize * (dataOptions.paging.pageNumber - 1) + 1;
                        var to = from + dataOptions.paging.pageSize - 1;
                        params.paging = from + ';' + to;
                    }
                    if (dataOptions.sort) {
                        // Параметры сортировки
                        params.sort = getSort(dataOptions);
                    }

                    //Формируем параметры фильтрации. Пустые параметры не передаём
                    var filter = angular.extend({}, dataOptions.filter);
                    for (var key in filter) {
                        if (filter[key] === undefined || filter[key] === null || filter[key] === '') {
                            delete filter[key];
                        }
                    }

                    jQuery.extend(params, filter);

                    $log.info(angular.toJson(params));
                    return params;
                }

                /**
                 * Функция для настройки ширины столбцов в зависимости от ширины таблицы
                 * @param gridOptions ссылка на gridOptions
                 * @param gridWidth ширина таблицы
                 */
                function fitColumnsWidth(gridOptions, gridWidth) {
                    var allColumnsWidth = gridOptions.columnDefs.reduce(function (currWidth, columnDef) {
                        return columnDef.visible ? currWidth + columnDef.width : currWidth;
                    }, 0);
                    var multiplier = (gridWidth - 50) / allColumnsWidth; //учитываем пометки для выделения слева и скрола
                    if (multiplier > 1) {
                        gridOptions.columnDefs.forEach(function (columnDef) {
                            if (columnDef.visible) {
                                columnDef.width = Math.round(columnDef.width * multiplier);
                            }
                        });
                    }
                }

                /**
                 * Функция для создания колонок таблицы
                 * @param dataOptions объект с настройками строк для таблицы (включая метаданные)
                 * @param gridOptions объект с настройками самой таблицы
                 * @param gridApi ссылка на gridApi. Опциональный
                 */
                function createGridColumns(dataOptions, gridOptions, gridApi) {
                    gridOptions.columnDefs = [];
                    for (var fieldName in dataOptions.metaData) {
                        var field = dataOptions.metaData[fieldName];
                        var columnDef = {};
                        columnDef.name = field.name;
                        columnDef.displayName = field.title;
                        columnDef.enableHiding = false;
                        columnDef.cellTooltip = true;
                        columnDef.visible = field.visible;
                        columnDef.width = field.width || 100;
                        columnDef.enableColumnMenu = field.enableColumnMenu;

                        //Если предусмотрены настройки сортировки, устанавливаем их
                        columnDef.enableSorting = (field.type !== 'java.util.List' &&
                        field.type !== 'java.util.SortedSet' &&
                        (gridOptions.excludeSortColumns && gridOptions.excludeSortColumns.indexOf(field.name) == -1));
                        /*if(field.hasOwnProperty("enableSorting")){
                         columnDef.enableSorting = field.enableSorting;
                         }*/

                        if (field.type == "java.util.Date") {
                            var format = field.format == "" ? "dd.MM.yyyy" : field.format;
                            columnDef.type = 'date';
                            columnDef.cellFilter = "date:'" + format + "'";
                        }
                        gridOptions.columnDefs.push(columnDef);
                    }

                    //Настраиваем ширину колонок по ширинре грида.
                    if (gridApi) {
                        fitColumnsWidth(gridOptions, gridApi.grid.gridWidth);
                    }
                }


                /**
                 * Функция для инициализации таблицы
                 * @param scope скоуп с данными для таблицы. Может включать ее конфигурацию в полях:
                 *      scope.dataOptions - параметры пэйджинга, сортировки, поиска по таблице
                 *      scope.gridOptions - параметры отображения и работы самой таблицы (стандартные настройки ангуляра)
                 * @param getData               функция для получения данных таблицы
                 * @param rowSelectionChanged   функция вызываемая при изменении выделения строк в таблице
                 * @param rowDblClick           функция вызываемая при двойном клике в строке таблицы
                 */
                function initGrid(scope, getData, rowSelectionChanged, rowDblClick) {
                    scope.numberOfSelectedItems = 0;
                    if (!scope.dataOptions) {
                        scope.dataOptions = {
                            paging: {pageNumber: 1, pageSize: Number(USER_DATA.paging)},
                            sort: null,
                            metaData: null,
                            filterList: {},
                            filter: {}
                        };
                    }

                    if (!scope.gridOptions) {
                        scope.gridOptions = {
                            paginationPageSizes: getPageSizes(),
                            paginationPageSize: scope.dataOptions.paging.pageSize,
                            rowSelection: true,
                            useExternalPagination: true,
                            useExternalSorting: true,
                            enableFullRowSelection: true,
                            multiSelect: true,
                            modifierKeysToMultiSelect: true,
                            appScopeProvider: {
                                onDblClick: function (row) {
                                    if (rowDblClick) {
                                        rowDblClick(row)
                                    }
                                }
                            },
                            rowTemplate: "<div ng-dblclick=\"grid.appScope.onDblClick(row)\" ng-repeat=\"(colRenderIndex, col) in colContainer.renderedColumns track by col.colDef.name\" class=\"ui-grid-cell\" ng-class=\"{ 'ui-grid-row-header-cell': col.isRowHeader }\" ui-grid-cell ></div>",
                            onRegisterApi: function (gridApi) {
                                scope.gridApi = gridApi;
                                scope.gridApi.core.on.sortChanged(scope, function (grid, sortColumns) {
                                    scope.dataOptions.sort = sortColumns;
                                    getData();
                                }); //Обработчик смены сортировки колонок. Устанавливаем параметры сортировки и обновляем данные в таблице
                                scope.gridApi.pagination.on.paginationChanged(scope, function (pageNumber, pageSize) {
                                    if (scope.dataOptions.paging.pageSize != pageSize) {
                                        // В случае изменения значения параметра "Кол-во записей на странице" переходим на 1-ю страницу
                                        scope.dataOptions.paging.pageNumber = 1;
                                        scope.gridOptions.paginationCurrentPage = 1;
                                    } else {
                                        scope.dataOptions.paging.pageNumber = pageNumber;
                                    }
                                    scope.dataOptions.paging.pageSize = pageSize;
                                    getData(); //обновляем данные с сервера, поскольку эти настройки используются при построении запроса к БД.
                                }); //Обработчик изменения параметров паджинации
                                scope.gridApi.selection.on.rowSelectionChanged(scope, function (row, evt) {
                                    setCurrentRow(scope.gridApi.grid.selection.lastSelectedRow, scope, rowSelectionChanged);
                                    scope.numberOfSelectedItems = scope.gridApi.grid.selection.selectedCount;
                                }); //Обработчик события выделения(снятии выделения) строки
                                scope.gridApi.selection.on.rowSelectionChangedBatch(scope, function (rows, evt) {
                                    scope.numberOfSelectedItems = scope.gridApi.grid.selection.selectedCount;
                                    setCurrentRow(scope.gridApi.grid.selection.lastSelectedRow, scope, rowSelectionChanged);
                                }); // Обработчик события когда выделяются(снимается выделение) все строки щелчком по заголовку таблицы


                                //Настройка переноса текста в таблице
                                scope.rowsRenderedTimeout = undefined;
                                var heightRowsChanged = [];
                                scope.gridApi.core.on.rowsRendered(scope, function () {
                                    // each rows rendered event (init, filter, pagination, tree expand)
                                    // Timeout needed : multi rowsRendered are fired, we want only the last one
                                    if (scope.rowsRenderedTimeout) {
                                        $timeout.cancel(scope.rowsRenderedTimeout)
                                    }
                                    scope.rowsRenderedTimeout = $timeout(function () {
                                        heightRowsChanged = calculateAutoHeight('', scope.gridApi.grid, heightRowsChanged);
                                    });
                                });
                                scope.gridApi.core.on.scrollEnd(scope, function () {
                                    heightRowsChanged = calculateAutoHeight('', scope.gridApi.grid, heightRowsChanged);
                                });
                            }
                        };
                    }
                }

                /**
                 * Метод для получения ссылки на метаданные поля.
                 * @param scope ссылка на $scope, либо на $scope.dataOptions, либо на другой объект с атрибутом metaData
                 * @param fieldName
                 * @returns {{}}
                 */
                var getFieldInfo = function (scope, fieldName) {
                    var result;

                    //Учитываем, что мог прийти $scope, а мог $scope.dataOptions
                    var dataOptions = scope.dataOptions ? scope.dataOptions : scope;
                    for (var property in dataOptions.metaData) {
                        if (dataOptions.metaData.hasOwnProperty(property)) { //не проверяем свойства предков
                            var meta = dataOptions.metaData[property];
                            if (meta.name == fieldName) {
                                result = meta;
                                break;
                            }
                        }
                    }
                    return result ? result : {}
                };

                /**
                 * Функция для получения представления для строк таблицы
                 * @param scope ссылка на объект, содержащий массив строк для таблицы (как правило, $scope.dataOptions)
                 * @param row строка для которой получаем представления
                 * @returns {{}}
                 */
                function getRowView(scope, row) {
                    var displayRow = {};
                    for (var field in row) {
                        var fieldInfo = getFieldInfo(scope, field);
                        if (!angular.equals(fieldInfo, {})) {
                            if (field == 'deleted') {
                                displayRow.deleted = row.deleted ? 'Да' : '';
                            } else if (field == 'month') {
                                displayRow.month = getMonth(row.month);
                            } else if (field == 'orderType') {
                                displayRow.orderType = row.orderType == 0 ? 'Основной' : 'Дополнительный';
                            } else if (fieldInfo.type == 'boolean') {
                                displayRow[field] = row[field] ? 'Да' : 'Нет';
                            } else if (fieldInfo.type == 'java.util.Date') {
                                if (row[field]) {
                                    displayRow[field] = row[field];
                                    row[field] = new Date(row[field])
                                } else {
                                    displayRow[field] = "";
                                }
                            } else if (fieldInfo.type.startsWith('com.ndlf.model')) {
                                // Ссылки
                                displayRow[field] = row[field] == null ? "" : row[field][fieldInfo.displayField];
                            } else if (fieldInfo.type == 'java.util.List' || fieldInfo.type == 'java.util.Set') {
                                // Список ссылок
                                if (row[field] == null || row[field].length == 0) {
                                    displayRow[field] = '';
                                } else {
                                    var values = [];
                                    if (fieldInfo.displayField != null) {
                                        row[field].forEach(function (value) {
                                            values.push(getFieldValue(value, fieldInfo.displayField));
                                        });
                                    }
                                    displayRow[field] = values.join(', ');
                                }
                            }
                            else {
                                displayRow[field] = row[field];
                            }
                        }
                    }
                    return displayRow;
                }

                /**
                 * Получает значение поля из объекта.
                 * Имя поля может быть задано как путь внутри объекта. Например: role.name
                 * @param value объект, из которого нужно получить значение поля
                 * @param field название поля
                 * @returns значение поля
                 */
                function getFieldValue(value, field) {
                    if (field.indexOf('.') != -1) {
                        //Имя представляет собой путь внутри полей объекта
                        var currentField = value;
                        field.split('.').forEach(function (fieldName) {
                            if (currentField) {
                                currentField = currentField[fieldName];
                            }
                        });
                        return currentField;
                    } else {
                        //Просто имя поля
                        return value[field];
                    }
                }

                /**
                 * Получает значение поля из объекта.
                 * Имя поля может быть задано как путь внутри объекта. Например: role.name
                 * @param object объект, из которого нужно получить значение поля
                 * @param field название поля
                 * @returns значение поля
                 */
                function getFieldValue(object, field) {
                    if (field.indexOf('.') != -1) {
                        //Имя представляет собой путь внутри полей объекта
                        var currentValue = object;
                        field.split('.').forEach(function (fieldName) {
                            if (currentValue) {
                                currentValue = currentValue[fieldName];
                            }
                        });
                        return currentValue;
                    } else {
                        //Просто имя поля
                        return object[field];
                    }
                }

                /**
                 * Функция для обновления внешнего вида таблицы
                 * @param scope ссылка на $scope, либо на $scope.dataOptions (или аналог)
                 */
                function updateViewData(scope) {
                    //Учитываем, что мог прийти $scope, а мог - $scope.dataOptions
                    var data = scope.dataOptions ? scope.dataOptions.data
                        : scope.data ? scope.data : [];
                    var displayData = [];

                    //Заполняем таблицу строками
                    data.forEach(function (row) {
                        displayData.push(getRowView(scope, row));
                    });
                    scope.gridOptions.data = displayData;
                }

                /**
                 * Функция для преобразования параметров запроса к виду, который понимает контроллер на сервере
                 * Измлекает из params[paramName] атрибут attributeName и сохраняет его значение в params[paramNewName], а params[paramName] удаляет
                 * @param params объект, содержащий параметры фильтрации
                 * @param paramName текущее наименование параметра (как правило, параметр - объект)
                 * @param paramNewName новое наименование параметра
                 * @param attributeName название атрибута, который должен быть перенесён из старого праметра в новый
                 */
                function extractAttribute(params, paramName, paramNewName, attributeName) {
                    if (!attributeName && params[paramName]) {
                        params[paramNewName] = params[paramName]
                    } else {
                        if (params[paramName] && params[paramName][attributeName] != undefined) { //проверяем явно из-за особенностей вычисления логических выражений JavaScript.
                            params[paramNewName] = params[paramName][attributeName];
                        }
                    }
                    if (paramName != paramNewName) {
                        delete params[paramName];
                    }
                }

                /**
                 * Функция для оправки запроса patch на заданный URL
                 * @param url
                 * @param entityType тип сущности (название модельного класса)
                 * @param body тело запроса
                 * @param successCallback
                 * @param failCallback
                 * @returns {promise} промис, завязанный на запрос patch, либо просто выполнившийся
                 */
                function sendPatchRequestToURL(url, entityType, body, successCallback, failCallback) {
                    var promise;
                    if (entityType) {
                        // Отправка данных
                        promise = $http.patch(url + entityType, body)
                            .then(
                                successCallback,
                                failCallback
                            );
                    } else {
                        $log.error("[sendPathRequestToURL] не передали entityType!");
                        var differed = $q.defer();
                        differed.resolve();
                        promise = differed.promise;
                    }
                    return promise;
                }

                /**
                 * Функция для отправки запроса на обновление сущности
                 * @param entityType тип сущности (название модельного класса)
                 * @param entity сущность, которую обновляем (объект)
                 * @param successCallback
                 * @param failCallback
                 * @returns {promise} промис, завязанный на запрос patch, либо просто выполнившийся
                 */
                function sendRequestForUpdate(entityType, entity, successCallback, failCallback) {
                    var promise = sendPatchRequestToURL('rest/entity/', entityType, entity, successCallback, failCallback);
                    return promise;
                }

                /**
                 * Функция для оправки запроса на обновление набора сущностей - отправляются пачкой, обрабатываются поштучно
                 * @param entityType тип сущности (название модельного класса)
                 * @param entityList массив сущностей, которые обновляем (массив)
                 * @param successCallback
                 * @param failCallback
                 * @returns {promise} промис, завязанный на запрос patch, либо просто выполнившийся
                 */
                function sendRequestForBatchUpdate(entityType, entityList, successCallback, failCallback) {
                    var promise;
                    if (entityList instanceof Array) {
                        promise = sendPatchRequestToURL('rest/entity/patchAll/', entityType, entityList, successCallback, failCallback);
                    } else {
                        var differed = $q.defer();
                        differed.resolve();
                        promise = differed.promise;
                    }
                    return promise;
                }


                /**
                 * Функция для отправки запроса post на заданный url
                 * @param entityType тип сущности (название модельного класса)
                 * @param entityType
                 * @param body тело запроса
                 * @param successCallback
                 * @param failCallback
                 * @returns {*}
                 */
                function sendPostRequestToURL(url, entityType, body, successCallback, failCallback) {
                    var promise;
                    if (entityType) {
                        // Отправка данных
                        promise = $http.post(url + entityType, body)
                            .then(
                                successCallback,
                                failCallback
                            );
                    } else {
                        $log.error("[sendRequestForCreate] не передали entityType!");
                        var differed = $q.defer();
                        differed.resolve();
                        promise = differed.promise;
                    }
                    return promise;
                }

                /**
                 * Функция для отправки запроса на создание сущности
                 * @param entityType тип сущности (название модельного класса)
                 * @param entity сущность, которую обновляем (объект)
                 * @param successCallback
                 * @param failCallback
                 * @returns {promise} промис, завязанный на запрос post, либо просто выполнившийся
                 */
                function sendRequestForCreate(entityType, entity, successCallback, failCallback) {
                    var promise = sendPostRequestToURL("rest/entity/", entityType, entity, successCallback, failCallback);
                    return promise;
                }

                /**
                 * Функция для оправки запроса на пакетное создание сущностей.
                 * @param entityType тип сущности (название модельного класса)
                 * @param entityList массив сущностей, которые обновляем (массив)
                 * @param successCallback
                 * @param failCallback
                 */
                function batchCreate(entityType, entityList, successCallback, failCallback) {
                    return $http.post('rest/entity/batch/' + entityType, entityList)
                        .then(
                            successCallback,
                            failCallback
                        )
                }

                /**
                 * Функция для оправки запроса на пакетное обновление сущностей. Обновлены будут только указанные поля
                 * @param entityType тип сущности (название модельного класса)
                 * @param entityList массив сущностей, которые обновляем (массив)
                 * @param successCallback
                 * @param failCallback
                 */
                function batchUpdate(entityType, entityList, successCallback, failCallback) {
                    return $http.patch('rest/entity/batch/' + entityType, entityList)
                        .then(
                            successCallback,
                            failCallback
                        )
                }

                /**
                 * Функция для оправки запроса на пакетное удаление сущностей.
                 * @param entityType тип сущности (название модельного класса)
                 * @param entityList массив сущностей, которые обновляем (массив)
                 * @param successCallback
                 * @param failCallback
                 */
                function batchDelete(entityType, entityList, successCallback, failCallback) {
                    var ids = [];
                    entityList.forEach(function (entity) {
                        ids.push(entity.id);
                    });
                    return $http.delete('rest/entity/batch/' + entityType, ids)
                        .then(
                            successCallback,
                            failCallback
                        )
                }

                /**
                 * Функция для оправки запроса на пакетное обновление сущностей с учетом спец. бизнес логики
                 * @param entityType тип сущности (название модельного класса)
                 * @param entityList массив сущностей, которые обновляем (массив)
                 * @param successCallback
                 * @param failCallback
                 */
                function complexBatchUpdate(entityType, entityList, successCallback, failCallback) {
                    return $http.patch('rest/entity/complex/batch/' + entityType, entityList)
                        .then(
                            successCallback,
                            failCallback
                        )
                }

                /**
                 * Функция для поиска в массиве строк, пришедших с сервера, строки, соответствующей той, что выделена в таблице
                 * @param data массив строк с сервера
                 * @param view выделенная строка
                 * @returns {*} строка, соответствующая выделенной.
                 */
                function getDataByView(data, view) {
                    return data.reduce(function (res, item) {
                        if (item.id == view.id) {
                            res = item;
                        }
                        return res; //есди совпало, возвращаем то, что совпало, иначе - то, что пришло.
                    }, {});
                } //Поиск выделенной строки в массиве строк, поступившем с сервера. Ищем по id, возвращаем последнюю найденную.

                /**
                 * Функция для обновления текущей строки таблицы
                 * @param lastSelectedRow выделенная строка
                 * @param scope ссылка на $scope контроллера, из которого вызываем функцию
                 * @param callback функция, которую нужно вызвать после обновления текущей строки (не обязательный)
                 */
                function setCurrentRow(lastSelectedRow, scope, callback) {
                    var gridApi;
                    if (scope.gridApi) {
                        gridApi = scope.gridApi;
                    } else {
                        return;
                    }
                    if ((lastSelectedRow && !lastSelectedRow.isSelected) || gridApi.grid.selection.selectedCount != 1) { //если всего одна строка выделена - можем редактировать.
                        //Строки не выделены, или выделены все
                        scope.editMode = false;
                        scope.currentEntityView = {};
                        scope.tempEntityData = {};
                        scope.tempEntityView = {};
                    } else {
                        //Выделена 1 строка
                        scope.editMode = true;
                        scope.currentEntityView = lastSelectedRow.entity;
                        var currentEntityData = getDataByView(scope.dataOptions.data, scope.currentEntityView);
                        scope.tempEntityData = scope.sendEntityData = angular.extend({}, currentEntityData);
                        scope.tempEntityView = angular.extend({}, scope.currentEntityView);
                    }

                    //Действия, которые нужно выполнить после обновления текущей строки, например, переопределить доступность кнопок.
                    if (callback && (typeof callback) == "function") {
                        callback();
                    }
                }

                /**
                 * Функция для проверки наличия у текущей роли текущего пользователя нужной операции.
                 * @param (array) operation операции, которые нужно проверить, константа из класса Operation
                 * @returns {boolean}
                 */
                function checkOperation(operation) {
                    if (!operation || operation.length == 0) {
                        return false;
                    }

                    return operation.reduce(function (result, operation) {
                        result = result && USER_DATA.authorities.indexOf(operation) >= 0
                        return result;
                    }, true);
                }

                /**
                 * Инициализирует переменные сессии для текущего раздела
                 */
                function initPageSession(scope, callback) {
                    return restoreFilter(scope, callback)
                }

                /**
                 * Запоминает значения фильтра в сессии
                 * @param scope скоуп, из которого будет взят фильтр
                 * @param place (необязательно) место для которого взят фильтр (добавляется как постфикс к url)
                 */
                function saveFilter(scope, place) {
                    if (scope.dataOptions.loadComplete) {
                        var url = $location.path() + "#" + place;
                        sessionStorage.setItem(url, JSON.stringify(scope.dataOptions.filter));
                        sessionStorage.setItem(url + "#filterList", JSON.stringify(scope.dataOptions.filterList));
                    }
                }

                /**
                 * Восстанавлиет фильтры для текущей страницы и очищает все остальные.
                 * Заполнение фильтров выполняется через таймаут, это обеспечивает корректное заполнение всех полей
                 * Применение фильтров выполняется через таймаут, это позволяет делать 1 запрос на выборку с полным набором фильтров
                 * @param scope скоуп, из которого будет взят фильтр
                 * @param callback функция, которая будет вызвана после установки фильтра
                 * @param place (необязательно) место для которого взят фильтр (добавляется как постфикс к url)
                 */
                function restoreFilter(scope, callback, place) {
                    return $q.when($timeout(function () {
                            Overlay.processRequest();
                            var url = $location.path() + "#" + place;
                            var filterListUrl = url + "#filterList";
                            for (var item in sessionStorage) {
                                //Очищаем сохраненные фильтры для других разделов
                                if (sessionStorage.hasOwnProperty(item) &&
                                    url.split("/")[1] != item.split("/")[1] && item != url && item != filterListUrl) {
                                    sessionStorage.removeItem(item);
                                }
                            }
                            var filter = JSON.parse(sessionStorage.getItem(url));
                            if (filter) {
                                scope.dataOptions.filter = filter;
                            }
                            var filterList = JSON.parse(sessionStorage.getItem(filterListUrl));
                            if (filterList) {
                                scope.dataOptions.filterList = filterList;
                            }
                        }, 100 //Таймаут на заполнение полей фильтра
                    )).then($timeout(function () {
                            callback();
                            Overlay.processResponse();
                        }, 1000))  //Таймаут на применение фильтра к таблице
                        .then(function () {
                            scope.dataOptions.loadComplete = true;
                        });
                }

                /**
                 * Функция для заполнения грида данными
                 * @param scope ссылка на $scope контроллера, из которого вызываем функцию
                 * @param data данные + метадата + параметры пэджинга
                 * @param customGridColumnsBuilder функция, для переопределения стандартного способа отображения данных в ячейках таблицы
                 */
                function fillGrid(scope, data, customGridColumnsBuilder) {
                    return $q.when(function () {
                        if (scope.dataOptions.customMetadata) {
                            createGridColumns(scope.dataOptions, scope.gridOptions, scope.gridApi);
                        } else if (scope.dataOptions.metaData == null) {
                            // Создаем столбцы, если их не было
                            scope.dataOptions.metaData = data.metaData;
                            if (customGridColumnsBuilder) {
                                customGridColumnsBuilder(scope.dataOptions, scope.gridOptions);
                            } else {
                                createGridColumns(scope.dataOptions, scope.gridOptions, scope.gridApi);
                            }
                        }
                        scope.gridApi.core.handleWindowResize();
                        if (data) {
                            var rows = data.list;
                            // Проверка, что получено данных не более чем запросили. Такое может случиться,
                            // если ДАО содержит ошибки
                            if (scope.dataOptions.paging) {
                                if (rows.length > scope.dataOptions.paging.pageSize) {
                                    // Обрезаем до требуемого количества
                                    rows = rows.slice(0, scope.dataOptions.paging.pageSize)
                                }
                            }
                            scope.dataOptions.data = rows;
                            scope.gridOptions.totalItems = data.total;
                            updateViewData(scope, scope.gridOptions, scope.gridApi);
                        }
                    }())
                }

                /**
                 * Функция для заполнения таблицы данными с сервера
                 * @param url урл для получения данных
                 * @param scope ссылка на $scope контроллера, из которого вызываем функцию
                 * @param callback функция, которую нуэно вызвать после первого формирования столбцов таблицы
                 * @param paramReplacementRules правила замены наименования одного параметра на другого. Необходимо для извлечения нужного поля из сложного объекта
                 * @param fields список полей, заполняемых в объектах при light вызове
                 * @param customGridColumnsBuilder функция, для переопределения стандартного способа отображения данных в ячейках таблицы
                 */
                function fetchData(url, scope, callback, paramReplacementRules, fields, customGridColumnsBuilder) {
                    // Получение данных
                    var params = getRequestParams(scope.dataOptions);
                    if (fields) {
                        params.fields = fields;
                    }
                    if (paramReplacementRules) {
                        paramReplacementRules.forEach(function (rule) {
                            extractAttribute(params, rule.paramName, rule.paramNewName, rule.attributeName);
                        })
                    }

                    return $http.get(url, {
                            params: params
                        })
                        .success(function (data) {
                            return fillGrid(scope, data, callback, customGridColumnsBuilder)
                        });
                }

                /**
                 * Набор костылей для реализации переноса слов/авто-высоты строк в гриде
                 * https://github.com/angular-ui/ui-grid/issues/2746
                 * @param gridContainer
                 * @param grid
                 * @param heightRowsChanged высота строк с предыдущего вычисления (либо пустой массив)
                 */
                function calculateAutoHeight(gridContainer, grid, heightRowsChanged) {
                    var rows = angular.element(gridContainer + ' .ui-grid .ui-grid-render-container-body .ui-grid-row');
                    var pinnedRowsLeft = angular.element(gridContainer + ' .ui-grid .ui-grid-pinned-container-left .ui-grid-row');
                    var gridHasRightContainer = grid.hasRightContainer();
                    if (gridHasRightContainer) {
                        var pinnedRowsRight = angular.element(gridContainer + ' .ui-grid .ui-grid-pinned-container-right .ui-grid-row');
                    }

                    var bodyContainer = grid.renderContainers.body;

                    // get count columns pinned on left
                    var columnsPinnedOnLeft = grid.renderContainers.left.renderedColumns.length;

                    for (var r = 0; r < heightRowsChanged.length; r++) {
                        heightRowsChanged[r].height = 30;
                    }
                    heightRowsChanged = [];

                    for (var r = 0; r < rows.length; r++) {
                        // Remove height CSS property to get new height if container resized (slidePanel)
                        var elementBody = angular.element(rows[r]).children('div');
                        elementBody.css('height', '');
                        var elementLeft = angular.element(pinnedRowsLeft[r]).children('div');
                        elementLeft.css('height', '');
                        if (gridHasRightContainer) {
                            var elementRight = angular.element(pinnedRowsRight[r]).children('div');
                            elementRight.css('height', '');
                        }

                        // GET Height when set in auto for each container
                        // BODY CONTAINER
                        var rowHeight = rows[r].offsetHeight;
                        // LEFT CONTAINER
                        var pinnedRowLeftHeight = 0;
                        if (columnsPinnedOnLeft) {
                            pinnedRowLeftHeight = pinnedRowsLeft[r].offsetHeight;
                        }
                        // RIGHT CONTAINER
                        var pinnedRowRightHeight = 0;
                        if (gridHasRightContainer) {
                            pinnedRowRightHeight = pinnedRowsRight[r].offsetHeight;
                        }
                        // LARGEST
                        var largest = Math.max(rowHeight, pinnedRowLeftHeight, pinnedRowRightHeight);

                        // Apply new row height in each container
                        elementBody.css('height', largest);
                        elementLeft.css('height', largest);
                        if (gridHasRightContainer) {
                            elementRight.css('height', largest);
                        }

                        // Apply new height in gridRow definition (used by scroll)
                        //bodyContainer.renderedRows[r].height = largest;
                        var heightRowChanged = bodyContainer.renderedRows[r];
                        if (heightRowChanged) {
                            heightRowChanged.height = largest;
                            heightRowsChanged.push(heightRowChanged);
                        }
                    }
                    // NEED TO REFRESH CANVAS
                    bodyContainer.canvasHeightShouldUpdate = true;
                    return heightRowsChanged;
                }

                /**
                 * Применяет функцию ко всем выделенным строкам таблицы
                 * @param scope - скоуп с данными таблицы
                 * @param f - функция
                 */
                function processSelectedEntities(scope, f) {
                    if (scope.gridApi.selection != undefined) {
                        scope.gridApi.selection.getSelectedRows().forEach(function (row) {
                            scope.dataOptions.data.forEach(function (entity) {
                                if (row.id == entity.id) {
                                    f(entity)
                                }
                            });
                        });
                    }
                }

                /**
                 * Функция опеределяет список количеств строк на странице
                 */
                function getPageSizes() {
                    var pageSizes = [10, 20, 50, 100, 200, 500];
                    if (pageSizes.indexOf(Number(USER_DATA.paging)) == -1) {
                        pageSizes.push(Number(USER_DATA.paging))
                    }

                    pageSizes.sort(function (a, b) {
                        if (a > b) return 1;
                        if (a < b) return -1;
                    });

                    return pageSizes;
                }

                /**
                 * Возвращает список объектов, соответствующих выделенным строкам в таблице
                 * @param scope
                 * @returns {Array}
                 */
                function getSelectedEntities(scope) {
                    var result = [];
                    if (scope.gridApi.selection != undefined) {
                        scope.gridApi.selection.getSelectedRows().forEach(function (row) {
                            scope.dataOptions.data.forEach(function (entity) {
                                if (row.id == entity.id) {
                                    result.push(entity);
                                }
                            });
                        });
                    }
                    return result;
                }

                /**
                 * Проверяет что все какой либо строке таблицы есть указанное значение в нужном поле.
                 * @param scope скоуп, в котором содержится конфиг таблицы
                 * @param field (необязательное) путь до поля внутри объекта. Если не указано - проверется, есть ли вообще в таблице значения
                 * @param requiredValues (необязательное) массив значений. Если не указано - проверется, есть ли вообще в таблице значения
                 */
                function isTableHasValue (scope, field, requiredValues) {
                    if (!field || !requiredValues || requiredValues.length == 0) {
                        return scope.dataOptions.data.length > 0
                    }
                    for (var i = 0; i < scope.dataOptions.data.length; i++) {
                        var item = scope.dataOptions.data[i];
                        if (isObjectHasValue(item, field, requiredValues)) {
                            return true;
                        }
                    }
                    return false;
                }

                return {
                    commonRestUrl: commonRestUrl,

                    //Функции для работы с данными
                    getEntityList: getEntityList,
                    getEntity: getEntity,
                    getSort: getSort,
                    getRequestParams: getRequestParams,
                    getRowView: getRowView,
                    updateViewData: updateViewData,
                    extractAttribute: extractAttribute,
                    sendRequestForUpdate: sendRequestForUpdate,
                    sendRequestForBatchUpdate: sendRequestForBatchUpdate,
                    batchCreate: batchCreate,
                    batchUpdate: batchUpdate,
                    batchDelete: batchDelete,
                    complexBatchUpdate: complexBatchUpdate,
                    sendRequestForCreate: sendRequestForCreate,
                    getDataByView: getDataByView,
                    getFieldValue: getFieldValue,

                    //Функции для работы с правами
                    checkOperation: checkOperation,

                    //Функции для работы с гридами
                    initGrid: initGrid,
                    fillGrid: fillGrid,
                    setCurrentRow: setCurrentRow,
                    fetchData: fetchData,
                    processSelectedEntities: processSelectedEntities,
                    getPageSizes: getPageSizes,
                    calculateAutoHeight: calculateAutoHeight,
                    fitColumnsWidth: fitColumnsWidth,
                    isTableHasValue: isTableHasValue,
                    getSelectedEntities: getSelectedEntities,

                    //Функции работы с данными сессии
                    initPageSession: initPageSession,
                    saveFilter: saveFilter,
                    restoreFilter: restoreFilter
                };
            }])
}());