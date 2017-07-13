package com.aplana.sbrf.taxaccounting.web.paging;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

public class DefaultSortHandlerMethodArgumentResolver extends SortHandlerMethodArgumentResolver {

    @Override
    public Sort resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Sort orders = super.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
        Sort defaultSort = new Sort(Sort.Direction.DESC, "rowId");

        if (orders != null && orders.iterator().hasNext()) {
            return orders.and(defaultSort);
        } else {
            return defaultSort;
        }
    }
}
