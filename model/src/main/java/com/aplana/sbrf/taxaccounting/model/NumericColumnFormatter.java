package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Используюется для форматирования значения числовых ячеек
 * Сериализация данного класса переопределена для GWT
 * @author lhaziev
 */
public class NumericColumnFormatter extends ColumnFormatter implements Serializable {
    private static final long serialVersionUID = 1645644163197913791L;

    private int precision = NumericColumn.MAX_PRECISION;
    private int maxLength = NumericColumn.MAX_LENGTH;

    public NumericColumnFormatter(int precision, int maxLength) {
        this.precision = precision;
        //this.maxLength = maxLength;
    }

    @Override
    public String format(String valueToFormat) {
        try {
            valueToFormat = valueToFormat.replace(" ", "");
            MathContext mathContext = new MathContext(maxLength);
            BigDecimal val = new BigDecimal(valueToFormat, mathContext);
            val = val.setScale(precision, RoundingMode.HALF_UP);

            boolean hasSign = (val.signum() == -1);
            if (hasSign) {
                val = val.abs();
            }
            String plainString = val.toPlainString();
            int pos = plainString.indexOf(".");
            int intLength;
            if (pos > 0) {
                intLength = pos;
            } else {
                intLength = plainString.length();
            }
            StringBuilder stringBuilder = new StringBuilder(plainString.substring(0, intLength));
            for (int i = 3; i < intLength; i += 3) {
                if (i < intLength) {
                    stringBuilder.insert(intLength - i, " ");
                }
            }
            if (precision > 0) {
                stringBuilder.append(plainString.substring(intLength, plainString.length()));
            }
            if (hasSign) {
                stringBuilder.insert(0, "-");
            }
            return stringBuilder.toString();
        } catch (NumberFormatException e) {
            return valueToFormat;
        }
    }
}
