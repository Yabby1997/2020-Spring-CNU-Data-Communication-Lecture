package ipc;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class IPCDlg extends JFrame implements BaseLayer {

	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	BaseLayer UnderLayer;

	private static LayerManager m_LayerMgr = new LayerManager();

	private JTextField ChattingWrite;

	Container contentPane;

	JTextArea ChattingArea;
	JTextArea srcAddress;
	JTextArea dstAddress;

	JLabel lblsrc;
	JLabel lbldst;

	JButton Setting_Button;
	JButton Chat_send_Button;

	static JComboBox<String> NICComboBox;

	int adapterNumber = 0;

	String Text;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		m_LayerMgr.AddLayer(new SocketLayer("Socket"));
		m_LayerMgr.AddLayer(new ChatAppLayer("ChatApp"));
		m_LayerMgr.AddLayer(new IPCDlg("GUI"));
		m_LayerMgr.ConnectLayers(" Socket ( *ChatApp ( *GUI ) ) ");
	}

	public IPCDlg(String pName) {
		
		pLayerName = pName;

		setTitle("IPC");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(250, 250, 644, 425);
		contentPane = new JPanel();
		((JComponent) contentPane).setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JPanel chattingPanel = new JPanel();// chatting panel
		chattingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "chatting",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		chattingPanel.setBounds(10, 5, 360, 276);
		contentPane.add(chattingPanel);
		chattingPanel.setLayout(null);

		JPanel chattingEditorPanel = new JPanel();// chatting write panel
		chattingEditorPanel.setBounds(10, 15, 340, 210);
		chattingPanel.add(chattingEditorPanel);
		chattingEditorPanel.setLayout(null);

		ChattingArea = new JTextArea();
		ChattingArea.setEditable(false);
		ChattingArea.setBounds(0, 0, 340, 210);
		chattingEditorPanel.add(ChattingArea);// chatting edit

		JPanel chattingInputPanel = new JPanel();// chatting write panel
		chattingInputPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		chattingInputPanel.setBounds(10, 230, 250, 20);
		chattingPanel.add(chattingInputPanel);
		chattingInputPanel.setLayout(null);

		ChattingWrite = new JTextField();
		ChattingWrite.setBounds(2, 2, 250, 20);// 249
		chattingInputPanel.add(ChattingWrite);
		ChattingWrite.setColumns(10);// writing area

		JPanel settingPanel = new JPanel();
		settingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "setting",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		settingPanel.setBounds(380, 5, 236, 371);
		contentPane.add(settingPanel);
		settingPanel.setLayout(null);

		JPanel sourceAddressPanel = new JPanel();
		sourceAddressPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		sourceAddressPanel.setBounds(10, 96, 170, 20);
		settingPanel.add(sourceAddressPanel);
		sourceAddressPanel.setLayout(null);

		lblsrc = new JLabel("Source Address");
		lblsrc.setBounds(10, 75, 170, 20);
		settingPanel.add(lblsrc);

		srcAddress = new JTextArea();
		srcAddress.setBounds(2, 2, 170, 20);
		sourceAddressPanel.add(srcAddress);// src address

		JPanel destinationAddressPanel = new JPanel();
		destinationAddressPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		destinationAddressPanel.setBounds(10, 212, 170, 20);
		settingPanel.add(destinationAddressPanel);
		destinationAddressPanel.setLayout(null);

		lbldst = new JLabel("Destination Address");
		lbldst.setBounds(10, 187, 190, 20);
		settingPanel.add(lbldst);

		dstAddress = new JTextArea();
		dstAddress.setBounds(2, 2, 170, 20);
		destinationAddressPanel.add(dstAddress);// dst address

		Setting_Button = new JButton("Setting");// setting
		Setting_Button.setBounds(80, 270, 100, 20);
		Setting_Button.addActionListener(new setAddressListener());
		settingPanel.add(Setting_Button);// setting

		Chat_send_Button = new JButton("Send");
		Chat_send_Button.setBounds(270, 230, 80, 20);
		Chat_send_Button.addActionListener(new setAddressListener());
		chattingPanel.add(Chat_send_Button);// chatting send button

		setVisible(true);

	}

	class setAddressListener implements ActionListener {
		private static final boolean DEBUG = true;
		
		public void debugMSG(String message) {
			if(DEBUG) {
				System.out.println("[IPCDlg]" + message);
			}
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			String action = e.getActionCommand();
			if(action.equals("Setting")) {
				Setting_Button.setText("Reset");
				srcAddress.setEnabled(false);
				dstAddress.setEnabled(false);
					
				int source = Integer.parseInt(srcAddress.getText());
				int destination = Integer.parseInt(dstAddress.getText());
					
				debugMSG("소스 포트 입력 : " + source);
				debugMSG("도착지 포트 입력 : " + destination);
					
				((SocketLayer) (m_LayerMgr.GetLayer("Socket"))).setClientPort(source);
				((SocketLayer) (m_LayerMgr.GetLayer("Socket"))).setServerPort(destination);
				((ChatAppLayer) (m_LayerMgr.GetLayer("ChatApp"))).SetEnetSrcAddress(source);
				((ChatAppLayer) (m_LayerMgr.GetLayer("ChatApp"))).SetEnetDstAddress(destination);
				
				((SocketLayer) (m_LayerMgr.GetLayer("Socket"))).Receive();
			}
			else if(action.equals("Reset")) {
				Setting_Button.setText("Setting");
				srcAddress.setText("");
				dstAddress.setText("");
				srcAddress.setEnabled(true);
				dstAddress.setEnabled(true);
				
				debugMSG("포트 세팅값 초기화");
				
				((SocketLayer) (m_LayerMgr.GetLayer("Socket"))).setClientPort(0);
				((SocketLayer) (m_LayerMgr.GetLayer("Socket"))).setServerPort(0);
				((ChatAppLayer) (m_LayerMgr.GetLayer("ChatApp"))).SetEnetSrcAddress(0);
				((ChatAppLayer) (m_LayerMgr.GetLayer("ChatApp"))).SetEnetDstAddress(0);
			}
			else if(action.equals("Send")) {
				if(Setting_Button.getText().equals("Reset") && (srcAddress.getText() != null || dstAddress.getText() != null)) {
					String message = ChattingWrite.getText();
					ChattingWrite.setText("");
					ChattingArea.append("[SEND]:" + message + "\n");
					
					debugMSG("메시지 전송 : " + message);
					
					byte[] messageBytes = message.getBytes();
					((ChatAppLayer) (m_LayerMgr.GetLayer("ChatApp"))).Send(messageBytes, messageBytes.length);
				}
				else {
					ChattingArea.append("주소 설정 오류\n");
				}
			}
		}
	}

	public boolean Receive(byte[] input) {	
		ChattingArea.append("[RECV]:" + new String(input) + "\n");
		return true;
	}

	@Override
	public void SetUnderLayer(BaseLayer pUnderLayer) {
		// TODO Auto-generated method stub
		if (pUnderLayer == null)
			return;
		this.p_UnderLayer = pUnderLayer;
	}

	@Override
	public void SetUpperLayer(BaseLayer pUpperLayer) {
		// TODO Auto-generated method stub
		if (pUpperLayer == null)
			return;
		this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
		// nUpperLayerCount++;
	}

	@Override
	public String GetLayerName() {
		// TODO Auto-generated method stub
		return pLayerName;
	}

	@Override
	public BaseLayer GetUnderLayer() {
		// TODO Auto-generated method stub
		if (p_UnderLayer == null)
			return null;
		return p_UnderLayer;
	}

	@Override
	public BaseLayer GetUpperLayer(int nindex) {
		// TODO Auto-generated method stub
		if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
			return null;
		return p_aUpperLayer.get(nindex);
	}

	@Override
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		this.SetUpperLayer(pUULayer);
		pUULayer.SetUnderLayer(this);

	}

}
