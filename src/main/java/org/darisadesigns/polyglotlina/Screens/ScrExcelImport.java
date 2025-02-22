/*
 * Copyright (c) 2014-2023, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: MIT Licence
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
package org.darisadesigns.polyglotlina.Screens;

import org.darisadesigns.polyglotlina.DictCore;
import org.darisadesigns.polyglotlina.Desktop.ImportFileHelper;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.DesktopInfoBox;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PButton;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PCheckBox;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PComboBox;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PDialog;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PLabel;
import org.darisadesigns.polyglotlina.Desktop.DesktopIOHandler;
import java.io.File;
import java.nio.charset.Charset;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.csv.CSVFormat;
import org.darisadesigns.polyglotlina.Desktop.CustomControls.PRadioButton;
import org.darisadesigns.polyglotlina.Desktop.DesktopPropertiesManager;
import org.darisadesigns.polyglotlina.Desktop.ImportFileHelper.DuplicateOption;
import org.darisadesigns.polyglotlina.PGTUtil;

/**
 *
 * @author draque
 */
public class ScrExcelImport extends PDialog {

    private final ScrMainMenu parent;

    /**
     * Creates new form scrExcelImport
     *
     * @param _core feed this the dictCore from the main program
     * @param _parent main menu form
     */
    public ScrExcelImport(DictCore _core, ScrMainMenu _parent) {
        super(_core);
        
        parent = _parent;
        initComponents();
        setModal(true);
    }

    @Override
    public final void setModal(boolean _modal) {
        super.setModal(_modal);
    }

    @Override
    public void updateAllValues(DictCore _core) {
        // does nothing
    }
    
    /**
     * @param _core the dictionary core
     * @param _parent main menu form
     * @return 
     */
    public static ScrExcelImport run(DictCore _core, ScrMainMenu _parent) {
        ScrExcelImport s = new ScrExcelImport(_core, _parent);

        s.setModal(true);
        s.toFront();
        s.setVisible(true);
        
        return s;
    }

    private void browseFile() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Excel/CSV Documents", "xls", "xlsx", "xlsm", "csv", "txt", "tsv");
        chooser.setFileFilter(filter);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtFileName.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void importFile() {
        File file = new File(txtFileName.getText());

        if (!file.exists()) {
            core.getOSHandler().getInfoBox().error("File Error", "File does not exist: " + txtFileName.getText());
            return;
        }
        try {
            ImportFileHelper reader = new ImportFileHelper(core);
            CSVFormat format = ((Delimiter) cmbPreferences.getSelectedItem()).getFormat();
            String quotOpt = ((QuoteOptions)cmbQuoteChar.getSelectedItem()).getValue();
            DuplicateOption dupOpt = DuplicateOption.IMPORT_ALL;
            
            if (rdoIgnrDups.isSelected()) {
                dupOpt = DuplicateOption.IGNORE_DUPES;
            } else if (rdoOverwDups.isSelected()) {
                dupOpt = DuplicateOption.OVERWRITE_DUPES;
            }
            
            reader.setOptions(txtConWord.getText(), 
                    txtLocalWord.getText(),
                    txtType.getText(), 
                    txtClass.getText(),
                    txtDefinition.getText(), 
                    txtPronunciation.getText(), 
                    format,
                    chkFirstLabels.isSelected(), 
                    true, 
                    quotOpt,
                    dupOpt);
            reader.importFile(txtFileName.getText(), Integer.valueOf(txtExcelSheet.getText()));

            if (parent != null) {
                parent.updateAllValues(core);
                new DesktopInfoBox(this).info("Success!", txtFileName.getText() + " imported successfully!");

                // if everything has completed without error, close the window and open Lexicon
                dispose();
                parent.openLexicon(true);
            }
        } catch (NumberFormatException e) {
            DesktopIOHandler.getInstance().writeErrorLog(e);
            new DesktopInfoBox(this).error("Import Error", "All column fields and sheet field must contain "
                    + "numeric values only:\n" + e.getLocalizedMessage());
        } catch (IllegalStateException e) {
            DesktopIOHandler.getInstance().writeErrorLog(e);
            new DesktopInfoBox(this).error("Import Error", "Could not import from file " + txtFileName.getText()
                    + ".\n The text encoding is not supported. Please open the import file in a "
                    + "text editor and save with encoding: " + Charset.defaultCharset().displayName());
        } catch (Exception e) {
            DesktopIOHandler.getInstance().writeErrorLog(e);
            new DesktopInfoBox(this).error("Import Error", "Could not import from file " + txtFileName.getText()
                    + windowsError()
                    + ".\n Check to make certain that column mappings are correct "
                    + "(nothing above max cell value) and that the file is not corrupt:\n"
                    + e.getLocalizedMessage());
        }
    }
    
