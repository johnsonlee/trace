package com.sdklite.trace.gradle

import org.gradle.api.Project;

public class TraceExtension {

    private final Project project;

    private boolean enabled;

    private boolean verbose;

    private String[] includes;

    private String[] excludes;

    public TraceExtension(final Project project) {
        this.project = project;
    }

    public boolean getEnabled() {
        return this.enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public boolean getVerbose() {
        return this.verbose;
    }

    public void setVerbose(final boolean verbose) {
        this.verbose = verbose;
    }

    public String[] getIncludes() {
        return this.includes;
    }

    public void setIncludes(final String... includes) {
        this.includes = includes;
    }

    public String[] getExcludes() {
        return this.excludes;
    }

    public void setExcludes(final String... excludes) {
        this.excludes = excludes;
    }

    @Override
    String toString() {
        return "{ enabled:$enabled, verbose:$verbose, includes:$includes, excludes:$excludes }";
    }
}