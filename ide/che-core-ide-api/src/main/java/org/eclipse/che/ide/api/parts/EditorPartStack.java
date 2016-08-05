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
package org.eclipse.che.ide.api.parts;

import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.resource.Path;

import javax.validation.constraints.NotNull;

/**
 * Part Stack is tabbed layout element, containing Parts. EditorPartStack is shared
 * across the Perspectives and allows to display EditorParts
 *
 * @author Nikolay Zamosenchuk
 */
public interface EditorPartStack extends PartStack {

    EditorPartPresenter getPartByTabId(@NotNull String tabId);

    PartPresenter getPartByPath(Path path);
    PartPresenter getNextFor(EditorPartPresenter editorPart);
    PartPresenter getPreviousFor(EditorPartPresenter editorPart);
    PartPresenter getLastClosed();
}
