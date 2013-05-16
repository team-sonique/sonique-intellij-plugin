package sonique.intellij.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerAdapter;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.components.JBList;

import javax.swing.*;
import java.awt.*;

import static com.intellij.ui.SimpleTextAttributes.REGULAR_ATTRIBUTES;
import static com.intellij.ui.SimpleTextAttributes.REGULAR_ITALIC_ATTRIBUTES;

public class RecentProjectsAction extends AnAction implements DumbAware {

    private final DefaultListModel listModel;
    private final ProjectManager projectManager;

    public RecentProjectsAction() {
        projectManager = ProjectManager.getInstance();
        listModel = new DefaultListModel();
        initialiseProjectModel(projectManager);
        projectManager.addProjectManagerListener(new ProjectListener());
    }

    private void initialiseProjectModel(ProjectManager projectManager) {
        for (Project openProject : projectManager.getOpenProjects()) {
            listModel.addElement(openProject);
        }
    }

    public void actionPerformed(AnActionEvent e) {
        Project currentProject = e.getProject();
        listModel.removeElement(currentProject);
        listModel.add(0, currentProject);
        JList projectList = new JBList(listModel);

        if (listModel.getSize() > 1) {
            projectList.setSelectedIndex(1);
        } else {
            projectList.setSelectedIndex(0);
        }

        projectList.setCellRenderer(new RecentProjectsRenderer());
        JBPopup popup = new PopupChooserBuilder(projectList)
                .setTitle("Recent Projects")
                .setItemChoosenCallback(new SelectProjectRunnable(projectList))
                .createPopup();

        popup.showCenteredInCurrentWindow(currentProject);
    }


    private class SelectProjectRunnable implements Runnable {
        private final JList list;

        public SelectProjectRunnable(JList list) {
            this.list = list;
        }

        @Override
        public void run() {
            Project selectedProject = (Project) list.getSelectedValue();
            listModel.removeElement(selectedProject);
            listModel.add(0, selectedProject);

            JFrame projectFrame = WindowManager.getInstance().getFrame(selectedProject);

            int frameState = projectFrame.getExtendedState();
            if ((frameState & Frame.ICONIFIED) == Frame.ICONIFIED) {
                // restore the frame if it is minimized
                projectFrame.setExtendedState(frameState ^ Frame.ICONIFIED);
            }
            projectFrame.toFront();
            projectFrame.requestFocus();
        }
    }

    private class ProjectListener extends ProjectManagerAdapter {

        @Override
        public void projectOpened(Project project) {
            listModel.addElement(project);
        }

        @Override
        public void projectClosed(Project project) {
            listModel.removeElement(project);
        }
    }

    private class RecentProjectsRenderer extends ColoredListCellRenderer {
        @Override
        protected void customizeCellRenderer(JList jList, Object value, int index, boolean isSelected, boolean hasFocus) {
            if (value instanceof Project) {
                setIcon(IconLoader.findIcon("/nodes/ideaProject.png"));
                Project project = (Project) value;

                append(project.getName(), REGULAR_ATTRIBUTES, true);
                append("   ");
                append("[" + project.getPresentableUrl() + "]", REGULAR_ITALIC_ATTRIBUTES);
            }
        }
    }
}
