/*
 * Copyright 2019 Scott Logic Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.scottlogic.deg.generator.fieldspecs;

import com.scottlogic.deg.generator.restrictions.DateTimeRestrictions;
import com.scottlogic.deg.generator.restrictions.DateTimeRestrictionsMerger;
import com.scottlogic.deg.generator.restrictions.MergeResult;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.scottlogic.deg.common.profile.constraints.atomic.IsOfTypeConstraint.Types.DATETIME;
import static com.scottlogic.deg.common.profile.constraints.atomic.IsOfTypeConstraint.Types.NUMERIC;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DateTimeRestrictionsMergeOperationTests {
    private DateTimeRestrictionsMerger merger;
    private DateTimeRestrictionsMergeOperation operation;
    private FieldSpec left;
    private FieldSpec right;

    @BeforeEach
    public void beforeEach() {
        merger = mock(DateTimeRestrictionsMerger.class);
        operation = new DateTimeRestrictionsMergeOperation(merger);

        left = FieldSpec.fromType(DATETIME).withDateTimeRestrictions(new DateTimeRestrictions());
        right = FieldSpec.fromType(DATETIME).withDateTimeRestrictions(new DateTimeRestrictions());
    }

    @Test
    public void applyMergeOperation_withNoDateTimeRestrictions_shouldNotApplyAnyRestrictions() {
        FieldSpec merging = FieldSpec.fromType(DATETIME);
        when(merger.merge(left.getDateTimeRestrictions(), right.getDateTimeRestrictions()))
            .thenReturn(new MergeResult<>(null));

        FieldSpec result = operation.applyMergeOperation(left, right);

        Assert.assertEquals(result, merging);
    }

    @Test
    public void applyMergeOperation_withContradictoryDateTimeRestrictionsAndNoTypeRestrictions_shouldPreventAnyDateTimeValues() {
        FieldSpec merging = FieldSpec.fromType(DATETIME);
        when(merger.merge(left.getDateTimeRestrictions(), right.getDateTimeRestrictions()))
            .thenReturn(MergeResult.unsuccessful());

        FieldSpec result = operation.applyMergeOperation(left, right);

        Assert.assertEquals(result, FieldSpec.nullOnlyFromType(DATETIME));
    }

    @Test
    public void applyMergeOperation_withContradictoryDateTimeRestrictions_shouldPreventAnyDateTimeValues() {
        FieldSpec merging = FieldSpec.fromType(DATETIME);
        when(merger.merge(left.getDateTimeRestrictions(), right.getDateTimeRestrictions()))
            .thenReturn(MergeResult.unsuccessful());

        FieldSpec result = operation.applyMergeOperation(left, right);

        Assert.assertEquals(result, FieldSpec.nullOnlyFromType(DATETIME));
    }


    @Test
    public void applyMergeOperation_withContradictoryDateTimeRestrictionsAndDateTimeTypeOnlyPermittedType_shouldPreventAnyDateTimeValues() {
        FieldSpec merging = FieldSpec.fromType(DATETIME);
        when(merger.merge(left.getDateTimeRestrictions(), right.getDateTimeRestrictions()))
            .thenReturn(MergeResult.unsuccessful());

        FieldSpec result = operation.applyMergeOperation(left, right);

        Assert.assertThat(result, not(sameInstance(merging)));
        Assert.assertThat(result.getWhitelist().set(), is(empty()));
    }

    @Disabled("same instance check not working due to being casted")
    @Test
    public void applyMergeOperation_withMergableDateTimeRestrictions_shouldApplyMergedDateTimeRestrictions() {
        FieldSpec merging = FieldSpec.fromType(DATETIME);
        DateTimeRestrictions merged = new DateTimeRestrictions();
        when(merger.merge(left.getDateTimeRestrictions(), right.getDateTimeRestrictions()))
            .thenReturn(new MergeResult<>(merged));

        FieldSpec result = operation.applyMergeOperation(left, right);

        Assert.assertThat(result, not(sameInstance(merging)));
        Assert.assertThat(result.getDateTimeRestrictions(), sameInstance(merged));
        Assert.assertThat(result.getType(), sameInstance(merging.getType()));
    }
}