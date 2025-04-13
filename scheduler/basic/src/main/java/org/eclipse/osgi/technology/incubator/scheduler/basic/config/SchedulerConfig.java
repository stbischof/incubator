/**
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     Stefan Bischof - initial
 */
package org.eclipse.osgi.technology.incubator.scheduler.basic.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(description = "Configuration for the scheduler. The scheduler is a singleton")
public @interface SchedulerConfig {

    /**
     * The PID of the configuration
     */
    String PID = "org.eclipse.osgi.technology.incubator.scheduler.basic";

    String TIMEZONE_DEFAULT = "system.default.timezone";
    String TIMEZONE_DESCRIPTION = "Set the time zone. The default is the system default time zone. "
            + "If the time zone does not exist, an error is logged and the system default time zone is used.";

    int COREPOOLSIZE_DEFAUL = 50;
    public static final String COREPOOLSIZE_DESCRIPTION = "corePoolSize of ScheduledThreadPoolExecutor";

    int SHUTDOWNTIMEOUT_SOFT_DEFAUL = 500;
    public static final String SHUTDOWNTIMEOUT_SOFT_DESCRIPTION = "Expected shutdownTimeout of ScheduledThreadPoolExecutor";

    int SHUTDOWNTIMEOUT_HARD_DEFAUL = 5000;
    public static final String SHUTDOWNTIMEOUT_HARD_DESCRIPTION = "Expected shutdownTimeout after the shutdownTimeoutSoft befor the shutdown of ScheduledThreadPoolExecutor is forced";

    @AttributeDefinition(description = TIMEZONE_DESCRIPTION)
    String timeZone() default TIMEZONE_DEFAULT;

    @AttributeDefinition(description = COREPOOLSIZE_DESCRIPTION)
    int corePoolSize() default COREPOOLSIZE_DEFAUL;

    @AttributeDefinition(description = SHUTDOWNTIMEOUT_SOFT_DESCRIPTION)
    int shutdownTimeoutSoft() default SHUTDOWNTIMEOUT_SOFT_DEFAUL;

    @AttributeDefinition(description = SHUTDOWNTIMEOUT_HARD_DESCRIPTION)
    int shutdownTimeoutHard() default SHUTDOWNTIMEOUT_HARD_DEFAUL;
}