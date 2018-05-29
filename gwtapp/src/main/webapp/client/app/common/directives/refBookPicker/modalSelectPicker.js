(function () {
    'use strict';

    /**
     * @description Модуль для выбора записи справочника в модальном окне
     */
    angular.module('app.refBookPickerModal', ['ui.router', 'app.rest'])

    /**
     * @description Контроллер для выбора записи справочника в модальном окне
     */
        .controller('refBookPickerModalCtrl', ['$scope', '$filter', 'APP_CONSTANTS', '$modalInstance', '$shareData',
            function ($scope, $filter, APP_CONSTANTS, $modalInstance, $shareData) {
                $scope.refBook = $shareData.refBook;
                $scope.temp = {
                    selectedRecord: null
                };

                /**
                 * Событие изменения значения в выпадашке. Если было выбрано какое то значение - закрываем диалог
                 */
                $scope.$watch("temp.selectedRecord", function (newValue, oldValue) {
                    if (newValue !== null) {
                        $modalInstance.close(newValue);
                    }
                });

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