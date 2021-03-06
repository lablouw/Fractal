/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.common;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.DefaultListModel;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jdesktop.swingx.JXImagePanel;

/**
 *
 * @author Lloyd
 */
public class ColorPalette extends javax.swing.JDialog {

    JColorChooser colorChooser = new JColorChooser();
    List<Color> currentColors = new ArrayList<>();
    DefaultListModel<Color> colorListModel = new DefaultListModel<>();
    JXImagePanel gradientPanel = new JXImagePanel();
    JXImagePanel representativePanel = new JXImagePanel();

    private double spectrumCycles = 1;
    private double spectrumPhase = 0;
    private double gamma = 1;
    private double gammaOffset = 0;
    private final Redrawable redrawable;
    
    public JPanel getRepresentativePanel() {
        return representativePanel;
    }

    /**
     * Creates new form ColorPalette
     */
    public ColorPalette(java.awt.Frame parent, boolean modal, Redrawable redrawable) {
        super(parent, modal);
        initComponents();
        setAlwaysOnTop(true);
        this.redrawable = redrawable;

        colorListModel.addElement(Color.RED);
        colorListModel.addElement(Color.GREEN);
        colorListModel.addElement(Color.BLUE);
        colorListModel.addElement(Color.BLACK);
        currentColors.add(Color.RED);
        currentColors.add(Color.GREEN);
        currentColors.add(Color.BLUE);
        currentColors.add(Color.BLACK);

        colorList.setModel(colorListModel);
        colorList.setCellRenderer(new ListCellRenderer<Color>() {
            @Override
            public Component getListCellRendererComponent(JList<? extends Color> list, Color value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = new JLabel("sf");
                l.setBackground(value);
                l.setForeground(value);
                l.setOpaque(true);
                if (isSelected) {
                    if (value.getRed() >= 128 && value.getGreen() >= 128 && value.getBlue() >= 128) {
                        l.setForeground(value.brighter().brighter());
                    } else {
                        l.setForeground(value.darker().darker());
                    }

                    l.setText("Selected");
                }
                return l;
            }
        });

        jSpinner1.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                spectrumCycles = (float) jSpinner1.getValue();
                if (redrawable != null && redrawCheckBox.isSelected()) {
                    redrawable.redraw();
                }
            }
        });

        jPanel1.setLayout(new GridLayout(1, 1));
        jPanel1.add(colorChooser);
        colorChooser.getSelectionModel().addChangeListener(new ColorChooserMouseListener());

        previewPanel.setLayout(new GridLayout(1, 1));
        previewPanel.add(gradientPanel);

        representativePanel.setStyle(JXImagePanel.Style.SCALED);
        representativePanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                representativePanelMouseClicked(evt);
            }

        });

        colorRepresentativeJPanel();
    }

    public Color interpolateToColor(double a, boolean modular) {// 0 <= a <= 1, modular == true => last color will interpolate back to first

        //apply gamma offset
        a = (a + 1 - gammaOffset) % 1;

        //apply gamma
        //We want similar compression behaviour when x~0 as when x~1 but because x^g is not symmetric around y=-x+1,
        //it behaves differently for x~0 and g<1 than it does for x~1 and g>1.
        //g>1 and x~1 works fine but for g<1 and x~0 we get much more compression as x->0. Therefore, for g<1 we need to flip x^g around y=-x+1.
        double a2 = a;
        if (gamma > 1) {
            a = Math.pow(a, gamma);
        } else if (gamma < 1) {
            a = (1 - Math.pow(1 - a, 1 / gamma));
        }

        //apply post-gamma phase
        a = (a + 1 - spectrumPhase) % 1;

        //Floating point issue
        if (a2 < 1 && a == 0) {
            a = 1 - 10E-10;
        }

        //apply cycles
        a = a * spectrumCycles;

        return ColorInterpolator.interpolate(a, currentColors, modular);
    }

    private void representativePanelMouseClicked(MouseEvent evt) {
        if (!isVisible()) {
            colorRepresentativeJPanel();
            setVisible(true);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
     * Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        previewPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        colorList = new javax.swing.JList<>();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        phaseSlider = new javax.swing.JSlider();
        gammaSlider = new javax.swing.JSlider();
        jSpinner1 = new javax.swing.JSpinner();
        redrawCheckBox = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        gammaOffsetSlider = new javax.swing.JSlider();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        javax.swing.GroupLayout previewPanelLayout = new javax.swing.GroupLayout(previewPanel);
        previewPanel.setLayout(previewPanelLayout);
        previewPanelLayout.setHorizontalGroup(
            previewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        previewPanelLayout.setVerticalGroup(
            previewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 43, Short.MAX_VALUE)
        );

        colorList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(colorList);

        jButton1.setText("Add");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Remove");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Color Selector"));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 232, Short.MAX_VALUE)
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Spectrum Parameters"));

        jLabel1.setText("Spectrum Cycles");

        jLabel2.setText("Phase");

        jLabel3.setText("Gamma");

        phaseSlider.setMaximum(255);
        phaseSlider.setValue(0);
        phaseSlider.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                phaseSliderMouseDragged(evt);
            }
        });
        phaseSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                phaseSliderMouseReleased(evt);
            }
        });

        gammaSlider.setMaximum(90);
        gammaSlider.setMinimum(-90);
        gammaSlider.setPaintLabels(true);
        gammaSlider.setValue(0);
        gammaSlider.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                gammaSliderMouseDragged(evt);
            }
        });
        gammaSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                gammaSliderMouseReleased(evt);
            }
        });

        jSpinner1.setModel(new javax.swing.SpinnerNumberModel(1.0f, 1.0f, null, 1.0f));

        redrawCheckBox.setText("Redraw if possible");

        jLabel4.setText("Gamma Offset");

        gammaOffsetSlider.setMaximum(255);
        gammaOffsetSlider.setValue(0);
        gammaOffsetSlider.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                gammaOffsetSliderMouseDragged(evt);
            }
        });
        gammaOffsetSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                gammaOffsetSliderMouseReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(redrawCheckBox, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(18, 18, 18)
                        .addComponent(gammaOffsetSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 558, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(gammaSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(phaseSlider, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jSpinner1, javax.swing.GroupLayout.Alignment.TRAILING))))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(phaseSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(gammaSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel4))
                    .addComponent(gammaOffsetSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 164, Short.MAX_VALUE)
                .addComponent(redrawCheckBox)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(previewPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(previewPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton1)
                            .addComponent(jButton2)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        int i = colorList.getSelectedIndex();
        colorListModel.remove(i);
        currentColors.remove(i);
        colorList.setSelectedIndex(Math.min(i, colorListModel.getSize()));
        colorRepresentativeJPanel();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        Random r = new Random();
        Color c = new Color(r.nextInt(256), r.nextInt(256), r.nextInt(256));
        colorListModel.addElement(c);
        currentColors.add(c);
        colorRepresentativeJPanel();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        colorRepresentativeJPanel();
    }//GEN-LAST:event_formWindowClosed

    private void phaseSliderMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_phaseSliderMouseDragged
        spectrumPhase = (double) phaseSlider.getValue() / (double) phaseSlider.getMaximum();
        colorRepresentativeJPanel();
    }//GEN-LAST:event_phaseSliderMouseDragged

    private void gammaSliderMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_gammaSliderMouseDragged
        if (gammaSlider.getValue() == 0) {
            gamma = 1;
        } else if (gammaSlider.getValue() > 0) {
            gamma = (gammaSlider.getValue() + 5f) / 5f;
        } else if (gammaSlider.getValue() < 0) {
            gamma = 5f / (-gammaSlider.getValue() + 5f);
        }
        colorRepresentativeJPanel();
    }//GEN-LAST:event_gammaSliderMouseDragged

    private void phaseSliderMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_phaseSliderMouseReleased
        if (redrawable != null && redrawCheckBox.isSelected()) {
            redrawable.redraw();
        }
    }//GEN-LAST:event_phaseSliderMouseReleased

    private void gammaSliderMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_gammaSliderMouseReleased
        if (redrawable != null && redrawCheckBox.isSelected()) {
            redrawable.redraw();
        }
    }//GEN-LAST:event_gammaSliderMouseReleased

    private void gammaOffsetSliderMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_gammaOffsetSliderMouseDragged
        gammaOffset = (double) gammaOffsetSlider.getValue() / (double) gammaOffsetSlider.getMaximum();
        colorRepresentativeJPanel();
    }//GEN-LAST:event_gammaOffsetSliderMouseDragged

    private void gammaOffsetSliderMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_gammaOffsetSliderMouseReleased
        if (redrawable != null && redrawCheckBox.isSelected()) {
            redrawable.redraw();
        }
    }//GEN-LAST:event_gammaOffsetSliderMouseReleased

    private void colorRepresentativeJPanel() {
        if (previewPanel.getWidth() == 0 || previewPanel.getHeight() == 0) {
            return;
        }

        BufferedImage img = new BufferedImage(previewPanel.getWidth(), previewPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < previewPanel.getWidth(); x++) {
            for (int y = 0; y < previewPanel.getHeight(); y++) {
                double a = (double) x / (double) previewPanel.getWidth();

                //apply gamma offset
                a = (a + 1 - gammaOffset) % 1;

                //We want similar compression behaviour when x~0 as when x~1 but because x^g is not symmetric around y=-x+1,
                //it behaves differently for x~0 and g<1 than it does for x~1 and g>1.
                //g>1 and x~1 works fine but for g<1 and x~0 we get much more compression as x->0. Therefore, for g<1 we need to flip x^g around y=-x+1.
                if (gamma > 1) {
                    a = Math.pow(a, gamma);
                } else if (gamma < 1) {
                    a = (1 - Math.pow(1 - a, 1 / gamma));
                }

                //apply post-gamma phase
                a = (a + 1 - spectrumPhase) % 1;

                img.setRGB(x, y, ColorInterpolator.interpolate(a, currentColors, true).getRGB());
            }
        }

        gradientPanel.setImage(img);
        representativePanel.setPreferredSize(new Dimension(240, 25));
        representativePanel.setImage(img);
    }

    private class ColorChooserMouseListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            int i = colorList.getSelectedIndex();
            Color c = colorChooser.getColor();
            colorListModel.set(colorList.getSelectedIndex(), c);
            currentColors.set(colorList.getSelectedIndex(), c);
            colorList.setModel(colorListModel);
            colorRepresentativeJPanel();
            colorList.setSelectedIndex(i);
        }

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList<Color> colorList;
    private javax.swing.JSlider gammaOffsetSlider;
    private javax.swing.JSlider gammaSlider;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JSlider phaseSlider;
    private javax.swing.JPanel previewPanel;
    private javax.swing.JCheckBox redrawCheckBox;
    // End of variables declaration//GEN-END:variables
}
