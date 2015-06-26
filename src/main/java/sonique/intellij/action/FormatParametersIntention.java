package sonique.intellij.action;

import com.intellij.openapi.editor.Editor;
import com.intellij.psi.*;
import com.siyeh.IntentionPowerPackBundle;
import com.siyeh.ipp.base.MutablyNamedIntention;
import com.siyeh.ipp.base.PsiElementEditorPredicate;
import com.siyeh.ipp.base.PsiElementPredicate;
import org.jetbrains.annotations.NotNull;
import sonique.intellij.SoniqueIntentionsBundle;

import static com.siyeh.ipp.psiutils.HighlightUtil.highlightElement;

public class FormatParametersIntention extends MutablyNamedIntention {

    @Override
    protected void processIntention(@NotNull PsiElement psiElement) {
        PsiParserFacade parserFacade = PsiParserFacade.SERVICE.getInstance(psiElement.getProject());

        if (psiElement instanceof PsiMethod) {
            PsiMethod method = (PsiMethod) psiElement;
            PsiParameterList list = method.getParameterList();
            PsiParameter[] parameters = list.getParameters();
            if (parameters.length == 0) {
                return;
            }

            for (PsiParameter parameter : parameters) {
                PsiElement whitespace = createWhiteSpace(parserFacade);
                list.getNode().addChild(whitespace.getNode(), parameter.getNode());
            }

            highlightElement(list, IntentionPowerPackBundle.message("press.escape.to.remove.highlighting.message"));
        } else if (psiElement instanceof PsiMethodCallExpression) {
            PsiMethodCallExpression method = (PsiMethodCallExpression) psiElement;
            PsiExpressionList list = method.getArgumentList();

            PsiExpression[] expressions = list.getExpressions();
            if (expressions.length == 0) {
                return;
            }

            for (PsiExpression expression : expressions) {
                PsiElement whitespace = createWhiteSpace(parserFacade);
                list.getNode().addChild(whitespace.getNode(), expression.getNode());
            }

            list.getNode().addChild(
                    createWhiteSpace(parserFacade).getNode(),
                    expressions[expressions.length - 1].getNode().getTreeNext()
            );

            highlightElement(list, IntentionPowerPackBundle.message("press.escape.to.remove.highlighting.message"));
        }
    }

    @NotNull
    private static PsiElement createWhiteSpace(PsiParserFacade parserFacade) {
        return parserFacade.createWhiteSpaceFromText("\n");
    }

    @NotNull
    @Override
    protected PsiElementPredicate getElementPredicate() {
        return new PsiElementEditorPredicate() {
            @Override
            public boolean satisfiedBy(PsiElement psiElement, Editor editor) {
                return psiElement instanceof PsiMethod || psiElement instanceof PsiMethodCallExpression;
            }
        };
    }

    @Override
    protected String getTextForElement(PsiElement psiElement) {
        return SoniqueIntentionsBundle.message("format.parameters");
    }
}
