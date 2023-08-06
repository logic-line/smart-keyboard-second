/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sikderithub.keyboard.keyboard.layout.tests;

import android.test.suitebuilder.annotation.SmallTest;

import com.android.inputmethod.keyboard.layout.LayoutBase;
import com.android.inputmethod.keyboard.layout.Qwerty;
import com.android.inputmethod.keyboard.layout.Symbols;
import com.android.inputmethod.keyboard.layout.SymbolsShifted;
import com.android.inputmethod.keyboard.layout.customizer.EnglishCustomizer;
import com.android.inputmethod.keyboard.layout.expected.ExpectedKey;

import java.util.Locale;

/*
 * en_IN: English (India)/qwerty
 */
@SmallTest
public final class TestsEnglishIN extends TestsEnglishUS {
    private static final Locale LOCALE = new Locale("en", "IN");
    private static final LayoutBase LAYOUT = new Qwerty(new EnglishINCustomizer(LOCALE));

    @Override
    LayoutBase getLayout() { return LAYOUT; }

    private static class EnglishINCustomizer extends EnglishCustomizer {
        EnglishINCustomizer(final Locale locale) { super(locale); }

        @Override
        public ExpectedKey getCurrencyKey() { return CURRENCY_RUPEE; }

        @Override
        public ExpectedKey[] getOtherCurrencyKeys() {
            return SymbolsShifted.CURRENCIES_OTHER_GENERIC;
        }

        // U+20B9: "₹" INDIAN RUPEE SIGN
        private static final ExpectedKey CURRENCY_RUPEE = key("\u20B9",
            Symbols.CURRENCY_GENERIC_MORE_KEYS);
    }
}
