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
package org.eclipse.osgi.technology.incubator.scheduler.api.whiteboard.prototypes;

import org.eclipse.osgi.technology.incubator.scheduler.api.Constants;
import org.eclipse.osgi.technology.incubator.scheduler.api.whiteboard.annotations.RequireSchedulerImplementation;
import org.osgi.service.component.annotations.ComponentPropertyType;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * An annotation to simplify using a CronJob
 */
@ComponentPropertyType
@ObjectClassDefinition
@RequireSchedulerImplementation
public @interface CronExpression {

    public static final String PREFIX_ = Constants.SERVICE_PROPERTY_CRONJOB_PREFIX;

    /**
     * The 'cronjob.cron' service property as defines in
     * {@link Constants#SERVICE_PROPERTY_CRONJOB_CRON}
     * 
     * @return
     */
    @AttributeDefinition(name = "cronJobCronExpression", description = "Cron Expression according the Cron Spec. see http://en.wikipedia.org/wiki/Cron")
    String[] cron();

    /**
     * The 'cronjob.name' service property as defines in
     * {@link Constants#SERVIC_PROPERTY_CRON_NAME()()}
     * 
     * @return
     */
    @AttributeDefinition(name = "cronJobName", description = "Human readable name of the cronjob")
    String name() default Constants.CRONJOB_NAME_DEFAULT;

}
