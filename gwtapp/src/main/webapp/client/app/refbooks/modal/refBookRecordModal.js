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
                $scope.record = $shareData.mode === 'CREATE' ? {} : $shareData.record;
                $scope.isEditMode = $shareData.mode === 'CREATE' || $shareData.mode === 'EDIT';
                $scope.mode = $shareData.mode;

                if ($scope.mode === 'CREATE' && $shareData.recordId) {
                    // Добавляем id группы версий записи справочника для создания новой версии
                    $scope.record[APP_CONSTANTS.REFBOOK_ALIAS.BUSINESS_ID_ALIAS] = {
                        value: $shareData.recordId,
                        attributeType: "NUMBER"
                    }
                }

                $scope.refBook.attributes.forEach(function (attribute) {
                    if ($scope.mode === 'CREATE' || !$scope.record[attribute.alias]) {
                        // При создании (и просто для пустых значений) надо указать тип атрибутов для корректной десериализации
                        $scope.record[attribute.alias] = {
                            attributeType: attribute.attributeType
                        }
                    } else {
                        if (attribute.attributeType === 'DATE' && $scope.record[attribute.alias]) {
                            // Преобразуем дату с сервера в js Date, чтобы календари корректно ее обрабатывали
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
                                value = refBookValue && refBookValue.value && typeof refBookValue.value !== 'undefined' ? refBookValue.value : "";
                                break;
                            case 'DATE':
                                value = refBookValue && refBookValue.value && typeof refBookValue.value !== 'undefined' && !isNaN(refBookValue.value) ? $filter('dateFormatter')(refBookValue.value) : "";
                                break;
                            case 'REFERENCE':
                                value = refBookValue && refBookValue.referenceObject && typeof refBookValue.referenceObject !== 'undefined' ? refBookValue.referenceObject[attribute.refBookAttribute.alias].value : "";
                                break;
                        }
                        return value;
                    }
                };

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
                    $scope.refBook.attributes.forEach(function (attribute) {
                        if (attribute.attributeType === 'REFERENCE' && $scope.record[attribute.alias] && $scope.record[attribute.alias].value) {
                            // Преобразуем ссылочные поля записи в подходящие для сервера
                            $scope.record[attribute.alias].value = $scope.record[attribute.alias].value.id;
                        }
                    });
                    var url;
                    if ($scope.mode === 'EDIT') {
                        url = "controller/actions/refBook/" + $scope.refBook.id + "/editRecord/" + $scope.record.id.value
                    } else {
                        url = "controller/actions/refBook/" + $scope.refBook.id + "/createRecord"
                    }
                    $http({
                        method: "POST",
                        url: url,
                        data: $scope.record
                    }).then(function () {
                        $modalInstance.close(true);
                    });
                };

                /**
                 * @description закрытие модального окна
                 */
                $scope.close = function () {
                    if ($shareData.mode === 'CREATE' || $shareData.mode === 'EDIT') {
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