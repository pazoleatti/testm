//SBRFEDOFNS
//Метод для изменения ширины колонки грида
//Взят тут: http://stackoverflow.com/questions/20012365/how-to-adjust-the-column-width-of-jqgrid-after-the-data-is-loaded
(function ($) {
    "use strict";
    $.jgrid.extend({
        setColWidth: function (iCol, newWidth, adjustGridWidth) {
            return this.each(function () {
                var $self = $(this), grid = this.grid, p = this.p, colName, colModel = p.colModel, i, nCol;
                if (typeof iCol === "string") {
                    // the first parametrer is column name instead of index
                    colName = iCol;
                    for (i = 0, nCol = colModel.length; i < nCol; i++) {
                        if (colModel[i].name === colName) {
                            iCol = i;
                            break;
                        }
                    }
                    if (i >= nCol) {
                        return; // error: non-existing column name specified as the first parameter
                    }
                } else if (typeof iCol !== "number") {
                    return; // error: wrong parameters
                }
                grid.resizing = { idx: iCol };
                grid.headers[iCol].newWidth = newWidth;

                //Ставим 1, чтобы он использовал ширину, которую сам рассчитает. 0 не выставлять
                grid.newWidth = 1;

                grid.dragEnd();   // adjust column width
                if (adjustGridWidth !== false) {
                    $self.jqGrid("setGridWidth", grid.newWidth, false); // adjust grid width too
                }
            });
        }
    });
}(jQuery));
