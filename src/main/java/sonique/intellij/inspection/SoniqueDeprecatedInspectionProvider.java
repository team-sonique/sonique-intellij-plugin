package sonique.intellij.inspection;

import com.intellij.codeInspection.InspectionToolProvider;

public class SoniqueDeprecatedInspectionProvider implements InspectionToolProvider {
    @Override
    public Class[] getInspectionClasses() {
        return new Class[]{
                SoniqueDeprecatedInspection.class
        };
    }
}