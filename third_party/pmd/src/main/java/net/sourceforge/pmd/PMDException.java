/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
package net.sourceforge.pmd;

/**
 * A convenience exception wrapper.  Contains the original exception, if any.  Also, contains
 * a severity number (int).  Zero implies no severity.  The higher the number the greater the
 * severity.
 *
 * @author Donald A. Leckie
 * @version $Revision: 7476 $, $Date: 2011-11-21 21:45:07 +0100 (Mo, 21. Nov 2011) $
 * @since August 30, 2002
 */
public class PMDException extends Exception {
    private static final long serialVersionUID = 6938647389367956874L;

    private int severity;

    public PMDException(String message) {
        super(message);
    }

    public PMDException(String message, Exception reason) {
        super(message, reason);
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    public int getSeverity() {
        return severity;
    }
}
