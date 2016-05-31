package com.sdklite.trace.gradle;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.Modifier;
import org.apache.commons.io.IOUtils;
import org.gradle.api.Project;

import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author johnsonlee
 */
class TraceCompiler {

    private final Project project;

    public TraceCompiler(final Project project) {
        this.project = project;
    }

    /**
     * Compile with the specified options
     *
     * @param options
     * @throws Exception
     */
    public void compile(final TraceOptions options) throws Exception {
        final ClassPool pool = ClassPool.default;

        for (final File ref : options.references) {
            println(" * $ref");
            pool.appendClassPath(ref.absolutePath);
        }

        for (final File input : options.inputs) {
            println(" + $input.absolutePath");

            if (input.directory) {
                this.compileDir(pool, input, options.output);
            } else {
                this.compileJar(pool, input, options.output);
            }
        }
    }

    /**
     * Compile files in the specified directory
     *
     * @param pool
     * @param input
     * @param output
     * @throws Exception
     */
    private void compileDir(final ClassPool pool, final File input, final File output) throws Exception {
        this.project.fileTree(dir: input).visit {
            final File file = it.file;

            if (file.directory) {
                return;
            }

            if (!file.name.endsWith('.class')) {
                this.compileFile(this.project, input, new File(file.absolutePath.replace(input.absolutePath, output.absolutePath)));
                return;
            }

            final InputStream is = new FileInputStream(file);

            try {
                this.compileClass(pool, is, output, false);
            } finally {
                IOUtils.closeQuietly(is);
            }
        }
    }

    /**
     * Compile jar file
     *
     * @param pool
     * @param input
     * @param output
     * @throws Exception
     */
    private void compileJar(final ClassPool pool, final File input, final File output) throws Exception {
        final JarFile jar = new JarFile(input);

        for (final Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements();) {
            final JarEntry entry = entries.nextElement();
            if (entry.directory) {
                new File(output, entry.name).mkdirs();
                continue;
            }

            final InputStream is = jar.getInputStream(entry);

            try {
                if (entry.name.endsWith(".class")) {
                    this.compileClass(pool, is, output, false);
                } else {
                    this.compileFile(is, new File(output, entry.name));
                }
            } finally {
                IOUtils.closeQuietly(is);
            }
        }
    }

    /**
     * Compile class from stream
     *
     * @param pool
     * @param is
     * @param output
     * @param ignore
     * @throws Exception
     */
    private void compileClass(final ClassPool pool, final InputStream is, final File output, final boolean ignore) throws Exception {
        final CtClass klass = pool.makeClass(is, false);

        println("   - ${klass.name}");

        if (!ignore) {
            if (!(klass.isAnnotation() || klass.isArray() || klass.isEnum() || klass.isInterface() || klass.isPrimitive() || klass.isFrozen())) {
                klass.declaredMethods.findAll { !Modifier.isAbstract(it.modifiers) && !java.lang.reflect.Modifier.isNative(it.modifiers) }.each { m ->
                    try {
                        m.addLocalVariable("${m.name}ElapsedTime", CtClass.longType);
                        m.insertBefore("{${m.name}ElapsedTime = System.nanoTime();}");
                        m.insertAfter("{android.util.Log.v(\"trace\", \"${klass.name}#${m.name}${m.signature} (\" + ((System.nanoTime() - ${m.name}ElapsedTime) / 1000000f) + \" ms)\");}");
                        println("     - ${klass.name}#${m.name}${m.signature}");
                    } catch (final Exception e) {
                    }
                }
            }
        }

        klass.writeFile(output.absolutePath);
    }

    /**
     * Compile normal file
     *
     * @param input
     * @param output
     * @throws IOException
     */
    private void compileFile(final File input, final File output) throws IOException {
        if (!output.exists()) {
            output.createNewFile();
        } else {
            this.project.logger.warn("`$output` already exists");
        }

        final FileInputStream fis = new FileInputStream(input);

        try {
            this.compileFile(input, output);
        } finally {
            fis.close();
        }
    }

    /**
     * Compile stream as file
     *
     * @param input
     * @param output
     * @throws IOException
     */
    private void compileFile(final InputStream input, final File output) throws IOException {
        if (!output.exists()) {
            output.getParentFile().mkdirs();
            output.createNewFile();
        } else {
            this.project.logger.warn("`$output` alread exists");
        }

        final OutputStream os = new FileOutputStream(output);

        try {
            IOUtils.copy(input, os);
        } finally {
            IOUtils.closeQuietly(os);
        }
    }
}

