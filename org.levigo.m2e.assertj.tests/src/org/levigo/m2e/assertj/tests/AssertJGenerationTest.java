package org.levigo.m2e.assertj.tests;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.project.ResolverConfiguration;
import org.eclipse.m2e.tests.common.AbstractMavenProjectTestCase;
import org.eclipse.m2e.tests.common.ClasspathHelpers;

@SuppressWarnings( "restriction" )
public class AssertJGenerationTest
    extends AbstractMavenProjectTestCase
{
    public void test_p001_simple()
        throws Exception
    {
        ResolverConfiguration configuration = new ResolverConfiguration();
        IProject project1 = importProject( "projects/assertj/assertj-p001/pom.xml", configuration );
        waitForJobsToComplete();

        project1.build( IncrementalProjectBuilder.FULL_BUILD, monitor );
        project1.build( IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor );
        waitForJobsToComplete();

        assertNoErrors( project1 );

        IJavaProject javaProject1 = JavaCore.create( project1 );
        IClasspathEntry[] cp1 = javaProject1.getRawClasspath();

        ClasspathHelpers.assertClasspath( new String[] { "/assertj-p001/src/main/java", //
            "/assertj-p001/src/test/java", //
            "org.eclipse.jdt.launching.JRE_CONTAINER/.*", //
            "org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER", //
            "/assertj-p001/target/generated-test-sources/assertj-assertions" //
        }, cp1 );

        assertTrue( project1.getFile( "target/generated-test-sources/assertj-assertions/test/Assertions.java" ).isSynchronized( IResource.DEPTH_ZERO ) );
        assertTrue( project1.getFile( "target/generated-test-sources/assertj-assertions/test/Assertions.java" ).isAccessible() );
    }

    private static void deleteRecursively(File file) {
      if (file.isDirectory())
        for (final File f : file.listFiles())
          deleteRecursively(f);
      file.delete();
    }
    
    public void testThat_Issue_1_a_nonexisting_target_does_not_cause_NPE()
        throws Exception
    {
      ResolverConfiguration configuration = new ResolverConfiguration();
      deleteRecursively(new File("projects/assertj/assertj-p001/target"));
      IProject project1 = importProject( "projects/assertj/assertj-p001/pom.xml", configuration );
      waitForJobsToComplete();
      
      project1.build( IncrementalProjectBuilder.FULL_BUILD, monitor );
      project1.build( IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor );
      waitForJobsToComplete();
      
      assertNoErrors( project1 );
      
      IJavaProject javaProject1 = JavaCore.create( project1 );
      IClasspathEntry[] cp1 = javaProject1.getRawClasspath();
      
      ClasspathHelpers.assertClasspath( new String[] { "/assertj-p001/src/main/java", //
          "/assertj-p001/src/test/java", //
          "org.eclipse.jdt.launching.JRE_CONTAINER/.*", //
          "org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER", //
          "/assertj-p001/target/generated-test-sources/assertj-assertions" //
      }, cp1 );
      
      assertTrue( project1.getFile( "target/generated-test-sources/assertj-assertions/test/Assertions.java" ).isSynchronized( IResource.DEPTH_ZERO ) );
      assertTrue( project1.getFile( "target/generated-test-sources/assertj-assertions/test/Assertions.java" ).isAccessible() );
    }

}
