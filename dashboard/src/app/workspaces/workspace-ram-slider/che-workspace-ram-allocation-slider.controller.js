/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

/**
 * Controller for the Workspace Ram slider
 * @author Florent Benoit
 */
export class CheWorkspaceRamAllocationSliderController {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor ($timeout) {
    "ngInject";
    this.$timeout = $timeout;
  }

  onChange() {
    if (!this.inputVal) {
      return;
    }
    this.ngModel = this.inputVal * 1000;

    this.$timeout(() => {
      this.cheOnChange();
    });
  }
}
