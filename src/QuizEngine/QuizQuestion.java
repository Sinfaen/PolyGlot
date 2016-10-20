/*
 * Copyright (c) 2016, draque.thompson
 * All rights reserved.
 *
 * Licensed under: Creative Commons Attribution-NonCommercial 4.0 International Public License
 *  See LICENSE.TXT included with this code to read the full license agreement.

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
package QuizEngine;

import PolyGlot.Nodes.ConWord;
import PolyGlot.Nodes.DictNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author draque.thompson
 */
public class QuizQuestion extends DictNode {
    public QuizQuestion(boolean _ignoreCase) {
        ignoreCase = _ignoreCase;
    }
    
    private final List<DictNode> multipleChoices = new ArrayList<>();
    private DictNode answer;
    private QuestionType type;
    private final boolean ignoreCase;
    
    /**
     * Adds a choice to the multiple choice selection
     * @param choice 
     */
    public void addChoice(DictNode choice) {
        multipleChoices.add(choice);
    }
    
    public void setType(QuestionType _type) {
        type = _type;
    }
    
    public QuestionType getType() {
        return type;
    }
    
    /**
     * Gets question based on all values
     * @return string form question.
     */
    public String getQuestion() throws Exception {
        // TODO: THIS
        // base question construction on question type and poentially answer object
        throw new Exception("NOT IMPLEMENTED YET");
    }
    
    /**
     * Gets potential answers (if any) in randomized order
     * @return 
     */
    public List<DictNode> getChoices() {
        long seed = System.nanoTime();
        Collections.shuffle(multipleChoices, new Random(seed));
        return multipleChoices;
    }
    
    @Override
    /**
     * Returns constructed string question value UNLESS there is an override value
     * set. Then ignores construction and uses override. Never includes text that 
     * might be written in the ConLang's font.
     */
    public String getValue() {
        String ret;
        
        if (value.equals("")) {
            ret = value;
        } else {
            if (answer instanceof ConWord) {
                ret = "What is this word's ";
                
                switch (type) {
                    case Local:
                    case PoS:
                    case Proc:
                    case Def:
                    case Classes:
                        for ()
                        break;
                }
            } else {
                ret = "UNSUPPORTED TYPE: " + answer.getClass().getName();
            }
        }
        
        return ret;
    }
    
    /**
     * Tests answer. Must be given in the form of a DictBode of some type
     * @param proposed proposed answer
     * @return 
     * @throws java.lang.Exception on answer type mismatch
     */
    public boolean testAnswer(DictNode proposed) throws Exception {
        boolean ret;
        
        if (proposed instanceof ConWord) {
            if (answer instanceof ConWord) {
                ConWord propWord = (ConWord)proposed;
                ConWord ansWord = (ConWord)answer;
                switch (type) {
                    case Local:                        
                        if (ignoreCase) {
                            ret = ansWord.getLocalWord().equalsIgnoreCase(propWord.getLocalWord());
                        } else {
                            ret = ansWord.getLocalWord().equals(propWord.getLocalWord());
                        }
                        break;
                    case PoS:
                        ret = Objects.equals(ansWord.getWordTypeId(), propWord.getWordTypeId());
                        break;
                    case Proc:
                        if (ignoreCase) {
                            ret = ansWord.getPronunciation().equalsIgnoreCase(propWord.getPronunciation());
                        } else {
                            ret = ansWord.getPronunciation().equals(propWord.getPronunciation());
                        }
                        break;
                    case Def:
                        if (ignoreCase) {
                            ret = ansWord.getDefinition().equalsIgnoreCase(propWord.getDefinition());
                        } else {
                            ret = ansWord.getDefinition().equals(propWord.getDefinition());
                        }
                        break;
                    case Classes:
                        Set<Entry<Integer, Integer>> propClasses = propWord.getClassValues();
                        Set<Entry<Integer, Integer>> ansClasses = ansWord.getClassValues();
                        
                        if (propClasses.size() != ansClasses.size()) {
                            ret = false;
                        } else {
                            ret = true;
                            for (Entry<Integer, Integer> e : propClasses) {
                                if (!Objects.equals(ansWord.getClassValue(e.getKey()), e.getValue())) {
                                    ret = false;
                                    break;
                                }
                            }
                        }
                        break;
                    default:
                        throw new Exception("Unknown question type for ConWord object: " + type.name());
                }
            } else {
                throw new Exception("Answer type mismatch. " + answer.getClass().getName() 
                        + " vs. " + proposed.getClass().getName());
            }
        } else {
            throw new Exception("Answer for checking type " + proposed.getClass().getName() 
                    + " not yet implemented.");
        }
        
        return ret;
    }
    
    public DictNode getAnswer() {
        return answer;
    }
    
    public void setAnswer(DictNode _answer) {
        answer = _answer;
    }
    
    @Override
    public void setEqual(DictNode _node) throws ClassCastException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
 
    public enum QuestionType {
        Local, PoS, Proc, Def, Classes
    }
}
