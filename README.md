# TextInputView

[![MavenCentral](https://maven-badges.herokuapp.com/maven-central/com.faltenreich/textinputview/badge.svg?style=flat)](https://mvnrepository.com/artifact/com.faltenreich/textinputview)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-TextInputView-green.svg?style=flat)](https://android-arsenal.com/details/1/6981)

*A different approach to the [TextInputLayout](https://developer.android.com/reference/android/support/design/widget/TextInputLayout.html)*

<img src="./images/preview.gif" width="250">

Instead of preserving extra space for repositioning its hint, the TextInputView will place it into the embedded EditText.
The Material Design Guidelines are being respected by adapting primary- and accent colors as well as the style of the target hint.

## Preview

<img src="./images/preview_unfocused.png" width="200">

<img src="./images/preview_focused.png" width="200">

<img src="./images/preview_overlap.png" width="200">

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
buildscript {
    repositories {
        mavenCentral()
    }
}
```gradle
dependencies {
    implementation 'com.faltenreich:textinputview:<version>'
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

    public MainActivity {
        super(R.layout.activity_main);
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextInputView textInputView = new TextInputView(editText);
    }
}

```

##### Kotlin
```kotlin
class MainActivity : AppCompatActivity(R.layout.activity_main) {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

**Why this library?**

I adore the Material Design Guidelines. For me they are an incredible source of wisdom for non-designers.
No longer do I have to spent dozens of hours for the fine details of my user interfaces.
Now I have more time to do the thing I love the most: developing sourcecode that keeps everything together.

The InputTextLayout is one of the many fine Material Design Components.
It helps users navigating a form by providing a permanent hint.
One particular problem with the InputTextLayout is the additional screen space it preserves for positioning its hint.
Not only does this lead to vertically larger user interfaces, 
but the preserved space stays completely empty until a user input has been given - and is therefor temporarily useless.

**How does the TextInputView work?**

The TextInputLayout wraps a given EditText and replaces its hint with a TextView that stays within the bounds of the original view.
Therefor it does not preserve additional screen space and stays there as long as it fits into the original EditText alongside the user input - 
otherwise it will be pushed to the side (or optionally be hidden immediately).
The latter should rarely occur as the user input of most forms stays relatively short.

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

### Third-party licenses

This software uses following technologies with great appreciation:

* [AndroidX](https://developer.android.com/jetpack/androidx)
* [gradle-maven-publish-plugin](https://github.com/vanniktech/gradle-maven-publish-plugin)
* [Material Components for Android](https://material.io/components)

### License

Copyright 2021 Philipp Fahlteich

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.