package forpdateam.ru.forpda.common

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import android.util.LruCache
import forpdateam.ru.forpda.App.Companion.getContext

/**
 * Created by radiationx on 19.07.17.
 */
/*
 * Copyright 2013 Simple Finance Corporation. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Style a [Spannable] with a custom [Typeface].
 *
 * @author Tristan Waddington
 */
class AssetsTypefaceSpan(context: Context?, typefaceName: String) :
    MetricAffectingSpan() {
    private var mTypeface: Typeface?

    /**
     * Load the [Typeface] and apply to a [Spannable].
     */
    init {
        mTypeface = sTypefaceCache[typefaceName]

        if (mTypeface == null) {
            mTypeface = Typeface.createFromAsset(
                getContext().assets,
                String.format("fonts/%s", typefaceName)
            )

            // Cache the loaded Typeface
            sTypefaceCache.put(typefaceName, mTypeface)
        }
    }

    override fun updateMeasureState(p: TextPaint) {
        p.setTypeface(mTypeface)

        // Note: This flag is required for proper typeface rendering
        p.flags = p.flags or Paint.SUBPIXEL_TEXT_FLAG
    }

    override fun updateDrawState(tp: TextPaint) {
        tp.setTypeface(mTypeface)

        // Note: This flag is required for proper typeface rendering
        tp.flags = tp.flags or Paint.SUBPIXEL_TEXT_FLAG
    }

    companion object {
        /**
         * An `LruCache` for previously loaded typefaces.
         */
        private val sTypefaceCache = LruCache<String, Typeface?>(12)
    }
}