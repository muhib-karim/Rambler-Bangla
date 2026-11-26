package dev.abotmaker.ramblerenhancements;

import android.text.InputType;
import android.view.inputmethod.EditorInfo;

import com.akshaykadam.pixelboard.extension.rambler.GboardRamblerScriptFix;
import com.akshaykadam.pixelboard.extension.rambler.GboardRamblerSegmentLock;
import com.akshaykadam.pixelboard.extension.ramblerlite.GboardRamblerLiteScriptRuntime;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class RamblerBanglaRuntimeTest {

    @After
    public void tearDown() {
        GboardRamblerScriptFix.jvmLayoutKind = 0;
        GboardRamblerSegmentLock.resetPin();
    }

    @Test
    public void liteScriptRuntimePreservesNativeBengaliAndRespectsBnLatn() {
        String stockRule = "\n3. Hinglish Override: force Banglish\n";
        Assert.assertEquals(
                GboardRamblerLiteScriptRuntime.NATIVE_SCRIPT_RULE,
                GboardRamblerLiteScriptRuntime.selectHinglishOverrideRule(stockRule, "en-US, bn-BD"));
        Assert.assertEquals(
                stockRule,
                GboardRamblerLiteScriptRuntime.selectHinglishOverrideRule(stockRule, "en-US, bn-Latn"));
        Assert.assertEquals(
                "",
                GboardRamblerLiteScriptRuntime.selectHinglishOverrideRule(stockRule, "en-US, fr-FR"));
        Assert.assertEquals(
                stockRule,
                GboardRamblerLiteScriptRuntime.selectHinglishOverrideRule(stockRule, ""));

        String samplePrompt =
                "Header\n"
                        + "   * Mandatory Indic Romanization (Anti-Gravity Gate): Bengali NEVER revert\n"
                        + "   * Universal Latin: keep.\n";
        String rewritten =
                GboardRamblerLiteScriptRuntime.rewriteCleanupPromptScriptGate(samplePrompt, "bn-BD");
        Assert.assertTrue(rewritten.contains("Enabled-Language Script Policy"));
        Assert.assertFalse(rewritten.contains("Mandatory Indic Romanization"));
        Assert.assertEquals(
                samplePrompt,
                GboardRamblerLiteScriptRuntime.rewriteCleanupPromptScriptGate(samplePrompt, "bn-Latn"));
    }

    @Test
    public void phoneticVoiceEngineConvertsBanglaKeepsEnglishAndProtectsSpans() {
        GboardRamblerScriptFix.jvmLayoutKind = 2; // multilingual AUTO
        String bnOut = GboardRamblerScriptFix.processVoice("ami bhalo achi");
        Assert.assertTrue("Expected Bengali Unicode output, got: " + bnOut, containsBengali(bnOut));

        String enOut = GboardRamblerScriptFix.processVoice("Please send the project report tomorrow.");
        Assert.assertEquals("Please send the project report tomorrow.", enOut);

        String mixedOut = GboardRamblerScriptFix.processVoice(
                "ami kal office jabo #RamblerBangla @muhib https://example.com 5pm. Please check it.");
        Assert.assertTrue("Expected Bangla sentence in mixed output: " + mixedOut, containsBengali(mixedOut));
        Assert.assertTrue("Hashtag must stay Latin: " + mixedOut, mixedOut.contains("#RamblerBangla"));
        Assert.assertTrue("Handle must stay Latin: " + mixedOut, mixedOut.contains("@muhib"));
        Assert.assertTrue("URL must stay Latin: " + mixedOut, mixedOut.contains("https://example.com"));
        Assert.assertTrue("English sentence must stay English: " + mixedOut, mixedOut.endsWith("Please check it."));

        GboardRamblerScriptFix.jvmLayoutKind = 3; // English-only layout
        Assert.assertEquals("ami bhalo achi", GboardRamblerScriptFix.processVoice("ami bhalo achi"));
    }

    @Test
    public void sensitiveAndIncognitoFieldsFailClosed() {
        EditorInfo normal = new EditorInfo();
        normal.inputType = InputType.TYPE_CLASS_TEXT;
        Assert.assertFalse(RamblerTextPipeline.isSensitiveOrIncognitoField(normal));

        EditorInfo password = new EditorInfo();
        password.inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
        Assert.assertTrue(RamblerTextPipeline.isSensitiveOrIncognitoField(password));

        EditorInfo incognito = new EditorInfo();
        incognito.inputType = InputType.TYPE_CLASS_TEXT;
        incognito.imeOptions = EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING;
        Assert.assertTrue(RamblerTextPipeline.isSensitiveOrIncognitoField(incognito));
    }

    private static boolean containsBengali(String s) {
        if (s == null) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= '\u0980' && c <= '\u09ff') return true;
        }
        return false;
    }
}
