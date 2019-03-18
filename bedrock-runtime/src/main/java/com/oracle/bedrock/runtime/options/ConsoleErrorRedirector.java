/*
 * File: ConsoleErrorRedirector.java
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

package com.oracle.bedrock.runtime.options;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.console.OutputRedirector;
import com.oracle.bedrock.runtime.console.StdErrRedirector;

/**
 * An {@link Option} to set the redirector to use to redirect a
 * process stderr stream.
 * <p>
 * Copyright (c) 2019. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class ConsoleErrorRedirector
        implements Option
{
    private final OutputRedirector redirector;

    private ConsoleErrorRedirector(OutputRedirector redirector)
    {
        this.redirector = redirector == null ? new StdErrRedirector() : redirector;
    }

    /**
     * Obtain the configured {@link OutputRedirector}.
     *
     * @return  the configured {@link OutputRedirector}
     */
    public OutputRedirector getRedirector()
    {
        return redirector;
    }

    /**
     * Obtain the default {@link ConsoleErrorRedirector} option.
     *
     * @return  the default {@link ConsoleErrorRedirector} option
     */
    @OptionsByType.Default
    public static ConsoleErrorRedirector defaultRedirector()
    {
        return new ConsoleErrorRedirector(new StdErrRedirector());
    }

    /**
     * Obtain a {@link ConsoleErrorRedirector} option that uses
     * the specified {@link OutputRedirector}.
     *
     * @return a {@link ConsoleErrorRedirector} option that uses
     *         the specified {@link OutputRedirector}
     */
    public static ConsoleErrorRedirector of(OutputRedirector redirector)
    {
        return new ConsoleErrorRedirector(redirector);
    }
}
