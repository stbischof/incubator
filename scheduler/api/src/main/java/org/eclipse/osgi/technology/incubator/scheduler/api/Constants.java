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
package org.eclipse.osgi.technology.incubator.scheduler.api;

public final class Constants {

    private Constants() {
        // Constants
    }

    /**
     * Specification Name
     */
    public static final String SPECIFICATION_NAME = "org.eclipse.osgi.technology.incubator.scheduler";

    /**
     * Specification Version
     */
    public static final String SPECIFICATION_VERSION = "0.0.1";

    /**
     * The service property Prefix for cronjobs
     */
    public static final String SERVICE_PROPERTY_CRONJOB_PREFIX = "cronjob.";

    /**
     * The service property that specifies the cron schedule. The type is String+.
     */
    public static final String SERVICE_PROPERTY_CRONJOB_CRON = SERVICE_PROPERTY_CRONJOB_PREFIX + "cron";

    /**
     * The service property that specifies the name of the cron job. The type is
     * String.
     */
    public static final String SERVICE_PROPERTY_CRONJOB_NAME = SERVICE_PROPERTY_CRONJOB_PREFIX + "name";

    /**
     * Default name of the cron job.
     */
    public static final String CRONJOB_NAME_DEFAULT = "unknown";

    /**
     * the named cron expression for annually execution.
     */
    public static final String CRON_EXPRESSION_ANNUALLY = "@annually";

    /**
     * the named cron expression for yearly execution.
     */
    public static final String CRON_EXPRESSION_YEARLY = "@yearly";

    /**
     * the named cron expression for monthly execution.
     */
    public static final String CRON_EXPRESSION_MONTHLY = "@monthly";
    /**
     * the named cron expression for weekly execution.
     */
    public static final String CRON_EXPRESSION_WEEKLY = "@weekly";

    /**
     * the named cron expression for daily execution.
     */
    public static final String CRON_EXPRESSION_DAILY = "@daily";

    /**
     * the named cron expression for hourly execution.
     */
    public static final String CRON_EXPRESSION_HOURLY = "@hourly";

    /**
     * the named cron expression for minutely execution.
     */
    public static final String CRON_EXPRESSION_MINUTELY = "@minutely";

    /**
     * the named cron expression for secondly execution.
     */
    public static final String CRON_EXPRESSION_SECONDLY = "@secondly";

    /**
     * the named cron expression for execution onreboot.
     */
    public static final String CRON_EXPRESSION_REBOOT = "@reboot";
}
