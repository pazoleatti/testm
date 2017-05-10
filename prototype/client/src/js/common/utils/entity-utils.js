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

                //Экзепляры getReport и getReportByUrl для привязки контекста
                var getReportWithContext = _getReport;
                var getReportByUrlWithContext = _getReportByUrl;


                /**
                 * Возвращает список идентификаторов объектов в виде строки. Разделитель - запятая
                 *
                 * @param entities список объектов
                 * @returns {string} список идентификаторов, разделенных запятой
                 */
                function getEntityIds(entities) {
                    var result = [];
                    entities.forEach(function (entity) {
                        result.push(entity.id);
                    });
                    return result.join(", ");
                }

                /**
                 * Возвращает для таблицы список идентификаторов объектов в выделенных строках
                 *
                 * TODO - если найдется другой способ, переделать
                 * Заменил проверку if (gridApi.constructor.name == "GridApi") на if (gridApi.selection != undefined),
                 * т.к. IE не поддерживает constructor.name.
                 *
                 * @param gridApi данные о таблице
                 * @returns {string} список идентификаторов, разделенных запятой
                 */
                function getSelectedEntityIds(gridApi) {
                    if (gridApi.selection != undefined) {
                        return getEntityIds(gridApi.selection.getSelectedRows());
                    } else {
                        return "";
                    }
                }

                /**
                 * Сформировать отчет по указанным параметрам. Приватная версия
                 *
                 * @param gridApi ссылка на таблицу, если требуется отображать в отчете только выделенные строки
                 * @param reportType тип отчета, можно использовать краткое название класса "MacroRegion", "Remains" и т.д.
                 * @param entityId (опционально) идентификатор объекта, для которого формируется отчет
                 * @param reportKind (опционально) вид отчета, произвольная строка, например: "xlsx", "csv", "pdf", .... По умолчанию "xlsx"
                 * @param filterParams (опционально) дополнительные параметры формирования отчета (фильтрация данных)
                 */
                function _getReport(gridApi, reportType, entityId, reportKind, filterParams) {
                    //Есть нужный контекст, то параметры запроса тянем из него
                    if (!this || !this.params) {
                        var params = angular.extend({}, filterParams ? filterParams : {});
                    } else {
                        var params = angular.extend({}, this.params ? this.params : {});
                    }

                    params.reportType = reportType;
                    if (entityId) {
                        params.entityId = entityId
                    }
                    params.reportKind = reportKind ? reportKind : 'xlsx';
                    delete params.paging; // убираем параметры пейджинга
                    if (gridApi && gridApi.grid.selection.selectedCount != 0) { // если есть выделенные строки, то только их
                        params.id = getSelectedEntityIds(gridApi)
                    }
                    //Преобразование формата даты в нормально передающийся через $window.location
                    for (var key in params) {
                        if (params[key] instanceof Date) {
                            params[key] = params[key].format("yyyy-mm-dd")
                        }
                    }
                    //Удаление пустых значений
                    for (var key in params) {
                        if (!params[key] && !(typeof params[key] == 'number' && params[key] == 0) || (typeof params[key] == 'string' && params[key].trim().length == 0)) {
                            delete params[key]
                        }
                    }
                    $window.location = 'rest/service/reportService/getReport?' + $.param(params)
                }

                function _getReportByUrl(url, filterParams, reportKind) {
                    if (!this || !this.params) {
                        var params = angular.extend({}, filterParams ? filterParams : {});
                    } else {
                        var params = angular.extend({}, this.params ? this.params : {});
                    }

                    params.reportKind = reportKind ? reportKind : 'xlsx';
                    delete params.paging; // убираем параметры пейджинга
                    //Преобразование формата даты в нормально передающийся через $window.location
                    for (var key in params) {
                        if (params[key] instanceof Date) {
                            params[key] = params[key].format("yyyy-mm-dd")
                        }
                    }
                    $window.location = url + '?' + $.param(params)
                }

                /**
                 * Публичная функция-обёртка для вызова _getReport с привязанным контекстом
                 * @param gridApi
                 * @param reportType
                 * @param entityId
                 * @param reportKind
                 * @param filterParams
                 */
                function getReport(gridApi, reportType, entityId, reportKind, filterParams) {
                    getReportWithContext(gridApi, reportType, entityId, reportKind, filterParams);
                }

                /**
                 * Функция-обёртка для вызова getReportByUrl с контекстом
                 * @param url
                 * @param filterParams
                 * @param reportKind
                 */
                function getReportByUrl(url, filterParams, reportKind) {
                    getReportByUrlWithContext(url, filterParams, reportKind);
                }


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
                 * Функция для заполнения метаданных таблицы
                 * @param scope объект, содержащиё параметры запроса. Как правило - $scope.dataOptions
                 * @param metaData метаданные
                 */
                function setMetaData(scope, metaData) {
                    scope.metaData = {};
                    metaData.forEach(function (field) {
                        scope.metaData[field.name] = field;
                    });
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
                 * @param rowDblClick           функция вызываемая при двойном клике в строке таблицы
                 * @param rowSelectionChanged   функция вызываемая при изменении выделения строк в таблице
                 */
                function initGrid(scope, getData, rowDblClick, rowSelectionChanged) {
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
                                        //Восстанавливаем выделение строк, если оно было
                                        restoreSelection(scope);
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
                 * Функция для очистки выделения в таблице
                 * @param gridApi ссылка на API таблицы (как правило, $scope.gridApi)
                 */
                function clearGridSelection(gridApi) {
                    gridApi.selection.clearSelectedRows();
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
                            } else if (fieldInfo.type.startsWith('com.mts.usim.model')) {
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
                 * Устанавливает значение поля в объекте.
                 * Имя поля может быть задано как путь внутри объекта. Например: role.name
                 * @param object объект, в котором нужно установить значение поля
                 * @param value значение
                 * @param field название поля
                 */
                function setFieldValue(object, value, field) {
                    if (field.indexOf('.') != -1) {
                        //Имя представляет собой путь внутри полей объекта
                        var currentField = value;
                        field.split('.').forEach(function (fieldName) {
                            if (currentField) {
                                currentField = currentField[fieldName];
                            }
                        });
                        currentField = value;
                    } else {
                        //Просто имя поля
                        object[field] = value;
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
                 * Устанавливает значение поля в объекте.
                 * Имя поля может быть задано как путь внутри объекта. Например: role.name
                 * @param object объект, в котором нужно установить значение поля
                 * @param value значение
                 * @param field название поля
                 */
                function setFieldValue(object, value, field) {
                    if (field.indexOf('.') != -1) {
                        //Имя представляет собой путь внутри полей объекта
                        var currentField = value;
                        field.split('.').forEach(function (fieldName) {
                            if (currentField) {
                                currentField = currentField[fieldName];
                            }
                        });
                        currentField = value;
                    } else {
                        //Просто имя поля
                        object[field] = value;
                    }
                }

                /**
                 * Функция для обновления внешнего вида таблицы
                 * @param scope ссылка на $scope, либо на $scope.dataOptions (или аналог)
                 * @param gridOptions ссылка на объект, содержащий массив строк для таблицы (как правило, $scope.gridOptions)
                 * @param gridApi сслыка на API таблицы
                 */
                function updateViewData(scope, gridOptions, gridApi) {
                    //Учитываем, что мог прийти $scope, а мог - $scope.dataOptions
                    var data = scope.dataOptions ? scope.dataOptions.data
                        : scope.data ? scope.data : [];
                    var displayData = [];

                    //Заполняем таблицу строками
                    data.forEach(function (row) {
                        displayData.push(getRowView(scope, row));
                    });
                    gridOptions.data = displayData;
                    clearGridSelection(gridApi)
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

                    if (gridApi.grid.selection.selectedCount > 0) {
                        saveSelection(scope);
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
                 * Функция для проверки наличия у текущей роли текущего пользователя хотя бы одной нужной операции.
                 * @param (array) operation операции, которые нужно проверить, константа из класса Operation
                 * @returns {boolean}
                 */
                function checkOneOperation(operation) {
                    if (!operation || operation.length == 0) {
                        return false;
                    }

                    return operation.reduce(function (result, operation) {
                        result = result || USER_DATA.authorities.indexOf(operation) >= 0
                        return result;
                    }, false);
                }

                /**
                 * Инициализирует переменные сессии для текущего раздела
                 */
                function initPageSession(scope, callback) {
                    sessionStorage.removeItem(APP_CONSTANTS.SESSION.SELECTED_IDS);
                    return restoreFilter(scope, callback)
                }

                /**
                 * Запоминает выделенные строки таблицы в сессии
                 */
                function saveSelection(scope) {
                    var ids = [];
                    scope.gridApi.selection.getSelectedRows().forEach(function (row) {
                        ids.push(row.id);
                    });
                    sessionStorage.setItem(APP_CONSTANTS.SESSION.SELECTED_IDS, JSON.stringify(ids));
                }

                /**
                 * Восстанавлиет выделенные строки таблицы из сессии
                 */
                function restoreSelection(scope) {
                    var ids = JSON.parse(sessionStorage.getItem(APP_CONSTANTS.SESSION.SELECTED_IDS));
                    if (ids) {
                        scope.gridOptions.data.forEach(function (row) {
                            ids.forEach(function (id) {
                                if (row.id == id) {
                                    scope.gridApi.selection.selectRow(row);
                                }
                            });
                        });
                    }
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
                                if (sessionStorage.hasOwnProperty(item) && item != APP_CONSTANTS.SESSION.SELECTED_IDS &&
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
                 * Проверяет что все выделенные строки в таблице имеют одно и то же значение в указанном поле.
                 * Дополнительным параметром можно указать значение, которое должно быть в этом поле (совпадать у всех строк)
                 * @param scope скоуп, в котором содержится конфиг таблицы
                 * @param field путь до поля внутри объекта
                 * @param requiredValue (необязательное) если указано - сравнивается со значениями поля всех строк (должно совпадать)
                 */
                function isSelectionHasSameValue(scope, field, requiredValue) {
                    var currentValue = undefined;
                    var hasEmpty = false;
                    var selectedItems = getSelectedEntities(scope);
                    if (selectedItems.length == 1) {
                        if (requiredValue) {
                            //Если выбрана одна строка, то проверяем ее значение в поле
                            return isSelectionHasValue(scope, field, [requiredValue])
                        } else {
                            //Если искомое значение не указано и выбрана одна строка, то сравнивать нет смысла
                            return true;
                        }
                    }
                    for (var i = 0; i < selectedItems.length; i++) {
                        //Проверяем каждую строку на соответствие требуемому значению + сравниваем между собой
                        var item = selectedItems[i];
                        var value = getFieldValue(item, field);
                        if (value != undefined) {
                            if (hasEmpty) {
                                return false;
                            }
                            if (currentValue == undefined) {
                                currentValue = value;
                            } else {
                                if (JSON.stringify(currentValue) !== JSON.stringify(value)
                                    || (requiredValue && JSON.stringify(value) !== JSON.stringify(requiredValue))) {
                                    return false;
                                }
                            }
                        } else {
                            if (requiredValue && JSON.stringify(value) !== JSON.stringify(requiredValue)) {
                                return false;
                            }
                            hasEmpty = true;
                        }
                    }
                    return true;
                }

                /**
                 * Проверяет что все выделенные строки в таблице имеют значение из списка в указанном поле.
                 * Список возможных значений указывается параметром, если не указан - то проверяется что значение поля непустое
                 * @param scope скоуп, в котором содержится конфиг таблицы
                 * @param field путь до поля внутри объекта
                 * @param requiredValues (необязательное) массив значений для сравнения со всеми остальными. Если не указано, то проверяется, что значение поля - непустое
                 */
                function isSelectionHasValue(scope, field, requiredValues) {
                    var selectedItems = getSelectedEntities(scope);
                    if (selectedItems.length == 0) {
                        return false;
                    }
                    for (var i = 0; i < selectedItems.length; i++) {
                        var item = selectedItems[i];
                        if (!isObjectHasValue(item, field, requiredValues)) {
                            return false;
                        }
                    }
                    return true;
                }

                /**
                 * Проверяет что указанный объект имеет определенное значение в указанном поле.
                 * Список возможных значений указывается параметром, если не указан - то проверяется что значение поля непустое
                 * @param object объект, в котором надо найти значение
                 * @param field путь до поля внутри объекта
                 * @param requiredValues (необязательное) массив значений для сравнения со всеми остальными. Если не указано, то проверяется, что значение поля - непустое
                 */
                function isObjectHasValue(object, field, requiredValues) {
                    var value = getFieldValue(object, field);
                    if (requiredValues) {
                        //Если искомые значения указаны - ищем их
                        if (value != undefined) {
                            return hasValueInArray(value, requiredValues)
                        }
                    } else {
                        //Иначе - достаточно, что значение поля непустое
                        return value !== undefined && value != null;
                    }
                }

                /**
                 * Проверяет существование элемента в массиве
                 * @param value значение элемента
                 * @param requiredValues массив элементов
                 */
                function hasValueInArray(value, requiredValues) {
                    if (requiredValues) {
                        var hasInArray = false;
                        for (var j = 0; j < requiredValues.length; j++) {
                            var requiredValue = requiredValues[j];
                            if (JSON.stringify(requiredValue) === JSON.stringify(value)) {
                                hasInArray = true;
                            }
                        }
                        return hasInArray;
                    } else {
                        return value !== undefined
                    }
                }

                /**
                 * Проверяет что все какой либо строке таблицы есть указанное значение в нужном поле.
                 * @param scope скоуп, в котором содержится конфиг таблицы
                 * @param field (необязательное) путь до поля внутри объекта. Если не указано - проверется, есть ли вообще в таблице значения
                 * @param requiredValues (необязательное) массив значений. Если не указано - проверется, есть ли вообще в таблице значения
                 */
                function isTableHasValue(scope, field, requiredValues) {
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

                /**
                 * Функция возвращает дату в формате дд.мм.гггг
                 * @param date
                 */
                function getDateStr(date) {
                    if (!date) {
                        return null;
                    }
                    var result = "";
                    if (typeof date != "string") {
                        var tmpDate = date;
                        if (typeof date === "number") {
                            tmpDate = new Date(date);
                        }
                        result += tmpDate.getDate() < 10 ? "0" + tmpDate.getDate() : tmpDate.getDate();
                        result += ".";
                        result += tmpDate.getMonth() + 1 < 10 ? "0" + (tmpDate.getMonth() + 1) : tmpDate.getMonth() + 1;
                        result += ".";
                        result += tmpDate.getFullYear();
                    } else {
                        var dateElts = date.split("-");
                        result = dateElts[2] + "." + dateElts[1] + "." + dateElts[0];
                    }
                    return result;
                }

                /**
                 * Проверяет наличие прав/роли у текущего пользователя
                 * @param role
                 */
                function hasRole(role) {
                    return $.inArray(role, USER_DATA.authorities) !== -1;
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

                return {
                    commonRestUrl: commonRestUrl,

                    //Функции для работы с данными
                    getEntityIds: getEntityIds,
                    getSelectedEntityIds: getSelectedEntityIds,
                    getReport: getReport,
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
                    setFieldValue: setFieldValue,
                    isObjectHasValue: isObjectHasValue,
                    getDateStr: getDateStr,
                    getReportByUrl: getReportByUrl,

                    //Функции для работы с правами
                    hasRole: hasRole,
                    checkOperation: checkOperation,
                    checkOneOperation: checkOneOperation,

                    //Функции для работы с гридами
                    initGrid: initGrid,
                    fillGrid: fillGrid,
                    clearGridSelection: clearGridSelection,
                    setCurrentRow: setCurrentRow,
                    fetchData: fetchData,
                    getSelectedEntities: getSelectedEntities,
                    isSelectionHasSameValue: isSelectionHasSameValue,
                    isSelectionHasValue: isSelectionHasValue,
                    isTableHasValue: isTableHasValue,
                    getPageSizes: getPageSizes,
                    calculateAutoHeight: calculateAutoHeight,
                    fitColumnsWidth: fitColumnsWidth,

                    //Функции работы с данными сессии
                    initPageSession: initPageSession,
                    saveSelection: saveSelection,
                    restoreSelection: restoreSelection,
                    saveFilter: saveFilter,
                    restoreFilter: restoreFilter
                };
            }])
}());