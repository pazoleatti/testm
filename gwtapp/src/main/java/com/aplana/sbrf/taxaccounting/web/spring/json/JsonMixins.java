package com.aplana.sbrf.taxaccounting.web.spring.json;

import java.lang.annotation.*;

/**
 * Annotation that can be used to specify custom JSON filters for a HTTP request handler.
 * It can be used to ignore some properties when serializing Java beans to JSON strings only
 * for this particular handler.
 * <p>
 * Example:
 * <pre>
 * &#064;JsonIgnoreProperties({ "prop1", "prop2" })
 * public interface BeanFilter {
 * }
 * ...
 * &#064;JsonFilters(&#064;JsonFilter(target = Bean.class, mixinSource = BeanFilter.class))
 * public &#064;RequestBody getBean() {
 * 	...
 * }
 * </pre>
 * </p>
 *
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 * @see com.fasterxml.jackson.annotation.JsonIgnoreProperties
 * @see com.fasterxml.jackson.databind.ObjectMapper#addMixInAnnotations(Class, Class)
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonMixins {
    /**
     * Definition of a JSON mixin for a particular Java bean class.
     */
    @interface JsonMixin {
        /**
         * Class whose properties should be mixed out.
         */
        Class<?> target();

        /**
         * Class or interface whose annotations should be added to target's annotations.
         */
        Class<?> mixinSource();

        /**
         * Addition params for property
         */
        JsonAdditionParams[] additionParams() default {};
    }

    /**
     * Addition params
     */
    @interface JsonAdditionParams {
        /**
         * The name of the property
         */
        String property();

        /**
         * The property max-length. (Applies only if a string-valued property is used.)
         */
        int maxLength() default -1;
    }

    /**
     * Sets additional JSON mixins.
     */
    JsonMixin[] value() default {};
}
