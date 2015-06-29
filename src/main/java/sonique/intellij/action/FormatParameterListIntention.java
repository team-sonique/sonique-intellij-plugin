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

public class FormatParameterListIntention extends MutablyNamedIntention {

    @Override
    protected void processIntention(@NotNull PsiElement psiElement) {
        PsiParserFacade parserFacade = PsiParserFacade.SERVICE.getInstance(psiElement.getProject());

        PsiParameterList list = (PsiParameterList) psiElement;
        PsiParameter[] parameters = list.getParameters();
        if (parameters.length == 0) {
            return;
        }

        for (PsiParameter parameter : parameters) {
            if (!(parameter.getPrevSibling() instanceof PsiWhiteSpace
                    && parameter.getPrevSibling().getText().contains("\n"))) {
                PsiElement whitespace = createWhiteSpace(parserFacade);
                list.addBefore(whitespace, parameter);
            }
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
                return psiElement instanceof PsiParameterList;
            }
        };
    }

    @Override
    protected String getTextForElement(PsiElement psiElement) {
        return SoniqueIntentionsBundle.message("format.parameter.list.intention.name");
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return SoniqueIntentionsBundle.message("format.parameter.list.intention.family.name");
    }
}
