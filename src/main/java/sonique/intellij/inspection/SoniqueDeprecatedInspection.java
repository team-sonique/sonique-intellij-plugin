package sonique.intellij.inspection;

import com.intellij.codeInspection.BaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class SoniqueDeprecatedInspection extends BaseJavaLocalInspectionTool {
    private static final String SONIQUE_INSPECTIONS_GROUP_NAME = "Sonique Inspections";
    private static final String SONIQUE_DEPRECATED_INSPECTION_DISPLAY_NAME = "Sonique Deprecated Inspection";
    private static final String SONIQUE_DEPRECATED_SHORT_NAME = "SoniqueDeprecated";

    @Nls
    @NotNull
    @Override
    public String getGroupDisplayName() {
        return SONIQUE_INSPECTIONS_GROUP_NAME;
    }

    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return SONIQUE_DEPRECATED_INSPECTION_DISPLAY_NAME;
    }

    @NotNull
    @Override
    public String getShortName() {
        return SONIQUE_DEPRECATED_SHORT_NAME;
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }


    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new SoniqueDeprecationElementVisitor(holder, false, false);
    }
}
