package ispd.arquivo.xml.utils;

public record SizeInfo(
        double minimum, double maximum,
        double average, double probability) {
    public SizeInfo() {
        this(0, 0, 0, 0);
    }

    public SizeInfo(final WrappedElement e) {
        this(e.minimum(), e.maximum(), e.average(), e.probability());
    }

    public static SizeInfo rangeFrom(final WrappedElement e) {
        return new SizeInfo(e.minimum(), e.maximum(), 0, 0);
    }

    public static SizeInfo noProbability(final WrappedElement e) {
        return new SizeInfo(e.minimum(), e.maximum(), e.average(), 0);
    }

    public SizeInfo rangeNormalized() {
        return new SizeInfo(
                SizeInfo.normalizeValue(this.average(), this.minimum()),
                SizeInfo.normalizeValue(this.average(), this.minimum()),
                this.average(),
                this.probability()
        );
    }

    private static double normalizeValue(final double value,
                                         final double bound) {
        final var d = Math.abs(value - bound) / value;
        return Math.min(1.0, d);
    }
}
