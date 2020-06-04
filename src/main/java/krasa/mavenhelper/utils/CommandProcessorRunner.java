package krasa.mavenhelper.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;

public class CommandProcessorRunner {

    private final Project myProject;

    public CommandProcessorRunner(Project myProject) {
        this.myProject = myProject;
    }

    public void runWriteAction(Runnable runnable, String commandName, String groupId) {
        CommandProcessor.getInstance().executeCommand(myProject,
                () -> ApplicationManager.getApplication().runWriteAction(runnable), commandName, groupId);
    }

}
