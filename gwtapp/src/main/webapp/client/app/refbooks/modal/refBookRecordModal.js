(function () {
    'use strict';

    /**
     * @description Модуль для создания, просмотра и редактирования записей справочников
     */
    angular.module('app.refBookRecordModal', ['ui.router', 'app.rest'])

    /**
     * @description Контроллер для создания, просмотра и редактирования записей справочников
     */
        .controller('refBookRecordModalCtrl', ['$scope', '$filter', 'APP_CONSTANTS', '$modalInstance', '$shareData',
            '$http', '$logPanel', 'LogEntryResource', '$dialogs',
            function ($scope, $filter, APP_CONSTANTS, $modalInstance, $shareData, $http, $logPanel, LogEntryResource, $dialogs) {
                $scope.refBook = $shareData.refBook;
                $scope.isVersionMode = $shareData.recordId && $shareData.recordId !== 'undefined';
                $scope.isEditMode = $shareData.mode === 'CREATE' || $shareData.mode === 'EDIT';
                $scope.record = ($shareData.mode === 'CREATE' && !$scope.isVersionMode) ? {} : $.extend(true, {}, $shareData.record);
                $scope.mode = $shareData.mode;
                $scope.temp = {};

                if ($shareData.gridData) {
                    // Добавляем функционал для пролистывания записей справочника в режиме просмотра
                    $scope.recordIndexes = new Map();
                    $shareData.gridData.forEach(function (record, index) {
                        $scope.recordIndexes.set(record.id.value, index);
                    });
                    $scope.gridIterator = {
                        currentIndex: $scope.recordIndexes.get($scope.record.id.value),
                        currentRecord: $shareData.gridData[this.currentIndex],
                        getNext: function () {
                            if (this.currentIndex + 1 < $shareData.gridData.length) {
                                this.currentIndex += 1;
                                this.currentRecord = $shareData.gridData[this.currentIndex]
                            }
                            return this.currentRecord;
                        },
                        getPrevious: function () {
                            if (this.currentIndex - 1 >= 0) {
                                this.currentIndex -= 1;
                                this.currentRecord = $shareData.gridData[this.currentIndex]
                            }
                            return this.currentRecord;
                        },
                        hasNext: function () {
                            return this.currentIndex + 1 < $shareData.gridData.length
                        },
                        hasPrev: function () {
                            return this.currentIndex - 1 >= 0
                        }
                    };
                }

                if ($scope.mode === 'CREATE' && $scope.isVersionMode) {
                    // Добавляем id группы версий записи справочника для создания новой версии
                    $scope.record[APP_CONSTANTS.REFBOOK_ALIAS.BUSINESS_ID_ALIAS] = {
                        value: $shareData.recordId,
                        attributeType: "NUMBER"
                    }
                }

                $scope.refBook.attributes.forEach(function (attribute) {
                    if (!$scope.record[attribute.alias] || $scope.record[attribute.alias].attributeType === 'undefined') {
                        // Для пустых значений надо указать тип атрибутов для корректной отрисовки компонентов и десериализации при сохранении
                        if (!$scope.record[attribute.alias]) {
                            $scope.record[attribute.alias] = {
                                attributeType: attribute.attributeType
                            }
                        } else {
                            scope.record[attribute.alias].attributeType = attribute.attributeType;
                        }
                    }
                    if (attribute.attributeType === 'DATE' && $scope.record[attribute.alias]) {
                        // Преобразуем дату с сервера в js Date, чтобы календари корректно ее обрабатывали
                        if ($scope.record[attribute.alias].value && typeof $scope.record[attribute.alias].value !== 'undefined') {
                            $scope.record[attribute.alias].value = new Date($scope.record[attribute.alias].value);
                        }
                    }
                    if (attribute.alias === APP_CONSTANTS.REFBOOK_ALIAS.RECORD_VERSION_FROM_ALIAS) {
                        $scope.versionFromAttribute = attribute
                    }
                    if (attribute.alias === APP_CONSTANTS.REFBOOK_ALIAS.RECORD_VERSION_TO_ALIAS) {
                        $scope.versionToAttribute = attribute
                    }
                });

                if ($scope.mode === 'EDIT') {
                    if ($scope.refBook.versioned) {
                        // Для режима редактирования перекладываем даты периода во временные переменные, чтобы корректно работала валидация
                        $scope.temp.versionFrom = $scope.record[APP_CONSTANTS.REFBOOK_ALIAS.RECORD_VERSION_FROM_ALIAS].value;
                        $scope.temp.versionTo = $scope.record[APP_CONSTANTS.REFBOOK_ALIAS.RECORD_VERSION_TO_ALIAS].value;
                    }
                }

                if ($scope.refBook.versioned) {
                    $scope.$watchGroup(['temp.versionFrom', 'temp.versionTo'], function () {
                        $scope.validateVersionDates();
                    });
                }

                /**
                 * Валидация периода актуальности записи.
                 * Нужна так как стандартная валидация min-date, max-date не умеет работать с динамическими ограничениями, но они все равно используются для блокировки дат в самом календаре
                 */
                $scope.validateVersionDates = function() {
                    var generatedVersionFromId = 'temp_versionfrom';
                    var generatedVersionToId = 'temp_versionto';

                    if ($scope.refBookRecordForm && $scope.refBookRecordForm[generatedVersionFromId] && $scope.refBookRecordForm[generatedVersionToId]) {
                        var versionFrom = $scope.temp.versionFrom;
                        var versionTo = $scope.temp.versionTo;

                        if (versionFrom != null && versionTo != null && versionTo < versionFrom) {
                            $scope.refBookRecordForm[generatedVersionFromId].$setValidity('versionDate', false);
                            $scope.refBookRecordForm[generatedVersionToId].$setValidity('versionDate', false);
                        } else {
                            $scope.refBookRecordForm[generatedVersionFromId].$setValidity('versionDate', true);
                            $scope.refBookRecordForm[generatedVersionToId].$setValidity('versionDate', true);
                            // Обновляем валидные даты внутри записи из темповых переменных
                            $scope.record[APP_CONSTANTS.REFBOOK_ALIAS.RECORD_VERSION_FROM_ALIAS].value = versionFrom;
                            $scope.record[APP_CONSTANTS.REFBOOK_ALIAS.RECORD_VERSION_TO_ALIAS].value = versionTo;
                        }
                    }
                };

                /**
                 * Получает разыменованное значение атрибута
                 * @param attribute атрибут справочника
                 */
                $scope.getAttributeValue = function(attribute) {
                    if (attribute) {
                        var value = "";
                        var refBookValue = $scope.record[attribute.alias];
                        switch (attribute.attributeType) {
                            case 'STRING':
                            case 'NUMBER':
                                value = refBookValue && typeof refBookValue.value !== 'undefined' ? refBookValue.value : "";
                                break;
                            case 'DATE':
                                // Особенность для справочника "Коды, определяющие налоговый (отчётный) период" - год отображать не надо
                                var formatter = $scope.refBook.id === APP_CONSTANTS.REFBOOK.PERIOD_CODE ? $filter('dateWithoutYearFormatter') : $filter('dateFormatter');
                                value = refBookValue && typeof refBookValue.value !== 'undefined' && (!$scope.isEditMode || !isNaN(refBookValue.value)) ? formatter(refBookValue.value) : "";
                                break;
                            case 'REFERENCE':
                                value = refBookValue && typeof refBookValue.referenceObject !== 'undefined' ? refCellFormatter(attribute, refBookValue) : "";
                                break;
                        }
                        return value;
                    }
                };

                function refCellFormatter(attribute, refBookValue) {
                    if (attribute.refBookId === APP_CONSTANTS.REFBOOK.DEDUCTION_MARK) {
                        return $filter("codeNameFormatter")({
                            'code': refBookValue.referenceObject["CODE"].value,
                            'name': refBookValue.referenceObject["NAME"].value
                        });
                    } else {
                        return refBookValue.referenceObject[attribute.refBookAttribute.alias].value;
                    }
                }

                /**
                 * Получает "красивое" значение атрибута, отображаемое в GUI.
                 * @param attribute
                 */
                $scope.getAttributeFineValue = function(attribute) {
                    var value = $scope.getAttributeValue(attribute);
                    if (value === '' || value === null) {
                        return "-";
                    }
                    return value;
                };

                /**
                 * Сохранение
                 */
                $scope.save = function () {
                    var tempRecord = $.extend(true, {}, $scope.record);
                    $scope.refBook.attributes.forEach(function (attribute) {
                        if (attribute.attributeType === 'REFERENCE' && tempRecord[attribute.alias] && tempRecord[attribute.alias].value) {
                            // Преобразуем ссылочные поля записи в подходящие для сервера
                            tempRecord[attribute.alias].value = tempRecord[attribute.alias].value.id;
                        }
                        tempRecord[attribute.alias].attributeType = attribute.attributeType;
                    });
                    var url;
                    if ($scope.mode === 'EDIT') {
                        tempRecord.id.attributeType = 'NUMBER';
                        url = "controller/actions/refBook/" + $scope.refBook.id + "/editRecord/" + tempRecord.id.value
                    } else {
                        url = "controller/actions/refBook/" + $scope.refBook.id + "/createRecord"
                    }
                    $http({
                        method: "POST",
                        url: url,
                        data: tempRecord
                    }).then(function () {
                        $modalInstance.close(true);
                    });
                };

                /**
                 * @description переход к предыдущей записи в таблице
                 */
                $scope.showPreviousRecord = function () {
                    if ($scope.gridIterator && $scope.gridIterator.hasPrev()) {
                        $scope.record = $.extend(true, {}, $scope.gridIterator.getPrevious());
                    }
                };

                /**
                 * @description переход к следующей записи в таблице
                 */
                $scope.showNextRecord = function () {
                    if ($scope.gridIterator && $scope.gridIterator.hasNext()) {
                        $scope.record = $.extend(true, {}, $scope.gridIterator.getNext());
                    }
                };

                /**
                 * @description закрытие модального окна
                 */
                $scope.close = function () {
                    if (($shareData.mode === 'CREATE' || $shareData.mode === 'EDIT') && $scope.refBookRecordForm.$dirty) {
                        $dialogs.confirmDialog({
                            title: $filter('translate')('title.confirm'),
                            content: $filter('translate')('refBook.confirm.cancelEdit'),
                            okBtnCaption: $filter('translate')('common.button.yes'),
                            cancelBtnCaption: $filter('translate')('common.button.no'),
                            okBtnClick: function () {
                                $modalInstance.close(false);
                            }
                        });
                    } else {
                        $modalInstance.close(false);
                    }
                };

                /**
                 * Переопределенный метод модуля {aplana.modal}, чтобы по нажатию на крестик
                 * выполнялась логика кнопки "Закрыть"
                 */
                $scope.modalCloseCallback = function () {
                    $scope.close();
                };
            }
        ]);
}());