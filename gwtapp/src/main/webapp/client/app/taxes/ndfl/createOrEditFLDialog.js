(function () {
    'use strict';

    /**
     * @description Модуль для для работы со страницей РНУ НДФЛ
     */

    angular.module('sbrfNdfl.createOrEditFLDialog', [])

    /**
     * @description Контроллер формы создания/редактирования ФЛ
     */
        .controller('createOrEditFLCtrl', ["$scope", "$http", "$uibModalInstance", "$alertService", "$translate", 'data', 'FormLeaveConfirmer',
            function ($scope, $http, $uibModalInstance, $alertService, $translate, data, FormLeaveConfirmer) {
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

                    if (data.mode === 'create') {
                        //Создание нового ФЛ
                        $translate('header.ndfl.fl.create').then(function (header) {
                            $scope.header = header;
                        });
                        $translate('button.add').then(function (title) {
                            $scope.buttonTitle = title;
                        });
                    } else {
                        $translate('header.ndfl.fl.edit').then(function (header) {
                            $scope.header = header;
                        });
                        $translate('button.save').then(function (title) {
                            $scope.buttonTitle = title;
                        });
                        $scope.entity = data.entity;
                        $scope.startEntity = angular.copy(data.entity);
                    }

                    // Селект выбора гражданства
                    $scope.citizenshipSelect = {
                        options: {
                            minimumResultsForSearch: -1,
                            allowClear: false,
                            formatSelection: function (citizenship) {
                                return citizenship.code;
                            },
                            formatResult: function (citizenship) {
                                return citizenship.name;
                            },
                            initSelection: function (element, callback) {
                                //данные об выбранном значении лучше брать в ng-model
                                var id = $(element).data('$ngModelController').$modelValue ? $(element).data('$ngModelController').$modelValue.id : null;
                            },
                            data: {
                                results: [
                                    {id: 1, name: 'Россия', code: 123},
                                    {id: 2, name: 'Сомали', code: 456}
                                ]
                            }
                        }
                    };
                    $scope.statusCodeSelect = {
                        options: {
                            minimumResultsForSearch: -1,
                            allowClear: false,
                            formatSelection: function (status) {
                                return status.code;
                            },
                            formatResult: function (status) {
                                return status.name;
                            },
                            initSelection: function (element, callback) {
                                var id = $(element).data('$ngModelController').$modelValue ? $(element).data('$ngModelController').$modelValue.id : null;
                            },
                            data: {
                                results: [
                                    {id: 1, code: '1', name: 'Налогоплательщик является налоговым резидентом Российской Федерации'},
                                    {id: 2, code: '2', name: 'Налогоплательщик не является налоговым резидентом Российской Федерации'}
                                ]
                            }
                        }
                    };
                    $scope.documentCodeSelect = {
                        options: {
                            minimumResultsForSearch: -1,
                            allowClear: false,
                            formatSelection: function (document) {
                                return document.code;
                            },
                            formatResult: function (document) {
                                return document.name;
                            },
                            initSelection: function (element, callback) {
                                var id = $(element).data('$ngModelController').$modelValue ? $(element).data('$ngModelController').$modelValue.id : null;
                            },
                            data: {
                                results: [
                                    {id: 1, code: '21', name: 'Паспорт гражданина Российской Федерации'},
                                    {id: 2, code: '03', name: 'Свидетельство о рождении'}
                                ]
                            }
                        }
                    };
                    $scope.subjectCodeSelect = {
                        options: {
                            minimumResultsForSearch: -1,
                            allowClear: false,
                            formatSelection: function (subject) {
                                return subject.code;
                            },
                            formatResult: function (subject) {
                                return subject.name;
                            },
                            initSelection: function (element, callback) {
                                var id = $(element).data('$ngModelController').$modelValue ? $(element).data('$ngModelController').$modelValue.id : null;
                            },
                            data: {
                                results: [
                                    {id: 1, code: '52', name: 'Нижегородская область'},
                                    {id: 2, code: '77', name: 'Московская область'}
                                ]
                            }
                        }
                    };
                    $scope.areaSelect = {
                        options: {
                            minimumResultsForSearch: -1,
                            allowClear: false,
                            formatSelection: function (area) {
                                return area.name;
                            },
                            formatResult: function (area) {
                                return area.name;
                            },
                            initSelection: function (element, callback) {
                                var id = $(element).data('$ngModelController').$modelValue ? $(element).data('$ngModelController').$modelValue.id : null;
                            },
                            data: {
                                results: [
                                    {id: 1, name: 'Ардатовский район'},
                                    {id: 2, name: 'Арзамасский район'},
                                    {id: 3,  name: 'Бронницы'},
                                    {id: 4, name: 'Дзержинский'}
                                ]
                            }
                        }
                    };
                    $scope.citySelect = {
                        options: {
                            minimumResultsForSearch: -1,
                            allowClear: false,
                            formatSelection: function (city) {
                                return city.name;
                            },
                            formatResult: function (city) {
                                return city.name;
                            },
                            initSelection: function (element, callback) {
                                var id = $(element).data('$ngModelController').$modelValue ? $(element).data('$ngModelController').$modelValue.id : null;
                            },
                            data: {
                                results: [
                                    {id: 1, name: 'Ардатов'},
                                    {id: 2, name: 'Арзамас'},
                                    {id: 3, name: 'Бронницы'},
                                    {id: 4, name: 'Дзержинский'}
                                ]
                            }
                        }
                    };
                    $scope.localitySelect = {
                        options: {
                            minimumResultsForSearch: -1,
                            allowClear: false,
                            formatSelection: function (city) {
                                return city.name;
                            },
                            formatResult: function (city) {
                                return city.name;
                            },
                            initSelection: function (element, callback) {
                                var id = $(element).data('$ngModelController').$modelValue ? $(element).data('$ngModelController').$modelValue.id : null;
                            },
                            data: {
                                results: [
                                    {id: 1, name: 'Простоквашино'}
                                ]
                            }
                        }
                    };
                    $scope.streetSelect = {
                        options: {
                            minimumResultsForSearch: -1,
                            allowClear: false,
                            formatSelection: function (city) {
                                return city.name;
                            },
                            formatResult: function (city) {
                                return city.name;
                            },
                            initSelection: function (element, callback) {
                                var id = $(element).data('$ngModelController').$modelValue ? $(element).data('$ngModelController').$modelValue.id : null;
                            },
                            data: {
                                results: [
                                    {id: 1, name: 'Ленина'},
                                    {id: 2, name: 'Красная'}
                                ]
                            }
                        }
                    };
                }

                initDialog();

                /**
                 * @description Сохранение данных
                 */
                $scope.save = function () {
                    //TODO: Send request to server for create/update data

                    FormLeaveConfirmer.clearListeners();
                    $uibModalInstance.close($scope.entity);
                };

                /**
                 * @description Проверка:Изменялись ли данные на форме
                 */
                $scope.isFormModified = function () {
                    var isModified = false;
                    if (data.mode === "create"){
                        angular.forEach($scope.entity, function (item) {
                            if (item !== undefined) {
                                isModified = true;
                            }
                        });
                    } else if (data.mode === "edit"){
                        if(!_.isEqual($scope.startEntity, $scope.entity)){
                            isModified = true;
                        }
                    }
                    return isModified;
                };

                /**
                 * @description Инициализация слушателей формы
                 */
                FormLeaveConfirmer.initializeListeners(
                    $scope.isFormModified,
                    "Отмена операции",
                    "Вы уверены, что хотите отменить создание/редактирование записи",
                    function () {
                        FormLeaveConfirmer.clearListeners();
                        $uibModalInstance.close($scope.entity);
                    },
                    undefined,
                    $uibModalInstance
                );

                /**
                 * @description Закрытие окна
                 */
                $scope.cancel = function () {
                    FormLeaveConfirmer.askSaveChanges(
                        function () {
                            $uibModalInstance.dismiss('Canceled');
                        }
                    );
                };

                /**
                 * @description Проверка значения на то, что оно является числом
                 */
                $scope.isNumber = function (value) {
                    if (!value || value === "") {
                        return true;
                    }
                    var INTEGER_REGEXP = /^\-?\d+$/;
                    return INTEGER_REGEXP.test(value)
                };
            }])
    ;
}());