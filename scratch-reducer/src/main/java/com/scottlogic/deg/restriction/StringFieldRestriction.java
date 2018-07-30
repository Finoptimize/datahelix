package com.scottlogic.deg.restriction;

import com.scottlogic.deg.input.Field;

import java.util.Set;

public class StringFieldRestriction implements IFieldRestriction {
    private final Field field;

    public StringFieldRestriction(Field field) {
        this.field = field;
    }

    public Set<String> among;
}
