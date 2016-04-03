package com.sdklite.trace.gradle;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.LibraryExtension;
import com.android.build.gradle.LibraryPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * @author johnsonlee
 */
public class TracePlugin implements Plugin<Project> {

    @Override
    public void apply(final Project project) {
        def isLibrary = project.plugins.hasPlugin(LibraryPlugin);
        def android = project.extensions.getByType(isLibrary ? LibraryExtension : AppExtension);
        android.registerTransform(new TraceTransform(project));
    }

}
