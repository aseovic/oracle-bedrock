/*
 * File: EventuallyTest.java
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
import com.oracle.bedrock.deferred.DeferredHelper;
import com.oracle.bedrock.deferred.DeferredNull;
import com.oracle.bedrock.deferred.Existing;
import com.oracle.bedrock.deferred.NeverAvailable;
import com.oracle.bedrock.deferred.NotAvailable;
import com.oracle.bedrock.deferred.PermanentlyUnavailableException;
import com.oracle.bedrock.deferred.TemporarilyUnavailableException;
import com.oracle.bedrock.testsupport.matchers.ThrowableMatcher;
import com.oracle.bedrock.testsupport.deferred.Eventually;
import com.oracle.bedrock.util.StopWatch;
import java.util.Collections;
import java.util.Map;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.oracle.bedrock.deferred.DeferredHelper.valueOf;
import static com.oracle.bedrock.deferred.DeferredHelper.within;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

/**
 * Unit tests for {@link Eventually}.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class EventuallyTest
{
    /**
     * Ensure that a {@link Eventually#assertDeferred(Deferred, Matcher, Option...)}
     * does not fail when called on a proxy that returns a primitive or wrapper
     * type.
     */
    @Test
    public void shouldWorkWithDeferredLambda()
    {
        Map<String, Integer> map = Collections.singletonMap("one", 1);
        Eventually.assertDeferred(() -> map.get("one"), is(1));
    }

    /**
     * Ensure that a {@link Eventually#assertThat(Object, Matcher, Option...)}
     * waits at least the default amount of time before throwing an exception when
     * a {@link Deferred} is {@link NotAvailable}.
     */
    @Test
    public void shouldWaitDefaultTimeout()
    {
        StopWatch stopWatch = new StopWatch();

        try
        {
            stopWatch.start();
            Eventually.assertThat(valueOf(new NotAvailable<String>(String.class)), is("hello world"));
        }
        catch (AssertionError e)
        {
            Assert.assertTrue("Failed to throw an PermanentlyUnavailableException as expected",
                              e.getCause() instanceof PermanentlyUnavailableException);

            stopWatch.stop();

            Assert.assertTrue(String.format("Failed to wait for the default duration of %d seconds.  Waited %s seconds",
                                            DeferredHelper.BEDROCK_DEFERRED_RETRY_TIMEOUT_SECS,
                                            stopWatch.getElapsedTimeIn(TimeUnit.SECONDS)),
                              stopWatch.getElapsedTimeIn(TimeUnit.SECONDS)
                              >= DeferredHelper.BEDROCK_DEFERRED_RETRY_TIMEOUT_SECS * 0.95);
        }
        catch (Exception e)
        {
            Assert.fail("Unexpected Exception:" + e);
        }
    }


    /**
     * Ensure that a {@link Eventually#assertThat(Object, Matcher, Option...)}
     * waits at least the specified time before throwing an exception when
     * the {@link Deferred} returns <code>null</code>.
     */
    @Test
    public void shouldWaitSpecifiedTimeBeforeTimeout()
    {
        StopWatch stopWatch         = new StopWatch();
        long      retryDurationSECS = 5;

        try
        {
            stopWatch.start();
            Eventually.assertThat(valueOf(new NotAvailable<String>(String.class)),
                                  is("hello world"),
                                  within(retryDurationSECS, TimeUnit.SECONDS));
        }
        catch (AssertionError e)
        {
            Assert.assertTrue("Failed to throw an PermanentlyUnavailableException as expected",
                              e.getCause() instanceof PermanentlyUnavailableException);

            stopWatch.stop();

            Assert.assertTrue(String.format("Failed to wait for the specified duration of %d seconds.  Waited %s seconds",
                                            retryDurationSECS,
                                            stopWatch.getElapsedTimeIn(TimeUnit.SECONDS)),
                              stopWatch.getElapsedTimeIn(TimeUnit.SECONDS) >= retryDurationSECS);
        }
        catch (Exception e)
        {
            Assert.fail("Unexpected Exception:" + e);
        }
    }


    /**
     * Ensure that a {@link Eventually#assertThat(Object, Matcher, Option...)}
     * fails fast when the {@link Deferred} throws an {@link PermanentlyUnavailableException}.
     */
    @Test
    public void shouldFailFast()
    {
        StopWatch stopWatch = new StopWatch();

        try
        {
            stopWatch.start();
            Eventually.assertThat(DeferredHelper.valueOf(new NeverAvailable<String>(String.class)), is("hello world"));
        }
        catch (AssertionError e)
        {
            Assert.assertTrue("Failed to throw an PermanentlyUnavailableException as expected",
                              e.getCause() instanceof PermanentlyUnavailableException);

            stopWatch.stop();

            Assert.assertTrue(String.format("Failed to fail fast.  Instead waited for Waited %s seconds",
                                            stopWatch.getElapsedTimeIn(TimeUnit.SECONDS)),
                              stopWatch.getElapsedTimeIn(TimeUnit.SECONDS)
                              < DeferredHelper.BEDROCK_DEFERRED_RETRY_TIMEOUT_SECS);
        }
        catch (Exception e)
        {
            Assert.fail("Unexpected Exception:" + e);
        }
    }


    /**
     * Ensure that a {@link Eventually#assertThat(Object, Matcher, Option...)}
     * returns immediately if the {@link Deferred} resolves to <code>null</code>.
     */
    @Test
    public void shouldReturnImmediatelyWhenEncounteringNull()
    {
        StopWatch stopWatch = new StopWatch();

        stopWatch.start();
        Eventually.assertThat(DeferredHelper.valueOf(new DeferredNull<String>(String.class)
        {
        } ),                  is(nullValue()));

        stopWatch.stop();

        Assert.assertTrue(String.format("Failed to return immediately when encountering a null.  Instead waited for Waited %s seconds",
                                        stopWatch.getElapsedTimeIn(TimeUnit.SECONDS)),
                          stopWatch.getElapsedTimeIn(TimeUnit.SECONDS)
                          < DeferredHelper.BEDROCK_DEFERRED_RETRY_TIMEOUT_SECS);
    }


    /**
     * Ensure that the exception thrown by {@link Eventually#assertThat(Object, Matcher, Option...)}
     * contains the last used value with the matcher.
     */
    @Test
    public void shouldUseLastEvaluatedValueWithMatcher()
    {
        Deferred<String> deferred = new Existing<String>("Hello World");

        try
        {
            Eventually.assertThat(valueOf(deferred), is("Gudday"), within(1, TimeUnit.SECONDS));
        }
        catch (AssertionError e)
        {
            Assert.assertTrue("Failed to throw an PermanentlyUnavailableException as expected",
                              e.getCause() instanceof PermanentlyUnavailableException);

            Assert.assertTrue(e.getMessage().contains("Hello World"));
        }
        catch (Exception e)
        {
            Assert.fail("Unexpected Exception:" + e);
        }
    }


    /**
     * Ensure that Eventually.assertThat works with wrappers and primitives.
     */
    @Test
    public void shouldEventuallyAssertUsingWrappersAndPrimitives()
    {
        Eventually.assertThat(Long.valueOf(5), is(5L));

        Eventually.assertThat(5L, is(Long.valueOf(5)));
    }


    /**
     * Ensure that Eventually.assertThat works with atomics.
     */
    @Test
    public void shouldEventuallyAssertThatUsingAtomics()
    {
        // use an atomic long
        AtomicLong atomicLong = new AtomicLong(42);

        Eventually.assertThat(valueOf(atomicLong), is(42L));

        // use an atomic integer
        AtomicInteger atomicInteger = new AtomicInteger(42);

        Eventually.assertThat(valueOf(atomicInteger), is(42));

        // use an atomic boolean
        AtomicBoolean atomicBoolean = new AtomicBoolean(true);

        Eventually.assertThat(valueOf(atomicBoolean), is(true));
    }


    /**
     * Ensure that Eventually.assertThat works with queues (collections).
     */
    @Test
    public void shouldEventuallyAssertThatQueueContainsElement()
    {
        ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();

        queue.add("Listening at address: Hummingbird.local:30003");
        queue.add("Set uncaught java.lang.Throwable");
        queue.add("Set deferred uncaught java.lang.Throwable");
        queue.add("Initializing jdb");
        queue.add("VM Started: >");
        queue.add("The application exited, (terminated)");

        Eventually.assertThat(queue, hasItem(containsString("VM Started:")));
    }


    /**
     * Ensure that Eventually.assertThat works with functions (lambdas).
     */
    @Test
    public void shouldEventuallyAssertThatFunctionIsSatisfied()
    {
        ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();

        queue.add("Listening at address: Hummingbird.local:30003");
        queue.add("Set uncaught java.lang.Throwable");
        queue.add("Set deferred uncaught java.lang.Throwable");
        queue.add("Initializing jdb");
        queue.add("VM Started: >");
        queue.add("The application exited, (terminated)");

        Eventually.assertThat(queue, (q) -> q.contains("VM Started: >"), is(true));
    }


    /**
     * Ensure that Eventually.assertThat works with functions (lambdas).
     */
    @Test
    public void shouldNotEventuallyAssertThatFunctionIsSatisfied()
    {
        ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();

        queue.add("Listening at address: Hummingbird.local:30003");
        queue.add("Set uncaught java.lang.Throwable");
        queue.add("Set deferred uncaught java.lang.Throwable");
        queue.add("Initializing jdb");
        queue.add("VM Started: >");
        queue.add("The application exited, (terminated)");

        Eventually.assertThat(queue, (q) -> q.contains("Food"), is(false));
    }


    /**
     * Ensure that Eventually.assertThat can be used with {@link ThrowableMatcher}s.
     */
    @Test
    public void shouldEventuallyMatchAThrowable()
    {
        // establish a deferred that always fails
        Deferred<Long> deferred = new Deferred<Long>()
        {
            @Override
            public Long get() throws TemporarilyUnavailableException, PermanentlyUnavailableException
            {
                throw new NullPointerException("oops!");
            }

            @Override
            public Class<Long> getDeferredClass()
            {
                return Long.class;
            }
        };

        Eventually.assertThat(deferred,
                              ThrowableMatcher.willThrow(NullPointerException.class).withMessage(equalTo("oops!"))
                              .causedBy(Matchers.nullValue()),
                              within(1, TimeUnit.SECONDS));
    }


    /**
     * Ensure that Eventually.assertThat on a deferred constant (Number) does not wait for full 2 minutes
     * and gives up straight away.
     */
    @Test
    public void shouldNotWaitForDeferredConstant()
    {
        Boolean myBoolean = true;

        checkConstant(true, is(myBoolean));
        checkConstant(1, is(2));
        checkConstant(1d, is(2d));
        checkConstant(1.01f, is(2.0f));
        checkConstant(1L, is(2L));
    }


    /**
     * Ensure that message regarding simple value was returned.
     */
    @Test
    public void shouldRaiseAssertionWithDeferredConstant()
    {
        try
        {
            Eventually.assertThat(1, is(2));
            Assert.fail("Should have raised AssertionError but did not");
        }
        catch (AssertionError e)
        {
            Assert.assertTrue(e.getMessage() != null
                              && e.getMessage().contains("Deferred was not retried as it was a simple value"));
        }
    }


    /**
     * Helper to ensure that Eventually.assertThat on a deferred constant (Number)
     * does not wait for full 2 minutes and gives up straight away.
     *
     * @param value         value to check
     * @param matchedValue  matcher
     */
    private void checkConstant(Object  value,
                               Matcher matchedValue)
    {
        StopWatch stopWatch = new StopWatch();

        try
        {
            stopWatch.start();
            Eventually.assertThat(value, matchedValue);
        }
        catch (AssertionError e)
        {
            stopWatch.stop();

            long seconds = stopWatch.getElapsedTimeIn(TimeUnit.SECONDS);

            Assert.assertTrue(String.format("Failed to return immediately when encountering a deferred constant value. Returned after %d sec",
                                            seconds),
                              seconds < DeferredHelper.BEDROCK_DEFERRED_RETRY_TIMEOUT_SECS);
            Assert.assertTrue(e.getMessage() != null
                              && e.getMessage().contains("Deferred was not retried as it was a simple value"));
        }
    }
}
