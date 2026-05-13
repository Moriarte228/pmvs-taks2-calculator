package com.example.calculator5.unit

import com.example.calculator5.i18n.Language
import com.example.calculator5.i18n.StringKey
import com.example.calculator5.i18n.translate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class I18nTest {

    @Test
    fun translations_differ_between_languages() {
        val ru = translate(StringKey.AppTitle, Language.Russian)
        val en = translate(StringKey.AppTitle, Language.English)
        val be = translate(StringKey.AppTitle, Language.Belarusian)
        assertNotEquals(ru, en)
        assertNotEquals(ru, be)
        assertNotEquals(en, be)
    }

    @Test
    fun every_key_has_a_translation_for_every_language() {
        for (key in StringKey.entries) {
            for (lang in Language.entries) {
                val translation = translate(key, lang)
                assertTrue(
                    translation.isNotBlank(),
                    "Missing translation for $key in $lang",
                )
            }
        }
    }

    @Test
    fun russian_app_title_matches_expected_phrase() {
        assertEquals("Финансовый калькулятор", translate(StringKey.AppTitle, Language.Russian))
    }
}
