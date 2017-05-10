(function() {
    'use strict'

    // https://developer.mozilla.org/ru/docs/Web/JavaScript/Reference/Global_Objects/String/startsWith
    // ECMAScript 6 (IE 11-, Chrome 41+, FF 17+)
    if (!String.prototype.startsWith) {
        Object.defineProperty(String.prototype, 'startsWith', {
            enumerable: false,
            configurable: false,
            writable: false,
            value: function (searchString, position) {
                position = position || 0;
                return this.lastIndexOf(searchString, position) === position;
            }
        });
    }
    if (!String.prototype.endsWith) {
        Object.defineProperty(String.prototype, 'endsWith', {
            value: function(searchString, position) {
                var subjectString = this.toString();
                if (position === undefined || position > subjectString.length) {
                    position = subjectString.length;
                }
                position -= searchString.length;
                var lastIndex = subjectString.indexOf(searchString, position);
                return lastIndex !== -1 && lastIndex === position;
            }
        });
    }

    if (!Date.prototype.getWeek) {
        Date.prototype.getWeek = function () {
            var target = new Date(this.valueOf());
            var dayNr = (this.getDay() + 6) % 7;
            target.setDate(target.getDate() - dayNr + 3);
            var firstThursday = target.valueOf();
            target.setMonth(0, 1);
            if (target.getDay() != 4) {
                target.setMonth(0, 1 + ((4 - target.getDay()) + 7) % 7);
            }
            return 1 + Math.ceil((firstThursday - target) / 604800000); // 604800000 = 7 * 24 * 3600 * 1000
        }
    }
}())