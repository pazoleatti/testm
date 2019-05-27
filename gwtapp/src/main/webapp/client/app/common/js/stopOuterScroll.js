/**
 * Изменяет скроллинг колесиком, чтобы событие не передавалось на родительский элемент
 */
var stopOuterScroll = function (element) {
    element[0].addEventListener('wheel', wheelHandler);

    function wheelHandler(event) {
        var $this = $(this);
        var scrollTop = this.scrollTop;
        var scrollHeight = this.scrollHeight;
        var height = this.clientHeight;
        var delta = event.deltaY;
        var up = delta < 0;

        var prevent = function () {
            event.stopPropagation();
            event.preventDefault();
            event.cancelBubble = true;  // IE events
            event.returnValue = false;  // IE events
        };
        if (!up && scrollHeight - height - scrollTop === 0) {
            $this.scrollTop(scrollHeight);
            return prevent();
        } else if (up && scrollTop === 0) {
            $this.scrollTop(0);
            return prevent();
        }
    }
};

