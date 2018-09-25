(function () {
    'use strict';

    /**
     * @description Модуль для выбора записи справочника в модальном окне
     */
    angular.module('app.refBookPickerModal', ['ui.router', 'app.rest'])

    /**
     * @description Контроллер для выбора записи справочника в модальном окне
     */
        .controller('refBookPickerModalCtrl', ['$scope', '$filter', 'APP_CONSTANTS', '$modalInstance', '$shareData', 'RefBookResource', '$injector',
            function ($scope, $filter, APP_CONSTANTS, $modalInstance, $shareData, RefBookResource, $injector) {
                $scope.refBookId = $shareData.refBookId;
                $scope.filter = '';
                if ($shareData.filter) {
                    $scope.filter = $shareData.filter;
                }
                $scope.temp = {
                    selectedRecord: null
                };

                RefBookResource.query({
                    id: $scope.refBookId
                }, function (data) {
                    $scope.refBook = data;
                    $scope.title = $scope.refBook.name;

                    // Добавляем выпадашку на страницу, после того как получена информация о справочнике
                    $injector.invoke(function ($compile) {
                        var parent = angular.element(document.querySelector("#selectContainer"));
                        var select = $compile("<div style=\"width: 100%\" data-ui-select2=\"select.options\" data-ng-controller=\"SelectRefBookCtrl\"\n" +
                            "         data-ng-init=\"initSelect(refBook.id, null, filter)\"\n" +
                            "         data-ng-model=\"temp.selectedRecord\"></div>")(parent.scope());
                        parent.append(select);
                    });
                });

                /**
                 * @description закрытие модального окна
                 */
                $scope.save = function () {
                    $modalInstance.close($scope.temp.selectedRecord);
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
                    $modalInstance.close();
                };
            }
        ]);
}());