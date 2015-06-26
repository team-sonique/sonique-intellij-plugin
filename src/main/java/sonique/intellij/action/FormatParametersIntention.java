package sonique.intellij.action;

import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiParserFacade;
import com.siyeh.IntentionPowerPackBundle;
import com.siyeh.ipp.base.MutablyNamedIntention;
import com.siyeh.ipp.base.PsiElementEditorPredicate;
import com.siyeh.ipp.base.PsiElementPredicate;
import com.siyeh.ipp.psiutils.HighlightUtil;
import org.jetbrains.annotations.NotNull;
import sonique.intellij.SoniqueIntentionsBundle;

public class FormatParametersIntention extends MutablyNamedIntention {

    @Override
    protected void processIntention(@NotNull PsiElement psiElement) {
        PsiParameterList list = (PsiParameterList) psiElement;
        PsiParserFacade parserFacade = PsiParserFacade.SERVICE.getInstance(list.getProject());

        for (PsiParameter parameter : list.getParameters()) {
            PsiElement whitespace = parserFacade.createWhiteSpaceFromText("\n");
            list.getNode().addChild(whitespace.getNode(), parameter.getNode());
        }

        HighlightUtil.highlightElement(list,
                IntentionPowerPackBundle.message(
                        "press.escape.to.remove.highlighting.message"));
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
        return SoniqueIntentionsBundle.message("format.parameters");
    }
}
