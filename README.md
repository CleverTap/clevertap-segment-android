analytics-android-integration-clevertap
======================================

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.clevertap.android/clevertap-segment-android/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.clevertap.android/clevertap-segment-android)
[![Javadocs](http://javadoc-badge.appspot.com/com.clevertap.android/clevertap-segment-android.svg?label=javadoc)](http://javadoc-badge.appspot.com/com.clevertap.android/clevertap-segment-android)

CleverTap integration for [analytics-android](https://github.com/segmentio/analytics-android).

## Installation

To install the Segment-CleverTap integration, simply add this line to your gradle file:

```
implementation 'com.clevertap.android:clevertap-segment-android:+'

```

## Usage

After adding the dependency, you must register the integration.  To do this, import the CleverTap integration:


```
import com.segment.analytics.android.integrations.clevertap.CleverTapIntegration;

```

And add the following line:

```
analytics = new Analytics.Builder(this, "write_key")
                .use(CleverTapIntegration.FACTORY)
                .build();
```

Please see [our documentation](https://segment.com/docs/integrations/clevertap/) for more information.


## License

```
WWWWWW||WWWWWW
 W W W||W W W
      ||
    ( OO )__________
     /  |           \
    /o o|    MIT     \
    \___/||_||__||_|| *
         || ||  || ||
        _||_|| _||_||
       (__|__|(__|__|

The MIT License (MIT)

Copyright (c) 2014 Segment, Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
