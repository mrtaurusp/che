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
package org.eclipse.che.ide.part.editor.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.parts.EditorPartStack;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.part.editor.multipart.EditorMultiPartStackPresenter;

import javax.validation.constraints.NotNull;

/**
 * Performs restoring closed editor tab for current editor part stack.
 *
 * @author Vlad Zhukovskiy
 * @author Roman Nikitenko
 */
@Singleton
public class ReopenClosedFileAction extends EditorAbstractAction {

    @Inject
    public ReopenClosedFileAction(EventBus eventBus,
                                  CoreLocalizationConstant locale,
                                  EditorAgent editorAgent,
                                  EditorMultiPartStackPresenter editorMultiPartStackPresenter) {
        super(locale.editorTabReopenClosedTab(), locale.editorTabReopenClosedTabDescription(), null, editorAgent,
              eventBus, editorMultiPartStackPresenter);
    }

    /** {@inheritDoc} */
    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        EditorPartPresenter currentEditor = getEditorTab(event).getRelativeEditorPart();
        EditorPartStack currentPartStack = editorMultiPartStack.getPartStackByPart(currentEditor);

        event.getPresentation().setEnabled(currentPartStack != null && currentPartStack.getLastClosed() != null);
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent event) {
        EditorPartPresenter currentEditor = getEditorTab(event).getRelativeEditorPart();
        EditorPartStack currentPartStack = editorMultiPartStack.getPartStackByPart(currentEditor);
        PartPresenter lastClosedPart = currentPartStack.getLastClosed();
        VirtualFile file = ((EditorPartPresenter)lastClosedPart).getEditorInput().getFile();

        eventBus.fireEvent(FileEvent.createOpenFileEvent(file));
    }
}
