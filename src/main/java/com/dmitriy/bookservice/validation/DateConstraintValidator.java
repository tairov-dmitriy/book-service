package com.dmitriy.bookservice.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Date;

public class DateConstraintValidator implements ConstraintValidator<DateConstraint, Date> {
    @Override
    public boolean isValid(Date date, ConstraintValidatorContext constraintValidatorContext) {
        return date == null || !date.after(new Date());
    }
}