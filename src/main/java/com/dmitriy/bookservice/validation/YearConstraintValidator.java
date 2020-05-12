package com.dmitriy.bookservice.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Calendar;

public class YearConstraintValidator implements ConstraintValidator<YearConstraint, Integer> {
    @Override
    public boolean isValid(Integer year, ConstraintValidatorContext constraintValidatorContext) {
        return year > -4000 && year != 0 && year <= Calendar.getInstance().get(Calendar.YEAR);
    }
}
