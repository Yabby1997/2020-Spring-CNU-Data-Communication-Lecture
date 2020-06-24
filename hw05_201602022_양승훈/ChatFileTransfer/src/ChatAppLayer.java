

import java.util.ArrayList;

public class ChatAppLayer implements BaseLayer {
    public int nUpperLayerCount = 0;
    public String pLayerName = null;
    public BaseLayer p_UnderLayer = null;
    public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
    _CHAT_APP m_sHeader;

    private byte[] fragBytes;
    private int fragCount = 0;
    private ArrayList<Boolean> ackChk = new ArrayList<Boolean>();

    private class _CHAT_APP {
        byte[] capp_totlen;
        byte capp_type;
        byte capp_unused;
        byte[] capp_data;

        public _CHAT_APP() {
            this.capp_totlen = new byte[2];
            this.capp_type = 0x00;
            this.capp_unused = 0x00;
            this.capp_data = null;
        }
    }

    public ChatAppLayer(String pName) {
        // super(pName);
        // TODO Auto-generated constructor stub
        pLayerName = pName;
        ResetHeader();
        ackChk.add(true);
    }

    private void ResetHeader() {
        m_sHeader = new _CHAT_APP();
    }

    private byte[] objToByte(_CHAT_APP Header, byte[] input, int length) {
        byte[] buf = new byte[length + 4];

        buf[0] = Header.capp_totlen[0];
        buf[1] = Header.capp_totlen[1];
        buf[2] = Header.capp_type;
        buf[3] = Header.capp_unused;

        if (length >= 0) System.arraycopy(input, 0, buf, 4, length);

        return buf;
    }

    public byte[] RemoveCappHeader(byte[] input, int length) {
        byte[] cpyInput = new byte[length - 4];
        System.arraycopy(input, 4, cpyInput, 0, length - 4);
        input = cpyInput;
        return input;
    }
    
    private void waitACK() { //ACK 체크
        while (ackChk.size() <= 0) {													//일정시간 대기하며 ack를 체크함 
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        ackChk.remove(0);
    }
  /**/
    private void fragSend(byte[] input, int length) {
        byte[] bytes = new byte[1456];
        int i = 0;
        m_sHeader.capp_totlen = intToByte2(length);											
        m_sHeader.capp_type = (byte) (0x01);

        // 첫번째 전송
        System.arraycopy(input, 0, bytes, 0, 1456);											//첫번째 1456바이트를 전송
        bytes = objToByte(m_sHeader, bytes, 1456);
        this.GetUnderLayer().Send(bytes, bytes.length);

        int maxLen = length / 1456;
        m_sHeader.capp_totlen = intToByte2(1456);											//중간 부분은 모두 크기가 10으로 잘려있다. 
        m_sHeader.capp_type = (byte)(0x02);													//중간 부분은 타입이 0x02이다.
        for(i = 1; i < maxLen; i++) {
        	//waitACK();
        	if(i + 1 == maxLen && length % 1456 == 0)										//마지막 데이터인 경우 타입을 바꿔준다. 
        		m_sHeader.capp_type = (byte)(0x03);
        	System.arraycopy(input, 1456 * i, bytes, 0, 1456);								//이후 자리부터 1456개를 bytes에 복사
        	bytes = objToByte(m_sHeader, bytes, 1456);
        	this.GetUnderLayer().Send(bytes, bytes.length);
        }

        if (length % 1456 != 0) {															//나머지 부분, 즉 마지막 부분
        	//waitACK();
            m_sHeader.capp_totlen = intToByte2(length % 1456);
        	m_sHeader.capp_type = (byte) (0x03);
        	bytes = new byte[length % 1456];												//나머지 부분의 크기에 맞는 바이트 배열 선언 
        	System.arraycopy(input, length - (length % 1456), bytes, 0, length % 1456);		//이후 자리부터 나머지 수만큼 복사 
            bytes = objToByte(m_sHeader, bytes, bytes.length);
            this.GetUnderLayer().Send(bytes, bytes.length);
        }
    }
 
    public boolean Send(byte[] input, int length) {
        byte[] bytes;
        m_sHeader.capp_totlen = intToByte2(length);
        m_sHeader.capp_type = (byte) (0x00);
        
        //waitACK();
        if(length > 1456) {												//1456보다 크면 나눠서 보내야하므로 fragsend
        	fragSend(input, length);
        }
        else {															//그렇지 않다면 그냥 보낸다. 
        	bytes = objToByte(m_sHeader, input, input.length);
        	this.GetUnderLayer().Send(bytes, bytes.length);
        }
        return true;
    }
 
    public synchronized boolean Receive(byte[] input) {
        byte[] data, tempBytes;
        int tempType = 0;

        //if (input == null) {											//null을 받으면 ack이다
        //	ackChk.add(true);
        //	return true;
        //}
        
        tempType |= (byte) (input[2] & 0xFF);							//헤더의 2번인덱스, 타입
        
        if(tempType == 0) {												//타입이 0이라면 단편화 되지 않고 한번에 받아온 프레임 
            data = RemoveCappHeader(input, input.length);				//헤더를 제거하고 
            this.GetUpperLayer(0).Receive(data);						//윗 레이어로 전달 
        }
        else{
        	if(tempType == 1) {
        		int size = byte2ToInt(input[0], input[1]);				//헤더의 0, 1번 인덱스, 크기
        		fragBytes = new byte[size];								//크기만큼의 fragBytes배열을 만들어줌
        		fragCount = 1;
        		tempBytes = RemoveCappHeader(input, input.length);		//헤더가 제거된것을 tempbytes로 저장
        		System.arraycopy(tempBytes, 0, fragBytes, 0, 1456);		//fragBytes의 첫 위치에 tempBytes를 넣어줌 
        	}
        	else {
        		tempBytes = RemoveCappHeader(input, input.length);		//헤더 제거된것을 tempbytes로 저장
        		System.arraycopy(tempBytes, 0, fragBytes, (fragCount++) * 1456, byte2ToInt(input[0], input[1]));		//fragbytes의 알맞은 위치에 tempbytes를 추가
        		if(tempType == 3) {
        			this.GetUpperLayer(0).Receive(fragBytes);			//마지막 단편화 데이터였다면 모두 모인것이므로 윗 레이어로 전달
        		}
        	}
        }
        return true;
    }
    
    private byte[] intToByte2(int value) {
        byte[] temp = new byte[2];
        temp[0] |= (byte) ((value & 0xFF00) >> 8);
        temp[1] |= (byte) (value & 0xFF);

        return temp;
    }

    private int byte2ToInt(byte value1, byte value2) {
        return (int)((value1 << 8) | (value2));
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
    }

    @Override
    public void SetUpperUnderLayer(BaseLayer pUULayer) {
        this.SetUpperLayer(pUULayer);
        pUULayer.SetUnderLayer(this);
    }
}
