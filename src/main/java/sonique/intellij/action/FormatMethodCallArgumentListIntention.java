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

public class FormatMethodCallArgumentListIntention extends MutablyNamedIntention {

    @Override
    protected void processIntention(@NotNull PsiElement psiElement) {
        PsiParserFacade parserFacade = PsiParserFacade.SERVICE.getInstance(psiElement.getProject());
        PsiCall method = (PsiCall) psiElement;
        PsiExpressionList list = method.getArgumentList();
        if (list == null) {
            return;
        }

        PsiExpression[] expressions = list.getExpressions();
        if (expressions.length == 0) {
            return;
        }

        for (PsiExpression expression : expressions) {
            if (!(expression.getPrevSibling() instanceof PsiWhiteSpace
                    && expression.getPrevSibling().getText().contains("\n"))) {
                PsiElement whitespace = createWhiteSpace(parserFacade);
                list.addBefore(whitespace, expression);
            }
        }

        PsiExpression lastExpression = expressions[expressions.length - 1];

        if (!(lastExpression.getNextSibling() instanceof PsiWhiteSpace
                && lastExpression.getNextSibling().getText().contains("\n"))) {
            PsiElement whitespace = createWhiteSpace(parserFacade);
            list.addAfter(whitespace, lastExpression);
        }

        highlightElement(list, IntentionPowerPackBundle.message("press.escape.to.remove.highlighting.message"));
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
                return psiElement instanceof PsiCall;
            }
        };
    }

    @Override
    protected String getTextForElement(PsiElement psiElement) {
        return SoniqueIntentionsBundle.message("format.method.call.arguments.intention.name");
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return SoniqueIntentionsBundle.message("format.method.call.arguments.intention.family.name");
    }
}
