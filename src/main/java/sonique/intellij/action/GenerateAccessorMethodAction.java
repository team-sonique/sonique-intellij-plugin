package sonique.intellij.action;

import com.intellij.codeInsight.generation.actions.BaseGenerateAction;

public class GenerateAccessorMethodAction extends BaseGenerateAction {

    public GenerateAccessorMethodAction() {
        super(new GenerateAccessorMethodHandler(new ShortStyleMethodNameGenerator()));
    }

    private static class ShortStyleMethodNameGenerator implements MethodNameGenerator {
        public String generateMethodNameFor(String fieldName) {
            return fieldName;
        }
    }
}
