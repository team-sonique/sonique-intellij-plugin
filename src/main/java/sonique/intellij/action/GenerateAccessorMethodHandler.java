package sonique.intellij.action;

import com.intellij.codeInsight.generation.*;
import com.intellij.ide.util.MemberChooser;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.IncorrectOperationException;

import java.util.List;

public class GenerateAccessorMethodHandler extends GenerateSetterHandler {

    private final MethodNameGenerator methodNameGenerator;

    public GenerateAccessorMethodHandler(MethodNameGenerator methodNameGenerator) {
        this.methodNameGenerator = methodNameGenerator;
    }

    @Override
    protected ClassMember[] chooseMembers(ClassMember[] members, boolean allowEmptySelection, boolean copyJavadocCheckbox, Project project) {
        MemberChooser<ClassMember> chooser = new MemberChooser<ClassMember>(members, allowEmptySelection, true, project);
        chooser.setTitle("Select Fields to Generate Accessor Methods");
        chooser.setCopyJavadocVisible(copyJavadocCheckbox);
        chooser.show();
        myToCopyJavaDoc = chooser.isCopyJavadoc();
        List<ClassMember> list = chooser.getSelectedElements();
        return list == null ? null : list.toArray(new ClassMember[list.size()]);
    }

    @Override
    protected GenerationInfo[] generateMemberPrototypes(PsiClass psiClass, ClassMember classMember) throws IncorrectOperationException {
        if (classMember instanceof PsiFieldMember) {
            GenerationInfo accessorMethod = generateAccessorMethodFor(((PsiFieldMember) classMember).getElement());
            if (accessorMethod != null) {
                return new GenerationInfo[]{accessorMethod};
            }
        }
        return GenerationInfo.EMPTY_ARRAY;
    }

    private GenerationInfo generateAccessorMethodFor(PsiField field) {
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

        PsiMethod accessorMethod = generateMethodFor(field, methodName, elementFactory);
        generateMethodBodyFor(accessorMethod, field.getName(), elementFactory);

        return accessorMethod;
    }

    private PsiMethod generateMethodFor(PsiField field, String methodName, PsiElementFactory elementFactory) {
        PsiMethod accessorMethod = elementFactory.createMethod(methodName, field.getType());
        PsiUtil.setModifierProperty(accessorMethod, PsiModifier.PUBLIC, true);
        if(field.hasModifierProperty(PsiModifier.STATIC)) {
            PsiUtil.setModifierProperty(accessorMethod, PsiModifier.STATIC, true);
        }

        return accessorMethod;
    }

    private PsiMethod generateMethodBodyFor(PsiMethod method, String propertyName, PsiElementFactory elementFactory) {
        StringBuilder methodBodyBuilder = new StringBuilder()
                .append("{\n")
                .append("return ").append(propertyName).append(";\n")
                .append("}\n");

        PsiCodeBlock methodBody = elementFactory.createCodeBlockFromText(methodBodyBuilder.toString(), null);
        method.getBody().replace(methodBody);
        method = (PsiMethod) CodeStyleManager.getInstance(method.getProject()).reformat(method);
        return method;
    }

}
