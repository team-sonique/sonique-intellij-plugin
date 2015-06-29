package sonique.intellij;

import com.intellij.CommonBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;

public class SoniqueIntentionsBundle {

    private static Reference<ResourceBundle> ourBundle;

    @NonNls
    private static final String BUNDLE = "sonique.intellij.SoniqueIntentionsBundle";

    public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, @NotNull Object... params) {
        return CommonBundle.message(getBundle(), key, params);
    }

    public static String defaultableMessage(@PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
        return CommonBundle.messageOrDefault(getBundle(), key, "default", true, params);
    }

    private SoniqueIntentionsBundle() {
    }

    private static ResourceBundle getBundle() {
        ResourceBundle bundle = com.intellij.reference.SoftReference.dereference(ourBundle);
        if (bundle == null) {
            bundle = ResourceBundle.getBundle(BUNDLE);
            ourBundle = new SoftReference<ResourceBundle>(bundle);
        }

        return bundle;
    }
}
