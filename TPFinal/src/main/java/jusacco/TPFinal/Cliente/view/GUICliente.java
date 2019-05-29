package jusacco.TPFinal.Cliente.view;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import jusacco.TPFinal.Cliente.controller.Controller;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class GUICliente  extends OutputStream implements ActionListener  {
	JFrame frame;
	JMenuBar mb;
	JButton btnChooser;
	JButton btnStart;
	JTextArea textArea;
	JLabel labelSeg;
	JLabel labelSam;
	JFormattedTextField cantidad;
	JPanel panel;
	JComboBox<String> comboBox;
	JFileChooser fc;
	JScrollPane scroll;
	Controller controlador;
	
	public GUICliente(Controller controlador){
        System.setOut(new PrintStream(this));
		this.controlador = controlador;
		
        //Creating the Frame
        this.frame = new JFrame("Renderizado distribuido");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - frame.getWidth())/4);
        int y = (int) ((dimension.getHeight() - frame.getHeight())/4);
        frame.setLocation(x, y);
        frame.setSize(800, 400);

        //Creating the MenuBar and adding components
        this.mb = new JMenuBar();
        mb.setLayout(new FlowLayout());
        btnChooser = new JButton("Cargar \".blend\"");
        btnChooser.addActionListener(this);
        btnStart = new JButton("Empezar a Renderizar");
        btnStart.setEnabled(false);
        btnStart.addActionListener(this);
        mb.add(btnChooser);
        mb.add(btnStart);
        String[] combo = new String[]{"Cantidad de samples","Tiempo limite"};
        comboBox = new JComboBox<String>(combo);
        comboBox.addActionListener(this);
        JLabel labelCombo = new JLabel("Renderizar por:");
        mb.add(labelCombo);
        mb.add(comboBox);
        cantidad = new JFormattedTextField(new Integer(10));
        cantidad.setPreferredSize(new Dimension(100,25));
        labelSeg = new JLabel("Segundos");
        labelSam = new JLabel("Samples");
        labelSeg.setPreferredSize(new Dimension(100,25));
        labelSam.setPreferredSize(new Dimension(100,25));
        mb.add(cantidad);
        mb.add(labelSeg);
        labelSeg.setVisible(false);
        mb.add(labelSam);
        
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        textArea = new JTextArea(20,80);
        textArea.setWrapStyleWord(true);
        textArea.setVisible(true);
        scroll = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,  JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        panel.add(scroll,BorderLayout.CENTER);
        
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Create a file chooser
        fc = new JFileChooser();
        
        //Adding Components to the frame.
        frame.getContentPane().add(BorderLayout.NORTH, mb);
        frame.getContentPane().add(BorderLayout.CENTER,panel);
        frame.pack();
        frame.setVisible(true);
        
        
	}
	 public void actionPerformed(ActionEvent e) {
		 if (e.getSource() == this.btnChooser) {
			FileFilter filter = new FileNameExtensionFilter("Blender File","blend");
			this.fc.setFileFilter(filter);
			this.fc.setCurrentDirectory(new File("./"));
			int returnVal = this.fc.showOpenDialog(null);
	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	        	this.controlador.setFile(fc.getSelectedFile());
	        }
		 }
		 if (e.getSource() == this.comboBox) {
			 if(this.comboBox.getSelectedIndex() == 0) {
				 this.controlador.setTipoRender(0);
				 this.labelSeg.setVisible(false);
				 this.labelSam.setVisible(true);
			 }else {
				 this.controlador.setTipoRender(1);
				 this.labelSeg.setVisible(true);
				 this.labelSam.setVisible(false);
			 }
		 }
		 if (e.getSource() == this.btnStart) {
			 int value = Integer.valueOf(this.cantidad.getText().replace(".", ""));
			 if(value > 0 ) {
				 this.frame.setTitle("Procesando...");
				 String str = this.controlador.enviarFile(value);
				 new JDialog().add(new JLabel(str));
			 }else {
				 this.textArea.append("ERROR: La cantidad de samples / segundos debe ser mayor a 0.\n");
			 }
		 }
		 if(this.controlador.isReady())
			 this.btnStart.setEnabled(true);
		 else
			 this.btnStart.setEnabled(false);
		       
	 }

     @Override
     public void write(int b) throws IOException {
         // redirects data to the text area
         this.textArea.append(String.valueOf((char)b));
         // scrolls the text area to the end of data
         this.textArea.setCaretPosition(textArea.getDocument().getLength());
     }
     
     
	
}