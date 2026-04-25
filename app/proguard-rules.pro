# Firebase Firestore ProGuard rules
-keepattributes *Annotation*
-keepattributes Signature
-keepclassmembers class com.example.salespossystem.data.** {
  *** get*();
  *** set*(***);
  public <init>();
}
-keep class com.example.salespossystem.data.** { *; }
