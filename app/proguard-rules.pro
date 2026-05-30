# Keep InputMethodService subclasses
-keep class com.bigsquarekeys.ime.** { *; }

# Keep Hilt-generated components
-keep class com.bigsquarekeys.Hilt_* { *; }
-keep class dagger.hilt.** { *; }

# Keep DataStore
-keep class androidx.datastore.** { *; }
