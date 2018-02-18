# TextInputView

*A different approach to the [TextInputLayout](https://developer.android.com/reference/android/support/design/widget/TextInputLayout.html)*

<img src="https://github.com/Faltenreich/TextInputView/blob/develop/preview.gif" width="200">

Instead of preserving extra space for repositioning its hint, the TextInputView will place it into the embedded EditText.
The Material Design Guidelines are being respected by adapting primary- and accent colors as well as the style of the target hint.

## Features
- **More space for you:** Reduce the extra space for a permanent hint to an absolute minimum
- **Customization:** Adjust textSize, textColor, padding and overlap action of the hint
- **Gravity:** Support for Gravity.START (Gravity.LEFT), Gravity.END (Gravity.RIGHT) - and even Gravity.CENTER
- **Compound drawables:** Support for setError() and other compound drawables
- **RTL:** Support for right-to-left configurations
- **Minimum footprint:** No dependencies *(other than org.jetbrains.kotlin:kotlin-stdlib-jre7)*

### Getting Started

##### Gradle
```gradle
dependencies {
    implementation 'com.github.Faltenreich:TextInputView:1.0.0'
}
```

##### XML
```xml
<com.faltenreich.textinputview.TextInputView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:overlapAction="push">
    
    <EditText
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Every pixel counts"/>
        
</com.faltenreich.textinputview.TextInputView>
```

##### Java
```java
public class MainActivity extends AppCompatActivity {
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        TextInputView textInputView = new TextInputView(editText);
    }
}

```

##### Kotlin
```kotlin
class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        val textInputView = TextInputView(editText)
    }
}
```

### Configuration

Property | Type | Description
--- | --- | ---
android:textColor | color | Applied when the embedded EditText loses focus (defaults to textColor of the hint)
android:textSize | dimension | Text size of the hint (defaults to textSize of the hint)
android:tint | color | Applied when the embedded EditText gets focused (defaults to tint of the hint)
overlapAction | enum | Applied when the input of the embedded EditText overlaps the hint (options: push, toggle) (defaults to toggle)

### FAQ

**How does the TextInputView work?**

The TextInput wraps a given EditText and replaces its hint with a TextView 
that is being repositioned as the user focuses the EditText or changes its text (similar to how the TextInputLayout works).

**What about gravity?**

The gravity of the embedded EditText may be set to Gravity.START, Gravity.END, Gravity.CENTER -
or to the obsolete Gravity.LEFT and Gravity.RIGHT, if you insist.
The hint of the TextInputView will adjust accordingly and either be moved to the left or right to make place for the user input.

**What about EditText.setError() or other compound drawables?**

The hint of the TextInputView will be positioned right before (drawableEnd) or after (drawableStart) a compound drawable, 
including its compoundDrawablePadding.

**What about RTL?**

Right-to-left input is supported throughout using the Android resource system (res/values-ldrtl).
The calculations are flipped in order to respect the inverted alignments.

### License

Copyright 2018 Philipp Fahlteich

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.