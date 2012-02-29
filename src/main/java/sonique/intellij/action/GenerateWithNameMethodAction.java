package sonique.intellij.action;

import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.openapi.util.text.StringUtil;

public class GenerateWithNameMethodAction extends BaseGenerateAction {

    public GenerateWithNameMethodAction() {
        super(new GenerateWithMethodHandler(new WithPropertyNameWithMethodNameGenerator()));
    }

    private static class WithPropertyNameWithMethodNameGenerator implements WithMethodNameGenerator {
        public String generateMethodNameFor(String fieldName) {
            return String.format("with%s", StringUtil.capitalizeWithJavaBeanConvention(fieldName));
        }
    }
}
