package projektPaczkKlient;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.WindowEvent;

/**
 * class that does all the graphics
 */
public class Window {
    /**main frame of window     */
    public JFrame jFrame;
    /**main panel that we can add things to     */
    public JPanel jPanel;
    /**Label that holds actual state of application */
    public JLabel jLabel;
    /**boolean saying wheather closing app button was pressed     */
    public boolean finished =false;

    /**
     * constructor of whole class
     * @param title title of window
     */
    public Window(String title) {
        jFrame = new JFrame(title);
        jFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        jFrame.addWindowListener(new java.awt.event.WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e) {
                if(JOptionPane.showConfirmDialog(jFrame,"Do you really want to exit?","Exit",
                        JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE)==JOptionPane.YES_OPTION){
                    finished=true;
                }

            }
        });
        jLabel=new JLabel();
        jFrame.getContentPane().add(jLabel, BorderLayout.NORTH);
        jPanel= new JPanel(new GridBagLayout());
        Border border = BorderFactory.createEmptyBorder(10,10,10,10);
        jPanel.setBorder(border);
        jFrame.add(jPanel,BorderLayout.CENTER);
        jFrame.setSize(400,300);

    }
}
