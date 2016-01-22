/**
 * Не позволяет выводить сообщение для IE8 об остановке выполнения сценария
 * @type {number}
 */
var i = 0;
(function () {
    for (; i < 6000000; i++) {
        if (i > 0 && i % 100000 == 0) {
            i++;
            window.setTimeout(arguments.callee);
            break;
        }
    }
})();
