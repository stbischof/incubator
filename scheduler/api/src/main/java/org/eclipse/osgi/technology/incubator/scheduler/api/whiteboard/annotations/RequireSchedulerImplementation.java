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
package org.eclipse.osgi.technology.incubator.scheduler.api.whiteboard.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.eclipse.osgi.technology.incubator.scheduler.api.Constants;
import org.osgi.namespace.implementation.ImplementationNamespace;

/**
 * Require an implementation for the this specification
 */

@org.osgi.annotation.bundle.Requirement(effective = "active", namespace = ImplementationNamespace.IMPLEMENTATION_NAMESPACE, name = Constants.SPECIFICATION_NAME, version = Constants.SPECIFICATION_VERSION)
@Retention(RetentionPolicy.CLASS)
public @interface RequireSchedulerImplementation {
}