(function () {
    'use strict';

    /**
     * @description Модуль для редактирования общих параметров'
     */

    angular.module('app.editParams', ['ui.router', 'app.rest', 'app.logPanel'])

    /**
     * @description Модуль для редактирования общих параметров'
     */
        .controller('editParamsCtrl', ['$scope', '$filter', '$http', '$modalInstance', 'parameter', '$logPanel','$dialogs',
            function ($scope, $filter, $http, $modalInstance, parameter, $logPanel, $dialogs) {

                /**
                 * @description Редактирование параметра
                 */
                $scope.save = function () {

                    $http({
                        method: "POST",
                        url: "controller/actions/editCommonParams",
                        params: {
                            config: $scope.parameter
                        }
                    }).then(function() {
                        $modalInstance.close('Canceled');
                    });
                };

                /**
                 * @description Переменная содержащая значения редактируемое значение
                 */
                $scope.parameter = parameter;

                /**
                 * @description Закрытие окна
                 */
                $scope.close = function () {
                    $dialogs.confirmDialog({
                        title: $filter('translate')('title.cancelChanges'),
                        content: $filter('translate')('commonParams.notSavedData'),
                        okBtnCaption: $filter('translate')('common.button.yes'),
                        cancelBtnCaption: $filter('translate')('DIALOGS_CANCELLATION'),
                        okBtnClick: function () {
                            $modalInstance.dismiss('Canceled');
                        }
                    });
                };

            }]);

}());

