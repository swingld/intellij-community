package org.jetbrains.idea.maven;

import org.jetbrains.idea.maven.navigator.PomTreeStructure;

import java.io.IOException;

public class BasicProjectImportingTest extends ProjectImportingTest {
  public void testSimpleProject() throws IOException {
    importProject("<groupId>mvn</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>");

    assertModules("project");
  }

  public void testProjectWithDependency() throws IOException {
    importProject("<groupId>mvn</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<dependencies>" +
                  "  <dependency>" +
                  "    <groupId>junit</groupId>" +
                  "    <artifactId>junit</artifactId>" +
                  "    <version>4.0</version>" +
                  "  </dependency>" +
                  "</dependencies>");

    assertModules("project");
  }

  public void testProjectWithProperty() throws IOException {
    importProject("<groupId>mvn</groupId>" +
                  "<artifactId>project</artifactId>" +
                  "<version>1</version>" +

                  "<dependencies>" +
                  "  <dependency>" +
                  "    <groupId>direct-system-dependency</groupId>" +
                  "    <artifactId>direct-system-dependency</artifactId>" +
                  "    <version>1.0</version>" +
                  "    <scope>system</scope>" +
                  "    <systemPath>${java.home}/lib/tools.jar</systemPath>" +
                  "  </dependency>" +
                  "</dependencies>");

    assertModules("project");
  }

  public void testProjectWithEnvProperty() throws IOException {
    importProject("<groupId>mvn</groupId>" +
                  "<artifactId>env-properties</artifactId>" +
                  "<version>1</version>" +

                  "<dependencies>" +
                  "  <dependency>" +
                  "    <groupId>direct-system-dependency</groupId>" +
                  "    <artifactId>direct-system-dependency</artifactId>" +
                  "    <version>1.0</version>" +
                  "    <scope>system</scope>" +
                  "    <systemPath>${env.JAVA_HOME}/lib/tools.jar</systemPath>" +
                  "  </dependency>" +
                  "</dependencies>");

    // This should fail when embedder will be able to handle env.XXX properties
    assertModules();
  }

  public void testModulesWithSlashesRegularAndBack() throws IOException {
    createProjectPom("<groupId>mvn.modules-with-slashes</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<packaging>pom</packaging>" +
                     "<version>1</version>" +

                     "<modules>" +
                     "  <module>dir\\m1</module>" +
                     "  <module>dir/m2</module>" +
                     "</modules>");

    createModulePom("dir/m1", "<groupId>mvn.modules-with-slashes</groupId>" +
                              "<artifactId>m1</artifactId>" +
                              "<version>1</version>");

    createModulePom("dir/m2", "<groupId>mvn.modules-with-slashes</groupId>" +
                              "<artifactId>m2</artifactId>" +
                              "<version>1</version>");

    importProject();
    assertModules("project", "m1", "m2");

    PomTreeStructure.RootNode r = createMavenTree();

    assertEquals(1, r.pomNodes.size());
    assertEquals("project", r.pomNodes.get(0).mavenProject.getArtifactId());

    assertEquals(2, r.pomNodes.get(0).modulePomsNode.pomNodes.size());
  }

  public void testModulesWithSameArtifactId() throws Exception {
    createProjectPom("<groupId>mvn</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<packaging>pom</packaging>" +
                     "<version>1</version>" +

                     "<modules>" +
                     "  <module>dir1/m</module>" +
                     "  <module>dir2/m</module>" +
                     "</modules>");

    createModulePom("dir1/m", "<groupId>mvn.group1</groupId>" +
                              "<artifactId>m</artifactId>" +
                              "<version>1</version>");

    createModulePom("dir2/m", "<groupId>mvn.group2</groupId>" +
                              "<artifactId>m</artifactId>" +
                              "<version>1</version>");

    importProject();
    assertModules("project", "m (mvn.group1)", "m (mvn.group2)");
  }

  public void testTestJarDependencies() throws Exception {
    createProjectPom("<groupId>mvn</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<dependencies>" +
                     "   <dependency>" +
                     "    <groupId>group</groupId>" +
                     "    <artifactId>artifact</artifactId>" +
                     "    <type>test-jar</type>" +
                     "    <version>1</version>" +
                     "  </dependency>" +
                     "</dependencies>");

    importProject();
    assertModules("project");
    assertModuleLibraries("project", "group:artifact:1:tests");
  }

  public void testDependencyWithClassifier() throws IOException {
    createProjectPom("<groupId>mvn</groupId>" +
                     "<artifactId>project</artifactId>" +
                     "<version>1</version>" +

                     "<dependencies>" +
                     "   <dependency>" +
                     "    <groupId>group</groupId>" +
                     "    <artifactId>artifact</artifactId>" +
                     "    <classifier>bar</classifier>" +
                     "    <version>1</version>" +
                     "  </dependency>" +
                     "</dependencies>");

    importProject();
    assertModules("project");
    assertModuleLibraries("project", "group:artifact:1:bar");
  }
}
