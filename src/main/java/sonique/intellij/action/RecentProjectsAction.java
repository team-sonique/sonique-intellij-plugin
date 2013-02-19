package sonique.intellij.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.components.JBList;

import javax.swing.*;
import java.awt.*;

public class RecentProjectsAction extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();

        DefaultListModel model = new DefaultListModel();
        for (Project openProject : openProjects) {
            model.addElement(openProject);
        }

        JList list = new JBList(model);
        list.setCellRenderer(new RecentProjectsRenderer());

        JBPopup popup = new PopupChooserBuilder(list)
                .setTitle("Recent Projects")
                .setItemChoosenCallback(new SelectProjectRunnable(list))
                .createPopup();

        popup.showCenteredInCurrentWindow(e.getProject());
    }

    private static class RecentProjectsRenderer extends ColoredListCellRenderer {
        @Override
        protected void customizeCellRenderer(JList jList, Object value, int i, boolean b, boolean b2) {
            if(value instanceof Project) {
                setIcon(IconLoader.findIcon("/nodes/ideaProject.png"));
                Project project = (Project) value;
                append(project.getName());
            }
        }
    }

    private static class SelectProjectRunnable implements Runnable {
        private final JList list;

        public SelectProjectRunnable(JList list) {
            this.list = list;
        }

        @Override
        public void run() {
            Project selectedProject = (Project) list.getSelectedValue();
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
}
