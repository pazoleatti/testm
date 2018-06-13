(function () {
    'use strict';

    /**
     * @description Модуль, содержащий директивы для работы с диалогом выбора из справочника
     */

    angular.module('app.refBookPicker', ['app.refBookPickerModal'])
    /**
     * @description Директива для выбора записи из выпадашки
     */
        .directive('refBookPicker', ['$aplanaModal', '$filter', function ($aplanaModal, $filter) {
            return {
                restrict: "E",
                require: 'ngModel',
                replace: true,
                templateUrl: 'client/app/common/directives/refBookPicker/refBookPicker.html?v=${buildUuid}',
                scope: {
                    ngModel: '=',
                    buttonTitle: '@',
                    refBookId: '@',
                    onSelect: '&'
                },
                link: function (scope, element, attr) {
                    if (!angular.isDefined(scope.buttonTitle)) {
                        scope.buttonTitle = $filter('translate')('button.select');
                    }

                    scope.openSelectDialog = function () {
                        $aplanaModal.open({
                            templateUrl: 'client/app/common/directives/refBookPicker/modalSelectPicker.html?v=${buildUuid}',
                            controller: 'refBookPickerModalCtrl',
                            windowClass: 'modal800',
                            resolve: {
                                $shareData: function () {
                                    return {
                                        refBookId: scope.refBookId
                                    };
                                }
                            }
                        }).result.then(function (selectedRecord) {
                            if (selectedRecord) {
                                scope.ngModel = selectedRecord;
                                if (angular.isDefined(scope.onSelect)) {
                                    scope.onSelect({selectedRecord: selectedRecord});
                                }
                            }
                        });
                    }
                }
            };
        }]);
}());