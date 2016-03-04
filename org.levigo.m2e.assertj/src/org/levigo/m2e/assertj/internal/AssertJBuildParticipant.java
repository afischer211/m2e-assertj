/*******************************************************************************
 * Copyright (c) 2008 levigo solutions gmbh All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies
 * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.levigo.m2e.assertj.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.Scanner;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.project.configurator.MojoExecutionBuildParticipant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.plexus.build.incremental.BuildContext;

public class AssertJBuildParticipant extends MojoExecutionBuildParticipant {
  private static final Logger log = LoggerFactory.getLogger(AssertJBuildParticipant.class);

  private static final IMaven maven = MavenPlugin.getMaven();

  public AssertJBuildParticipant(MojoExecution execution) {
    super(execution, true);
  }

  private static class ClassFileMatcher {
    private List<String> packages = new ArrayList<String>();

    private List<String> classes = new ArrayList<String>();

    private List<Pattern> includes = new ArrayList<Pattern>();

    private List<Pattern> excludes = new ArrayList<Pattern>();

    public ClassFileMatcher(List<String> packages, List<String> classes, List<String> includes, List<String> excludes) {
      if (null != packages)
        this.packages.addAll(packages);

      if (null != classes)
        this.classes.addAll(classes);

      if (null != includes)
        for (String s : includes)
          try {
            this.includes.add(Pattern.compile(s));
          } catch (Exception e) {
            log.warn("Ingoring invalid include pattern " + s + ": " + e.getMessage());
          }

      if (null != excludes)
        for (String s : excludes)
          try {
            this.excludes.add(Pattern.compile(s));
          } catch (Exception e) {
            log.warn("Ingoring invalid exclude pattern " + s + ": " + e.getMessage());
          }

      // add default excludes
      this.excludes.add(Pattern.compile(".*Assert(ions)?"));
    }

    public boolean matches(String fileName) {
      if (!fileName.endsWith(".class"))
        return false;

      String className = fileName.substring(0, fileName.length() - 6).replace(File.separatorChar, '.');
      for (String p : packages) {
        if (className.startsWith(p)) {
          return isIncluded(className) && !isExcluded(className);
        }
      }
      for (String c : classes) {
        if (className.equals(c)) {
          return isIncluded(className) && !isExcluded(className);
        }
      }
      return false;
    }

    private boolean isIncluded(String className) {
      if (includes.isEmpty())
        return true;
      for (Pattern includePattern : includes) {
        if (includePattern.matcher(className).matches())
          return true;
      }
      log.debug("Ignoring " + className + " as it does not match any include regex.");
      return false;
    }

    private boolean isExcluded(String className) {
      if (excludes.isEmpty())
        return false;
      for (Pattern excludePattern : excludes) {
        if (excludePattern.matcher(className).matches()) {
          log.debug("Ignoring " + className + " as it matches exclude regex : " + excludePattern);
          return true;
        }
      }
      return false;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<IProject> build(int kind, IProgressMonitor monitor) throws Exception {
    final BuildContext buildContext = getBuildContext();

    MavenProject mavenProject = getMavenProjectFacade().getMavenProject(monitor);

    List<String> classpathElements = new ArrayList<String>(mavenProject.getCompileClasspathElements());
    classpathElements.addAll(mavenProject.getTestClasspathElements());

    ClassFileMatcher deltaScanner = new ClassFileMatcher(//
        (List<String>) getMojoParameterValue("packages", List.class, monitor),
        (List<String>) getMojoParameterValue("classes", List.class, monitor),
        (List<String>) getMojoParameterValue("includes", List.class, monitor),
        (List<String>) getMojoParameterValue("excludes", List.class, monitor));

    boolean foundDelta = false;
    for (String classpathElement : classpathElements) {
      File f = new File(classpathElement);
      if (f.isDirectory()) {
        String[] deletions = buildContext.newDeleteScanner(f).getIncludedFiles();
        if (null != deletions && deletions.length > 0) {
          log.info("###################### Found deletion in " + f);

          // clean out target
          File generated = getTargetDir(monitor);
          for (File g : generated.listFiles()) {
            deleteRecursively(g);
          }

          foundDelta = true;
          break;
        } else {
          Scanner ds = buildContext.newScanner(f);
          ds.scan();
          String[] includedFiles = ds.getIncludedFiles();
          if (includedFiles != null)
            for (String file : includedFiles) {
              foundDelta |= deltaScanner.matches(file);
              log.info("###################### Found matching class file " + file + ": " + foundDelta);
              break;
            }
        }
        log.info("###################### Check for delta in " + f + ": " + foundDelta);
      }

      if (foundDelta)
        break;
    }

    if (!foundDelta) {
      log.info("No changes");
      return null;
    }

    log.info("Running template generation");

    // execute mojo
    Set<IProject> result = super.build(kind, monitor);

    // tell m2e builder to refresh generated files
    final File generated = getTargetDir(monitor);
    if (generated != null) {
      buildContext.refresh(generated);

      /*
       * For some weird reason the java build triggered by buildContext.refresh(generated) above does not (yet) see the
       * changed class file (although we detected the changed class file result). This causes compile errors upon
       * added/removed/updated fields and properties. We schedule another workspace refresh to correct this situation.
       */
      new Job("Refresh generated assertions") {
        @Override
        protected IStatus run(IProgressMonitor monitor) {
          log.info("Refreshing generated assertions");

          IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
          try {
            IJobManager jobManager = Job.getJobManager();
            jobManager.join(ResourcesPlugin.FAMILY_MANUAL_BUILD, monitor);
            jobManager.join(ResourcesPlugin.FAMILY_AUTO_BUILD, monitor);

            myWorkspaceRoot.getFolder(Path.fromOSString(generated.getAbsoluteFile().getCanonicalPath())).refreshLocal(
                IResource.DEPTH_INFINITE, monitor);
          } catch (Exception e) {
            log.error("Failed to refresh the generated assertions", e);
          }
          return Status.OK_STATUS;
        }
      }.schedule();
    }

    return result;
  }

  /**
   * Recursively delete a file or directory.
   * 
   * @param file
   */
  private static void deleteRecursively(File file) {
    if (file.isDirectory())
      for (final File f : file.listFiles())
        deleteRecursively(f);
    file.delete();
  }

  private File getTargetDir(IProgressMonitor monitor) throws CoreException {
    File generated = getMojoParameterValue("targetDir", File.class, monitor);
    return generated;
  }

  private <T> T getMojoParameterValue(String name, Class<T> type, IProgressMonitor monitor) throws CoreException {
    MavenProject mavenProject = getMavenProjectFacade().getMavenProject(monitor);
    return maven.getMojoParameterValue(mavenProject, getMojoExecution(), name, type, monitor);
  }
}
