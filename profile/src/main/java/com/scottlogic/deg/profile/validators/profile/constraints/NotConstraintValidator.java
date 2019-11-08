package com.scottlogic.deg.profile.validators.profile.constraints;

import com.scottlogic.deg.common.validators.ValidationResult;
import com.scottlogic.deg.profile.dtos.FieldDTO;
import com.scottlogic.deg.profile.dtos.constraints.NotConstraintDTO;
import com.scottlogic.deg.profile.validators.profile.ConstraintValidator;

import java.util.List;

public class NotConstraintValidator extends ConstraintValidator<NotConstraintDTO>
{
    public NotConstraintValidator(String rule, List<FieldDTO> fields)
    {
        super(rule, fields);
    }

    @Override
    public ValidationResult validate(NotConstraintDTO dto)
    {
        return ConstraintValidator.validateConstraint(dto.constraint, rule, fields);
    }
}
