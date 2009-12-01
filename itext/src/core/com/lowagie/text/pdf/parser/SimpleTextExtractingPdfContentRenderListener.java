/*
 * $Id$
 *
 * This file is part of the iText project.
 * Copyright (c) 1998-2009 1T3XT BVBA
 * Authors: Kevin Day, Bruno Lowagie, Paulo Soares, et al.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY 1T3XT,
 * 1T3XT DISCLAIMS THE WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA, 02110-1301 USA, or download the license from the following URL:
 * http://itextpdf.com/terms-of-use/
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License,
 * you must retain the producer line in every PDF that is created or manipulated
 * using iText.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the iText software without
 * disclosing the source code of your own applications.
 * These activities include: offering paid services to customers as an ASP,
 * serving PDFs on the fly in a web application, shipping iText with a closed
 * source product.
 *
 * For more information, please contact iText Software Corp. at this
 * address: sales@itextpdf.com
 */
package com.lowagie.text.pdf.parser;


/**
 * A simple text extraction renderer.
 * 
 * This renderer keeps track of the current Y position of each string.  If it detects
 * that the y position has changed, it inserts a line break into the output.  If the
 * PDF renders text in a non-top-to-bottom fashion, this will result in the text not
 * being a true representation of how it appears in the PDF.
 * 
 * This renderer also uses a simple strategy based on the font metrics to determine if
 * a blank space should be inserted into the output.
 * 
 * @since	2.1.5
 */
public class SimpleTextExtractingPdfContentRenderListener implements TextProvidingRenderListener {

    /** keeps track of the Y position of the last rendered text */
    private float lastYPos;
    /** keeps track of the X position of the end of the last rendered text */
    private float lastEndingXPos;

    private Matrix lastTextLineMatrix;
    
    
    private Vector lastStart;
    private Vector lastEnd;
    
    /** used to store the resulting String. */
    private StringBuffer result;

    /**
     * Creates a new text extraction renderer.
     */
    public SimpleTextExtractingPdfContentRenderListener() {
        reset();
    }

    public void reset() {
        lastYPos = 0f;
        lastEndingXPos = 0f;
        lastTextLineMatrix = null;
        result = new StringBuffer();
    }
    
    /**
     * Returns the result so far.
     * @return	a String with the resulting text.
     */
    public String getResultantText(){
        return result.toString();
    }
    /**
     * Writes text to the result.
     * @param text  The text that needs to be displayed
     * @param gs    The current graphics state, including the current font and various spacings needed to compute glyph widths
     * @param renderInto The rectangle that the result will be rendered into
     * @param spaceWidth The scaled width of a space character in the current font
     * @see com.lowagie.text.pdf.parser.RenderListener#renderText(String, GraphicsState, Rectangle)
     */

    /**
     * Captures text using a simplified algorithm for inserting hard returns and spaces
     * @see com.lowagie.text.pdf.parser.AbstractRenderListener#renderText(java.lang.String, com.lowagie.text.pdf.parser.GraphicsState, com.lowagie.text.pdf.parser.Matrix, com.lowagie.text.pdf.parser.Matrix)
     */
    public void renderText(TextRenderInfo renderInfo) {
        boolean firstRender = result.length() == 0;
        boolean hardReturn = false;

        Vector start = renderInfo.getStartPoint();
        Vector end = renderInfo.getEndPoint();
        
        if (!firstRender){
            Vector x0 = start;
            Vector x1 = lastStart;
            Vector x2 = lastEnd;
            
            // see http://mathworld.wolfram.com/Point-LineDistance2-Dimensional.html
            float dist = (x2.subtract(x1)).cross((x1.subtract(x0))).lengthSquared() / x2.subtract(x1).lengthSquared();

            float sameLineThreshold = 1f; // we should probably base this on the current font metrics, but 1 pt seems to be sufficient for the time being
            if (dist > sameLineThreshold)
                hardReturn = true;
            
            // Note:  Technically, we should check both the start and end positions, in case the angle of the text changed without any displacement
            // but this sort of thing probably doesn't happen much in reality, so we'll leave it alone for now
        }
        
        if (hardReturn){
            //System.out.println("<< Hard Return >>");
            result.append('\n');
        } else if (!firstRender){ 
            if (result.charAt(result.length()-1) != ' ' && renderInfo.getText().charAt(0) != ' '){ // we only insert a blank space if the trailing character of the previous string wasn't a space, and the leading character of the current string isn't a space
                float spacing = lastEnd.subtract(start).length();
                if (spacing > renderInfo.getSingleSpaceWidth()/2f){
                    result.append(' ');
                    //System.out.println("Inserting implied space before '" + renderInfo.getText() + "'");
                }
            }
        } else {
            //System.out.println("Displaying first string of content '" + text + "' :: x1 = " + x1);
        }
        
        //System.out.println("[" + renderInfo.getStartPoint() + "]->[" + renderInfo.getEndPoint() + "] " + renderInfo.getText());
        result.append(renderInfo.getText());

        lastStart = start;
        lastEnd = end;
        
    }

}
