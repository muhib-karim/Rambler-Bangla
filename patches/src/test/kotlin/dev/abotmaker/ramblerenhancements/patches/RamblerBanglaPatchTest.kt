package dev.abotmaker.ramblerenhancements.patches

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class RamblerBanglaPatchTest {
    private val stockHinglishRule =
        "\n3. Hinglish Override: For any combination of Indian languages (e.g., Hindi, Bengali, Marathi, Kannada, etc.) and English, override all native script defaults. Transcribe and edit exclusively in Romanized script (Hinglish/Banglish). Example: Use \"Aap kaise ho?\" instead of \"\u0906\u092a \u0915\u0948\u0938\u0947 \u0939\u0948\u0902?\".\n"

    private fun escapeSmali(s: String): String =
        s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")

    private fun buildSynthetic1854Builder(ruleLiteral: String = stockHinglishRule) =
        ImmutableMethod(
            "Lfja;",
            "d",
            listOf(
                ImmutableMethodParameter("Ljava/lang/String;", emptySet(), null),
                ImmutableMethodParameter("Ljava/lang/String;", emptySet(), null),
            ),
            "Ljava/lang/String;",
            AccessFlags.PUBLIC.value or AccessFlags.STATIC.value,
            emptySet(),
            emptySet(),
            MutableMethodImplementation(6),
        ).toMutable().apply {
            addInstructions(
                0,
                """
                move-object v0, p0
                move-object v1, p1
                const-string v2, "{HINGLISH_OVERRIDE_RULE}"
                const-string v3, "${escapeSmali(ruleLiteral)}"
                invoke-virtual {v0, v2, v3}, Ljava/lang/String;->replace(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
                move-result-object v0
                const-string v2, "{ENABLED_LANGUAGES}"
                invoke-virtual {v0, v2, v1}, Ljava/lang/String;->replace(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
                move-result-object v0
                return-object v0
                """.trimIndent(),
            )
        }

    @Test
    fun patchesGboard1854OrderAndRejectsDoublePatching() {
        val builder = buildSynthetic1854Builder()
        val targets = analyzeBanglaLiteBuilder(builder)
        assertEquals(3, targets.ruleRegister)
        assertEquals(1, targets.languagesRegister)
        assertEquals(0, targets.promptResultRegister)

        installBanglaLiteScriptHooks(targets)

        val refs = builder.implementation!!.instructions
            .mapNotNull { (it as? ReferenceInstruction)?.reference?.toString() }
        assertTrue(refs.any { it.contains("selectHinglishOverrideRule") })
        assertTrue(refs.any { it.contains("rewriteCleanupPromptScriptGate") })

        // Fail-closed on second patch attempt
        try {
            analyzeBanglaLiteBuilder(builder)
            fail("Expected double-patch attempt to fail closed")
        } catch (expected: PatchException) {
            assertTrue(expected.message.orEmpty().contains("already installed"))
        }
    }

    @Test
    fun failsClosedOnDriftedHinglishOverrideFingerprint() {
        val drifted = buildSynthetic1854Builder(stockHinglishRule + "!")
        try {
            analyzeBanglaLiteBuilder(drifted)
            fail("Expected drifted Hinglish rule fingerprint to fail closed")
        } catch (expected: PatchException) {
            assertTrue(expected.message.orEmpty().contains("FAIL-CLOSED"))
        }
    }
}
