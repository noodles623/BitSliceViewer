import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagConstraints;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

class BitSliceGUI extends JFrame {
	
	private static final long serialVersionUID = 1L;
	
	private int curPlane = -1;
	
	private JLabel viewL;
	
	private ByteArrayInputStream[] planes = new ByteArrayInputStream[24];
	private ByteArrayInputStream original;
	
	private ImageIcon[] planesI = new ImageIcon[24];
	private ImageIcon originalI;
	
	private JButton pressed;
	
	private Color redc = new Color(0xff000);
	
	private JButton[] planesB = new JButton[24];

	public BitSliceGUI(String addr, BitSlice bsF, BitSlice[] bsXS) throws IOException {
		super("Bit Slice");

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setPreferredSize(new Dimension(960, 720));
		
		for(int i = 0; i < 24; i++)
			planesB[i] = new JButton(String.valueOf(i));
 

		Container pane = getContentPane();
		pane.setLayout(new GridBagLayout());
		
		GridBagConstraints viewC = new GridBagConstraints();
		GridBagConstraints nextC = new GridBagConstraints();
		GridBagConstraints prevC = new GridBagConstraints();
		GridBagConstraints fullC = new GridBagConstraints();
		GridBagConstraints fileC = new GridBagConstraints();	
		
		viewL = new JLabel(new ImageIcon("./Images/loading.gif"));

		JButton next = new JButton("next");
		JButton prev = new JButton("prev");
		
		JButton full = new JButton("full");
		JButton file = new JButton("files");
		
		pressed = full;
		full.setBackground(redc);
		
        next.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
            	if(curPlane < 23 && curPlane != -1) {
            		viewL.setIcon(planesI[curPlane+1]);
            		curPlane++;
            		planesB[curPlane-1].setBackground(next.getBackground());
            		pressed = planesB[curPlane];
            		pressed.setBackground(redc);
            	}           		
            }
        });
        
        prev.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
            	if(curPlane > 0) {
            		viewL.setIcon(planesI[curPlane-1]);
            		curPlane--;
            		planesB[curPlane+1].setBackground(prev.getBackground());
            		pressed = planesB[curPlane];
            		pressed.setBackground(redc);
            	}           		
            }
        });
        
        full.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
            	if(curPlane != -1) {
            		viewL.setIcon(originalI);
            		curPlane = -1;
            		pressed.setBackground(full.getBackground());
            		pressed = full;
            		pressed.setBackground(redc);
            	}           		
            }
        });        
        
		viewL.setPreferredSize(new Dimension(500,500));
		
		viewC.fill = GridBagConstraints.HORIZONTAL;
		viewC.gridwidth = 26;

		prevC.gridy = 1;
		nextC.gridy = 1;
		fullC.gridy = 1;
		fileC.gridy = 1;

		fileC.gridx = 0;
		fullC.gridx = 1;
		prevC.gridx = 2;
		nextC.gridx = 3;		
		
		pane.add(file, fileC);
		pane.add(full, fullC);
		pane.add(viewL, viewC);
		pane.add(prev, prevC);
		pane.add(next, nextC);
		GridBagConstraints numC = new GridBagConstraints();
		numC.fill = GridBagConstraints.HORIZONTAL;
		for(int i = 0; i < 24; i++) {
			numC.gridx = (i < 10) ? 4+i : i-10;
			numC.gridy = (i < 10) ? 1 : 2;
			JButton button = planesB[i]; 
			pane.add(button, numC);
	        button.addActionListener(new ActionListener() {
	            @Override
	            public void actionPerformed(ActionEvent evt) {
	            	int num = Integer.valueOf(button.getText());
	        		if(num != curPlane) {
	        			viewL.setIcon(planesI[num]);
	        			curPlane = num;
	            		pressed.setBackground(button.getBackground());
	            		pressed = button;
	            		button.setBackground(redc);
	            		planesB[num] = button;
	        		}
	            }
	        });
		}
		
        pack();
		setVisible(true);
		
		closeOriginal(bsF);
		closePlanes(bsXS);
		viewL.setIcon(originalI);
	}
	
	private static BitSlice loadOriginal(String addr) {
		BitSlice bsF = new BitSlice(addr, -1);
		bsF.run();
		return bsF;
	}
	
	private void closeOriginal(BitSlice bsF) throws IOException {
		try {
			bsF.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		original = bsF.getBitPlane();
		originalI = new ImageIcon(scaleImage(ImageIO.read(original)));
	}
	
	private static BitSlice[] loadPlanes(String addr) throws IOException {
		BitSlice[] bsXS = new BitSlice[24];
		for(int i = 0; i < 24; i++) {
			bsXS[i] = new BitSlice(addr, i);
			bsXS[i].run();
		}
		return bsXS;
	}
		
	private void closePlanes(BitSlice[] bsXS) throws IOException {
		try {
			for(BitSlice bs : bsXS) 
				bs.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		for(int i = 0; i < 24; i++)
			planes[i] = bsXS[i].getBitPlane();
		
		for(int i = 0; i < 24; i++)
			planesI[i] = new ImageIcon(scaleImage(ImageIO.read(planes[i])));
	}
	
	private BufferedImage scaleImage(BufferedImage img) {
		int h = img.getHeight();
		int w = img.getWidth();
		if(h > 500 || w > 500) {
			int hn = h;
			int wn = w;
			if(h > 500 && h > w) {
				hn = 500;
				wn = -w;
			} else if (w > 500 && w > h) {
				wn = 500;
				hn = -h;
			}
			Image scaled = img.getScaledInstance(wn, hn, Image.SCALE_SMOOTH);
			BufferedImage buffered = new BufferedImage(scaled.getWidth(null), scaled.getHeight(null), img.getType());
			buffered.getGraphics().drawImage(scaled, 0, 0 , null);
			return buffered;
		}
		
		return img;
	}
	
	public static void main(String[] args) {
		String addr = args[0];

		try {
			BitSlice bsF = loadOriginal(addr);
			BitSlice[] bsXS = loadPlanes(addr);
			BitSliceGUI mw = new BitSliceGUI(addr, bsF, bsXS);
		} catch (IOException e) { e.printStackTrace(); }
	}
}
