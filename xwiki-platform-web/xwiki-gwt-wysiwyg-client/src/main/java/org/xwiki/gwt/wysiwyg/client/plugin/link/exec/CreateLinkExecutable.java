/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.gwt.wysiwyg.client.plugin.link.exec;

import org.xwiki.gwt.dom.client.Range;
import org.xwiki.gwt.user.client.ui.rta.RichTextArea;
import org.xwiki.gwt.user.client.ui.rta.cmd.internal.AbstractInsertElementExecutable;
import org.xwiki.gwt.wysiwyg.client.plugin.link.LinkConfig;
import org.xwiki.gwt.wysiwyg.client.plugin.link.LinkConfigHTMLParser;
import org.xwiki.gwt.wysiwyg.client.plugin.link.LinkConfigHTMLSerializer;
import org.xwiki.gwt.wysiwyg.client.plugin.link.LinkConfigJSONParser;
import org.xwiki.gwt.wysiwyg.client.plugin.link.LinkConfigJSONSerializer;

import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Node;

/**
 * Creates a link by inserting the link XHTML.
 * 
 * @version $Id$
 */
public class CreateLinkExecutable extends AbstractInsertElementExecutable<LinkConfig, AnchorElement>
{
    /**
     * Creates a new executable that can be used to insert links in the specified rich text area.
     * 
     * @param rta the execution target
     */
    public CreateLinkExecutable(RichTextArea rta)
    {
        super(rta);

        configHTMLParser = new LinkConfigHTMLParser();
        configHTMLSerializer = new LinkConfigHTMLSerializer();
        configJSONParser = new LinkConfigJSONParser();
        configJSONSerializer = new LinkConfigJSONSerializer();
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractInsertElementExecutable#isEnabled()
     */
    public boolean isEnabled()
    {
        if (!super.isEnabled()) {
            return false;
        }

        // Create link executable is enabled either for creating a new link or for editing an existing link. Check if
        // we're editing a link.
        if (getSelectedElement() != null) {
            return true;
        }

        // If no anchor on ancestor, test all the nodes touched by the selection to not contain an anchor.
        Range range = rta.getDocument().getSelection().getRangeAt(0);
        if (domUtils.getFirstDescendant(range.cloneContents(), LinkExecutableUtils.ANCHOR_TAG_NAME) != null) {
            return false;
        }

        // Check if the selection does not contain any block elements.
        Node commonAncestor = range.getCommonAncestorContainer();
        if (!domUtils.isInline(commonAncestor)) {
            // The selection may contain a block element, check if it actually does.
            Node leaf = domUtils.getFirstLeaf(range);
            Node lastLeaf = domUtils.getLastLeaf(range);
            while (true) {
                if (leaf != null) {
                    // Check if it has any non-in-line parents up to the commonAncestor.
                    Node parentNode = leaf;
                    while (parentNode != commonAncestor) {
                        if (!domUtils.isInline(parentNode)) {
                            // Found a non-in-line parent, return false.
                            return false;
                        }
                        parentNode = parentNode.getParentNode();
                    }
                }
                // Go to next leaf, if any are left.
                if (leaf == lastLeaf) {
                    break;
                } else {
                    leaf = domUtils.getNextLeaf(leaf);
                }
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractInsertElementExecutable#getCacheKeyPrefix()
     */
    @Override
    protected String getCacheKeyPrefix()
    {
        return CreateLinkExecutable.class.getName();
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractInsertElementExecutable#getSelectedElement()
     */
    @Override
    protected AnchorElement getSelectedElement()
    {
        return LinkExecutableUtils.getSelectedAnchor(rta);
    }
}
