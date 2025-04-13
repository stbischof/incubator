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

/**
 * Singleton exception thrown when a scheduler is shutdown and kills the tasks
 */
public class ShutdownException extends Throwable {
    private static final long serialVersionUID = 1L;

    private ShutdownException() {
    }

    /**
     * The singleton exception
     */
    public static ShutdownException SINGLETON = new ShutdownException();

}
