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
            '$http', '$logPanel', 'LogEntryResource', '$dialogs', "$injector", "$compile", "$timeout",
            function ($scope, $filter, APP_CONSTANTS, $modalInstance, $shareData, $http, $logPanel, LogEntryResource, $dialogs, $injector, $compile, $timeout) {
                $scope.refBook = $shareData.refBook;
                $scope.record = $shareData.record;
                $scope.isEditMode = $shareData.mode === 'CREATE' || $shareData.mode === 'EDIT';

                /**
                 * Получает разыменованное значение атрибута
                 * @param attribute атрибут справочника
                 */
                function getAttributeValue(attribute) {
                    var value = "";
                    switch (attribute.attributeType) {
                        case 'STRING':
                            value = $scope.record[attribute.alias].stringValue ? $scope.record[attribute.alias].stringValue : "";
                            break;
                        case 'NUMBER':
                            value = $scope.record[attribute.alias].numberValue ? $scope.record[attribute.alias].numberValue : "";
                            break;
                        case 'DATE':
                            //TODO: проверить отображение дат на стенде, локально идут со смещением в 1 день
                            value = $scope.record[attribute.alias].dateValue ? $filter('dateFormatter')($scope.record[attribute.alias].dateValue) : "";
                            break;
                        case 'REFERENCE':
                            value = $scope.record[attribute.alias].referenceValue ? $scope.record[attribute.alias].referenceValue : "";
                            break;
                    }
                    return value;
                }

                /**
                 * Динамически добавляет поля в диалог в зависимости от его режима работы и типа справочника
                 */
                $scope.constructFields = function () {
                    var fieldsHtml = "";
                    $scope.refBook.attributes.forEach(function (attribute) {
                        if (attribute.visible) {
                            if ($shareData.mode === 'VIEW') {
                                fieldsHtml += "<div class=\"row-fluid\">\n" +
                                    "    <div class=\"span4\">\n" +
                                    "        <label class=\"control-label\">" + attribute.name + ":</label>\n" +
                                    "    </div>\n" +
                                    "    <div class=\"span8\">\n" +
                                    "        <label class=\"font-normal info-text\">" + getAttributeValue(attribute) + "</label>\n" +
                                    "    </div>\n" +
                                    "</div>"
                            }
                        }
                    });
                    $injector.invoke(function ($compile) {
                        var fieldsContainer = angular.element(document.querySelector("#refBookRecordFieldsContainer"));
                        var fields = $compile(fieldsHtml)(fieldsContainer.scope());
                        fieldsContainer.append(fields);
                    });
                };

                $timeout(function () {
                    // Загружаем поля через таймаут, т.к на момент старта контроллера диалог еще не открыт и контейнер для полей не существует
                    $scope.constructFields();
                }, 500);

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