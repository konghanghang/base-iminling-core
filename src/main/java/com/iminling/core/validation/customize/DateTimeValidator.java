package com.iminling.core.validation.customize;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DateTimeValidator implements ConstraintValidator<DateTimeValid, String> {

    private DateTimeValid dateTimeValid;

    @Override
    public void initialize(DateTimeValid constraintAnnotation) {
        this.dateTimeValid = constraintAnnotation;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 如果 value 为空则不进行格式验证，为空验证可以使用 @NotBlank @NotNull @NotEmpty 等注解来进行控制，职责分离
        if (value == null) {
            return true;
        }
        String format = dateTimeValid.format();
        if (value.length() != format.length()) {
            return false;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        try {
            simpleDateFormat.parse(value);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }


}
