(function () {
    'use strict';

    /**
     * @description Модуль для обработки и регистрации событий над элементами справочников
     * @author Sergey Molokovskikh
     */
    angular.module('app.refBookInterceptors', ['aplana.modal.dialogs'])
        .config(function ($provide) {

            /**
             * Типы событий
             * @type {{beforeSaveRecord: boolean, onSaveRecord: boolean, onCloseWindowRecord: boolean}}
             * @private
             */
            var _eventTypes = {
                beforeSaveRecord: true,
                onSaveRecord: true,
                onCloseWindowRecord: true
            };

            /**
             * Подписчики
             * @type {{}}
             * @private
             */
            var _subscribes = {};

            /**
             * Проверка на правильность заполнения типа подписчика
             * @param eventType
             * @private
             */
            function _checkSubscribeType(eventType) {
                if (!_eventTypes[eventType]) {
                    throw new Error("Неправильный тип события элемента справочника для подписки");
                }
            }

            /**
             * Пордписка на событие
             * @param eventType
             * @param subscribeFn
             */
            function _subscribe(eventType, subscribeFn) {
                _checkSubscribeType(eventType);

                var subscribesByEventType = _subscribes[eventType] || [];
                _subscribes[eventType] = subscribesByEventType;

                subscribesByEventType.push(subscribeFn);
            }

            /**
             * Обработка событий элемента справочника
             */


            /**
             * Отправка данных подписчикам
             * @param eventData Данные события
             * @param eventType Тип события
             */
            function fireEvent($q, eventData, eventType) {
                var subscribes = _subscribes[eventType];
                if (!subscribes) {
                    //нет подписчков
                    return $q.resolve();
                }
                //Каждому подписчику сообщим о событии и доставим связанные данные
                if (subscribes instanceof Array) {
                    var asyncSubscribes = subscribes.map(function (subscribe) {
                        return $q.resolve(subscribe(eventData));
                    });
                    return $q.all(asyncSubscribes);
                }
            }


            /**
             * Сервис перхватчиков событий над элементами справочника
             * @constructor
             */
            var RefBookInterceptors = function ($q) {
                this.$q = $q;
            };

            RefBookInterceptors.prototype.getEventTypes = function () {
                return _eventTypes;
            };

            RefBookInterceptors.prototype.subscribe = _subscribe;

            RefBookInterceptors.prototype.beforeSaveRecord = function (eventData) {
                return fireEvent(this.$q, eventData, 'beforeSaveRecord');
            };

            RefBookInterceptors.prototype.onSaveRecord = function (eventData) {
                return fireEvent(this.$q, eventData, 'onSaveRecord');
            };

            RefBookInterceptors.prototype.onCloseWindowRecord = function (eventData) {
                return fireEvent(this.$q, eventData, 'onCloseWindowRecord');
            };

            $provide.service('$refBookInterceptors', RefBookInterceptors);

        });
})();
