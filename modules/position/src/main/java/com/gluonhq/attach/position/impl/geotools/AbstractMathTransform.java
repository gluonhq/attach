/*
 *    Derived from:
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2001-2015, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */

package com.gluonhq.attach.position.impl.geotools;

import java.io.Serializable;

/**
 * Provides a default implementation for most methods required by the {MathTransform}
 * interface. {@code AbstractMathTransform} provides a convenient base class from which other
 * transform classes can be easily derived. In addition, {@code AbstractMathTransform} implements
 * methods required by the {MathTransform2D} interface, but <strong>does not</strong>
 * implements {@code MathTransform2D}. Subclasses must declare {@code implements MathTransform2D}
 * themselves if they know to maps two-dimensional coordinate systems. For more information,
 * please check out the <a href="http://docs.codehaus.org/display/GEOTOOLS/Coordinate+Transformation+Parameters">
 * tutorial</a>.
 *
 * @author Martin Desruisseaux (IRD)
 * @since 3.9.0
 */
public abstract class AbstractMathTransform {
    /**
     * Constructs a math transform.
     */
    protected AbstractMathTransform() {
    }

    /**
     * Gets the dimension of input points.
     */
    public abstract int getSourceDimensions();

    /**
     * Gets the dimension of output points.
     */
    public abstract int getTargetDimensions();

    /**
     * Tests whether this transform does not move any points.
     * The default implementation always returns {@code false}.
     */
    public boolean isIdentity() {
        return false;
    }

    /**
     * Constructs an error message.
     *
     * @param argument  The argument name with the wrong number of dimensions.
     * @param dimension The wrong dimension.
     * @param expected  The expected dimension.
     */
    private static String constructMessage(final String argument,
                                           final int dimension,
                                           final int expected) {
        StringBuilder sb = new StringBuilder();
        sb.append(argument);
        sb.append(",");
        sb.append(dimension);
        sb.append(",");
        sb.append(expected);
        return sb.toString();
    }

    /**
     * Returns a hash value for this transform.
     */
    @Override
    public int hashCode() {
        return getSourceDimensions() + 37 * getTargetDimensions();
    }

    /**
     * Checks if source coordinates need to be copied before to apply the transformation.
     * This convenience method is provided for {@code transform(...)} method implementation.
     * This method make the following assumptions:
     * <ul>
     *  <li>Coordinates will be iterated from lower index to upper index.</li>
     *  <li>
     *     Coordinates are read and writen in shrunk. For example (longitude,latitude,height)
     *     values for one coordinate are read together, and the transformed (x,y,z) values are
     *     written together only after.
     *  </li>
     * </ul>
     * <p>
     * However, this method does not assumes that source and target dimension are the same (in the
     * special case where source and target dimension are always the same, a simplier and more
     * efficient check is possible). The following example prepares a transformation from 2
     * dimensional points to three dimensional points:
     * </p>
     * <blockquote><pre>
     * public void transform(double[] srcPts, int srcOff,
     *                       double[] dstPts, int dstOff, int numPts)
     * {
     *     if (srcPts==dstPts &amp;&amp; <strong>needCopy</strong>(srcOff, 2, dstOff, 3, numPts) {
     *         final double[] old = srcPts;
     *         srcPts = new double[numPts*2];
     *         System.arraycopy(old, srcOff, srcPts, 0, srcPts.length);
     *         srcOff = 0;
     *     }
     * }</pre></blockquote>
     * <p>
     * <strong>This method is for internal usage by the referencing module only. Do not use!
     * It will be replaced by a different mechanism in a future GeoTools version.</strong>
     * </p>
     *
     * @param srcOff    The offset in the source coordinate array.
     * @param dimSource The dimension of input points.
     * @param dstOff    The offset in the destination coordinate array.
     * @param dimTarget The dimension of output points.
     * @param numPts    The number of points to transform.
     * @return {@code true} if the source coordinates should be copied before to apply the
     * transformation in order to avoid an overlap with the destination array.
     */
    protected static boolean needCopy(final int srcOff, final int dimSource,
                                      final int dstOff, final int dimTarget, final int numPts) {
        if (numPts <= 1 || (srcOff >= dstOff && dimSource >= dimTarget)) {
            /*
             * Source coordinates are stored after target coordinates. If implementation
             * read coordinates from lower index to upper index, then the destination will
             * not overwrite the source coordinates, even if there is an overlaps.
             */
            return false;
        }
        return srcOff < dstOff + numPts * dimTarget &&
                dstOff < srcOff + numPts * dimSource;
    }

    /**
     * Ensures that the specified longitude stay within &plusmn;&pi; radians. This method
     * is typically invoked after geographic coordinates are transformed. This method may add
     * or subtract some amount of 2&pi; radians to <var>x</var>.
     *
     * @param x The longitude in radians.
     * @return The longitude in the range &plusmn;&pi; radians.
     */
    protected static double rollLongitude(final double x) {
        return x - (2 * Math.PI) * Math.floor(x / (2 * Math.PI) + 0.5);
    }

    /**
     * Default implementation for inverse math transform. This inner class is the inverse
     * of the enclosing MathTransform. It is serializable only if the enclosing
     * math transform is also serializable.
     *
     * @author Martin Desruisseaux (IRD)
     * @since 3.9.0
     */
    protected abstract class Inverse extends AbstractMathTransform implements Serializable {
        /**
         * Serial number for interoperability with different versions. This serial number is
         * especilly important for inner classes, since the default {@code serialVersionUID}
         * computation will not produce consistent results across implementations of different
         * Java compiler. This is because different compilers may generate different names for
         * synthetic members used in the implementation of inner classes. See:
         * <p>
         * http://developer.java.sun.com/developer/bugParade/bugs/4211550.html
         */
        private static final long serialVersionUID = 3528274816628012283L;

        /**
         * Constructs an inverse math transform.
         */
        protected Inverse() {
        }

        /**
         * Gets the dimension of input points. The default
         * implementation returns the dimension of output
         * points of the enclosing math transform.
         */
        public int getSourceDimensions() {
            return AbstractMathTransform.this.getTargetDimensions();
        }

        /**
         * Gets the dimension of output points. The default
         * implementation returns the dimension of input
         * points of the enclosing math transform.
         */
        public int getTargetDimensions() {
            return AbstractMathTransform.this.getSourceDimensions();
        }

        /**
         * Tests whether this transform does not move any points.
         * The default implementation delegate this tests to the
         * enclosing math transform.
         */
        @Override
        public boolean isIdentity() {
            return AbstractMathTransform.this.isIdentity();
        }

        /**
         * Returns a hash code value for this math transform.
         */
        @Override
        public int hashCode() {
            return ~AbstractMathTransform.this.hashCode();
        }
    }
}

