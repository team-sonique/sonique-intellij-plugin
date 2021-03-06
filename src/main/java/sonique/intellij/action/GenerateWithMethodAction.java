package sonique.intellij.action;

import com.intellij.codeInsight.generation.actions.BaseGenerateAction;

public class GenerateWithMethodAction extends BaseGenerateAction {

    public GenerateWithMethodAction() {
        super(new GenerateWithMethodHandler(new ShortStyleMethodNameGenerator()));
    }

    private static class ShortStyleMethodNameGenerator implements MethodNameGenerator {
        public String generateMethodNameFor(String fieldName) {
            return "with";
        }
    }
}
