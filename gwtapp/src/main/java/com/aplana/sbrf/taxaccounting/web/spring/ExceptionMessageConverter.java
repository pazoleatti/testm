package com.aplana.sbrf.taxaccounting.web.spring;

import com.aplana.sbrf.taxaccounting.model.error.ExceptionMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * Своя реализация {@link org.springframework.http.converter.HttpMessageConverter} для {@link ExceptionMessage}, вовзвращяемый при возникновении исключения.
 * Будет всегда писать в виде json независимо от значения {@link MediaType} определенного в {@link org.springframework.web.bind.annotation.RequestMapping}.
 *
 * Например, если имеем "GetMapping(value = "...", produces = MediaType.IMAGE_PNG_VALUE)", то spring не сможет записать {@link ExceptionMessage} в ответе запроса и
 * {@link org.springframework.web.bind.annotation.ExceptionHandler} не отработает как надо, в ответ получим html.
 */
public class ExceptionMessageConverter extends MappingJackson2HttpMessageConverter {

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return clazz == ExceptionMessage.class;
    }
}
