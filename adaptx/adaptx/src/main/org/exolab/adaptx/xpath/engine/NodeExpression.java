/*
 * (C) Copyright Keith Visco 1999  All rights reserved.
 *
 * The contents of this file are released under an Open Source 
 * Definition (OSD) compliant license; you may not use this file 
 * execpt in compliance with the license. Please see license.txt, 
 * distributed with this file. You may also obtain a copy of the
 * license at http://www.clc-marketing.com/xslp/license.txt
 *
 * The program is provided "as is" without any warranty express or
 * implied, including the warranty of non-infringement and the implied
 * warranties of merchantibility and fitness for a particular purpose.
 * The Copyright owner will not be liable for any damages suffered by
 * you as a result of using the Program. In no event will the Copyright
 * owner be liable for any special, indirect or consequential damages or
 * lost profits even if the Copyright owner has been advised of the
 * possibility of their occurrence.
 *
 * $Id$
 */


package org.exolab.adaptx.xpath.engine;


import org.exolab.adaptx.xpath.XPathNode;
import org.exolab.adaptx.xpath.XPathResult;
import org.exolab.adaptx.xpath.XPathContext;
import org.exolab.adaptx.xpath.XPathExpression;
import org.exolab.adaptx.xpath.XPathException;


/**
 * Represents a NodeExpr
 * @author <a href="kvisco@ziplink.net">Keith Visco</a>
**/
abstract class NodeExpression
    extends XPathExpression
    implements MatchExpression
{

    
    //public static final short ANCESTOR_EXPR  = 0;
    public static final short ANY_EXPR        = 0;
    public static final short ATTRIBUTE_EXPR  = 1;
    public static final short ELEMENT_EXPR    = 2;
    public static final short ID_EXPR         = 3;
    public static final short IDENTITY_EXPR   = 4;
    public static final short PARENT_EXPR     = 5;
    public static final short TEXT_EXPR       = 6;
    public static final short COMMENT_EXPR    = 7;
    public static final short PI_EXPR         = 8;
    public static final short WILDCARD_EXPR   = 9;
    public static final short NAMESPACE_EXPR  = 10;
    
    
    
      //------------------/
     //- Public Methods -/
    //------------------/
    
    /**
     * Returns the type of this NodeExpr
     * @return the type of this NodeExpr
    **/
    public abstract short getNodeExprType();

    
    /**
     * Determines if the given node is matched by this MatchExpr with
     * respect to the given context.
     * @param node the node to determine a match for
     * @param context the XPathContext
     * @return true if the given node is matched by this MatchExpr
     * @exception XPathException when an error occurs during
     * evaluation
    **/
    public abstract boolean matches( XPathNode node, XPathContext context )
        throws XPathException;


    
} //-- NodeExpr
