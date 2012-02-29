package sonique.intellij.inspection;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.codeInsight.daemon.JavaErrorMessages;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightMessageUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.infos.MethodCandidateInfo;
import com.intellij.psi.util.MethodSignatureBackedByPsiMethod;
import com.intellij.refactoring.util.RefactoringUtil;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;

public class SoniqueDeprecationElementVisitor extends JavaElementVisitor {
    private static Logger LOGGER = (Logger) Logger.getInstance(SoniqueDeprecationElementVisitor.class);

    private ProblemsHolder myHolder;
    private boolean myIgnoreInsideDeprecated;
    private boolean myIgnoreAbstractDeprecatedOverrides;

    public SoniqueDeprecationElementVisitor(ProblemsHolder holder,
                                            boolean ignoreInsideDeprecated,
                                            boolean ignoreAbstractDeprecatedOverrides) {
        myHolder = holder;
        myIgnoreInsideDeprecated = ignoreInsideDeprecated;
        myIgnoreAbstractDeprecatedOverrides = ignoreAbstractDeprecatedOverrides;
    }

    @Override
    public void visitReferenceElement(PsiJavaCodeReferenceElement reference) {
        JavaResolveResult result = reference.advancedResolve(true);
        PsiElement resolved = result.getElement();
        checkDeprecated(resolved, reference.getReferenceNameElement(), null, myIgnoreInsideDeprecated, myHolder);
    }

    @Override
    public void visitReferenceExpression(PsiReferenceExpression expression) {
        visitReferenceElement(expression);
    }

    @Override
    public void visitNewExpression(PsiNewExpression expression) {
        PsiType type = expression.getType();
        PsiExpressionList list = expression.getArgumentList();
        if (!(type instanceof PsiClassType)) return;
        PsiClassType.ClassResolveResult typeResult = ((PsiClassType) type).resolveGenerics();
        PsiClass aClass = typeResult.getElement();
        if (aClass == null) return;
        if (aClass instanceof PsiAnonymousClass) {
            type = ((PsiAnonymousClass) aClass).getBaseClassType();
            typeResult = ((PsiClassType) type).resolveGenerics();
            aClass = typeResult.getElement();
            if (aClass == null) return;
        }
        PsiResolveHelper resolveHelper = JavaPsiFacade.getInstance(expression.getProject()).getResolveHelper();
        PsiMethod[] constructors = aClass.getConstructors();
        if (constructors.length > 0 && list != null) {
            JavaResolveResult[] results = resolveHelper.multiResolveConstructor((PsiClassType) type, list, list);
            MethodCandidateInfo result = null;
            if (results.length == 1) result = (MethodCandidateInfo) results[0];

            PsiMethod constructor = result == null ? null : result.getElement();
            if (constructor != null && expression.getClassReference() != null) {
                checkDeprecated(constructor, expression.getClassReference(), null, myIgnoreInsideDeprecated, myHolder);
            }
        }
    }

    @Override
    public void visitMethod(PsiMethod method) {
        MethodSignatureBackedByPsiMethod methodSignature = MethodSignatureBackedByPsiMethod.create(method, PsiSubstitutor.EMPTY);
        if (!method.isConstructor()) {
            List<MethodSignatureBackedByPsiMethod> superMethodSignatures = method.findSuperMethodSignaturesIncludingStatic(true);
            checkMethodOverridesDeprecated(methodSignature, superMethodSignatures, myIgnoreAbstractDeprecatedOverrides, myHolder);
        } else {
            checkImplicitCallToSuper(method);
        }
    }

    private void checkImplicitCallToSuper(PsiMethod method) {
        PsiClass containingClass = method.getContainingClass();
        assert containingClass != null;
        PsiClass superClass = containingClass.getSuperClass();
        if (hasDefaultDeprecatedConstructor(superClass)) {
            PsiCodeBlock body = method.getBody();
            if (body != null) {
                PsiStatement[] statements = body.getStatements();
                if (statements.length == 0 || !RefactoringUtil.isSuperOrThisCall(statements[0], true, true)) {
                    registerDefaultConstructorProblem(superClass, method.getNameIdentifier(), false);
                }
            }
        }
    }

    private void registerDefaultConstructorProblem(PsiClass superClass, PsiElement nameIdentifier, boolean asDeprecated) {
        myHolder.registerProblem(nameIdentifier, "Default constructor in " + superClass.getQualifiedName() + " is deprecated",
                asDeprecated ? ProblemHighlightType.LIKE_DEPRECATED : ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
    }

    @Override
    public void visitClass(PsiClass aClass) {
        PsiMethod[] currentConstructors = aClass.getConstructors();
        if (currentConstructors.length == 0) {
            PsiClass superClass = aClass.getSuperClass();
            if (hasDefaultDeprecatedConstructor(superClass)) {
                boolean isAnonymous = aClass instanceof PsiAnonymousClass;
                registerDefaultConstructorProblem(superClass, isAnonymous ? ((PsiAnonymousClass) aClass).getBaseClassReference() : aClass.getNameIdentifier(), isAnonymous);
            }
        }
    }

    private static boolean hasDefaultDeprecatedConstructor(PsiClass superClass) {
        if (superClass != null) {
            PsiMethod[] constructors = superClass.getConstructors();
            for (PsiMethod constructor : constructors) {
                if (constructor.getParameterList().getParametersCount() == 0 && constructor.isDeprecated()) {
                    return true;
                }
            }
        }
        return false;
    }

    static void checkMethodOverridesDeprecated(MethodSignatureBackedByPsiMethod methodSignature,
                                               List<MethodSignatureBackedByPsiMethod> superMethodSignatures,
                                               boolean ignoreAbstractDeprecatedOverrides, ProblemsHolder holder) {
        PsiMethod method = methodSignature.getMethod();
        PsiElement methodName = method.getNameIdentifier();
        for (MethodSignatureBackedByPsiMethod superMethodSignature : superMethodSignatures) {
            PsiMethod superMethod = superMethodSignature.getMethod();
            PsiClass aClass = superMethod.getContainingClass();
            if (aClass == null) continue;
            // do not show deprecated warning for class implementing deprecated methods
            if (ignoreAbstractDeprecatedOverrides && !aClass.isDeprecated() && superMethod.hasModifierProperty(PsiModifier.ABSTRACT)) continue;
            if (superMethod.isDeprecated()) {
                String description = JavaErrorMessages.message("overrides.deprecated.method",
                        HighlightMessageUtil.getSymbolName(aClass, PsiSubstitutor.EMPTY));
                holder.registerProblem(methodName, description, ProblemHighlightType.LIKE_DEPRECATED);
            }
        }
    }

    public static void checkDeprecated(PsiElement refElement,
                                       PsiElement elementToHighlight,
                                       @Nullable TextRange rangeInElement,
                                       boolean ignoreInsideDeprecated,
                                       ProblemsHolder holder) {

        PsiAnnotation annotation = AnnotationUtil.findAnnotationInHierarchy((PsiModifierListOwner) refElement, new HashSet<String>(asList("sonique.annotations.SoniqueDeprecated")));
        if (annotation != null) {
            String description = String.format("%s has been deprecated.\nReason: %s.\nUse: %s.\nSince: %s",
                    HighlightMessageUtil.getSymbolName(refElement, PsiSubstitutor.EMPTY),
                    annotation.findAttributeValue("reason").getText(),
                    annotation.findAttributeValue("use").getText(),
                    annotation.findAttributeValue("date").getText());

            holder.registerProblem(elementToHighlight, description, ProblemHighlightType.LIKE_DEPRECATED, rangeInElement);
        }
    }
}
