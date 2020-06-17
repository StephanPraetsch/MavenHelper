package krasa.mavenhelper.analyzer.action;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.DomManager;
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
    private VirtualFile virtualFile;

    @Mock
    private DomManagerSupplier domManagerSupplier;

    @Mock
    private DomManager domManager;

    @Mock
    private PsiManager psiManager;

    @Mock
    private XmlFile xmlFile;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        psiManager = new PsiManagerImpl(project);
        when(project.getService(PsiManager.class)).thenReturn(psiManager);
        when(mavenProject.getFile()).thenReturn(virtualFile);
        when(psiManager.findFile(virtualFile)).thenReturn(xmlFile);
        domManager = new DomManagerImpl(project);
        when(domManagerSupplier.get(project)).thenReturn(domManager);
    }

    @Test
    public void testExclusion() {

        // given
//        DomFileElement<MavenDomProjectModel> fileElement = new DomFileElementImpl(
//                xmlFile,
//                EvaluatedXmlNameImpl.createEvaluatedXmlName(new XmlName("localName"), "namespaceKey", false),
//                new DomFileDescription(),
//                new FileStub());
//        when(domManager.getFileElement(xmlFile, MavenDomProjectModel.class)).thenReturn(fileElement);

        uut = new ExcludeDependencyAction(project, mavenProject, mavenArtifactNode,
                CommandProcessorRunnerForTests.INSTANCE, domManagerSupplier) {
            @Override
            public void dependencyExcluded() {
            }
        };


        // when
        uut.actionPerformed(actionEvent);

        // then

    }

}