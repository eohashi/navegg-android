# HOW TO USE

## Compilation

When to launch new releases, not forget of changes two archives:
	-> gradle.properties
		Field:
		  -> VERSION_NAME
	-> build.gradle
		Field:
		  -> versionName
## build.gradle
Now change the local where will create of the archives AAR.
	-> build.gradle
		Field:
		 -> repository(url:"LOCAL YOUR PC")

After that, open your terminal or use terminal of the Android Studio and run the command:
	-> ./gradlew install

this command will generate archives AAR inside your folder.
