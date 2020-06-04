package krasa.mavenhelper.utils;

import static org.mockito.Mockito.mock;

import com.intellij.openapi.project.Project;

public class CommandProcessorRunnerForTests extends CommandProcessorRunner {

    public static CommandProcessorRunnerForTests INSTANCE = new CommandProcessorRunnerForTests();

    private CommandProcessorRunnerForTests() {
        super(mock(Project.class));
    }

    @Override
    public void runWriteAction(Runnable runnable, String commandName, String groupId) {
        runnable.run();
    }

}
