/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.mandelbrot.coloring.orbittrap;
import java.util.List;

/**
 *
 * @author Lloyd
 */
public class OrbitTrapSettingsDialog<OT extends OrbitTrap, OTCS extends OrbitTrapColorStrategy<OT>> extends javax.swing.JDialog {//TODO: generics probably not necessary here

    private final OT orbitTrap;
    private final List<OTCS> colorStrategies;

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane1StateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 637, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane1StateChanged
        orbitTrap.setActiveColorStrategy(colorStrategies.get(jTabbedPane1.getSelectedIndex()));
    }//GEN-LAST:event_jTabbedPane1StateChanged

    /**
     * Creates new form CircleOrbitTrapSettingsFrame
     */
    public OrbitTrapSettingsDialog(OT orbitTrap, List<OTCS> colorStrategies) {
        initComponents();
        this.orbitTrap = orbitTrap;
        this.colorStrategies = colorStrategies;

        setModal(false);
        setTitle("Circle orbit trap coloring settings");
        validate();
        
        for (OrbitTrapColorStrategy colorStrategy : colorStrategies) {
            jTabbedPane1.addTab(colorStrategy.getName(), colorStrategy.getSettingsComponent());
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables
}