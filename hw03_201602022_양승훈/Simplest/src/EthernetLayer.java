

import java.util.ArrayList;

public class EthernetLayer implements BaseLayer {
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();

	private class _ETHERNET_ADDR {
		private byte[] addr = new byte[6];

		public _ETHERNET_ADDR() {
			this.addr[0] = (byte) 0x00;
			this.addr[1] = (byte) 0x00;
			this.addr[2] = (byte) 0x00;
			this.addr[3] = (byte) 0x00;
			this.addr[4] = (byte) 0x00;
			this.addr[5] = (byte) 0x00;
		}
	}

	private class _ETHERNET_HEADER {
		_ETHERNET_ADDR enet_dstaddr;
		_ETHERNET_ADDR enet_srcaddr;
		byte[] enet_type;
		byte[] enet_data;

		public _ETHERNET_HEADER() {
			this.enet_dstaddr = new _ETHERNET_ADDR();
			this.enet_srcaddr = new _ETHERNET_ADDR();
			this.enet_type = new byte[2];
			this.enet_data = null;
		}
	}

	_ETHERNET_HEADER m_sHeader = new _ETHERNET_HEADER();

	public EthernetLayer(String pName) {
		// super(pName);
		// TODO Auto-generated constructor stub
		pLayerName = pName;
		ResetHeader();
	}

	public void ResetHeader() {
		for (int i = 0; i < 5; i++) {
			m_sHeader.enet_dstaddr.addr[i] = (byte) 0x00;
			m_sHeader.enet_srcaddr.addr[i] = (byte) 0x00;
		}
		m_sHeader.enet_type[0] = (byte) 0x00;
		m_sHeader.enet_type[1] = (byte) 0x00;
		m_sHeader.enet_data = null;
	}

	public _ETHERNET_ADDR GetEnetDstAddress() {
		return m_sHeader.enet_dstaddr;
	}

	public _ETHERNET_ADDR GetEnetSrcAddress() {
		return m_sHeader.enet_srcaddr;
	}

	public void SetEnetType(byte[] input) {
		for (int i = 0; i < 2; i++) {
			m_sHeader.enet_type[i] = input[i];
		}
	}

	public void SetEnetDstAddress(byte[] input) {
		for (int i = 0; i < 6; i++) {
			m_sHeader.enet_dstaddr.addr[i] = input[i];
		}
	}

	public void SetEnetSrcAddress(byte[] input) {
		for (int i = 0; i < 6; i++) {
			m_sHeader.enet_srcaddr.addr[i] = input[i];
		}
	}

	public byte[] ObjToByte(_ETHERNET_HEADER Header, byte[] input, int length) {
		byte[] buf = new byte[length + 14];

		buf[0] = Header.enet_dstaddr.addr[0];
		buf[1] = Header.enet_dstaddr.addr[1];
		buf[2] = Header.enet_dstaddr.addr[2];
		buf[3] = Header.enet_dstaddr.addr[3];
		buf[4] = Header.enet_dstaddr.addr[4];
		buf[5] = Header.enet_dstaddr.addr[5];

		buf[6] = Header.enet_srcaddr.addr[0];
		buf[7] = Header.enet_srcaddr.addr[1];
		buf[8] = Header.enet_srcaddr.addr[2];
		buf[9] = Header.enet_srcaddr.addr[3];
		buf[10] = Header.enet_srcaddr.addr[4];
		buf[11] = Header.enet_srcaddr.addr[5];

		buf[12] = Header.enet_type[0];
		buf[13] = Header.enet_type[1];

		for (int i = 0; i < length; i++)
			buf[14 + i] = input[i];

		return buf;
	}

	public boolean Send(byte[] input, int length) {
		byte[] type = {0x06, 0x08};
		this.SetEnetType(type);
		byte[] bytes = ObjToByte(m_sHeader, input, length);
		this.GetUnderLayer().Send(bytes, length + 14);

		return false;
	}

	public byte[] RemoveEtherHeader(byte[] input, int length) {
		byte[] data = new byte[length - 14];
		for (int i = 0; i < length - 14; i++)
			data[i] = input[14 + i];
		return data;
	}

	public boolean IsItMyPacket(byte[] input) {
		for (int i = 0; i < 6; i++) {
			if (m_sHeader.enet_srcaddr.addr[i] == input[6 + i])
				continue;
			else
				return false;
		}냐
		return true;
	}

	public boolean IsItMine(byte[] input) {
		for (int i = 0; i < 6; i++) {
			if (m_sHeader.enet_srcaddr.addr[i] == input[i])
				continue;
			else {
				return false;
			}
		}
		return true;
	}

	public boolean IsItBroadcast(byte[] input) {
		for (int i = 0; i < 6; i++) {
			if (input[i] == 0xff) {
				continue;
			} else
				return false;
		}
		return true;
	}

	public boolean Receive(byte[] input) {
		byte[] data;
		boolean MyPacket, Mine, Broadcast;
		MyPacket = IsItMyPacket(input);

		if (MyPacket == true){
			//내가 만든 패킷이면 수신하지 않음.
			return false;
		}else {
			Broadcast = IsItBroadcast(input);
			if (Broadcast == false) {
				//브로드 캐스팅도 아니면서, 
				Mine = IsItMine(input);
				if (Mine == false){
					// 목적지가 자신이 아니면 수신하지 않음.
					return false;
				}
			}
		}
		data = RemoveEtherHeader(input, input.length);
		this.GetUpperLayer(0).Receive(data);
		
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

