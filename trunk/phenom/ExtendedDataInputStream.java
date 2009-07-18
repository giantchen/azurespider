package phenom.utils.io;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.InputStream;

public class ExtendedDataInputStream extends DataInputStream {

	public ExtendedDataInputStream(InputStream in) {
		super(in);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	/*
	 * Default java data is high bit first low bit last
	 * the Qianlong is high bit last low bit first
	 */
	public int readInt1() throws Exception {
		int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return ((ch1 << 0) + (ch2 << 8) + (ch3 << 16) + (ch4 << 24));
	}	
	
	/*
	 * struct Weight {
    	unsigned long date; // ���ڸ�12bitΪ�꣬����4bitΪ�£�����5bitΪ��
    	unsigned long gift_stock; // �͹���* 10000
        unsigned long stock_amount; // �����* 10000
        unsigned long stock_price; // ��ɼ�* 1000
        unsigned long bonus; // ����* 1000
        unsigned long trans_num; // ת����
        unsigned long total_amount; // �ܹɱ�����λ��
        unsigned long liquid_amoun; // ��ͨ�ɣ���λ��
	 * */
	public int readInt2() throws Exception {
		int n = readInt1();
		int nYear = n >> 20;
        int nMon = (int)(((n << 12))>> 28);
        int nDay = (n & 0xffff)>> 11;        
		return nYear * 10000 + nMon * 100 + nDay;
	}
	
	public int readShortLowToHigh() throws Exception {
		int ch1 = in.read();
        int ch2 = in.read();
        
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return ((ch1 << 0) + (ch2 << 8));
	}
	
	public long readUnsignedInt() throws Exception {		
		byte []bytes = new byte[4];
		bytes[0] = super.readByte();
		bytes[1] = super.readByte();
		bytes[2] = super.readByte();
		bytes[3] = super.readByte();
		
		return readUnsignedInt(bytes);
	}
	
	public long readUnsignedInt(byte[] bytes) {		
		long b0 = ((long) (bytes[0] & 0xff));
		long b1 = ((long) (bytes[1] & 0xff)) << 8;
		long b2 = ((long) (bytes[2] & 0xff)) << 16;
		long b3 = ((long) (bytes[3] & 0xff)) << 24;
		return (long) (b0 | b1 | b2 | b3);
	}
	
	
}
