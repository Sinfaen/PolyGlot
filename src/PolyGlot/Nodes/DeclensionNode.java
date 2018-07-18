/*
 * Copyright (c) 2014-2018, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: Creative Commons Attribution-NonCommercial 4.0 International Public License
 * See LICENSE.TXT included with this code to read the full license agreement.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package PolyGlot.Nodes;

import PolyGlot.DeclensionDimension;
import PolyGlot.PGTUtil;
import PolyGlot.WebInterface;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class represents both the header for declension templates, and the actual
 * body object for fully realized declension constructs (with full combined Dim Ids)
 * @author draque
 */
public class DeclensionNode extends DictNode{
    private String notes = "";
    private String combinedDimId = "";
    private boolean mandatory = false;
    private int highestDimension = 1;
    private final Map<Integer, DeclensionDimension> dimensions = new HashMap<>();
    private DeclensionDimension buffer = new DeclensionDimension(-1);
    
    /**
     * gets dimensional buffer
     * @return current buffer
     */
    public DeclensionDimension getBuffer() {
        return buffer;
    }
    
    /**
     * Inserts current value of dimensional buffer.
     * Clears buffer after insert.
     * @throws java.lang.Exception if buffer ID is -1
     */
    public void insertBuffer() throws Exception {
        if (buffer.getId() == -1) {
            throw new Exception("Dimension with ID -1 cannot be inserted.");
        }
        
        this.addDimension(buffer);
        buffer = new DeclensionDimension(-1);
    }
    
    /**
     * clears current value of buffer
     */
    public void clearBuffer() {
        buffer = new DeclensionDimension(-1);
    }
    
    /**
     * Adds a dimension to this declension
     * @param dim Dimension to be added. Set id if desired, generated otherwise
     * @return id of created dimension (whether user or system set)
     */
    public Integer addDimension(DeclensionDimension dim) {
        DeclensionDimension addDim;
        Integer ret;
        
        // use given ID if available, create one otherwise
        if (dim.getId().equals(-1)) {
            ret = highestDimension + 1;            
        } else {
            ret = dim.getId();
        }
        
        // highest current dimension is always whichever value is larger. Prevent overlap.
        highestDimension = highestDimension > ret ? highestDimension : ret;
        
        addDim = new DeclensionDimension(ret);
        addDim.setValue(dim.getValue());
        addDim.setMandatory(dim.isMandatory());
        
        dimensions.put(ret, addDim);
               
        return ret;
    }
    
    /**
     * eliminates all dimensions from node
     */
    public void clearDimensions() {
        dimensions.clear();
    }
    
    /**
     * Deletes a dimension from this declension (it rhymes!)
     * @param id id of dimension to be deleted
     */
    public void deleteDimension(Integer id) {
        if (dimensions.containsKey(id)) {
            dimensions.remove(id);
        }
    }
        
    public DeclensionNode(Integer _declentionId) {
        id = _declentionId;
    }
    
    public boolean isMandatory() {
        return mandatory;
    }
    
    public void setMandatory(boolean _mandatory) {
        mandatory = _mandatory;
    }
    
    public void setNotes(String _notes) {
        notes = _notes;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public Collection<DeclensionDimension> getDimensions() {
        return dimensions.values();

    }

    protected Map<Integer, DeclensionDimension> getRawDimensions() {
        return dimensions;
    }
    
    public void setCombinedDimId(String _id) {
        combinedDimId = _id;
    }
    
    public String getCombinedDimId() {
        return combinedDimId;
    }
    
    public void writeXMLTemplate(Document doc, Element rootElement, Integer relatedId) {
        Element wordNode = doc.createElement(PGTUtil.declensionXID);
        rootElement.appendChild(wordNode);

        Element wordValue = doc.createElement(PGTUtil.declensionIdXID);
        wordValue.appendChild(doc.createTextNode(this.getId().toString()));
        wordNode.appendChild(wordValue);

        wordValue = doc.createElement(PGTUtil.declensionTextXID);
        wordValue.appendChild(doc.createTextNode(this.getValue()));
        wordNode.appendChild(wordValue);

        wordValue = doc.createElement(PGTUtil.declensionNotesXID);
        wordValue.appendChild(doc.createTextNode(WebInterface.archiveHTML(this.getNotes())));
        wordNode.appendChild(wordValue);

        wordValue = doc.createElement(PGTUtil.declensionIsTemplateXID);
        wordValue.appendChild(doc.createTextNode("1"));
        wordNode.appendChild(wordValue);

        wordValue = doc.createElement(PGTUtil.declensionRelatedIdXID);
        wordValue.appendChild(doc.createTextNode(relatedId.toString()));
        wordNode.appendChild(wordValue);

        wordValue = doc.createElement(PGTUtil.declensionMandatoryXID);
        wordValue.appendChild(doc.createTextNode(this.isMandatory() ? PGTUtil.True : PGTUtil.False));
        wordNode.appendChild(wordValue);

        // record dimensions of declension
        Iterator<DeclensionDimension> dimIt = this.getDimensions().iterator();
        while (dimIt.hasNext()) {
            dimIt.next().writeXML(doc, wordNode);
        }
    }
    
    public void writeXMLWordDeclension(Document doc, Element rootElement, Integer relatedId) {
        Element wordNode = doc.createElement(PGTUtil.declensionXID);
        rootElement.appendChild(wordNode);

        Element wordValue = doc.createElement(PGTUtil.declensionIdXID);
        wordValue.appendChild(doc.createTextNode(this.getId().toString()));
        wordNode.appendChild(wordValue);

        wordValue = doc.createElement(PGTUtil.declensionTextXID);
        wordValue.appendChild(doc.createTextNode(this.getValue()));
        wordNode.appendChild(wordValue);

        wordValue = doc.createElement(PGTUtil.declensionNotesXID);
        wordValue.appendChild(doc.createTextNode(this.getNotes()));
        wordNode.appendChild(wordValue);

        wordValue = doc.createElement(PGTUtil.declensionRelatedIdXID);
        wordValue.appendChild(doc.createTextNode(relatedId.toString()));
        wordNode.appendChild(wordValue);

        wordValue = doc.createElement(PGTUtil.declensionComDimIdXID);
        wordValue.appendChild(doc.createTextNode(this.getCombinedDimId()));
        wordNode.appendChild(wordValue);

        wordValue = doc.createElement(PGTUtil.declensionIsTemplateXID);
        wordValue.appendChild(doc.createTextNode("0"));
        wordNode.appendChild(wordValue);
    }
    
    @Override
    public void setEqual(DictNode _node) throws ClassCastException {
        if (!(_node instanceof DeclensionNode)) {
            throw new ClassCastException("Object not of type DeclensionNode");
        }
        
        DeclensionNode node = (DeclensionNode) _node;
        
        this.setNotes(node.getNotes());
        this.setValue(node.getValue());
        this.setMandatory(node.isMandatory());
        this.setCombinedDimId(node.getCombinedDimId());

        node.getRawDimensions().entrySet().forEach((entry) -> {
            DeclensionDimension copyOfDim = new DeclensionDimension(entry.getKey());
            copyOfDim.setEqual(entry.getValue());
            dimensions.put(entry.getKey(), copyOfDim);
        });
    }
}
