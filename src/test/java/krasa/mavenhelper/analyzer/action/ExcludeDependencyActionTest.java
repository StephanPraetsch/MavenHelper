package krasa.mavenhelper.analyzer.action;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.impl.DomManagerImpl;

import org.jetbrains.idea.maven.model.MavenArtifactNode;
import org.jetbrains.idea.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import krasa.mavenhelper.utils.CommandProcessorRunnerForTests;
import krasa.mavenhelper.utils.DomManagerSupplier;

public class ExcludeDependencyActionTest {

    private ExcludeDependencyAction uut;

    @Mock
    private Project project;

    @Mock
    private MavenProject mavenProject;

    @Mock
    private MavenArtifactNode mavenArtifactNode;

    @Mock
    private AnActionEvent actionEvent;

    @Mock
    private VirtualFile mavenProjectFile;

    @Mock
    private PsiManager psiManager;

    @Mock
    private XmlFile xmlFile;

    @Mock
    private DomManagerSupplier domManagerSupplier;

    @Before
    public void setUp() {
        initMocks(this);
        uut = new ExcludeDependencyAction(project, mavenProject, mavenArtifactNode,
                CommandProcessorRunnerForTests.INSTANCE, domManagerSupplier) {
            @Override
            public void dependencyExcluded() {
            }
        };
        when(project.getService(PsiManager.class)).thenReturn(psiManager);
    }

    @Test
    public void name() {

        // given
        when(mavenProject.getFile()).thenReturn(mavenProjectFile);
        when(psiManager.findFile(mavenProjectFile)).thenReturn(xmlFile);
        when(domManagerSupplier.get(any())).thenReturn(new DomManagerImpl(project));

        // when
        uut.actionPerformed(actionEvent);

        // then

    }

}