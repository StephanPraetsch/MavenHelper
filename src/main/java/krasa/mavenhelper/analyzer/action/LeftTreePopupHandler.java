package krasa.mavenhelper.analyzer.action;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.ui.PopupHandler;

import java.awt.*;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.jetbrains.idea.maven.model.MavenArtifact;
import org.jetbrains.idea.maven.model.MavenArtifactNode;
import org.jetbrains.idea.maven.project.MavenProject;

import krasa.mavenhelper.analyzer.MyTreeUserObject;
import krasa.mavenhelper.utils.CommandProcessorRunner;
import krasa.mavenhelper.utils.DomManagerSupplier;

/**
 * @author Vojtech Krasa
 */
public class LeftTreePopupHandler extends PopupHandler {
	private final Project project;
	private final MavenProject mavenProject;
	protected final JTree tree;
    private JPopupMenu popup;
    private final CommandProcessorRunner commandProcessorRunner;
    private final DomManagerSupplier domManagerSupplier;

	public LeftTreePopupHandler(Project project, MavenProject mavenProject, JTree tree) {
		this.project = project;
		this.mavenProject = mavenProject;
		this.tree = tree;
		this.commandProcessorRunner = new CommandProcessorRunner(project);
		this.domManagerSupplier = DomManagerSupplier.DEFAULT;
	}

	private DefaultMutableTreeNode getRoot() {
		return (DefaultMutableTreeNode) getModel().getRoot();
	}

	private DefaultTreeModel getModel() {
		return (DefaultTreeModel) tree.getModel();
	}

	@SuppressWarnings("Duplicates")
	public void invokePopup(final Component comp, final int x, final int y) {
		final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		if (selectedNode == null) {
			return;
		}
		final MyTreeUserObject myTreeUserObject = (MyTreeUserObject) selectedNode.getUserObject();
		final MavenArtifactNode mavenArtifactNode = myTreeUserObject.getMavenArtifactNode();
		DefaultActionGroup actionGroup = new DefaultActionGroup();

		if (myTreeUserObject.getMavenArtifactNode().getParent() == null) {
			actionGroup.add(new JumpToSourceAction(project, mavenProject, mavenArtifactNode, domManagerSupplier));
			actionGroup.add(new RemoveDependencyAction(project, mavenProject, mavenArtifactNode, domManagerSupplier) {
				@Override
				public void dependencyDeleted() {
					getModel().removeNodeFromParent(selectedNode);
				}
			});
		} else {
			actionGroup.add(new JumpToSourceAction(project, mavenProject, mavenArtifactNode, domManagerSupplier));
			actionGroup.add(new ExcludeDependencyAction(project, mavenProject, mavenArtifactNode, commandProcessorRunner, domManagerSupplier) {

				@Override
				public void dependencyExcluded() {//
					// root
					// | d1
					// | L d2
					// | | L d3
					// | L d3
					// L ...
					// After d3 is excluded; remove d3 in the whole subtree.
					// getModel().removeNodeFromParent(selectedNode);

					DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) selectedNode.getPath()[1];
					Enumeration enumeration = treeNode.breadthFirstEnumeration();
					MavenArtifact excludedArtifact = myArtifact.getArtifact();
					java.util.List<DefaultMutableTreeNode> toRemove = new ArrayList<DefaultMutableTreeNode>();
					while (enumeration.hasMoreElements()) {
						DefaultMutableTreeNode o = (DefaultMutableTreeNode) enumeration.nextElement();
						MyTreeUserObject userObject = (MyTreeUserObject) o.getUserObject();
						MavenArtifact artifact = userObject.getArtifact();
						if (artifact.getArtifactId().equals(excludedArtifact.getArtifactId())
								&& artifact.getGroupId().equals(excludedArtifact.getGroupId())) {
							toRemove.add(o);
						}
					}
					for (DefaultMutableTreeNode node : toRemove) {
						getModel().removeNodeFromParent(node);
					}

				}
			});
		}
		popup = ActionManager.getInstance().createActionPopupMenu("", actionGroup).getComponent();
		popup.show(comp, x, y);
	}

	public void hidePopup() {
		if (popup != null && popup.isVisible()) {
			popup.setVisible(false);
			popup = null;
		}
	}
	

}
