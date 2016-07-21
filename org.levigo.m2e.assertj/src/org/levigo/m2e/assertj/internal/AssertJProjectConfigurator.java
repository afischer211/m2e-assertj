/*******************************************************************************
 * Copyright (c) 2008 levigo solutions gmbh All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies
 * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.levigo.m2e.assertj.internal;

import java.io.File;

import org.apache.maven.plugin.MojoExecution;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.lifecyclemapping.model.IPluginExecutionMetadata;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.AbstractBuildParticipant;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.m2e.jdt.AbstractSourcesGenerationProjectConfigurator;
import org.eclipse.m2e.jdt.IClasspathDescriptor;
import org.eclipse.m2e.jdt.IClasspathEntryDescriptor;

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
  
  // super implementation needs to be overridden just to use the test output location instead of the main one
  public void configureRawClasspath(ProjectConfigurationRequest request, IClasspathDescriptor classpath,
      IProgressMonitor monitor) throws CoreException {
    IMavenProjectFacade facade = request.getMavenProjectFacade();

    assertHasNature(request.getProject(), JavaCore.NATURE_ID);

    for(MojoExecution mojoExecution : getMojoExecutions(request, monitor)) {
      File[] sources = getSourceFolders(request, mojoExecution, monitor);

      for(File source : sources) {
        IPath sourcePath = getFullPath(facade, source);

        if(sourcePath != null) {
          IClasspathEntryDescriptor entry = classpath.addSourceEntry(sourcePath, facade.getTestOutputLocation(), true);
          entry.setClasspathAttribute(IClasspathAttribute.IGNORE_OPTIONAL_PROBLEMS, "true"); //$NON-NLS-1$
        }
      }
    }
  }
}
