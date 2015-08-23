/*
 * Copyright (C) 2015 The ProjectDisco Open Source Project
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

package com.android.systemui.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import com.android.systemui.R;

public class AdaptiveBrightnessToggleView extends CheckBox {

    public AdaptiveBrightnessToggleView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

    @Override
    public void setChecked(boolean b) {
      if (b) {
    	      this.setButtonDrawable(R.drawable.ic_qs_brightness_auto_on_alpha);
    } else {
    	      this.setButtonDrawable(R.drawable.ic_qs_brightness_auto_off_alpha);
        }
    super.setChecked(b);
    }
}
