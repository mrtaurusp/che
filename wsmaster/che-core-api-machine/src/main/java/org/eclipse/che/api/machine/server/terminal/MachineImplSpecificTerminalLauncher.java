/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.machine.server.terminal;

import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.spi.Instance;

/**
 * Machine implementation specific launcher of websocket terminal.
 *
 * @author Alexander Garagatyi
 */
public interface MachineImplSpecificTerminalLauncher {
    /**
     * Type of machine implementation this terminal fits.
     */
    String getMachineType();

    /**
     * Starts websocket terminal inside of machine.
     */
    void launchTerminal(Instance machine) throws MachineException;
}
