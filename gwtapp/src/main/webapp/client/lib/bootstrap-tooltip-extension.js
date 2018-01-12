/* ===========================================================
 * bootstrap-tooltip-extension.js v0.0.2
 * https://github.com/andresgutgon/bootstrap-tooltip-extension
 * ===========================================================
 *
 * This file extends bootstrap-tooltip.js that add
 * More tooltip positions 'bottom-left', 'bottom-right', 'top-left' and 'top-right'
 *
 * =========================================================== */

(function ($) {
    var old, oldPopover,
        TooltipExtension = function (element, options) {
            this.init('tooltip', element, options);
        },
        PopoverExtension = function (element, options) {
            this.init('popover', element, options)
        };

    function setPosition($tip, placement, pos) {
        var actualWidth,
            actualHeight,
            tp;

        actualWidth = $tip[0].offsetWidth;
        actualHeight = $tip[0].offsetHeight;

        switch (placement) {
            case 'bottom':
                tp = {top: pos.top + pos.height, left: pos.left + pos.width / 2 - actualWidth / 2};
                break;
            case 'top':
                tp = {top: pos.top - actualHeight, left: pos.left + pos.width / 2 - actualWidth / 2};
                break;
            case 'left':
                tp = {top: pos.top + pos.height / 2 - actualHeight / 2, left: pos.left - actualWidth};
                break;
            case 'right':
                tp = {top: pos.top + pos.height / 2 - actualHeight / 2, left: pos.left + pos.width};
                break;
            // Extra positions. This are not part of bootstrap
            // Extrange:
            //  I've to make top position 10px smaller in 'top-left' and 'top-right'
            //  And 10px bigger in 'bottom-left' and 'bottom-right'
            // This should behave like 'top' and 'bottom'. But they don't.
            case 'bottom-left':
                tp = {top: pos.top + pos.height + 10, left: pos.left};
                if (this.$element.outerWidth() <= 18) { // if button is small move tooltip left
                    tp.left -= 4;
                }
                break;
            case 'bottom-right':
                tp = {top: pos.top + pos.height + 10, left: pos.left + pos.width - actualWidth};
                if (this.$element.outerWidth() <= 18) { // if button is small move tooltip left
                    tp.left += 4;
                }
                break;
            case 'top-left':
                tp = {top: pos.top - actualHeight - 10, left: pos.left };
                if (this.$element.outerWidth() <= 18) { // if button is small move tooltip left
                    tp.left -= 4;
                }
                break;
            case 'top-right':
                tp = {top: pos.top - actualHeight - 10, left: pos.left + pos.width - actualWidth};
                if (this.$element.outerWidth() <= 18) { // if button is small move tooltip left
                    tp.left += 4;
                }
                break;
        }
        return tp;
    }

    TooltipExtension.prototype = $.extend({}, $.fn.tooltip.Constructor.prototype, {
        constructor: TooltipExtension, show: function () {
            var $tip,
                pos,
                placement,
                tp,
                e = $.Event('show');

            if (this.hasContent() && this.enabled) {
                this.$element.trigger(e);
                if (e.isDefaultPrevented()) {
                    return;
                }
                $tip = this.tip();
                this.setContent();

                if (this.options.animation) {
                    $tip.addClass('fade');
                }

                placement = typeof this.options.placement === 'function' ?
                    this.options.placement.call(this, $tip[0], this.$element[0]) :
                    this.options.placement;

                $tip
                    .detach()
                    .css({ top: 0, left: 0, display: 'block' });

                if (this.options.container) {
                    $tip.appendTo(this.options.container);
                } else {
                    $tip.insertAfter(this.$element);
                }

                pos = this.getPosition();

                tp = setPosition.call(this, $tip, placement, pos);

                this.applyPlacement(tp, placement);

                if (this.options.moveArrow) {
                    this.moveArrow();
                }

                this.$element.trigger('shown');
            }
        },
        /**
         * Calculate arrow position relative to button width.
         */
        moveArrow: function () {
            var placement = this.options.placement
                , button = this.$element
                , template = $(this.options.template)
                , $arrow = this.tip().find(".tooltip-arrow, .arrow")
                , arrow_width = parseInt($arrow.css("width"), 10) // This is needed we get here Ex.: '18px'
                , new_arrow_position = (button.outerWidth() / 2) - (arrow_width / 2);
            switch (placement) {
                case 'bottom-left':
                    $arrow.css("left", new_arrow_position);
                    break;
                case 'bottom-right':
                    $arrow.css("right", new_arrow_position);
                    break;
                case 'top-left':
                    $arrow.css("left", new_arrow_position);
                    break;
                case 'top-right':
                    $arrow.css("right", new_arrow_position);
                    break;
                case 'right':
                    $arrow.css("right", new_arrow_position);
                    break;
            }
        },
        updatePosition: function () {
            var placement,
                pos,
                tp;
            if (this.hasContent() && this.enabled) {
                var $tip = this.tip();

                pos = this.getPositionOnly();

                placement = typeof this.options.placement === 'function' ?
                    this.options.placement.call(this, $tip[0], this.$element[0]) :
                    this.options.placement;

                tp = setPosition.call(this, $tip, placement, pos);
                $tip.offset(tp);
            }
        }
    });

    /* TOOLTIP EXTRA PLUGIN DEFINITION
     * ========================= */

    old = $.fn.tooltip;

    $.fn.tooltip = function (option) {
        return this.each(function () {
            var $this = $(this),
                data = $this.data('tooltip'),
                options = typeof option === 'object' && option;
            if (!data) {
                $this.data('tooltip', (data = new TooltipExtension(this, options)));
            }
            if (typeof option === 'string') {
                data[option]();
            }
        });
    };

    $.fn.tooltip.Constructor = TooltipExtension;

    $.fn.tooltip.defaults = {
        animation: true, placement: 'top', selector: false, template: '<div class="tooltip"><div class="tooltip-arrow"></div><div class="tooltip-inner"></div></div>', trigger: 'hover focus', title: '', delay: 0, html: false, container: false
    };

    /* TOOLTIP EXTRA NO CONFLICT
     * =================== */

    $.fn.tooltip.noConflict = function () {
        $.fn.tooltip = old;
        return this;
    };

    //поповер нужно переопределить, чтобы он подцепил новый тултип
    PopoverExtension.prototype = $.extend({}, $.fn.tooltip.Constructor.prototype, {

        constructor: PopoverExtension, setContent: function () {
            var $tip = this.tip(),
                title = this.getTitle(),
                content = this.getContent();

            $tip.find('.popover-title')[this.options.html ? 'html' : 'text'](title);
            $tip.find('.popover-content')[this.options.html ? 'html' : 'text'](content);

            $tip.removeClass('fade top bottom left right in');
        }, hasContent: function () {
            return this.getTitle() || this.getContent();
        }, getContent: function () {
            var content,
                $e = this.$element,
                o = this.options;

            content = (typeof o.content == 'function' ? o.content.call($e[0]) : o.content) || $e.attr('data-content');

            return content;
        }, tip: function () {
            if (!this.$tip) {
                this.$tip = $(this.options.template)
            }
            return this.$tip
        }, destroy: function () {
            this.hide().$element.off('.' + this.type).removeData(this.type)
        }
    });

    /* POPOVER EXTRA PLUGIN DEFINITION
     * ========================= */

    oldPopover = $.fn.popover;

    $.fn.popover = function (option) {
        return this.each(function () {
            var $this = $(this),
                data = $this.data('popover'),
                options = typeof option === 'object' && option;
            if (!data) {
                $this.data('popover', (data = new PopoverExtension(this, options)));
            }
            if (typeof option === 'string') {
                data[option]();
            }
        });
    };

    $.fn.popover.Constructor = PopoverExtension;

    $.fn.popover.defaults = $.extend({}, $.fn.tooltip.defaults, {
        placement: 'right', trigger: 'click', content: '', template: '<div class="popover"><div class="arrow"></div><h3 class="popover-title"></h3><div class="popover-content"></div></div>'
    });

    /* POPOVER EXTRA NO CONFLICT
     * =================== */

    $.fn.popover.noConflict = function () {
        $.fn.popover = oldPopover;
        return this;
    };


})(window.jQuery);