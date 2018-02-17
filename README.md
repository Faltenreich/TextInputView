# TextInputView

*A different approach to the [TextInputLayout](https://developer.android.com/reference/android/support/design/widget/TextInputLayout.html)*

<img src="https://github.com/Faltenreich/TextInputView/blob/develop/preview.gif" width="200">

Instead of preserving extra space for repositioning its hint, the TextInputView will place it into the embedded EditText.
The Material Design Guidelines are being respected by adapting primary- and accent colors as well as the style of the target hint.

## Features
- **More space for you:** Reduce the extra space for a permanent hint to an absolute minimum
- **Customization:** Adjust textSize, textColor, padding and overlap action of the hint
- **Gravity:** Support for Gravity.START (Gravity.LEFT), Gravity.END (Gravity.RIGHT) - and even Gravity.CENTER
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
        android:hint="I will stay on user input"/>
        
</com.faltenreich.textinputview.TextInputView>
```

##### Java
```java
public class MainActivity extends AppCompatActivity {
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        TextInputView textInputView = new TextInputView(getContext(), findViewById(R.id.editText));
    }
}

```

##### Kotlin
```kotlin
class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        val textInputView = TextInputView(context, findViewById(R.id.editText))
    }
}
```

### Configuration

Property | Type | Description
--- | --- | ---
android:textColor | color | Applied when embedded EditText loses focus (defaults to textColor of the hint)
android:textSize | dimension | Text size of the hint (defaults to textSize of the hint)
android:tint | color | Applied when embedded EditText gets focused (defaults to tint of the hint)
overlapAction | enum | Applied when the input text overlaps the hint including its padding (options: push, toggle) (defaults to toggle)

### FAQ

**How does the TextInputView work?**

The TextInput wraps a given EditText and replaces its hint with a TextView 
that is being repositioned as the user focuses the EditText or changes its text.

**What about RTL?**

Right-to-left input is supported throughout using the Android resource system (res/values-ldrtl).

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