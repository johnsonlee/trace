# Trace

Trace is a gradle plugin for android project to trace java method invocation, it uses the [Transform API](http://tools.android.com/tech-docs/new-build-system/transform-api) and [Javassist](http://jboss-javassist.github.io/javassist/) to manipulate the bytecode, and print the java method with elapsed time to logcat.

## Getting Started

Configure this `build.gradle` like this:

```groovy
buildscript {
    repositories {
        mavenLocal()
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:1.5.0'
        classpath 'com.sdklite.trace:gradle-plugin:0.0.1' // HERE
    }
}
```

Then apply the trace plugin below the android plugin

```groovy
apply plugin: 'com.sdklite.trace'
```

Finally, build your project and install the application to your android device, then you can filter the logcat like this:

```bash
adb logcat -s trace
```