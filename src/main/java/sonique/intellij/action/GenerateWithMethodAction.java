package sonique.intellij.action;

import com.intellij.codeInsight.generation.actions.BaseGenerateAction;

public class GenerateWithMethodAction extends BaseGenerateAction {

    public GenerateWithMethodAction() {
        super(new GenerateWithMethodHandler(new ShortStyleWithMethodNameGenerator()));
    }

    private static class ShortStyleWithMethodNameGenerator implements WithMethodNameGenerator {
        public String generateMethodNameFor(String fieldName) {
            return "with";
        }
    }
}
