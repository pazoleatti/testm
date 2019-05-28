/**
 * Изменяет скроллинг колесиком, чтобы событие не передавалось на родительский элемент
 */
var stopOuterScroll = function (element) {
    element[0].addEventListener('wheel', wheelHandler);

    function wheelHandler(event) {
        var scrollHeight = this.scrollHeight;
        var height = this.clientHeight;
        var delta = event.deltaY;

        var prevent = function () {
            event.stopPropagation();
            event.preventDefault();
            event.cancelBubble = true;  // IE events
            event.returnValue = false;  // IE events
        };
        if (scrollHeight > height) {
            event.currentTarget.scrollTop += sign(delta) * event.currentTarget.offsetHeight * 0.15;
            return prevent();
        }
    }
    function sign(x) {
        return ((x > 0) - (x < 0)) || +x;
    }
};

