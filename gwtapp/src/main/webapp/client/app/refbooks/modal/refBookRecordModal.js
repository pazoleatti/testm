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
                $scope.record = $shareData.record;
                $scope.isEditMode = $shareData.mode === 'CREATE' || $shareData.mode === 'EDIT';
                $scope.mode = $shareData.mode;

                $scope.refBook.attributes.forEach(function (attribute) {
                    if (attribute.attributeType === 'DATE' && $scope.record[attribute.alias]) {
                        // Преобразуем дату с сервера в js Date, чтобы календари корректно ее обрабатывали
                        $scope.record[attribute.alias].dateValue = new Date($scope.record[attribute.alias].dateValue);
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
                                value = refBookValue && refBookValue.stringValue && typeof refBookValue.stringValue !== 'undefined' ? refBookValue.stringValue : "";
                                break;
                            case 'NUMBER':
                                value = refBookValue && refBookValue.numberValue && typeof refBookValue.numberValue !== 'undefined' ? refBookValue.numberValue : "";
                                break;
                            case 'DATE':
                                //TODO: проверить отображение дат на стенде, локально идут со смещением в 1 день
                                value = refBookValue && refBookValue.dateValue && typeof refBookValue.dateValue !== 'undefined' ? $filter('dateFormatter')(refBookValue.dateValue) : "";
                                break;
                            case 'REFERENCE':
                                value = refBookValue && refBookValue.referenceObject && typeof refBookValue.referenceObject !== 'undefined' ? refBookValue.referenceObject[attribute.refBookAttribute.alias].value : "";
                                break;
                        }
                        return value;
                    }
                };

                /**
                 * Сохранение
                 */
                $scope.save = function () {
                    $scope.refBook.attributes.forEach(function (attribute) {
                        if (attribute.attributeType === 'REFERENCE' && $scope.record[attribute.alias] && $scope.record[attribute.alias].referenceValue) {
                            // Преобразуем ссылочные поля записи в подходящие для сервера
                            $scope.record[attribute.alias].referenceValue = $scope.record[attribute.alias].referenceValue.id;
                        }
                    });
                    $http({
                        method: "POST",
                        url: "controller/actions/refBook/" + $scope.refBook.id + "/editRecord/" + $scope.record.id.numberValue,
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