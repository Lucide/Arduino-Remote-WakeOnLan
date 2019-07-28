package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;

public class Gui extends JFrame{
	private static final long serialVersionUID=1497394827489721954L;
	private JPanel contentPane;
	private JLabel lbTitle;
	private JScrollPane scLog;
	public JLabel lbDescription;
	public JLabel lbLight;
	public JTextArea taLog;

	public Gui(){
		setIconImage(Toolkit.getDefaultToolkit().getImage(Gui.class.getResource("/resources/icon.png")));
		Font fontDefault=new Font("Segoe UI Light",Font.PLAIN,15), fontTitle=fontDefault.deriveFont(25F);

		setBackground(Color.GRAY);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100,100,320,400);
		contentPane=new JPanel();
		contentPane.setBackground(Color.DARK_GRAY);
		contentPane.setBorder(null);
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane=new GridBagLayout();
		gbl_contentPane.columnWidths=new int[]{0,0,0,0};
		gbl_contentPane.rowHeights=new int[]{0,0,0,0,0,0};
		gbl_contentPane.columnWeights=new double[]{1.0,1.0,1.0,Double.MIN_VALUE};
		gbl_contentPane.rowWeights=new double[]{1.0,2.0,2.0,2.0,2.0,Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);

		lbTitle=new JLabel("WOL Trigger Server");
		lbTitle.setForeground(Color.LIGHT_GRAY);
		lbTitle.setPreferredSize(new Dimension(0,0));
		lbTitle.setFont(fontTitle);
		lbTitle.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_lbTitle=new GridBagConstraints();
		gbc_lbTitle.fill=GridBagConstraints.BOTH;
		gbc_lbTitle.gridwidth=3;
		gbc_lbTitle.gridx=0;
		gbc_lbTitle.gridy=0;
		contentPane.add(lbTitle,gbc_lbTitle);

		lbDescription=new JLabel("0 clients connected");
		lbDescription.setForeground(Color.LIGHT_GRAY);
		lbDescription.setPreferredSize(new Dimension(0,0));
		lbDescription.setHorizontalAlignment(SwingConstants.CENTER);
		lbDescription.setFont(fontDefault);
		GridBagConstraints gbc_lbDescription=new GridBagConstraints();
		gbc_lbDescription.gridwidth=3;
		gbc_lbDescription.fill=GridBagConstraints.BOTH;
		gbc_lbDescription.gridx=0;
		gbc_lbDescription.gridy=1;
		contentPane.add(lbDescription,gbc_lbDescription);

		scLog=new JScrollPane();
		scLog.setPreferredSize(new Dimension(0,0));
		scLog.setBorder(new MatteBorder(1,0,1,0,Color.GRAY));
		scLog.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scLog.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		GridBagConstraints gbc_scLog=new GridBagConstraints();
		gbc_scLog.gridwidth=3;
		gbc_scLog.gridheight=2;
		gbc_scLog.fill=GridBagConstraints.BOTH;
		gbc_scLog.gridx=0;
		gbc_scLog.gridy=2;
		contentPane.add(scLog,gbc_scLog);

		taLog=new JTextArea();
		taLog.setLineWrap(true);
		taLog.setEditable(false);
		taLog.setBackground(Color.DARK_GRAY);
		taLog.setForeground(Color.LIGHT_GRAY);
		taLog.setFont(new Font("Monospaced",Font.PLAIN,14));
		scLog.setViewportView(taLog);

		lbLight=new JLabel("\u2022");
		lbLight.setForeground(Color.RED);
		lbLight.setPreferredSize(new Dimension(0,0));
		lbLight.setHorizontalAlignment(SwingConstants.CENTER);
		lbLight.setFont(new Font("Segoe UI Light",Font.PLAIN,200));
		GridBagConstraints gbc_lbLight=new GridBagConstraints();
		gbc_lbLight.gridwidth=3;
		gbc_lbLight.fill=GridBagConstraints.BOTH;
		gbc_lbLight.gridx=0;
		gbc_lbLight.gridy=4;
		contentPane.add(lbLight,gbc_lbLight);
	}

}
