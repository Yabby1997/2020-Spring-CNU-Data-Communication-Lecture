package ipc;

import java.util.ArrayList;

public class ChatAppLayer implements BaseLayer {
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	private static final boolean DEBUG = true;
	
	public void debugMSG(String message) {
		if(DEBUG) {
			System.out.println("[ChatAppLayer]" + message);
		}
	}
	
	private class _CAPP_HEADER {
		int capp_src;
		int capp_dst;
		byte[] capp_totlen;
		byte[] capp_data;

		public _CAPP_HEADER() {
			this.capp_src = 0x00000000;
			this.capp_dst = 0x00000000;
			this.capp_totlen = new byte[2];
			this.capp_data = null;
		}
	}

	_CAPP_HEADER m_sHeader = new _CAPP_HEADER();

	public ChatAppLayer(String pName) {
		// super(pName);
		// TODO Auto-generated constructor stub
		pLayerName = pName;
		ResetHeader();
	}

	public void ResetHeader() {
		for (int i = 0; i < 2; i++) {
			m_sHeader.capp_totlen[i] = (byte) 0x00;
		}
		m_sHeader.capp_data = null;
	}

	public byte[] ObjToByte(_CAPP_HEADER Header, byte[] input, int length) {
		byte[] buf = new byte[length +10];
		byte[] srctemp = intToByte4(Header.capp_src);
		byte[] dsttemp = intToByte4(Header.capp_dst);

		buf[0] = dsttemp[0];
		buf[1] = dsttemp[1];
		buf[2] = dsttemp[2];
		buf[3] = dsttemp[3];
			
		buf[4] = srctemp[0];
		buf[5] = srctemp[1];
		buf[6] = srctemp[2];
		buf[7] = srctemp[3];
		
		
		buf[8] = (byte) (length % 256);
		buf[9] = (byte) (length / 256);

		for (int i = 0; i < length; i++)
			buf[10 + i] = input[i];

		return buf;
	}

	public boolean Send(byte[] input, int length) {
		byte[] encodedBytes = this.ObjToByte(this.m_sHeader, input, length);						//ObjToByte를 통해 입력받은 src, dst주소와 문자열을 조합해 인코딩 
		debugMSG("인코딩된 바이트스트림 : " + new String(encodedBytes));
		((SocketLayer) this.GetUnderLayer()).Send(encodedBytes, length + 10);						//아래 레이어로 인코딩된 정보를 전달 
		return true;
	}

	public byte[] RemoveCappHeader(byte[] input, int length) {
		byte[] decodedBytes = new byte[length - 10];												//인코딩된 정보를 디코딩할 때 앞 10개는 추가된 데이터이므로 제거해준다. 
		for(int i = 10; i < length; i ++) {
			decodedBytes[i - 10] = input[i];
		}
		return decodedBytes;
	}

	public synchronized boolean Receive(byte[] input) {
		byte[] data;
		byte[] temp_src = intToByte4(m_sHeader.capp_src);
		for (int i = 0; i < 2; i++) {
			if (input[i] != temp_src[i]) {
				return false;
			}
		}
		data = RemoveCappHeader(input, input.length);
		this.GetUpperLayer(0).Receive(data);
		// 주소설정
		return true;
	}

	byte[] intToByte4(int value) {
		
		byte[] temp = new byte[4];
		
		temp[0] |= (byte) ((value & 0xFF000000) >> 24);
		temp[1] |= (byte) ((value & 0xFF0000) >> 16);
		temp[2] |= (byte) ((value & 0xFF00) >> 8);
		temp[3] |= (byte) (value & 0xFF);
		
		return temp;
		
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
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		this.SetUpperLayer(pUULayer);
		pUULayer.SetUnderLayer(this);
	}

	public void SetEnetSrcAddress(int srcAddress) {
		// TODO Auto-generated method stub
		m_sHeader.capp_src = srcAddress;
		debugMSG("클라이언트 포트 설정됨 : " + srcAddress);
	}

	public void SetEnetDstAddress(int dstAddress) {
		// TODO Auto-generated method stub
		m_sHeader.capp_dst = dstAddress;
		debugMSG("서버 포트 설정됨 : " + dstAddress);
	}

}
