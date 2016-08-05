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
package org.eclipse.che.ide.part.editor.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fires when editor tab either pinned or not.
 *
 * @author Vlad Zhukovskiy
 */
public class PinEditorTabEvent extends GwtEvent<PinEditorTabEvent.PinEditorTabEventHandler> {

    public interface PinEditorTabEventHandler extends EventHandler {
        void onEditorTabPinned(PinEditorTabEvent event);
    }

    private static Type<PinEditorTabEventHandler> TYPE;

    public static Type<PinEditorTabEventHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<>();
        }
        return TYPE;
    }

    private final String  tabId;
    private final boolean pin;

    public PinEditorTabEvent(String tabId, boolean pin) {
        this.tabId = tabId;
        this.pin = pin;
    }

    /** {@inheritDoc} */
    @Override
    public Type<PinEditorTabEventHandler> getAssociatedType() {
        return getType();
    }

    /**
     * Return tab's ID associated with pin operation.
     *
     * @return tab's ID
     */
    public String getTabId() {
        return tabId;
    }

    /**
     * Return true if opened file should be pinned.
     *
     * @return true if opened file should be pinned
     */
    public boolean isPin() {
        return pin;
    }

    /** {@inheritDoc} */
    @Override
    protected void dispatch(PinEditorTabEventHandler handler) {
        handler.onEditorTabPinned(this);
    }
}
