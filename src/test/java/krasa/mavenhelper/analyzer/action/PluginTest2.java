package krasa.mavenhelper.analyzer.action;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.intellij.util.ui.UIUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.model.MavenExplicitProfiles;
import org.jetbrains.idea.maven.project.MavenGeneralSettings;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectResolver;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.project.MavenProjectsTree;
import org.jetbrains.idea.maven.server.MavenServerManager;
import org.junit.Before;

public class PluginTest2 extends LightJavaCodeInsightFixtureTestCase {

    private Project project;

    // https://github.com/JetBrains/intellij-community/blob/master/plugins/maven/src/test/java/org/jetbrains/idea/maven/MavenImportingTestCase.java

    @Override
    protected String getTestDataPath() {
        return "/home/stephan/workspaces/github/MavenHelper/src/test/resources/krasa/mavenhelper/analyzer/action";
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.project = getProject();
    }

    private File ourTempDir;

    protected File myDir;

    protected Project myProject;

    protected MavenProjectsTree myProjectsTree;

    protected MavenProjectResolver myProjectResolver;

    protected MavenProjectsManager myProjectsManager;

    protected List<VirtualFile> myAllPoms = new ArrayList<>();

    protected VirtualFile myProjectPom;

    protected VirtualFile myProjectRoot;

    private void ensureTempDirCreated() throws IOException {
        if (ourTempDir != null) return;

        ourTempDir = new File(FileUtil.getTempDirectory(), "mavenTests");
        FileUtil.delete(ourTempDir);
        FileUtil.ensureExists(ourTempDir);
    }

    protected void importProject() {
        importProjectWithProfiles();
    }

    protected void importProjectWithProfiles(String... profiles) {
        doImportProjects(Collections.singletonList(myProjectPom), true, profiles);
    }

    protected void doImportProjects(final List<VirtualFile> files, boolean failOnReadingError, String... profiles) {
        initProjectsManager(false);

        readProjects(files, profiles);

        ApplicationManager.getApplication().invokeAndWait(() -> {
            myProjectsManager.waitForResolvingCompletion();
            myProjectsManager.scheduleImportInTests(files);
            myProjectsManager.importProjects();
        });

        if (failOnReadingError) {
            for (MavenProject each : myProjectsTree.getProjects()) {
                assertFalse("Failed to import Maven project: " + each.getProblems(), each.hasReadingProblems());
            }
        }
    }

    protected void readProjects(List<VirtualFile> files, String... profiles) {
        myProjectsManager.resetManagedFilesAndProfilesInTests(files, new MavenExplicitProfiles(Arrays.asList(profiles)));
        waitForReadingCompletion();
    }

    protected void waitForReadingCompletion() {
        UIUtil.invokeAndWaitIfNeeded((Runnable) () -> {
            try {
                myProjectsManager.waitForReadingCompletion();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    protected VirtualFile createProjectPom(@NotNull @Language(value = "XML", prefix = "<project>", suffix = "</project>") String xml) {
        return myProjectPom = createPomFile(myProjectRoot, xml);
    }

    protected void setUpInWriteAction() throws Exception {
        myProjectsManager = MavenProjectsManager.getInstance(myProject);
        removeFromLocalRepository("test");
        File projectDir = new File(myDir, "project");
        projectDir.mkdirs();
        myProjectRoot = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(projectDir);
    }

    protected void removeFromLocalRepository(String relativePath) {
        if (SystemInfo.isWindows) {
            MavenServerManager.getInstance().shutdown(true);
        }
        FileUtil.delete(new File(getRepositoryPath(), relativePath));
    }

    protected String getRepositoryPath() {
        String path = getRepositoryFile().getPath();
        return FileUtil.toSystemIndependentName(path);
    }

    protected File getRepositoryFile() {
        return getMavenGeneralSettings().getEffectiveLocalRepository();
    }

    protected MavenGeneralSettings getMavenGeneralSettings() {
        return MavenProjectsManager.getInstance(myProject).getGeneralSettings();
    }

    protected VirtualFile createPomFile(final VirtualFile dir,
                                        @Language(value = "XML", prefix = "<project>", suffix = "</project>") String xml) {
        VirtualFile f = dir.findChild("pom.xml");
        if (f == null) {
            try {
                f = WriteAction.computeAndWait(() -> {
                    VirtualFile res = dir.createChildData(null, "pom.xml");
                    return res;
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            myAllPoms.add(f);
        }
        setFileContent(f, createPomXml(xml), true);
        return f;
    }

    private static void setFileContent(final VirtualFile file, final String content, final boolean advanceStamps) {
        try {
            WriteAction.runAndWait(() -> {
                if (advanceStamps) {
                    file.setBinaryContent(content.getBytes(StandardCharsets.UTF_8), -1, file.getTimeStamp() + 4000);
                } else {
                    file.setBinaryContent(content.getBytes(StandardCharsets.UTF_8), file.getModificationStamp(), file.getTimeStamp());
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String createPomXml(@NonNls @Language(value = "XML", prefix = "<project>", suffix = "</project>") String xml) {
        return "<?xml version=\"1.0\"?>" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"" +
                "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">" +
                "  <modelVersion>4.0.0</modelVersion>" +
                xml +
                "</project>";
    }

    protected void initProjectsManager(boolean enableEventHandling) {
        myProjectsManager.initForTests();
        myProjectsTree = myProjectsManager.getProjectsTreeForTests();
        myProjectResolver = new MavenProjectResolver(myProjectsTree);
        if (enableEventHandling) myProjectsManager.enableAutoImportInTests();
    }

    public void testSomething() throws Exception {

        ensureTempDirCreated();
        myDir = new File(ourTempDir, getTestName(false));
        FileUtil.ensureExists(myDir);

        myFixture.configureByFiles("pom.xml");
        VirtualFile virtualFile = myFixture.getFile().getVirtualFile();

        myProject = myFixture.getProject();

        setUpInWriteAction();
        createProjectPom("<groupId>test</groupId>" +
                "<artifactId>project</artifactId>" +
                "<version>1</version>");
        importProject();
        MavenProject mavenProject = myProjectsManager.findProject(virtualFile);

//        MavenProjectsManager mavenProjectsManager = MavenProjectsManager.getInstance(project);
//        MavenProject mavenProject = mavenProjectsManager.findProject(virtualFile);

        System.out.println("pom.xml exists: " + new File(getTestDataPath() + "/pom.xml").exists());
        System.out.println("virtualFile: " + virtualFile);
        System.out.println("mavenProject: " + mavenProject);
        System.out.println("basePath: " + project.getBasePath() + " with files");
        Files.walk(Paths.get(project.getBasePath())).forEach(System.out::println);
    }

}