package com.scottlogic.deg.generator.reducer;

import com.scottlogic.deg.generator.Field;
import com.scottlogic.deg.generator.ProfileFields;
import com.scottlogic.deg.generator.inputs.RuleInformation;
import com.scottlogic.deg.generator.constraints.atomic.*;
import com.scottlogic.deg.generator.restrictions.*;
import com.scottlogic.deg.schemas.v3.RuleDTO;
import org.hamcrest.Matchers;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNull;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.*;

class ConstraintReducerTest {

    private final ConstraintReducer constraintReducer = new ConstraintReducer(
            new FieldSpecFactory(),
            new FieldSpecMerger()
    );

    @Test
    void shouldProduceCorrectFieldSpecsForExample() {
        // ARRANGE
        final Field quantityField = new Field("quantity");
        final Field countryField = new Field("country");
        final Field cityField = new Field("city");

        ProfileFields fieldList = new ProfileFields(
                Arrays.asList(quantityField, countryField, cityField));

        final Set<Object> countryAmong = new HashSet<>(Arrays.asList("UK", "US"));

        final List<AtomicConstraint> constraints = Arrays.asList(
                new IsGreaterThanConstantConstraint(quantityField, 0, rule()),
                new IsGreaterThanConstantConstraint(quantityField, 5, rule()).negate(),
                new IsInSetConstraint(countryField, countryAmong, rule()),
                new IsOfTypeConstraint(cityField, IsOfTypeConstraint.Types.STRING, rule()));

        // ACT
        final RowSpec reducedConstraints = constraintReducer.reduceConstraintsToRowSpec(
                fieldList,
                constraints).get();

        // ASSERT
        FieldSpec quantityFieldSpec = reducedConstraints.getSpecForField(quantityField);
        Assert.assertThat("Quantity fieldspec has no set restrictions", quantityFieldSpec.getSetRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Quantity fieldspec has no string restrictions", quantityFieldSpec.getStringRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Quantity fieldspec has no null restrictions", quantityFieldSpec.getNullRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has a Numeric type constraint",
                quantityFieldSpec.getTypeRestrictions().getAllowedTypes(),
                equalTo(Collections.singleton(IsOfTypeConstraint.Types.NUMERIC)));
        Assert.assertThat("Quantity fieldspec has no datetime restrictions",
                quantityFieldSpec.getDateTimeRestrictions(), Is.is(IsNull.nullValue()));
        Assert.assertThat("Quantity fieldspec has numeric restrictions", quantityFieldSpec.getNumericRestrictions(),
                Is.is(IsNull.notNullValue()));
        Assert.assertThat("Quantity fieldspec has correct lower bound limit",
                quantityFieldSpec.getNumericRestrictions().min.getLimit(), Is.is(BigDecimal.ZERO));
        Assert.assertThat("Quantity fieldspec has exclusive lower bound",
                quantityFieldSpec.getNumericRestrictions().min.isInclusive(), Is.is(false));
        Assert.assertThat("Quantity fieldspec has correct upper bound limit",
                quantityFieldSpec.getNumericRestrictions().max.getLimit(), Is.is(BigDecimal.valueOf(5)));
        Assert.assertThat("Quantity fieldspec has inclusive upper bound",
                quantityFieldSpec.getNumericRestrictions().max.isInclusive(), Is.is(true));

        FieldSpec countryFieldSpec = reducedConstraints.getSpecForField(countryField);
        Assert.assertThat("Country fieldspec has no string restrictions", countryFieldSpec.getStringRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Country fieldspec has no null restrictions", countryFieldSpec.getNullRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has a Numeric type constraint",
                quantityFieldSpec.getTypeRestrictions().getAllowedTypes(),
                equalTo(Collections.singleton(IsOfTypeConstraint.Types.NUMERIC)));
        Assert.assertThat("Country fieldspec has no datetime restrictions",
                countryFieldSpec.getDateTimeRestrictions(), Is.is(IsNull.nullValue()));
        Assert.assertThat("Country fieldspec has no numeric restrictions",
                countryFieldSpec.getNumericRestrictions(), Is.is(IsNull.nullValue()));
        Assert.assertThat("Country fieldspec has set restrictions", countryFieldSpec.getSetRestrictions(),
                Is.is(IsNull.notNullValue()));
        Assert.assertThat("Country fieldspec set restrictions have no blacklist",
                countryFieldSpec.getSetRestrictions().getBlacklist(), anyOf(nullValue(), empty()));
        Assert.assertThat("Country fieldspec set restrictions have whitelist",
                countryFieldSpec.getSetRestrictions().getWhitelist(), notNullValue());
        Assert.assertThat("Country fieldspec set restrictions whitelist has correct size",
                countryFieldSpec.getSetRestrictions().getWhitelist().size(), Is.is(2));
        Assert.assertThat("Country fieldspec set restrictions whitelist contains 'UK'",
                countryFieldSpec.getSetRestrictions().getWhitelist().contains("UK"), Is.is(true));
        Assert.assertThat("Country fieldspec set restrictions whitelist contains 'US'",
                countryFieldSpec.getSetRestrictions().getWhitelist().contains("US"), Is.is(true));

        FieldSpec cityFieldSpec = reducedConstraints.getSpecForField(cityField);
        Assert.assertThat("City fieldspec has no set restrictions", cityFieldSpec.getSetRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("City fieldspec has no string restrictions", cityFieldSpec.getStringRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("City fieldspec has no null restrictions", cityFieldSpec.getNullRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("City fieldspec has no datetime restrictions", cityFieldSpec.getDateTimeRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("City fieldspec has no numeric restrictions", cityFieldSpec.getNumericRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("City fieldspec has type restrictions", cityFieldSpec.getTypeRestrictions(),
                Is.is(IsNull.notNullValue()));
        Assert.assertThat(
                "City fieldspec has string type restriction",
                cityFieldSpec.getTypeRestrictions().getAllowedTypes(),
                IsEqual.equalTo(Collections.singleton(IsOfTypeConstraint.Types.STRING)));
    }

    @Test
    void shouldReduceIsGreaterThanConstantConstraint() {
        final Field field = new Field("test0");
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
                new IsGreaterThanConstantConstraint(field, 5, rule()));

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a numeric type constraint", outputSpec.getTypeRestrictions().getAllowedTypes(),
                equalTo(Collections.singleton(IsOfTypeConstraint.Types.NUMERIC)));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no string restrictions", outputSpec.getStringRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no datetime restrictions", outputSpec.getDateTimeRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has numeric restrictions", outputSpec.getNumericRestrictions(),
                Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec numeric restrictions have no upper bound", outputSpec.getNumericRestrictions().max,
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec numeric restrictions have lower bound", outputSpec.getNumericRestrictions().min,
                Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec numeric restriction lower bound is correct",
                outputSpec.getNumericRestrictions().min.getLimit(), Is.is(BigDecimal.valueOf(5)));
        Assert.assertThat("Fieldspec numeric restriction lower bound is exclusive",
                outputSpec.getNumericRestrictions().min.isInclusive(), Is.is(false));
    }

    @Test
    void shouldReduceNegatedIsGreaterThanConstantConstraint() {
        final Field field = new Field("test0");
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
                new IsGreaterThanConstantConstraint(field, 5, rule()).negate());

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a Numeric type constraint",
                outputSpec.getTypeRestrictions().getAllowedTypes(),
                equalTo(Collections.singleton(IsOfTypeConstraint.Types.NUMERIC)));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no string restrictions", outputSpec.getStringRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no datetime restrictions", outputSpec.getDateTimeRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has numeric restrictions", outputSpec.getNumericRestrictions(),
                Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec numeric restrictions have no lower bound",
                outputSpec.getNumericRestrictions().min, Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec numeric restrictions have upper bound",
                outputSpec.getNumericRestrictions().max, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec numeric restrictions upper bound limit is correct",
                outputSpec.getNumericRestrictions().max.getLimit(), Is.is(BigDecimal.valueOf(5)));
        Assert.assertThat("Fieldspec numeric resitrctions upper bound is inclusive",
                outputSpec.getNumericRestrictions().max.isInclusive(), Is.is(true));
    }

    @Test
    void shouldReduceIsGreaterThanOrEqualToConstantConstraint() {
        final Field field = new Field("test0");
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
                new IsGreaterThanOrEqualToConstantConstraint(field, 5, rule()));

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a numeric type restriction", outputSpec.getTypeRestrictions().getAllowedTypes(),
                equalTo(Collections.singleton(IsOfTypeConstraint.Types.NUMERIC)));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no string restrictions", outputSpec.getStringRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no datetime restrictions", outputSpec.getDateTimeRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has numeric restrictions", outputSpec.getNumericRestrictions(),
                Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec numeric restrictions have no upper bound", outputSpec.getNumericRestrictions().max,
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec numeric restrictions have lower bound", outputSpec.getNumericRestrictions().min,
                Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec numeric restriction lower bound is correct",
                outputSpec.getNumericRestrictions().min.getLimit(), Is.is(BigDecimal.valueOf(5)));
        Assert.assertThat("Fieldspec numeric restriction lower bound is inclusive",
                outputSpec.getNumericRestrictions().min.isInclusive(), Is.is(true));
    }

    @Test
    void shouldReduceNegatedIsGreaterThanOrEqualToConstantConstraint() {
        final Field field = new Field("test0");
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
                new IsGreaterThanOrEqualToConstantConstraint(field, 5, rule()).negate());

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a Numeric type constraint",
                outputSpec.getTypeRestrictions().getAllowedTypes(),
                equalTo(Collections.singleton(IsOfTypeConstraint.Types.NUMERIC)));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no string restrictions", outputSpec.getStringRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no datetime restrictions", outputSpec.getDateTimeRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has numeric restrictions", outputSpec.getNumericRestrictions(),
                Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec numeric restrictions have no lower bound",
                outputSpec.getNumericRestrictions().min, Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec numeric restrictions have upper bound",
                outputSpec.getNumericRestrictions().max, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec numeric restrictions upper bound limit is correct",
                outputSpec.getNumericRestrictions().max.getLimit(), Is.is(BigDecimal.valueOf(5)));
        Assert.assertThat("Fieldspec numeric restrictions upper bound is exclusive",
                outputSpec.getNumericRestrictions().max.isInclusive(), Is.is(false));
    }

    @Test
    void shouldReduceIsLessThanConstantConstraint() {
        final Field field = new Field("test0");
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
                new IsLessThanConstantConstraint(field, 5, rule()));

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a Numeric type constraint",
                outputSpec.getTypeRestrictions().getAllowedTypes(),
                equalTo(Collections.singleton(IsOfTypeConstraint.Types.NUMERIC)));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no string restrictions", outputSpec.getStringRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no datetime restrictions", outputSpec.getDateTimeRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has numeric restrictions", outputSpec.getNumericRestrictions(),
                Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec numeric restrictions have no lower bound",
                outputSpec.getNumericRestrictions().min, Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec numeric restrictions have upper bound",
                outputSpec.getNumericRestrictions().max, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec numeric restrictions upper bound limit is correct",
                outputSpec.getNumericRestrictions().max.getLimit(), Is.is(BigDecimal.valueOf(5)));
        Assert.assertThat("Fieldspec numeric restrictions upper bound is exclusive",
                outputSpec.getNumericRestrictions().max.isInclusive(), Is.is(false));
    }

    @Test
    void shouldReduceNegatedIsLessThanConstantConstraint() {
        final Field field = new Field("test0");
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
                new IsLessThanConstantConstraint(field, 5, rule()).negate());

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a numeric type constraint", outputSpec.getTypeRestrictions().getAllowedTypes(),
                equalTo(Collections.singleton(IsOfTypeConstraint.Types.NUMERIC)));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no string restrictions", outputSpec.getStringRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no datetime restrictions", outputSpec.getDateTimeRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has numeric restrictions", outputSpec.getNumericRestrictions(),
                Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec numeric restrictions have no upper bound", outputSpec.getNumericRestrictions().max,
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec numeric restrictions have lower bound", outputSpec.getNumericRestrictions().min,
                Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec numeric restriction lower bound is correct",
                outputSpec.getNumericRestrictions().min.getLimit(), Is.is(BigDecimal.valueOf(5)));
        Assert.assertThat("Fieldspec numeric restriction lower bound is inclusive",
                outputSpec.getNumericRestrictions().min.isInclusive(), Is.is(true));
    }

    @Test
    void shouldReduceIsLessThanOrEqualToConstantConstraint() {
        final Field field = new Field("test0");
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
                new IsLessThanOrEqualToConstantConstraint(field, 5, rule()));

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a Numeric type constraint",
                outputSpec.getTypeRestrictions().getAllowedTypes(),
                equalTo(Collections.singleton(IsOfTypeConstraint.Types.NUMERIC)));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no string restrictions", outputSpec.getStringRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no datetime restrictions", outputSpec.getDateTimeRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has numeric restrictions", outputSpec.getNumericRestrictions(),
                Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec numeric restrictions have no lower bound",
                outputSpec.getNumericRestrictions().min, Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec numeric restrictions have upper bound",
                outputSpec.getNumericRestrictions().max, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec numeric restrictions upper bound limit is correct",
                outputSpec.getNumericRestrictions().max.getLimit(), Is.is(BigDecimal.valueOf(5)));
        Assert.assertThat("Fieldspec numeric restrictions upper bound is inclusive",
                outputSpec.getNumericRestrictions().max.isInclusive(), Is.is(true));
    }

    @Test
    void shouldReduceNegatedIsLessThanOrEqualToConstantConstraint() {
        final Field field = new Field("test0");
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
                new IsLessThanOrEqualToConstantConstraint(field, 5, rule()).negate());

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a numeric type constraint", outputSpec.getTypeRestrictions().getAllowedTypes(),
                equalTo(Collections.singleton(IsOfTypeConstraint.Types.NUMERIC)));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no string restrictions", outputSpec.getStringRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no datetime restrictions", outputSpec.getDateTimeRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has numeric restrictions", outputSpec.getNumericRestrictions(),
                Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec numeric restrictions have no upper bound", outputSpec.getNumericRestrictions().max,
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec numeric restrictions have lower bound", outputSpec.getNumericRestrictions().min,
                Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec numeric restriction lower bound is correct",
                outputSpec.getNumericRestrictions().min.getLimit(), Is.is(BigDecimal.valueOf(5)));
        Assert.assertThat("Fieldspec numeric restriction lower bound is exclusive",
                outputSpec.getNumericRestrictions().min.isInclusive(), Is.is(false));
    }

    @Test
    void shouldreduceIsAfterConstantDateTimeConstraint() {
        final Field field = new Field("test0");
        final LocalDateTime testTimestamp = LocalDateTime.of(2018, 2, 4, 23, 25, 16);
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
                new IsAfterConstantDateTimeConstraint(field, testTimestamp, rule()));

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a temporal type constraint", outputSpec.getTypeRestrictions().getAllowedTypes(),
                equalTo(Collections.singleton(IsOfTypeConstraint.Types.TEMPORAL)));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no string restrictions", outputSpec.getStringRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no numeric restrictions", outputSpec.getNumericRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has datetime restrictions", outputSpec.getDateTimeRestrictions(),
                Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have no upper bound",
                outputSpec.getDateTimeRestrictions().max, Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have lower bound",
                outputSpec.getDateTimeRestrictions().min, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have correct lower bound limit",
                outputSpec.getDateTimeRestrictions().min.getLimit(), Is.is(testTimestamp));
        Assert.assertThat("Fieldspec datetime restrictions have exclusive lower bound",
                outputSpec.getDateTimeRestrictions().min.isInclusive(), Is.is(false));
    }

    @Test
    void shouldreduceNegatedIsAfterConstantDateTimeConstraint() {
        final Field field = new Field("test0");
        final LocalDateTime testTimestamp = LocalDateTime.of(2018, 2, 4, 23, 25, 16);
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
                new IsAfterConstantDateTimeConstraint(field, testTimestamp, rule()).negate());

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a temporal type constraint", outputSpec.getTypeRestrictions().getAllowedTypes(),
                equalTo(Collections.singleton(IsOfTypeConstraint.Types.TEMPORAL)));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no string restrictions", outputSpec.getStringRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no numeric restrictions", outputSpec.getNumericRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has datetime restrictions", outputSpec.getDateTimeRestrictions(),
                Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have no lower bound",
                outputSpec.getDateTimeRestrictions().min, Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have upper bound",
                outputSpec.getDateTimeRestrictions().max, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have correct upper bound limit",
                outputSpec.getDateTimeRestrictions().max.getLimit(), Is.is(testTimestamp));
        Assert.assertThat("Fieldspec datetime restrictions have inclusive upper bound",
                outputSpec.getDateTimeRestrictions().max.isInclusive(), Is.is(true));
    }

    @Test
    void shouldreduceIsAfterOrEqualToConstantDateTimeConstraint() {
        final Field field = new Field("test0");
        final LocalDateTime testTimestamp = LocalDateTime.of(2018, 2, 4, 23, 25, 16);
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
                new IsAfterOrEqualToConstantDateTimeConstraint(field, testTimestamp, rule()));

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has no type restrictions", outputSpec.getTypeRestrictions().getAllowedTypes(),
                equalTo(Collections.singleton(IsOfTypeConstraint.Types.TEMPORAL)));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no string restrictions", outputSpec.getStringRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no numeric restrictions", outputSpec.getNumericRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has datetime restrictions", outputSpec.getDateTimeRestrictions(),
                Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have no upper bound",
                outputSpec.getDateTimeRestrictions().max, Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have lower bound",
                outputSpec.getDateTimeRestrictions().min, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have correct lower bound limit",
                outputSpec.getDateTimeRestrictions().min.getLimit(), Is.is(testTimestamp));
        Assert.assertThat("Fieldspec datetime restrictions have inclusive lower bound",
                outputSpec.getDateTimeRestrictions().min.isInclusive(), Is.is(true));
    }

    @Test
    void shouldreduceNegatedIsAfterOrEqualToConstantDateTimeConstraint() {
        final Field field = new Field("test0");
        final LocalDateTime testTimestamp = LocalDateTime.of(2018, 2, 4, 23, 25, 16);
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
                new IsAfterOrEqualToConstantDateTimeConstraint(field, testTimestamp, rule()).negate());

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a Temporal type constraint",
                outputSpec.getTypeRestrictions().getAllowedTypes(),
                equalTo(Collections.singleton(IsOfTypeConstraint.Types.TEMPORAL)));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no string restrictions", outputSpec.getStringRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no numeric restrictions", outputSpec.getNumericRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has datetime restrictions", outputSpec.getDateTimeRestrictions(),
                Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have no lower bound",
                outputSpec.getDateTimeRestrictions().min, Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have upper bound",
                outputSpec.getDateTimeRestrictions().max, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have correct upper bound limit",
                outputSpec.getDateTimeRestrictions().max.getLimit(), Is.is(testTimestamp));
        Assert.assertThat("Fieldspec datetime restrictions have exclusive upper bound",
                outputSpec.getDateTimeRestrictions().max.isInclusive(), Is.is(false));
    }

    @Test
    void shouldreduceIsBeforeConstantDateTimeConstraint() {
        final Field field = new Field("test0");
        final LocalDateTime testTimestamp = LocalDateTime.of(2018, 2, 4, 23, 25, 16);
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
                new IsBeforeConstantDateTimeConstraint(field, testTimestamp, rule()));

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a Temporal type constraint",
                outputSpec.getTypeRestrictions().getAllowedTypes(),
                equalTo(Collections.singleton(IsOfTypeConstraint.Types.TEMPORAL)));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no string restrictions", outputSpec.getStringRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no numeric restrictions", outputSpec.getNumericRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has datetime restrictions", outputSpec.getDateTimeRestrictions(),
                Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have no lower bound",
                outputSpec.getDateTimeRestrictions().min, Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have upper bound",
                outputSpec.getDateTimeRestrictions().max, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have correct upper bound limit",
                outputSpec.getDateTimeRestrictions().max.getLimit(), Is.is(testTimestamp));
        Assert.assertThat("Fieldspec datetime restrictions have exclusive upper bound",
                outputSpec.getDateTimeRestrictions().max.isInclusive(), Is.is(false));
    }

    @Test
    void shouldreduceNegatedIsBeforeConstantDateTimeConstraint() {
        final Field field = new Field("test0");
        final LocalDateTime testTimestamp = LocalDateTime.of(2018, 2, 4, 23, 25, 16);
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
                new IsBeforeConstantDateTimeConstraint(field, testTimestamp, rule()).negate());

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a temporal type constraint", outputSpec.getTypeRestrictions().getAllowedTypes(),
                equalTo(Collections.singleton(IsOfTypeConstraint.Types.TEMPORAL)));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no string restrictions", outputSpec.getStringRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no numeric restrictions", outputSpec.getNumericRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has datetime restrictions", outputSpec.getDateTimeRestrictions(),
                Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have no upper bound",
                outputSpec.getDateTimeRestrictions().max, Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have lower bound",
                outputSpec.getDateTimeRestrictions().min, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have correct lower bound limit",
                outputSpec.getDateTimeRestrictions().min.getLimit(), Is.is(testTimestamp));
        Assert.assertThat("Fieldspec datetime restrictions have inclusive lower bound",
                outputSpec.getDateTimeRestrictions().min.isInclusive(), Is.is(true));
    }

    @Test
    void shouldreduceIsBeforeOrEqualToConstantDateTimeConstraint() {
        final Field field = new Field("test0");
        final LocalDateTime testTimestamp = LocalDateTime.of(2018, 2, 4, 23, 25, 16);
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
                new IsBeforeOrEqualToConstantDateTimeConstraint(field, testTimestamp, rule()));

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a Temporal type constraint",
                outputSpec.getTypeRestrictions().getAllowedTypes(),
                equalTo(Collections.singleton(IsOfTypeConstraint.Types.TEMPORAL)));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no string restrictions", outputSpec.getStringRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no numeric restrictions", outputSpec.getNumericRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has datetime restrictions", outputSpec.getDateTimeRestrictions(),
                Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have no lower bound",
                outputSpec.getDateTimeRestrictions().min, Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have upper bound",
                outputSpec.getDateTimeRestrictions().max, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have correct upper bound limit",
                outputSpec.getDateTimeRestrictions().max.getLimit(), Is.is(testTimestamp));
        Assert.assertThat("Fieldspec datetime restrictions have inclusive upper bound",
                outputSpec.getDateTimeRestrictions().max.isInclusive(), Is.is(true));
    }

    @Test
    void shouldreduceNegatedIsBeforeorEqualToConstantDateTimeConstraint() {
        final Field field = new Field("test0");
        final LocalDateTime testTimestamp = LocalDateTime.of(2018, 2, 4, 23, 25, 16);
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
                new IsBeforeOrEqualToConstantDateTimeConstraint(field, testTimestamp, rule()).negate());

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a temporal type constraint", outputSpec.getTypeRestrictions().getAllowedTypes(),
                equalTo(Collections.singleton(IsOfTypeConstraint.Types.TEMPORAL)));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no string restrictions", outputSpec.getStringRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no numeric restrictions", outputSpec.getNumericRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has datetime restrictions", outputSpec.getDateTimeRestrictions(),
                Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have no upper bound",
                outputSpec.getDateTimeRestrictions().max, Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have lower bound",
                outputSpec.getDateTimeRestrictions().min, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have correct lower bound limit",
                outputSpec.getDateTimeRestrictions().min.getLimit(), Is.is(testTimestamp));
        Assert.assertThat("Fieldspec datetime restrictions have exclusive lower bound",
                outputSpec.getDateTimeRestrictions().min.isInclusive(), Is.is(false));
    }

    @Test
    void shouldMergeAndReduceIsAfterConstantDateTimeConstraintWithIsBeforeConstantDateTimeConstraint() {
        final Field field = new Field("test0");
        final LocalDateTime startTimestamp = LocalDateTime.of(2013, 11, 19, 10, 43, 12);
        final LocalDateTime endTimestamp = LocalDateTime.of(2018, 2, 4, 23, 25, 8);
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Arrays.asList(
                new IsAfterConstantDateTimeConstraint(field, startTimestamp, rule()),
                new IsBeforeConstantDateTimeConstraint(field, endTimestamp, rule()));

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a Temporal type constraint",
                outputSpec.getTypeRestrictions().getAllowedTypes(),
                equalTo(Collections.singleton(IsOfTypeConstraint.Types.TEMPORAL)));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no string restrictions", outputSpec.getStringRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no numeric restrictions", outputSpec.getNumericRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has datetime restrictions", outputSpec.getDateTimeRestrictions(),
                Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have lower bound",
                outputSpec.getDateTimeRestrictions().min, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have correct lower bound limit",
                outputSpec.getDateTimeRestrictions().min.getLimit(), Is.is(startTimestamp));
        Assert.assertThat("Fieldspect datetime restrictions have exclusive lower bound",
                outputSpec.getDateTimeRestrictions().min.isInclusive(), Is.is(false));
        Assert.assertThat("Fieldspec datetime restrictions have upper bound",
                outputSpec.getDateTimeRestrictions().max, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have correct upper bound limit",
                outputSpec.getDateTimeRestrictions().max.getLimit(), Is.is(endTimestamp));
        Assert.assertThat("Fieldspec datetime restrictions have exclusive upper bound",
                outputSpec.getDateTimeRestrictions().max.isInclusive(), Is.is(false));
    }

    @Test
    void shouldReduceMatchesRegexConstraint() {
        final Field field = new Field("test0");
        String pattern = ".*\\..*";
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
                new MatchesRegexConstraint(field, Pattern.compile(pattern), rule()));

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a String type constraint",
                outputSpec.getTypeRestrictions().getAllowedTypes(),
                equalTo(Collections.singleton(IsOfTypeConstraint.Types.STRING)));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no numeric restrictions", outputSpec.getNumericRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no datetime restrictions", outputSpec.getDateTimeRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has string restrictions", outputSpec.getStringRestrictions(),
                Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec string restrictions have a string generator",
                outputSpec.getStringRestrictions().stringGenerator, Is.is(IsNull.notNullValue()));
    }

    @Test
    void shouldReduceSingleFormatConstraint() {
        final Field field = new Field("test0");
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));

        List<AtomicConstraint> constraints = Arrays.asList(
                new FormatConstraint(field, "Hello '$1'", rule())
        );

        ConstraintReducer testObject = new ConstraintReducer(new FieldSpecFactory(), new FieldSpecMerger());

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a String type constraint",
                outputSpec.getTypeRestrictions().getAllowedTypes(),
                containsInAnyOrder(
                        IsOfTypeConstraint.Types.TEMPORAL,
                        IsOfTypeConstraint.Types.STRING,
                        IsOfTypeConstraint.Types.NUMERIC));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no numeric restrictions", outputSpec.getNumericRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no datetime restrictions", outputSpec.getDateTimeRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has string restrictions", outputSpec.getStringRestrictions(),

                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec format restrictions has a value",
                outputSpec.getFormatRestrictions(), Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec format restrictions has a value",
                outputSpec.getFormatRestrictions().formatString, Is.is("Hello '$1'"));
    }

    @Test
    void shouldNotReduceMultipleFormatConstraint() {
        final Field field = new Field("test0");
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));

        List<AtomicConstraint> constraints = Arrays.asList(
                new FormatConstraint(field, "Lorem '$1'", rule()),
                new FormatConstraint(field, "Ipsum '$1'", rule())
        );

        ConstraintReducer testObject = new ConstraintReducer(new FieldSpecFactory(), new FieldSpecMerger());

        Assertions.assertThrows(
                UnsupportedOperationException.class,
                () -> constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints));
    }

    @Test
    void shouldReduceStringLongerThanConstraint() {
        final Field field = new Field("test0");

        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
                new IsStringLongerThanConstraint(field, 5, rule())
        );

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a String type constraint",
                outputSpec.getTypeRestrictions().getAllowedTypes(),
                equalTo(Collections.singleton(IsOfTypeConstraint.Types.STRING)));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no numeric restrictions", outputSpec.getNumericRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no datetime restrictions", outputSpec.getDateTimeRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has string restrictions", outputSpec.getStringRestrictions(),
                Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec string restrictions have aa string generator",
                outputSpec.getStringRestrictions().stringGenerator, notNullValue());
    }

    @Test
    void shouldReduceStringShorterThanConstraint() {
        final Field field = new Field("test0");

        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
                new IsStringShorterThanConstraint(field, 5, rule())
        );

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a string constrint", outputSpec.getTypeRestrictions().getAllowedTypes(),
                equalTo(Collections.singleton(IsOfTypeConstraint.Types.STRING)));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no numeric restrictions", outputSpec.getNumericRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no datetime restrictions", outputSpec.getDateTimeRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has string restrictions", outputSpec.getStringRestrictions(),
                Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec string restrictions have a string generator",
                outputSpec.getStringRestrictions().stringGenerator, notNullValue());
    }

    @Test
    void shouldReduceStringHasLengthConstraint() {
        final Field field = new Field("test0");

        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
                new StringHasLengthConstraint(field, 5, rule())
        );

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec has a String type constraint",
                outputSpec.getTypeRestrictions().getAllowedTypes(),
                equalTo(Collections.singleton(IsOfTypeConstraint.Types.STRING)));
        Assert.assertThat("Fieldspec has a String type constraint",
                outputSpec.getTypeRestrictions().getAllowedTypes(),
                equalTo(Collections.singleton(IsOfTypeConstraint.Types.STRING)));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no numeric restrictions", outputSpec.getNumericRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no datetime restrictions", outputSpec.getDateTimeRestrictions(),
                Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has string restrictions", outputSpec.getStringRestrictions(),
                Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec string restrictions has a string generator",
                outputSpec.getStringRestrictions().stringGenerator, notNullValue());
    }

    @Test
    void shouldNotReduceMixedExplicitTypesConstraints() {
        final Field field = new Field("test0");

        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Arrays.asList(
                new StringHasLengthConstraint(field, 5, rule()),
                new IsOfTypeConstraint(field, IsOfTypeConstraint.Types.TEMPORAL, rule())
        );

        Optional<RowSpec> satisfyingRowSpec = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints);

        Assert.assertThat("RowSpec should be empty", satisfyingRowSpec, Is.is(Optional.empty()));
    }

    @Test
    void shouldNotReduceMixedImplicitTypesConstraints() {
        final Field field = new Field("test0");

        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Arrays.asList(
                new StringHasLengthConstraint(field, 5, rule()),
                new IsGreaterThanConstantConstraint(field, 5, rule())
        );


        Optional<RowSpec> testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints);

        Assert.assertThat("Output is not null", testOutput, Is.is(Optional.empty()));

    }

    @Test
    void whenHasNumericRestrictions_shouldFilterSet() {

        final Field field = new Field("test0");
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));

        List<AtomicConstraint> constraints = Arrays.asList(
                new IsOfTypeConstraint(field, IsOfTypeConstraint.Types.NUMERIC, rule()),
                new IsInSetConstraint(field, new HashSet<>(Arrays.asList(1, "lorem", 5, "ipsum", 2)), rule())
        );

        Optional<RowSpec> testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints);

        FieldSpec spec = testOutput.get().getSpecForField(field);

        SetRestrictions setRestrictions = spec.getSetRestrictions();

        Assert.assertThat(setRestrictions.getWhitelist(), containsInAnyOrder(1, 5, 2));
    }

    @Test
    void whenHasStringRestrictions_shouldFilterSet() {

        final Field field = new Field("test0");
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));

        List<AtomicConstraint> constraints = Arrays.asList(
                new MatchesRegexConstraint(field, Pattern.compile("(lorem|ipsum)"), rule()),
                new IsInSetConstraint(field, new HashSet<>(Arrays.asList(1, "lorem", 5, "ipsum", 2)), rule())
        );

        Optional<RowSpec> testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints);

        FieldSpec spec = testOutput.get().getSpecForField(field);

        SetRestrictions setRestrictions = spec.getSetRestrictions();

        Assert.assertThat(setRestrictions.getWhitelist(), containsInAnyOrder("lorem", "ipsum"));
    }

    @Test
    void shouldReduceMatchesRegexWithSingletonConstraint() {
        final Field field = new Field("test0");

        String infinitePattern = ".*";
        String singletonPattern = "thisisatest";

        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Arrays.asList(
                new MatchesRegexConstraint(field, Pattern.compile(infinitePattern), rule()),
                new MatchesRegexConstraint(field, Pattern.compile(singletonPattern), rule()));

        Optional<RowSpec> testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints);

        FieldSpec outputSpec = testOutput.get().getSpecForField(field);

        Assert.assertThat("Fieldspec has string restrictions", outputSpec.getStringRestrictions(),
                Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec string restrictions have an stringGenerator",
                outputSpec.getStringRestrictions().stringGenerator, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec string restrictions stringGenerator has nodes",
                outputSpec.getStringRestrictions().stringGenerator.getValueCount(),
                Matchers.greaterThan(0L));
    }

    private static RuleInformation rule(){
        RuleDTO rule = new RuleDTO();
        rule.rule = "rule";
        return new RuleInformation(rule);
    }
}
