package krasa.mavenhelper.utils;

import com.intellij.openapi.project.Project;
import com.intellij.util.xml.DomManager;

public interface DomManagerSupplier {

    DomManagerSupplier DEFAULT = DomManager::getDomManager;

    DomManager get(Project project);

}
