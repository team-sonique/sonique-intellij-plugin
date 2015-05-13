Sonique Super Awesome Plugin
============================

*Make sure you build this plugin with JDK 6*, bad things happen to good people otherwise.

Updating and building
---------------------
- Open the project in IntelliJ
- Ensure the IntelliJ Plugin SDK is set up (with JDK 6) in Project Structure → Project.
- Wait for indexing to finish :)
- Make your important, super awesome changes.
- Update the version of the plugin in META-INF/plugin.xml.
- Right click on the project → Prepare Plugin Module 'sonique-intellij-plugin' For Deployment.
- Update the version of the plugin in updatePlugins.xml.

This will build the plugin and replace the sonique-intellij-plugin.jar in the root of this project. Check in your changes (make sure to commit the jar as well).

