(function () {
    'use strict';

    /**
     * @description Модуль для работы со формами ндфл
     */
    angular.module('app.createFormDialog', [])
        /**
         * @description Контроллер формы создания/редактирования ФЛ
         */
        .controller('createFormCtrl', ["$scope", "$http", "$uibModalInstance", "$alertService", "$translate", 'data',
            function ($scope, $http, $uibModalInstance, $alertService, $translate) {
                /**
                 * @description Инициализация первичных данных
                 */
                function initDialog() {
                    //Получаем scope из главного окна
                    $scope.parentScope = undefined;
                    try {
                        $scope.parentScope = $scope.$resolve.data.scope;
                    } catch (ex) {
                    }

                    $scope.entity = {
                        period: $scope.parentScope.periodSelect.options.data.results[0],
                        department: $scope.parentScope.departmentSelect.options.data.results[0],
                        formKind: $scope.parentScope.formKindSelect.options.data.results[0]
                    };

                    $translate('header.ndfl.form.create').then(function (header) {
                        $scope.header = header;
                    });
                }
                initDialog();

                /**
                 * @description Сохранение данных
                 */
                $scope.save = function () {
                    //TODO: Send request to server for create/update data
                    $uibModalInstance.close($scope.entity);
                };

                /**
                 * @description Закрытие окна
                 */
                $scope.cancel = function () {
                    $uibModalInstance.dismiss('Canceled');
                };
            }]);
}());