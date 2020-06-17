package krasa.mavenhelper.analyzer.action;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

import org.jetbrains.idea.maven.model.MavenExplicitProfiles;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.junit.Before;

public class PluginTest extends LightJavaCodeInsightFixtureTestCase {

    private Project project;

    @Override
    protected String getTestDataPath() {
        return "/home/stephan/workspaces/github/MavenHelper/src/test/resources/krasa/mavenhelper/analyzer/action";
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.project = getProject();
    }

    protected void waitForReadingCompletion(MavenProjectsManager mavenProjectsManager) {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            try {
                mavenProjectsManager.waitForReadingCompletion();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void testSomething() throws Exception {

        myFixture.configureByFiles("pom.xml");
        VirtualFile virtualFile = myFixture.getFile().getVirtualFile();

        MavenProjectsManager mavenProjectsManager = MavenProjectsManager.getInstance(project);
        mavenProjectsManager.initForTests();

        mavenProjectsManager.resetManagedFilesAndProfilesInTests(Collections.singletonList(virtualFile), new MavenExplicitProfiles(Collections.emptyList()));
        waitForReadingCompletion(mavenProjectsManager);

        ApplicationManager.getApplication().invokeAndWait(() -> {
            mavenProjectsManager.waitForResolvingCompletion();
            mavenProjectsManager.scheduleImportInTests(Collections.singletonList(virtualFile));
            mavenProjectsManager.importProjects();
        });

        MavenProject mavenProject = mavenProjectsManager.findProject(virtualFile);

        System.out.println("pom.xml exists: " + new File(getTestDataPath() + "/pom.xml").exists());
        System.out.println("mavenProjectManager: " + mavenProjectsManager);
        System.out.println("projectsTreeForTests: "+mavenProjectsManager.getProjectsTreeForTests());
        System.out.println("virtualFile: " + virtualFile);
        System.out.println("mavenProject: " + mavenProject);
        System.out.println("basePath: " + project.getBasePath() + " with files");
        Files.walk(Paths.get(project.getBasePath())).forEach(System.out::println);

        assertThat(virtualFile).isNotNull();
        assertThat(mavenProject).isNotNull();
    }

}
