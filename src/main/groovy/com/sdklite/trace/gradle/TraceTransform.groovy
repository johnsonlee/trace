package com.sdklite.trace.gradle;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.LibraryExtension;
import com.android.build.gradle.LibraryPlugin;
import com.android.build.api.transform.*
import org.gradle.api.Project;

/**
 * @author johnsonlee
 */
public class TraceTransform extends Transform {

    private static final Set<QualifiedContent.ContentType> CONTENT_CLASS = Collections.unmodifiableSet(new HashSet<QualifiedContent.ContentType>([
            QualifiedContent.DefaultContentType.CLASSES
    ]));

    private static final Set<QualifiedContent.Scope> SCOPE_APP_PROJECT = Collections.unmodifiableSet(new HashSet<QualifiedContent.Scope>([
            QualifiedContent.Scope.PROJECT,
            QualifiedContent.Scope.PROJECT_LOCAL_DEPS,
            QualifiedContent.Scope.SUB_PROJECTS,
            QualifiedContent.Scope.SUB_PROJECTS_LOCAL_DEPS,
            QualifiedContent.Scope.EXTERNAL_LIBRARIES
    ]));

    private static final Set<QualifiedContent.Scope> SCOPE_LIB_PROJECT = Collections.unmodifiableSet(new HashSet<QualifiedContent.Scope>([
            QualifiedContent.Scope.PROJECT,
            QualifiedContent.Scope.PROJECT_LOCAL_DEPS,
    ]));

    private static final Set<QualifiedContent.Scope> SCOPE_REF_PROJECT = Collections.unmodifiableSet(new HashSet<QualifiedContent.Scope>([
            QualifiedContent.Scope.PROJECT_LOCAL_DEPS,
            QualifiedContent.Scope.SUB_PROJECTS,
            QualifiedContent.Scope.SUB_PROJECTS_LOCAL_DEPS,
            QualifiedContent.Scope.EXTERNAL_LIBRARIES,
            QualifiedContent.Scope.PROVIDED_ONLY
    ]));

    private final Project project;

    private final boolean isLibrary;

    private final TraceCompiler compiler;

    public TraceTransform(final Project project) {
        this.project = project;
        this.isLibrary = project.plugins.hasPlugin(LibraryPlugin);
        this.compiler = new TraceCompiler(project);
    }

    @Override
    String getName() {
        return "trace";
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return CONTENT_CLASS;
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return this.isLibrary ? SCOPE_LIB_PROJECT : SCOPE_APP_PROJECT;
    }

    @Override
    Set<QualifiedContent.Scope> getReferencedScopes() {
        return SCOPE_REF_PROJECT;
    }

    @Override
    boolean isIncremental() {
        return false;
    }

    @Override
    void transform(final Context context, final Collection<TransformInput> inputs, final Collection<TransformInput> references, final TransformOutputProvider outputProvider, final boolean isIncremental) throws IOException, TransformException, InterruptedException {
        if (!project.extensions.getByType(TraceExtension).enabled) {
            return;
        }

        // clean output
        outputProvider.deleteAll();

        final def android = this.project.extensions.findByType(this.isLibrary ? LibraryExtension : AppExtension);
        final List<QualifiedContent> classes = new ArrayList<QualifiedContent>();
        final List<QualifiedContent> libraries = new ArrayList<QualifiedContent>();

        inputs.each {
            classes.addAll(it.directoryInputs);
            classes.addAll(it.jarInputs);
        }

        references.each {
            libraries.addAll(it.directoryInputs)
            libraries.addAll(it.jarInputs);
        }

        final def input = classes.find { it instanceof DirectoryInput };
        final def output = outputProvider.getContentLocation(input.name, input.contentTypes, input.scopes, Format.DIRECTORY);
        final def builder = new TraceOptions.Builder(output)
                .inputs(classes*.file)
                .references(android.bootClasspath)
                .references(libraries*.file);
        this.compiler.compile(builder.build());
    }

}
