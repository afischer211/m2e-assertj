/*******************************************************************************
 * Copyright (c) 2008 levigo solutions gmbh All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies
 * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.levigo.m2e.assertj.internal;

import org.apache.maven.plugin.MojoExecution;
import org.eclipse.m2e.core.lifecyclemapping.model.IPluginExecutionMetadata;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.AbstractBuildParticipant;
import org.eclipse.m2e.jdt.AbstractSourcesGenerationProjectConfigurator;

public class AssertJProjectConfigurator extends AbstractSourcesGenerationProjectConfigurator {
  @Override
  public AbstractBuildParticipant getBuildParticipant(IMavenProjectFacade projectFacade, MojoExecution execution,
      IPluginExecutionMetadata executionMetadata) {
    return new AssertJBuildParticipant(execution);
  }

  @Override
  protected String getOutputFolderParameterName() {
    return "targetDir";
  }
}
