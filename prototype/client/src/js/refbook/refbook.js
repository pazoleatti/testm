(function() {
    'use strict'

    angular
        .module('mtsUsim.refBook', [
            'ui.router',
            'aplana.collapse',
            'ui.grid',
            'pascalprecht.translate', // локализация
            'ui.grid.pagination',     // пейджинг
            'ui.grid.resizeColumns',  // изменение ширины столбцов
            'ui.grid.saveState',      // сохранение состояния таблицы при переходах
            'ui.grid.selection',      // выделение строк
            'ui.grid.autoResize',
            'ui.bootstrap',
            'ui.select',
            'aplana.entity-utils',
            'aplana.mtsUsim.directives',
            'ngMessages',
            'mtsUsim.filterDirectives'
        ])
        .config(function ($stateProvider) {
            $stateProvider
                .state('refbook', {
                    url: '/refbook/:refbookName',
                    templateUrl: 'js/refbook/refbook.html',
                    controller: 'refbookController'
                })
        })
        .controller('refbookController', [
            '$scope', '$state', '$stateParams', '$http', '$translate', '$compile',
            '$filter', 'USER_DATA', '$log', '$q', '$injector', 'aplanaEntityUtils', '$timeout', 'aplanaDialogs', '$alertService',
            function($scope, $state, $stateParams, $http, $translate, $compile,
                     $filter, USER_DATA, $log, $q, $injector, aplanaEntityUtils, $timeout, aplanaDialogs, $alertService) {
                $scope.refbookName = $stateParams.refbookName;
                $translate('entity.' + $scope.refbookName).then(function(txt){
                    $scope.refbookTitle = txt
                });

                $scope.fulltextFilterEnabled = true;
                $scope.searchButtonEnabled = true;
                $scope.clearButtonEnabled = true;
                $scope.macroRegionFilterEnabled = true;
                $scope.tariffFilterEnabled = true;
                $scope.vendorFilterEnabled = true;
                $scope.editMode = false; // true - режим редактирования; false - режим просмотра
                $scope.currentEntityView = {};
                $scope.tempEntityData = {};
                $scope.showImportButton = importButtonIsVisible();
                $scope.importButtonEnabled = importIsAvailable();

                // Параметры отображения данных таблицы
                $scope.dataOptions = {
                    paging: {pageNumber: 1, pageSize: Number(USER_DATA.paging)},
                    sort: null,
                    filter: {},
                    filterList: {},
                    metaData: null // Информация о классе отображаемых данных
                };

                $scope.gridOptions = {
                    paginationPageSizes: aplanaEntityUtils.getPageSizes(),
                    paginationPageSize: $scope.dataOptions.paging.pageSize,
                    rowSelection: true,
                    useExternalPagination: true,
                    useExternalSorting: true,
                    enableFullRowSelection: true,
                    multiSelect: true,
                    modifierKeysToMultiSelect: true,
                    excludeSortColumns: ["code"],
                    onRegisterApi: function(gridApi) {
                        $scope.gridApi = gridApi;
                        $scope.gridApi.core.on.sortChanged($scope, function (grid, sortColumns) {
                            $scope.dataOptions.sort = sortColumns;
                            fetchData()
                        });
                        $scope.gridApi.pagination.on.paginationChanged($scope, function (pageNumber, pageSize) {
                            if ($scope.dataOptions.paging.pageSize != pageSize) {
                                // В случае изменения значения параметра "Кол-во записей на странице" переходим на 1-ю страницу
                                $scope.dataOptions.paging.pageNumber = 1;
                                $scope.gridOptions.paginationCurrentPage = 1
                            } else {
                                $scope.dataOptions.paging.pageNumber = pageNumber
                            }
                            $scope.dataOptions.paging.pageSize = pageSize;
                            fetchData()
                        });
                        $scope.gridApi.selection.on.rowSelectionChanged($scope, function(row, evt) {
                            // Вызывается при выделении(снятии выделение) строки
                            setCurrentRow($scope.gridApi.grid.selection.lastSelectedRow)
                        });
                        $scope.gridApi.selection.on.rowSelectionChangedBatch($scope, function(rows, evt) {
                            // Вызывается когда выделяются(снимается выделение) все строки щелчком по заголовку таблицы
                            setCurrentRow($scope.gridApi.grid.selection.lastSelectedRow)
                        })

                        //Настройка переноса текста в таблице
                        $scope.rowsRenderedTimeout = undefined;
                        var heightRowsChanged = [];
                        $scope.gridApi.core.on.rowsRendered($scope, function () {
                            // each rows rendered event (init, filter, pagination, tree expand)
                            // Timeout needed : multi rowsRendered are fired, we want only the last one
                            if ($scope.rowsRenderedTimeout) {
                                $timeout.cancel($scope.rowsRenderedTimeout)
                            }
                            $scope.rowsRenderedTimeout = $timeout(function () {
                                heightRowsChanged = aplanaEntityUtils.calculateAutoHeight('', $scope.gridApi.grid, heightRowsChanged);
                            });
                        });
                        $scope.gridApi.core.on.scrollEnd($scope, function () {
                            heightRowsChanged = aplanaEntityUtils.calculateAutoHeight('', $scope.gridApi.grid, heightRowsChanged);
                        });
                    }
                };


                function getMacroregions() {
                    //Пытаемся для начала получить из $rootScope. Оставляем проверку $rootScope.currentUser для защиты от дурака.
                    var promise;
                    if (!USER_DATA.macroRegion) {
                        //если пусто - должны получить полный список с сервера
                        promise = $http.get('rest/entity/MacroRegion').then(function (response) {
                            $log.info(angular.toJson(response));
                            try {
                                $scope.dataOptions.filterList.listMacroRegions = response.data.list;
                            } catch (ex) {
                                $log.error("Возникла ошибка при получении списка Макрорегионов с сервера: " + ex.message);
                                $scope.dataOptions.filterList.listMacroRegions = undefined;
                            }
                        });
                    } else {
                        $scope.dataOptions.filter.macroRegion = USER_DATA.macroRegion.id;
                        var defer = $q.defer();
                        defer.resolve();
                        promise = defer.promise;
                    }

                    return promise;
                } //Получаем из с сервера список макрорегионов.
                getMacroregions();

                /**
                 * Действие по кнопке "Редактировать" в панели редактирования
                 */
                $scope.editRecordClick = function() {
                    if (!$scope.editMode) {
                        $scope.editMode = true;
                        createEditPanelControls()
                    }
                };
                /**
                 * Действие по кнопке "Сохранить" в панели редактирования
                 */
                $scope.saveRecordClick = function() {
                    if ($scope.editMode) {
                        $scope.editMode = false;

                        if ($scope.tempEntityData.id == undefined) {
                            createData($scope.tempEntityData)
                        } else {
                            updateData($scope.tempEntityData,
                                function() {
                                    var currentEntityData = getDataByView($scope.currentEntityView);
                                    // Копирование значений из временного объекта в постоянный
                                    angular.extend(currentEntityData, $scope.tempEntityData);
                                    angular.extend($scope.currentEntityView, getRowView(currentEntityData));
                                    $scope.searchButtonClick();
                                },
                                function() {
                                    angular.extend($scope.tempEntityData, getDataByView($scope.currentEntityView))
                                })
                        }
                    }
                };
                /**
                 * Действие по кнопке "Отменить" в панели редактирования
                 */
                $scope.cancelRecordClick = function() {
                    if ($scope.editMode) {
                        $scope.editMode = false;
                        angular.extend($scope.tempEntityData, getDataByView($scope.currentEntityView))
                    }
                };
                /**
                 * Действие по кнопке "Найти"
                 */
                $scope.searchButtonClick = function() {
                    fetchData()
                };
                /**
                 * Создание новой записи
                 */
                $scope.addNewRecordClick = function() {
                    clearGridSelection();
                    $scope.cancelRecordClick();
                    $scope.tempEntityData = {};
                    $scope.tempEntityView = {};
                    $scope.editRecordClick()
                };
                /**
                 * Удаление записи
                 */
                $scope.lockRecordClick = function() {
                    if ($scope.currentEntityView != null) {
                        $scope.cancelRecordClick();
                        var currentEntityData = getDataByView($scope.currentEntityView);
                        deleteData(currentEntityData)
                    }
                };

                /**
                 * Восстановление записи
                 */
                $scope.unlockRecordClick = function() {
                    if ($scope.currentEntityView != null) {
                        $scope.cancelRecordClick();
                        var currentEntityData = getDataByView($scope.currentEntityView);

                        if (!currentEntityData.deleted) { // Проверка, что запись удалена
                            return
                        }
                        currentEntityData.deleted = false;
                        updateData(currentEntityData,
                            function() {
                                var currentEntityData = getDataByView($scope.currentEntityView);
                                // Копирование значений из временного объекта в постоянный
                                angular.extend(currentEntityData, $scope.tempEntityData);
                                angular.extend($scope.currentEntityView, getRowView(currentEntityData));
                                $scope.searchButtonClick()
                            },
                            function() {
                                angular.extend($scope.tempEntityData, getDataByView($scope.currentEntityView))
                            });
                    }
                };

                // Формирование параметров запроса
                var getRequestParams = function() {
                    var params = {};
                    // Параметры пейджинга
                    var from = $scope.dataOptions.paging.pageSize * ($scope.dataOptions.paging.pageNumber - 1) + 1;
                    var to = from + $scope.dataOptions.paging.pageSize - 1;
                    params.paging = from + ';' + to;
                    // Параметры сортировки
                    if ($scope.dataOptions.sort && $scope.dataOptions.sort.length != 0) {
                        var sort = '';
                        $scope.dataOptions.sort.forEach(function(column) {
                            sort += column.field + '-' + column.sort.direction + ';'
                        });
                        params.sort = sort
                    }
                    angular.extend(params, $scope.dataOptions.filter);
                    return params
                };

                $scope.exportDataClick = function() {
                    aplanaEntityUtils.getReport($scope.gridApi, $scope.refbookName, null, 'xlsx', getRequestParams())
                };

                //Импортировать
                $scope.importDataButtonClick = function () {
                    if(importIsAvailable) {
                        aplanaDialogs.import('Импорт данных', 'rest/service/refBook/import/' + $scope.refbookName, function () {
                            $alertService.success('Импорт выполнен успешно');
                            fetchData();
                        });
                    }
                };

                function importIsAvailable() {
                    return $scope.refbookName == 'Price' || $scope.refbookName == 'Delivery';
                }

                function importButtonIsVisible() {
                    return $scope.refbookName == 'Price' && aplanaEntityUtils.hasRole('REF_BOOK_IMPORT')
                        || $scope.refbookName == 'Delivery' && aplanaEntityUtils.hasRole('REF_BOOK_IMPORT_DELIVERY');
                }

                var clearGridSelection = function () {
                    $scope.gridApi.selection.clearSelectedRows()
                };
                /**
                 * Обновляет данные в таблице с учетом фильтрации, выполняет запрос к серверу
                 */
                var fetchData = function() {
                    // Получение данных
                    var params = getRequestParams();
                    $http.get('rest/entity/' + $scope.refbookName, {params: params})
                        .success(function (data) {
                            //Привязываем параметры запроса к контексту функций, возвращающих отчёты.
                            aplanaEntityUtils.setContext(params);
                            // Создаем столбцы, если их не было
                            if ($scope.dataOptions.metaData == null) {
                                $scope.dataOptions.metaData = data.metaData;
                                aplanaEntityUtils.createGridColumns($scope.dataOptions, $scope.gridOptions, $scope.gridApi);
                                createShowPanelControls();
                            }
                            if (data) {
                                var rows = data.list;
                                // Проверка, что получено данных не более чем запросили. Такое может случиться,
                                // если ДАО содержит ошибки
                                if (rows.length > $scope.dataOptions.paging.pageSize) {
                                    $translate('exceedPagingSize').then(function(msg){
                                        console.warn(msg)
                                    });
                                    // Обрезаем до требуемого количества
                                    rows = rows.slice(0, $scope.dataOptions.paging.pageSize)
                                }
                                $scope.dataOptions.data = rows;
                                $scope.gridOptions.totalItems = data.total;
                                updateViewData();
                                $log.log($scope)
                            }
                        })
                };

                /**
                 * Очистка полей фильтрации
                 */
                $scope.clearButtonClick = function() {
                    $scope.dataOptions.filter = {};
                    fetchData();
                };

                /**
                 * Отображает диалог для выбора списка регионов
                 */
                $scope.showRegionDialog = function(field) {
                    //TODO: сюда хочется передавать имя поля внутри объекта, в которое надо установить новые значения. Сейчас передается значение самого поля, т.к передать его текстом сходу не удалось
                    var fields = [
                        {title: 'Наименование: ', value: $scope.tempEntityData.name ? $scope.tempEntityData.name : null},
                        {title: 'Описание: ', value: $scope.tempEntityData.description ? $scope.tempEntityData.description : null}
                    ];

                    var params = {
                        header: "Выбор регионов",
                        rows: $scope.tempEntityData.regionList,
                        fields: fields,
                        refBook: 'Region',
                        metaData: [
                            {
                                name: "id",
                                order: 0,
                                title: "ID",
                                type :"java.lang.Long",
                                visible : false,
                                width : 100
                            },
                            {
                                name: "name",
                                order: 1,
                                title: "Регион",
                                type :"java.lang.String",
                                visible : true,
                                width : 250
                            },
                            {
                                displayField: "name",
                                name: "macroRegion",
                                order: 1,
                                title: "Макрорегион",
                                type :"com.mts.usim.model.MacroRegion",
                                visible : true,
                                width : 250
                            }
                        ]
                    };
                    var dlg = aplanaDialogs.refBook(params);
                    dlg.result.then(function (selectedItems) {
                        //TODO: хочется избежать явного присваивания поля
                        $scope.tempEntityData.regionList = selectedItems;
                        $scope.refBookEditForm.$pristine = false;
                        $scope.refBookEditForm.$dirty = true;
                    });
                };

                /**
                 * Отображает диалог для выбора списка тарифов
                 */
                $scope.showTariffDialog = function (field) {
                    var fields = [
                        {title: 'Наименование: ', value: $scope.tempEntityData.name ? $scope.tempEntityData.name : null},
                        {title: 'Тип карты: ', value: $scope.tempEntityData.cardType ? $scope.tempEntityData.cardType.name : null},
                        {title: 'Производитель: ', value: $scope.tempEntityData.vendor ? $scope.tempEntityData.vendor.name : null},
                        {title: 'Код профиля: ', value: $scope.tempEntityData.code ? $scope.tempEntityData.code : null},
                        {title: 'Алгоритм: ', value: $scope.tempEntityData.authAlgorythm ? $scope.tempEntityData.authAlgorythm.name : null},
                        {title: 'Дата подписания: ', value: $scope.tempEntityData.signDate ? $scope.tempEntityData.signDate.getDate() + '.' + $scope.tempEntityData.signDate.getMonth() + '.' + $scope.tempEntityData.signDate.getFullYear() : null},
                        {title: 'Описание: ', value: $scope.tempEntityData.description ? $scope.tempEntityData.description : null}
                    ];

                    var params = {
                        header: "Выбор тарифных планов",
                        rows: $scope.tempEntityData.tariffList,
                        fields: fields,
                        refBook: 'Tariff',
                        metaData: [
                            {
                                name: "id",
                                order: 0,
                                title: "ID",
                                type: "java.lang.Long",
                                visible: false,
                                width: 100
                            },
                            {
                                name: "name",
                                order: 1,
                                title: "Название",
                                type: "java.lang.String",
                                visible: true,
                                width: 500
                            }
                        ]
                    };
                    var dlg = aplanaDialogs.refBook(params);
                    dlg.result.then(function (selectedItems) {
                        $scope.tempEntityData.tariffList = selectedItems;
                        $scope.refBookEditForm.$pristine = false;
                        $scope.refBookEditForm.$dirty = true;
                    });
                };

                // Первоначальное наполнение таблицы
                fetchData();

                /**
                 * Форматирует и обновляет выводимые на экран значения
                 */
                var updateViewData = function() {
                    var data = $scope.dataOptions.data;
                    var displayData = [];
                    // Изменяем выводимую информацию
                    data.forEach(function(row) {
                        displayData.push(getRowView(row))
                    });
                    $scope.gridOptions.data = displayData;
                    clearGridSelection()
                };

                var getFieldInfo = function(fieldName) {
                    var result;
                    $scope.dataOptions.metaData.forEach(function(meta) {
                        if (meta.name == fieldName) {
                            result = meta
                        }
                    });
                    return result ? result : {}
                };

                /**
                 * Представление данных записи для отображения на экране
                 * @param row
                 * @returns {Array}
                 */
                var getRowView = function(row) {
                    var displayRow = [];
                    for (var field in row) {
                        var fieldInfo = getFieldInfo(field);
                        if (field == 'deleted') {
                            displayRow.deleted = row.deleted ? 'Да' : ''
                        } else if (fieldInfo.type == 'boolean') {
                            displayRow[field] = row[field] ? 'Да' : 'Нет'
                        } else if (fieldInfo.type == 'java.util.Date') {
                            if (row[field] == null) {
                                displayRow[field] = ''
                                row[field] = null;
                            } else {
                                displayRow[field] = row[field]
                                row[field] = new Date(row[field])
                            }
                        } else if (fieldInfo.type.startsWith('com.mts.usim.model')) {
                            // Ссылки
                            displayRow[field] = row[field] == null ? "" : row[field][fieldInfo.displayField]
                        } else if (fieldInfo.type == 'java.util.List') {
                            // Список ссылок
                            if (row[field] == null || row[field].length == 0) {
                                displayRow[field] = ''
                            } else {
                                var values = [];
                                if (fieldInfo.displayField != null) {
                                    row[field].forEach(function (value) {
                                        values.push(value[fieldInfo.displayField])
                                    })
                                }
                                displayRow[field] = values.join(', ')
                            }
                        } else if (field == 'currentNumber'){
                            var formatLength = fieldInfo.format.length;
                            var fieldLength = row[field].toString().length;
                            if (fieldLength < formatLength){
                                var tempLength = formatLength - fieldLength;
                                displayRow[field] = row[field];
                                for (var i = 0; i < tempLength; i++){
                                    displayRow[field] = "0" + displayRow[field];
                                }
                                row[field] = displayRow[field];
                            } else {
                                displayRow[field] = row[field];
                                row[field] = displayRow[field];
                            }
                        } else {
                            displayRow[field] = row[field]
                        }
                    }
                    return displayRow
                };

                /**
                 * Следит за последней выделенной строкой.
                 * @param lastSelectedRow
                 */
                var setCurrentRow = function (lastSelectedRow) {
                    if (!lastSelectedRow.isSelected || $scope.gridApi.grid.selection.selectedCount != 1) {
                        $scope.currentEntityView = {};
                        $scope.tempEntityData = {};
                        $scope.tempEntityView = {}
                    } else {
                        $scope.currentEntityView = lastSelectedRow.entity;
                        var currentEntityData = getDataByView($scope.currentEntityView);
                        $scope.tempEntityData = angular.extend({}, currentEntityData);
                        $scope.tempEntityView = angular.extend({}, $scope.currentEntityView)
                    }
                    $scope.cancelRecordClick()
                };

                /**
                 * Поиск объекта по его представлению
                 * @param view
                 */
                var getDataByView = function(view) {
                    var result = {};
                    $scope.dataOptions.data.forEach(function(entity) {
                        if (entity.id == view.id) {
                            result = entity
                        }
                    });
                    return result
                };

                var createShowPanelControls = function () {
                    var html = " ";
                    $scope.dataOptions.metaData.forEach(function(field) {
                        if (!field.readOnly) {
                            html +=
                                '<div class="form-group form-group-sm">\n' +
                                '   <label class="col-sm-4 control-label">' + field.title + ':</label>\n' +
                                '   <div class="col-sm-7">' +
                                '      <label class="control-label">{{currentEntityView.' + field.name + '}}</label>' +
                                '   </div' +
                                '></div>\n'
                        }
                    });
                    $injector.invoke(function($compile){
                        var parent = $("#showForm");
                        parent.html($(html));
                        $compile(parent.contents())(parent.scope())
                    })
                };

                var createEditPanelControls = function () {
                    // Для хранения настроек виджетов
                    $scope.editPanel = {};
                    var html = " ";
                    for (var i = 0; i < $scope.dataOptions.metaData.length; i++) {
                        var field = $scope.dataOptions.metaData[i];
                        if (!field.readOnly) {
                            if (field.pattern) {
                                //Преобразуем строку патерна в сам JS паттерн, иначе некоторые символы могут обрабатываться некорректно
                                field.pattern = new RegExp(field.pattern);
                            }
                            html +=
                                    '<div class="form-group form-group-sm"' +
                                    '       data-ng-class="' + field.required + ' ? \'required\' : \'\'">\n' +
                                    '   <label class="col-sm-4 control-label">' + field.title + ':</label>\n'
                            if (field.canSelectAll == true) {
                                html +=  '   <div class="col-sm-7">' +
                                         '      <div>(<input type="checkbox" id="allItems" data-ng-click="selectAllItems()"/> Все)</div>' +
                                    getFieldTemplate(field, i) +
                                    '   </div>' +
                                    '</div>\n'
                            } else {
                                html +=  '   <div class="col-sm-7">' +
                                    getFieldTemplate(field, i) +
                                    '   </div>' +
                                    '</div>\n'
                            }
                        }
                    }
                    $injector.invoke(function($compile){
                        var parent = $("#refbook-edit-panel #editForm")
                        parent.html($(html));
                        $compile(parent.contents())(parent.scope())
                    });
                    $scope.refBookEditForm.$setPristine();
                };

                $scope.selectAllItems = function (){
                    var elem = document.getElementById("allItems");
                    if (elem.checked){
                        var macroRegions = $scope.dataOptions.filterList.listMacroRegions;
                        $scope.tempEntityData.macroRegionList = [];
                        for(var i = 0; i < macroRegions.length; i++){
                            var item = macroRegions[i];
                            $scope.tempEntityData.macroRegionList.push(item);
                        }
                    } else {
                        $scope.tempEntityData.macroRegionList = undefined;
                    }
                    if ($scope.refBookEditForm.$pristine)
                    {
                        $scope.refBookEditForm.$setDirty();
                    }
                };

                /**
                 * Для выбора значения справочника открывается диалог
                 * @returns {boolean}
                 */
                $scope.isRefbookWithDialog = function() {
                    return $scope.refbookName == 'AuthAlgorythm' || $scope.refbookName == 'Profile';
                }

                /**
                 * Текст обработчика события click у поля справочника
                 * @param field
                 * @returns {string}
                 */
                function refbookFieldOnСlickText(field) {
                    if($scope.refbookName == 'AuthAlgorythm') {
                        return 'data-ng-click="showRegionDialog(tempEntityData.' + field + ')"'
                    } if($scope.refbookName == 'Profile') {
                        return 'data-ng-click="showTariffDialog(tempEntityData.' + field + ')"'
                    } else {
                        return '';
                    }
                }

                /**
                 * Формирует html-код для редактирования значения указанного поля
                 * @param field поле, для которого требуется создать виджеты
                 * @param metaIndex индекс мета-информации из массива связанной с полем
                 * @returns {string} html-код
                 */
                var getFieldTemplate = function(field, metaIndex) {
                    var html = '';
                    if (field.type == 'java.util.List') {
                        $scope.editPanel[field.name] = {
                            field: field,
                            items: [],
                            searchItems: function(searchText) {
                                var url = 'rest/entity/light/' + field.referenceType.substring('com.mts.usim.model.'.length)
                                var params = {
                                    fulltext: searchText,
                                    sort: field.displayField + '-asc',
                                };
                                // Получение данных
                                $http.get(url, {params: params})
                                    .success(function (data) {
                                        $scope.editPanel[field.name].items = data.list
                                    })
                            }
                        };
                        var fieldScope = 'editPanel[\'' + field.name + '\']';
                        html =
                            '    <ui-select remove-selected="false"' +
                            '      multiple' +
                            '      ng-model="tempEntityData.' + field.name + '"' +
                            refbookFieldOnСlickText(field.name) +
                            '      ng-show="editMode"' +
                            '      require-multiple>' +
                            getValidationHtml(field, metaIndex) +
                            '        <ui-select-match>' +
                            '          <span ng-bind="$item[' + fieldScope + '.field.displayField]"></span>' +
                            '        </ui-select-match>' +
                            '        <ui-select-choices ng-show="!isRefbookWithDialog() &&" repeat="item in ' + fieldScope + '.items track by $index"' +
                            '            refresh="' + fieldScope + '.searchItems($select.search)"' +
                            '            refresh-delay="500">' +
                            '          <span ng-bind-html="item[' + fieldScope + '.field.displayField] | highlight: $select.search"></span>' +
                            '        </ui-select-choices>' +
                            '    </ui-select>\n'
                    } else
                    if (field.type.startsWith('com.mts.usim.model')){
                        $scope.editPanel[field.name] = {
                            field: field,
                            items: [],
                            searchItems: function(searchText) {
                                var url = 'rest/entity/light/' + field.type.substring('com.mts.usim.model.'.length)
                                var params = {
                                    fulltext: searchText,
                                    sort: field.displayField + '-asc',
                                };
                                //Доп. условия для фильтрации в справочнике Коды для поля Тип Карты
                                if ($scope.refbookName == "Code") {
                                    if (field.name == "tariff") {
                                        $scope.tempEntityData.cardType = undefined;
                                    }
                                    if (field.name == "cardType" && $scope.tempEntityData.tariff) {
                                        params.tariffId = $scope.tempEntityData.tariff.id
                                    }
                                }
                                // Получение данных
                                $http.get(url, {params: params})
                                    .success(function (data) {
                                        $scope.editPanel[field.name].items = data.list
                                    })
                            },
                            selectClick: function() {
                                if ($scope.tempEntityData[field.name] == undefined) {
                                    $scope.tempEntityData[field.name] = null; // Очистка значения для передачи на сервер
                                }
                            }
                        };
                        var fieldScope = 'editPanel[\'' + field.name + '\']';
                        html =
                            '    <ui-select' +
                            '      ng-model="tempEntityData.' + field.name + '"' +
                            '      on-select="' + fieldScope + '.selectClick()"' +
                            '      ng-click="' + fieldScope + '.searchItems($select.search)"' +
                            getValidationHtml(field, metaIndex) +
                            '      ng-show="editMode">' +
                            '        <ui-select-match allow-clear="' + !field.required + '">' +
                            '          <span ng-bind="$select.selected[' + fieldScope + '.field.displayField]"></span>' +
                            '        </ui-select-match>' +
                            '        <ui-select-choices repeat="item in ' + fieldScope + '.items track by $index"' +
                            '            refresh="' + fieldScope + '.searchItems($select.search)"' +
                            '            refresh-delay="500">' +
                            '          <span ng-bind-html="item[' + fieldScope + '.field.displayField] | highlight: $select.search"></span>' +
                            '        </ui-select-choices>' +
                            '    </ui-select>\n'
                    } else {
                        switch (field.type) {
                            case 'int':
                            case 'java.lang.Integer':
                                html =
                                    '<input type="text" class="form-control input-sm" placeholder="{{' + field.defaultValue + '}}" ' +
                                    '  data-ng-show="editMode"' +
                                    '  name="' + field.name + '"' +
                                    (!field.pattern ? '  ng-pattern="/^-?\\d+$/"'
                                        : ' ng-pattern="dataOptions.metaData[' + metaIndex + '].pattern" ') +
                                    '  data-ng-model="tempEntityData.' + field.name + '">\n' +
                                    getNotifyHtml(field);
                                break;
                            case 'double':
                            case 'java.lang.Double':
                                html =
                                    '<input type="text" class="form-control input-sm" placeholder="{{' + field.defaultValue + '}}" ' +
                                    '  data-ng-show="editMode"' +
                                    '  name="' + field.name + '"' +
                                    (!field.pattern ? '  ng-pattern="/^-?\\d+(\\.\\d)?\\d*$/"'
                                        : ' ng-pattern="dataOptions.metaData[' + metaIndex + '].pattern" ') +
                                    '  data-ng-model="tempEntityData.' + field.name + '">\n' +
                                    getNotifyHtml(field);
                                break;
                            case 'long':
                            case 'java.lang.Long':
                                html =
                                    '<input type="text" class="form-control input-sm" ' +
                                    '  data-ng-show="editMode"' +
                                    '  name="' + field.name + '"' +
                                    (!field.pattern ? '  ng-pattern="/^-?\\d+$/"'
                                        : ' ng-pattern="dataOptions.metaData[' + metaIndex + '].pattern" ') +
                                    '  data-ng-model="tempEntityData.' + field.name + '">\n' +
                                    getNotifyHtml(field);
                                break;
                            case 'boolean':
                                html =
                                    '<input type="checkbox" ' +
                                    '  name="' + field.name + '"' +
                                    '  data-ng-show="editMode"' +
                                    '  data-ng-model="tempEntityData.' + field.name + '">\n';
                                break;
                            case 'java.util.Date': //todo настроить формат даты
                                $scope.editPanel[field.name] = {
                                    field: field,
                                    is_open: false,
                                    selectClick: function() {
                                        $scope.editPanel[field.name].is_open = true
                                    }
                                };
                                html =
                                    '  <p class="input-group" data-ng-show="editMode">' +
                                    '    <input ' +
                                    '  name="' + field.name + '"' +
                                    '      type="text"' +
                                    '      class="form-control" datetime="yyyy-MM-dd"' +
                                    '      uib-datepicker-popup="yyyy-MM-dd" datepicker-timezone' +
                                    '      is-open="editPanel[\'' + field.name + '\'].is_open"' +
                                    '      close-text="Выбрать" current-text="Сегодня" clear-text="Очистить"' +
                                    getValidationHtml(field, metaIndex) +
                                    '      data-ng-model="tempEntityData.' + field.name + '">\n' +
                                    '    <span class="input-group-btn">' +
                                    '      <button type="button" class="btn btn-default btn-sm"' +
                                    '        ng-click="editPanel[\'' + field.name + '\'].selectClick()">' +
                                    '        <i class="glyphicon glyphicon-calendar"></i></button>' +
                                    '    </span>\n' +
                                    '  </p>\n' +
                                    getNotifyHtml(field);
                                break;
                            case 'java.lang.String':
                            default:
                                html =
                                    '<input type="text" class="form-control input-sm" ' +
                                    '  data-ng-show="editMode"' +
                                    '  name="' + field.name + '"' +
                                    getValidationHtml(field, metaIndex) +
                                    '  data-ng-model="tempEntityData.' + field.name + '">\n' +
                                    getNotifyHtml(field)
                        }
                    }
                    return html
                };

                var getValidationHtml = function(field, metaIndex) {
                    var html = '';
                    if (field.required) {
                        html += ' data-ng-required="true" '
                    }
                    if (field.maxLength && field.type == 'java.lang.String') {
                        html += ' data-ng-maxlength="' + field.maxLength + '" '
                    }
                    if (field.minLength && field.type == 'java.lang.String') {
                        html += ' data-ng-minlength="' + field.minLength + '" '
                    }
                    if (field.max && field.type == 'long') {
                        html += ' data-ng-max="' + field.max + '" '
                    }
                    if (field.min && field.type == 'long') {
                        html += ' data-ng-min="' + field.min + '" '
                    }
                    if (field.pattern) {
                        //html += ' ng-pattern="' + field.pattern + '" '
                        html += ' ng-pattern="dataOptions.metaData[' + metaIndex+ '].pattern" '
                    }
                    return html
                };
                var getNotifyHtml = function(field) {
                    var html = '';
                    if (field.required) {
                        html += '<p data-ng-message="required"></p>'//{{"error.form.required" | translate}}</p>'
                    }
                    if (field.maxLength && field.type == 'java.lang.String') {
                        html += '<p data-ng-message="maxlength">{{"error.form.string.maxLength" | translate}}&nbsp;' + field.maxLength + '</p>'
                    }
                    if (field.minLength && field.type == 'java.lang.String') {
                        html += '<p data-ng-message="minlength">{{"error.form.string.minLength" | translate}}&nbsp;' + field.minLength + '</p>'
                    }
                    if (field.max && field.type == 'long') {
                        html += '<p data-ng-message="max">{{"error.form.number.max" | translate}}&nbsp;' + field.max + '</p>'
                    }
                    if (field.min && field.type == 'long') {
                        html += '<p data-ng-message="min">{{"error.form.number.min" | translate}}&nbsp;' + field.min + '</p>'
                    }
                    if (!field.pattern
                        && (field.type == 'int' || field.type == 'java.lang.Integer' || field.type == 'double' || field.type == 'java.lang.Double')) {
                        html += '<span ng-show="refBookEditForm.' + field.name + '.$error.pattern" class="text-danger">{{"error.form.pattern.format" | translate}}</span>'
                    }
                    if (field.pattern && field.name == 'currentNumber') {
                        html += '<span ng-show="refBookEditForm.' + field.name + '.$error.pattern" class="text-danger">{{"error.form.pattern.format" | translate}}</span>'
                    }else if (field.pattern) {
                        html += '<span ng-show="refBookEditForm.' + field.name + '.$error.pattern" class="text-danger">{{"error.form.pattern" | translate}}</span>'
                    }
                    return html.length > 0 ?
                    '<div class="text-danger" data-ng-messages="refBookEditForm.' + field.name + '.$error">' +
                    html +
                    '</div>'
                        : ''
                };
                /**
                 * Отправка запроса для обновления записи
                 */
                var updateData = function(row, successCallback, failCallback) {
                    var url = 'rest/entity/' + $scope.refbookName + '/' + row.id
                    // Отправка данных
                    $http.put(url, row).then(
                        successCallback,
                        failCallback
                    )
                };

                /**
                 * Отправка запроса для создания новой записи
                 * @param row
                 */
                var createData = function(row) {
                    var url = 'rest/entity/' + $scope.refbookName
                    // Отправка данных
                    $http.post(url, row).then(
                        function successCallback(response) {
                            $scope.searchButtonClick()
                        },
                        function failCallback(response) {
                            $scope.editMode = true
                        }
                    )
                };

                /**
                 * Отправка запроса для удаления записи
                 * @param row
                 */
                var deleteData = function(row) {
                    if (row.deleted) { // Проверка, что запись уже удалена
                        return
                    }
                    var url = 'rest/entity/' + $scope.refbookName + '/' + row.id;
                    // Отправка данных
                    $http.delete(url, row).then(
                        function successCallback(response) {
                            $log.log('OK');
                            $scope.searchButtonClick()
                        }
                    )
                }
            }])
}());
