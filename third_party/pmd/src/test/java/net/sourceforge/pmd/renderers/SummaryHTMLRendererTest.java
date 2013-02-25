package net.sourceforge.pmd.renderers;

import java.util.Properties;

import net.sourceforge.pmd.PMD;
import net.sourceforge.pmd.Report.ProcessingError;
import net.sourceforge.pmd.renderers.Renderer;
import net.sourceforge.pmd.renderers.SummaryHTMLRenderer;

public class SummaryHTMLRendererTest extends AbstractRendererTst {

    @Override
    public Renderer getRenderer() {
	Properties properties = new Properties();
	properties.put(SummaryHTMLRenderer.LINK_PREFIX, "link_prefix");
	properties.put(SummaryHTMLRenderer.LINE_PREFIX, "line_prefix");
        return new SummaryHTMLRenderer(properties);
    }

    @Override
    public String getExpected() {
        return "<html><head><title>PMD</title></head><body>" + PMD.EOL +
                "<h2><center>Summary</h2></center><table align=\"center\" cellspacing=\"0\" cellpadding=\"3\"><th>Rule name</th><th>Number of violations</th><tr><td>Foo</td><td align=center>1</td></tr></table><h2><center>Detail</h2></center><table align=\"center\" cellspacing=\"0\" cellpadding=\"3\"><tr>" + PMD.EOL +
                "<center><h3>PMD report</h3></center><center><h3>Problems found</h3></center><table align=\"center\" cellspacing=\"0\" cellpadding=\"3\"><tr>" + PMD.EOL +
                "<th>#</th><th>File</th><th>Line</th><th>Problem</th></tr>" + PMD.EOL +
                "<tr bgcolor=\"lightgrey\"> " + PMD.EOL +
                "<td align=\"center\">1</td>" + PMD.EOL +
                "<td width=\"*%\"><a href=\"link_prefixn/a.html#line_prefix1\">n/a</a></td>" + PMD.EOL +
                "<td align=\"center\" width=\"5%\">1</td>" + PMD.EOL +
                "<td width=\"*\">msg</td>" + PMD.EOL +
                "</tr>" + PMD.EOL +
                "</table></table></body></html>" + PMD.EOL;

    }

    @Override
    public String getExpectedEmpty() {
        return "<html><head><title>PMD</title></head><body>" + PMD.EOL +
        "<h2><center>Summary</h2></center><table align=\"center\" cellspacing=\"0\" cellpadding=\"3\"><th>Rule name</th><th>Number of violations</th></table><h2><center>Detail</h2></center><table align=\"center\" cellspacing=\"0\" cellpadding=\"3\"><tr>" + PMD.EOL +
        "<center><h3>PMD report</h3></center><center><h3>Problems found</h3></center><table align=\"center\" cellspacing=\"0\" cellpadding=\"3\"><tr>" + PMD.EOL +
        "<th>#</th><th>File</th><th>Line</th><th>Problem</th></tr>" + PMD.EOL +
        "</table></table></body></html>" + PMD.EOL;
    }

    @Override
    public String getExpectedMultiple() {
        return "<html><head><title>PMD</title></head><body>" + PMD.EOL +
        "<h2><center>Summary</h2></center><table align=\"center\" cellspacing=\"0\" cellpadding=\"3\"><th>Rule name</th><th>Number of violations</th><tr><td>Foo</td><td align=center>2</td></tr></table><h2><center>Detail</h2></center><table align=\"center\" cellspacing=\"0\" cellpadding=\"3\"><tr>" + PMD.EOL +
        "<center><h3>PMD report</h3></center><center><h3>Problems found</h3></center><table align=\"center\" cellspacing=\"0\" cellpadding=\"3\"><tr>" + PMD.EOL +
        "<th>#</th><th>File</th><th>Line</th><th>Problem</th></tr>" + PMD.EOL +
        "<tr bgcolor=\"lightgrey\"> " + PMD.EOL +
        "<td align=\"center\">1</td>" + PMD.EOL +
        "<td width=\"*%\"><a href=\"link_prefixn/a.html#line_prefix1\">n/a</a></td>" + PMD.EOL +
        "<td align=\"center\" width=\"5%\">1</td>" + PMD.EOL +
        "<td width=\"*\">msg</td>" + PMD.EOL +
        "</tr>" + PMD.EOL +
        "<tr> " + PMD.EOL +
        "<td align=\"center\">2</td>" + PMD.EOL +
        "<td width=\"*%\"><a href=\"link_prefixn/a.html#line_prefix1\">n/a</a></td>" + PMD.EOL +
        "<td align=\"center\" width=\"5%\">1</td>" + PMD.EOL +
        "<td width=\"*\">msg</td>" + PMD.EOL +
        "</tr>" + PMD.EOL +
        "</table></table></body></html>" + PMD.EOL;
    }

    @Override
    public String getExpectedError(ProcessingError error) {
        return "<html><head><title>PMD</title></head><body>" + PMD.EOL +
        "<h2><center>Summary</h2></center><table align=\"center\" cellspacing=\"0\" cellpadding=\"3\"><th>Rule name</th><th>Number of violations</th></table><h2><center>Detail</h2></center><table align=\"center\" cellspacing=\"0\" cellpadding=\"3\"><tr>" + PMD.EOL +
        "<center><h3>PMD report</h3></center><center><h3>Problems found</h3></center><table align=\"center\" cellspacing=\"0\" cellpadding=\"3\"><tr>" + PMD.EOL +
        "<th>#</th><th>File</th><th>Line</th><th>Problem</th></tr>" + PMD.EOL +
        "</table><hr/><center><h3>Processing errors</h3></center><table align=\"center\" cellspacing=\"0\" cellpadding=\"3\"><tr>" + PMD.EOL +
        "<th>File</th><th>Problem</th></tr>" + PMD.EOL +
        "<tr bgcolor=\"lightgrey\"> " + PMD.EOL +
        "<td>file</td>" + PMD.EOL +
        "<td>Error</td>" + PMD.EOL +
        "</tr>" + PMD.EOL +
        "</table></table></body></html>" + PMD.EOL;
    }

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(SummaryHTMLRendererTest.class);
    }
}



