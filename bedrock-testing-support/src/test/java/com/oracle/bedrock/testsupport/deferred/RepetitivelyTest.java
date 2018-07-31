/*
 * File: RepetitivelyTest.java
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * The contents of this file are subject to the terms and conditions of
 * the Common Development and Distribution License 1.0 (the "License").
 *
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License by consulting the LICENSE.txt file
 * distributed with this file, or by consulting https://oss.oracle.com/licenses/CDDL
 *
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file LICENSE.txt.
 *
 * MODIFICATIONS:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 */

package com.oracle.bedrock.testsupport.deferred;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.deferred.Deferred;
import com.oracle.bedrock.testsupport.matchers.RecordingMatcher;
import com.oracle.bedrock.options.Timeout;
import com.oracle.bedrock.testsupport.deferred.Repetitively;
import com.oracle.bedrock.util.Duration;
import com.oracle.bedrock.util.StopWatch;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static com.oracle.bedrock.testsupport.matchers.ConstantMatcher.constant;
import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.Is.is;

/**
 * Functional Tests for {@link Repetitively}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class RepetitivelyTest
{
    /**
     * Ensure a constant can be repetitively asserted to the same value.
     */
    @Test
    public void shouldRepetitivelyAssertConstant()
    {
        Duration                  duration = Duration.of(5, TimeUnit.SECONDS);
        RecordingMatcher<Integer> matcher  = RecordingMatcher.of(is(constant()));

        StopWatch                 watch    = new StopWatch();

        watch.start();

        Repetitively.assertThat(42, matcher, Timeout.of(duration));

        watch.stop();

        assertThat(matcher.attempted(), is(true));
        assertThat(matcher.hasSucceeded(), is(true));
        assertThat(matcher.hasFailed(), is(false));

        assertThat(watch.getElapsedTimeIn(TimeUnit.SECONDS),
                   greaterThanOrEqualTo((long) (duration.to(TimeUnit.SECONDS) * 0.95)));
    }


    /**
     * Ensure a {@link Repetitively#assertThat(String, Deferred, Matcher, Option...)} fails immediately.
     */
    @Test
    public void shouldNotRepetitivelyAssertIncorrectConstant()
    {
        Duration                  duration = Duration.of(5, TimeUnit.SECONDS);
        RecordingMatcher<Integer> matcher  = RecordingMatcher.of(is(1));

        StopWatch                 watch    = new StopWatch();

        watch.start();

        try
        {
            Repetitively.assertThat(42, matcher, Timeout.of(duration));

            throw new IllegalStateException();
        }
        catch (AssertionError error)
        {
            assertThat(matcher.attempted(), is(true));
            assertThat(matcher.hasSucceeded(), is(false));
            assertThat(matcher.hasFailed(), is(true));
            assertThat(matcher.getFailureCount(), is(1));

            assertThat(watch.getElapsedTimeIn(TimeUnit.SECONDS), lessThan(1L));
        }
        catch (IllegalStateException e)
        {
            Assert.fail("Should have failed immediately");
        }
    }


    /**
     * Ensure a function can be repetitively asserted to a value.
     */
    @Test
    public void shouldRepetitivelyAssertFunction()
    {
        Duration                  duration = Duration.of(5, TimeUnit.SECONDS);
        RecordingMatcher<Integer> matcher  = RecordingMatcher.of(is(constant()));

        StopWatch                 watch    = new StopWatch();

        watch.start();

        Repetitively.assertThat(42, (value) -> value, matcher, Timeout.of(duration));

        watch.stop();

        assertThat(matcher.attempted(), is(true));
        assertThat(matcher.hasSucceeded(), is(true));
        assertThat(matcher.hasFailed(), is(false));

        assertThat(watch.getElapsedTimeIn(TimeUnit.SECONDS),
                   greaterThanOrEqualTo((long) (duration.to(TimeUnit.SECONDS) * 0.95)));
    }
}
