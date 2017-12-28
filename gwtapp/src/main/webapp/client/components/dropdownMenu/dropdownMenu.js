/**
 * aplana-dropdownMenu(Выпадающее главное меню)
 * Директива aplana-dropdownMenu служит для отображения кнопки с выпадающим меню
 * http://localhost:8080/#/aplana_dropdownMenu
 */
(function () {
    'use strict';
    angular.module('aplana.dropdownMenu', ['aplana.utils'])
        // родительская директива содержит в себе набор отдельных элементов меню
        // используется для синхронизации открытия/закрытия выпадающих элементов
        .directive('aplanaDropdownMenu', function() {
            return {
                restrict: 'A',
                replace: true,
                transclude: true,
                template: '<span ng-transclude></span>',
                controller: function() {
                    // список элементов меню. указатели на скоуп элементов
                    var items = [];

                    // закрыть все всплывающие окна
                    this.closeAll = function(selected) {
                        angular.forEach(items, function(item) {
                            if (selected !== item) {
                                item.element.removeClass('open').removeClass('cbr-page-menu__item_active');
                            }
                        });
                    };

                    // добавляет элемент в список
                    this.addItem = function(item) {
                        items.push(item);
                    };

                    // обработчик клик на любом месте, чтобы закрыть менюшку
                    $(document).click(function(event) {
                        if ($(event.target).closest('.cbr-page-menu__item.dropdown').length) {
                            return;
                        }
                        var el = $(".cbr-page-menu__item.dropdown.open");
                        if ((el) && (el.length > 0)){
                            angular.forEach(el, function(item){
                                $(item).removeClass('open').removeClass('cbr-page-menu__item_active');
                            });
                        }
                        event.stopPropagation();
                    });
                }
            };
        })

        // директива отдельный элемент меню, содержит в вёрстке выпадающую часть, по которым и происходит переход
        .directive('aplanaDropdownMenuTopItem', [
            'AplanaUtils',
            function (AplanaUtils) {
                return {
                    restrict: 'A',
                    scope:{
                        items: '=', // массив с элементами в выпадающей части
                        title: '=', // наименование элемента
                        rigthAlign: '=',  // выпадающая часть выравнивается слева или справа, по умолчанию слева,
                        model: '=' // общий объект для хранения значений чекбоксов
                    },
                    transclude: true,
                    replace: true,
                    require: '^?aplanaDropdownMenu',
                    templateUrl: AplanaUtils.templatePath + 'dropdownMenu/dropdownMenuTopItem.html',
                    link: function(scope, iElement, iAttrs, mainMenuCtrl) {
                        // показывает менюшку всплывающую
                        scope.showDropdown = function () {
                            mainMenuCtrl.closeAll(scope);
                            iElement.toggleClass('open').toggleClass('cbr-page-menu__item_active');
                        };
                        // сохранение элемента в скоупе, чтобы обработать в родительской директиве
                        scope.element = iElement;
                        // сохраняем ссылку в родительской директиве
                        mainMenuCtrl.addItem(scope);

                        // обработчик клика на элементе меню
                        scope.onClick = function(func) {
                            if (func) {
                                scope.$parent.$eval(func);
                            }
                            scope.element.removeClass('open').removeClass('cbr-page-menu__item_active');
                        };

                        // viewPermission - отображается или нет элемент меню
                        angular.forEach(scope.items, function (item) {
                            if (typeof item.permission === 'boolean') {
                                item.viewPermission = item.permission;
                            } else if (item.permission) {
                                item.permission.then(function (result) {
                                    item.viewPermission = result.permission;
                                });
                            }
                        });

                    }
                };
            }
        ])
    ;
}());