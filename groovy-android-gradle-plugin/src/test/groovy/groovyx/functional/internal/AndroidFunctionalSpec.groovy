package groovyx.functional.internal

import groovyx.internal.TestProperties

import static groovyx.internal.TestProperties.compileSdkVersion
import static groovyx.internal.TestProperties.groovyVersion

abstract class AndroidFunctionalSpec extends FunctionalSpec {

  enum AndroidPlugin {
    LIBRARY('com.android.library'),
    APP('com.android.application')

    private final String value

    AndroidPlugin(String value) {
      this.value = value
    }
  }

  void createBuildFileForApplication(String androidPluginVersion = TestProperties.androidPluginVersion,
                                     String javaVersion = 'JavaVersion.VERSION_1_7') {
    createBuildFile(AndroidPlugin.APP, androidPluginVersion, javaVersion)
  }

  void createBuildFileForLibrary(String androidPluginVersion = TestProperties.androidPluginVersion,
                                     String javaVersion = 'JavaVersion.VERSION_1_7') {
    createBuildFile(AndroidPlugin.LIBRARY, androidPluginVersion, javaVersion)
  }

  void createBuildFile(AndroidPlugin plugin, String androidPluginVersion = TestProperties.androidPluginVersion,
                       String javaVersion = 'JavaVersion.VERSION_1_7') {
    createProguardRules()

    buildFile << """
      buildscript {
        repositories {
          jcenter()
          google()
          maven { url "${localRepo.toURI()}" }
        }
        dependencies {
          classpath 'com.android.tools.build:gradle:$androidPluginVersion'
          classpath 'org.codehaus.groovy:groovy-android-gradle-plugin:$PLUGIN_VERSION'
        }
      }

      apply plugin: '$plugin.value'
      apply plugin: 'groovyx.android'

      repositories {
        jcenter()
        google()
      }

      android {
        compileSdkVersion $compileSdkVersion

        defaultConfig {
          minSdkVersion ${plugin == AndroidPlugin.LIBRARY ? '28' : '16' }
          targetSdkVersion $compileSdkVersion

          versionCode 1
          versionName '1.0.0'

          testInstrumentationRunner 'android.support.test.runner.AndroidJUnitRunner'
        }

        buildTypes {
          debug {
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.txt'
            testProguardFile 'proguard-rules.txt'
          }
          release {
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.txt'
          }
        }

        compileOptions {
          sourceCompatibility $javaVersion
          targetCompatibility $javaVersion
        }
      }

      dependencies {
        implementation 'org.codehaus.groovy:groovy:$groovyVersion:grooid'

        annotationProcessor 'com.google.auto.value:auto-value:1.6.3'
        compileOnly 'com.google.auto.value:auto-value-annotations:1.6.3'

        androidTestImplementation 'com.android.support.test:runner:1.0.2'
        androidTestImplementation 'com.android.support.test:rules:1.0.2'

        testImplementation 'junit:junit:4.12'
      }

      configurations.all {
        resolutionStrategy.force 'com.android.support:support-annotations:28.0.0'
      }

      // force unit test types to be assembled too
      android.testVariants.all { variant ->
        tasks.getByName('assemble').dependsOn variant.assemble
      }
    """
  }

  void createProguardRules() {
    file('proguard-rules.txt') <<
"""
-dontobfuscate

-keep class org.codehaus.groovy.vmplugin.**
-keep class org.codehaus.groovy.runtime.dgm*

-keepclassmembers class org.codehaus.groovy.runtime.dgm* {*;}
-keepclassmembers class ** implements org.codehaus.groovy.runtime.GeneratedClosure {*;}
-keepclassmembers class org.codehaus.groovy.reflection.GroovyClassValue* {*;}
-keepclassmembers class groovyx.example.** {*;}
-keepclassmembers class com.arasthel.swissknife.utils.Finder {*;}

-dontwarn org.codehaus.groovy.**
-dontwarn groovy**
-dontnote org.codehaus.groovy.**
-dontnote groovy**

-keep class org.xmlpull.v1.**
-dontwarn org.xmlpull.v1.**

-keep class com.squareup.**
-keep class okio.**
-dontwarn com.squareup.**
-dontwarn okio.**
"""
  }

  void createAndroidManifest() {
    file('src/main/AndroidManifest.xml') << """
      <?xml version="1.0" encoding="utf-8"?>
      <manifest xmlns:android="http://schemas.android.com/apk/res/android"
          package="groovyx.test">

        <application
            android:allowBackup="true"
            android:label="Test App">
          <activity
              android:name=".MainActivity"
              android:label="Test App">
            <intent-filter>
              <action android:name="android.intent.action.MAIN"/>
              <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
          </activity>
        </application>

      </manifest>
    """.trim()
  }

  void createMainActivityLayoutFile() {
    file('src/main/res/layout/activity_main.xml') << """
      <?xml version="1.0" encoding="utf-8"?>
      <FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
          android:layout_width="match_parent"
          android:layout_height="match_parent"
      >

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Hello Groovy!"
            android:gravity="center"
            android:textAppearance="?android:textAppearanceLarge"
        />

      </FrameLayout>
    """.trim()
  }
}
