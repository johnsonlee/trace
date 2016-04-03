package com.sdklite.trace.gradle;

/**
 * @author johnsonlee
 */
class TraceOptions {

    private final List<File> inputs;

    private final List<File> references;

    private final File output;

    private TraceOptions(final List<File> inputs, final List<File> references, final File output) {
        this.inputs = inputs;
        this.references = references;
        this.output = output;
    }

    public Collection<File> getInputs() {
        return Collections.unmodifiableCollection(this.inputs);
    }

    public Collection<File> getReferences() {
        return Collections.unmodifiableCollection(this.references);
    }

    public File getOutput() {
        return this.output;
    }

    public static final class Builder {

        private final List<File> inputs = new ArrayList<File>();

        private final List<File> references = new ArrayList<File>();

        private final File output;

        public Builder(final File output) {
            this.output = output;
        }

        public Builder inputs(final List<File> inputs) {
            this.inputs.addAll(inputs);
            return this;
        }

        public Builder inputs(final File input) {
            this.inputs.add(input);
            return this;
        }

        public Builder references(final File reference) {
            this.references.add(reference);
            return this;
        }

        public Builder references(final List<File> references) {
            this.references.addAll(references);
            return this;
        }

        public TraceOptions build() {
            return new TraceOptions(this.inputs, this.references, this.output);
        }
    }

}