    private String windowsError() {
        String ret = "";
        
        if (PGTUtil.IS_WINDOWS) {
            ret += "\nIf the file is open in Excel, please close Excel.";
        }
        
        return ret;
    }

    public enum Delimiter {
        comma("Comma", CSVFormat.DEFAULT),
        semicolon("Semicolon", CSVFormat.newFormat(';')),
        tab("Tab", CSVFormat.TDF);

        private final String name;
        private final CSVFormat format;

        Delimiter(String s, CSVFormat _format) {
            name = s;
            format = _format;
        }

        public boolean equalsName(String otherName) {
            return name.equals(otherName);
        }

        @Override
        public String toString() {
            return this.name;
        }
        
        public CSVFormat getFormat() {
            return this.format;
        }
    }
    
    public enum QuoteOptions {
        none("None", ""),
        doubleQuotes("Double Quotes", "\""),
        singleQuotes("Single Quotes", "'");
        
        
        private final String name;
        private final String quote;
        
        QuoteOptions(String _name, String _quote) {
            name = _name;
            quote = _quote;
        }
        
        public String getValue() {
            return quote;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnGrpCopyChoice = new javax.swing.ButtonGroup();
        txtFileName = new javax.swing.JTextField();
        jLabel1 = new PLabel("");
        btnBrowse = new PButton(nightMode);
        jPanel1 = new javax.swing.JPanel();
        chkFirstLabels = new PCheckBox(nightMode);
        jLabel2 = new PLabel("");
        jLabel3 = new PLabel("");
        jLabel4 = new PLabel("");
        jLabel5 = new PLabel("");
        jLabel6 = new PLabel("");
        jLabel7 = new PLabel("");
        jLabel8 = new PLabel("");
        jLabel9 = new PLabel("");
        txtConWord = new javax.swing.JTextField();
        txtLocalWord = new javax.swing.JTextField();
        txtType = new javax.swing.JTextField();
        txtDefinition = new javax.swing.JTextField();
        txtPronunciation = new javax.swing.JTextField();
        txtClass = new javax.swing.JTextField();
        jLabel11 = new PLabel("");
        txtExcelSheet = new javax.swing.JTextField();
        jLabel10 = new PLabel("");
        cmbPreferences = new PComboBox<Delimiter>(((DesktopPropertiesManager)core.getPropertiesManager()).getFontMenu(), core);
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jLabel12 = new PLabel("");
        cmbQuoteChar = new PComboBox<>(((DesktopPropertiesManager)core.getPropertiesManager()).getFontMenu(), core);
        btnImport = new PButton(nightMode);
        btnCancel = new PButton(nightMode);
        jPanel2 = new javax.swing.JPanel();
        jLabel13 = new PLabel("Duplicate Handling");
        rdoImpAll = new PRadioButton(core);
        rdoIgnrDups = new PRadioButton(core);
        rdoOverwDups = new PRadioButton(core);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Import From External Format");
        setMinimumSize(new java.awt.Dimension(457, 559));

        txtFileName.setToolTipText("path and name of file to import");

        jLabel1.setText("Import File");

        btnBrowse.setText("Browse...");
        btnBrowse.setToolTipText("Browse for file to import");
        btnBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBrowseActionPerformed(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel1.setName(""); // NOI18N

        chkFirstLabels.setText("First row is labels");
        chkFirstLabels.setToolTipText("This lets the tool know to skip the first row.");

        jLabel2.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        jLabel2.setText("CONLANG");

        jLabel3.setText("ConWord");
        jLabel3.setToolTipText("Constructed Language Word");

        jLabel4.setText("Local Word");
        jLabel4.setToolTipText("Synonym in local language");

        jLabel5.setText("Part of Speech");
        jLabel5.setToolTipText("Part of speech");

        jLabel6.setText("Definition");
        jLabel6.setToolTipText("Word definition");

        jLabel7.setText("Pronunciation");
        jLabel7.setToolTipText("Word pronunciation");

        jLabel8.setText("Classes");
        jLabel8.setToolTipText("Word Classes");

        jLabel9.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        jLabel9.setText("COLUMN #");

        txtConWord.setToolTipText("Column number of constructed word");

        txtLocalWord.setToolTipText("Column number of local word");

        txtType.setToolTipText("Column number of Part of Speech");

        txtDefinition.setToolTipText("Column number of definition");

        txtPronunciation.setToolTipText("Column number of pronuncation");

        txtClass.setToolTipText("Column number(s) for word class values (for example, gender)");

        jLabel11.setText("Excel Sheet");

        txtExcelSheet.setText("0");
        txtExcelSheet.setToolTipText("Sheet of excel doument to import");

        jLabel10.setText("Delimiter");

        cmbPreferences.setModel(new javax.swing.DefaultComboBoxModel<>(new Delimiter[] { Delimiter.comma, Delimiter.semicolon, Delimiter.tab }));
        cmbPreferences.setToolTipText("Set the delimiter to comma, semicolon, or tab (only matters for CSV imports)");

        jTextPane1.setEditable(false);
        jTextPane1.setContentType("text/html"); // NOI18N
        jTextPane1.setText("<html>Columns may be addressed numerically or alphabetically. <b>If numerically, begin with 0.</b>  <br><br>Map the columns within the target Excel/CSV file to their related fields within the conlang dictionary.   <br><br>If the “First row is labels” box is selected, the first row of the grid will be ignored.  <br><br>To map multiple columns to a single field (generally multiple to definition), simply add the row numbers separated by a comma.  <br><br>The delimiter is the character separating entries if you are importing from a CSV. Leave blank to default to a comma.  <br><br>If you don't know what \"Excel Sheet\" means, or are using a CSV leave it at 0. </html>");
        jTextPane1.setToolTipText("");
        jScrollPane2.setViewportView(jTextPane1);

        jLabel12.setText("Quotes");

        cmbQuoteChar.setModel(new javax.swing.DefaultComboBoxModel<>(new QuoteOptions[] {QuoteOptions.none, QuoteOptions.doubleQuotes, QuoteOptions.singleQuotes}));
        cmbQuoteChar.setToolTipText("If you are using quote encapsulation in your CSV, select the character here.");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(chkFirstLabels)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtLocalWord, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtConWord)
                            .addComponent(txtType)
                            .addComponent(txtDefinition)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(txtPronunciation, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE)
                                    .addComponent(txtClass, javax.swing.GroupLayout.Alignment.TRAILING)))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10)
                            .addComponent(jLabel12))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cmbPreferences, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(7, 7, 7)
                                .addComponent(cmbQuoteChar, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtExcelSheet, javax.swing.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(chkFirstLabels)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jLabel9))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(txtConWord, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(txtLocalWord, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(txtType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(txtDefinition, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(txtPronunciation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(txtClass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(cmbPreferences, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel12)
                            .addComponent(cmbQuoteChar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11)
                            .addComponent(txtExcelSheet, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );

        btnImport.setText("Import");
        btnImport.setToolTipText("Perform import");
        btnImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImportActionPerformed(evt);
            }
        });

        btnCancel.setText("Cancel");
        btnCancel.setToolTipText("Cancel import");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel13.setText("Duplicate Handling");

        btnGrpCopyChoice.add(rdoImpAll);
        rdoImpAll.setSelected(true);
        rdoImpAll.setText("Import All");
        rdoImpAll.setToolTipText("Import all values, regardless of duplicates found.");

        btnGrpCopyChoice.add(rdoIgnrDups);
        rdoIgnrDups.setText("Ignore Duplicates");
        rdoIgnrDups.setToolTipText("If duplicates exist in the import file, they will be ignored.");

        btnGrpCopyChoice.add(rdoOverwDups);
        rdoOverwDups.setText("Overwrite Duplicates");
        rdoOverwDups.setToolTipText("If duplicates are found, existing values will be overwritten.");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel13)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rdoImpAll)
                .addGap(18, 18, 18)
                .addComponent(rdoIgnrDups)
                .addGap(18, 18, 18)
                .addComponent(rdoOverwDups)
                .addContainerGap(83, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rdoImpAll)
                    .addComponent(rdoIgnrDups)
                    .addComponent(rdoOverwDups)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtFileName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnBrowse))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnCancel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnImport)))
                .addContainerGap())
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtFileName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(btnBrowse))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnImport)
                    .addComponent(btnCancel)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImportActionPerformed
        importFile();
    }//GEN-LAST:event_btnImportActionPerformed

    private void btnBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseActionPerformed
        browseFile();
    }//GEN-LAST:event_btnBrowseActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBrowse;
    private javax.swing.JButton btnCancel;
    private javax.swing.ButtonGroup btnGrpCopyChoice;
    private javax.swing.JButton btnImport;
    private javax.swing.JCheckBox chkFirstLabels;
    private javax.swing.JComboBox<Delimiter> cmbPreferences;
    private javax.swing.JComboBox<QuoteOptions> cmbQuoteChar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JRadioButton rdoIgnrDups;
    private javax.swing.JRadioButton rdoImpAll;
    private javax.swing.JRadioButton rdoOverwDups;
    private javax.swing.JTextField txtClass;
    private javax.swing.JTextField txtConWord;
    private javax.swing.JTextField txtDefinition;
    private javax.swing.JTextField txtExcelSheet;
    private javax.swing.JTextField txtFileName;
    private javax.swing.JTextField txtLocalWord;
    private javax.swing.JTextField txtPronunciation;
    private javax.swing.JTextField txtType;
    // End of variables declaration//GEN-END:variables
}
