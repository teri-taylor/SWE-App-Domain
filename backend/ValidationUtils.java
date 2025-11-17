package org.example;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class ValidationUtils {

    // accountNumber must be digits only (no decimals / letters)
    public static boolean isValidAccountNumber(String number) {
        return number != null && number.matches("\\d+");
    }

    public static boolean isValidAccountName(String name) {
        return name != null && !name.trim().isEmpty();
    }

    // monetary values limited to two decimal places
    public static boolean hasTwoDecimalPlaces(BigDecimal value) {
        if (value == null) return false;
        return value.scale() <= 2;
    }

    // formatted with commas and two decimals
    public static String formatMoney(BigDecimal value) {
        if (value == null) return "0.00";
        DecimalFormat df = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(Locale.US));
        return df.format(value);
    }
}
