package sonique.intellij.action;

import com.intellij.codeInsight.generation.*;
import com.intellij.ide.util.MemberChooser;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.codeStyle.VariableKind;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GenerateWithMethodHandler extends GenerateSetterHandler {

    private final MethodNameGenerator methodNameGenerator;

    public GenerateWithMethodHandler(MethodNameGenerator methodNameGenerator) {
        this.methodNameGenerator = methodNameGenerator;
    }

    @Nullable
    @Override
    protected ClassMember[] chooseMembers(ClassMember[] classMembers, boolean allowEmptySelection, boolean copyJavadocCheckbox, Project project, @Nullable Editor editor) {
        MemberChooser<ClassMember> chooser = new MemberChooser<ClassMember>(classMembers, allowEmptySelection, true, project);
        chooser.setTitle("Select Fields to Generate With Methods");
        chooser.setCopyJavadocVisible(copyJavadocCheckbox);
        chooser.show();
        myToCopyJavaDoc = chooser.isCopyJavadoc();
        List<ClassMember> list = chooser.getSelectedElements();
        return list == null ? null : list.toArray(new ClassMember[list.size()]);
    }

    @Override
    protected GenerationInfo[] generateMemberPrototypes(PsiClass psiClass, ClassMember classMember) throws IncorrectOperationException {
        if (classMember instanceof PsiFieldMember) {
            GenerationInfo withMethod = generateWithMethodFor(((PsiFieldMember) classMember).getElement());
            if (withMethod != null) {
                return new GenerationInfo[]{withMethod};
            }
        }
        return GenerationInfo.EMPTY_ARRAY;
    }

    private GenerationInfo generateWithMethodFor(PsiField field) {
        PsiMethod templateMethod = generateMethodPrototype(field);
        PsiMethod existingMethod = field.getContainingClass().findMethodBySignature(templateMethod, false);

        return existingMethod == null ? new PsiGenerationInfo(templateMethod) : null;
    }

    private PsiMethod generateMethodPrototype(PsiField field) {
        Project project = field.getProject();
        JavaCodeStyleManager codeStyleManager = JavaCodeStyleManager.getInstance(project);
        PsiElementFactory elementFactory = JavaPsiFacade.getInstance(project).getElementFactory();

        String propertyName = codeStyleManager.variableNameToPropertyName(field.getName(), codeStyleManager.getVariableKind(field));
        String methodName = methodNameGenerator.generateMethodNameFor(propertyName);
        String parameterName = codeStyleManager.propertyNameToVariableName(propertyName, VariableKind.PARAMETER);

        PsiMethod withMethod = generateMethodFor(field, methodName, parameterName, elementFactory);
        generateMethodBodyFor(withMethod, propertyName, parameterName, elementFactory);

        return withMethod;
    }

    private PsiMethod generateMethodFor(PsiField field, String methodName, String parameterName, PsiElementFactory elementFactory) {
        PsiMethod withMethod = elementFactory.createMethod(methodName, elementFactory.createType(field.getContainingClass()));
        PsiParameter parameter = elementFactory.createParameter(parameterName, field.getType());
        withMethod.getParameterList().add(parameter);
        PsiUtil.setModifierProperty(withMethod, PsiModifier.PUBLIC, true);

        return withMethod;
    }

    private PsiMethod generateMethodBodyFor(PsiMethod method, String propertyName, String parameterName, PsiElementFactory elementFactory) {
        StringBuilder methodBodyBuilder = new StringBuilder()
                .append("{\n")
                .append("this.").append(propertyName).append("=").append(parameterName).append(";\n")
                .append("return this;\n")
                .append("}\n");

        PsiCodeBlock methodBody = elementFactory.createCodeBlockFromText(methodBodyBuilder.toString(), null);
        method.getBody().replace(methodBody);
        method = (PsiMethod) CodeStyleManager.getInstance(method.getProject()).reformat(method);
        return method;
    }

}
