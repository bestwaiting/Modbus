package com.bestwaiting.modbus;

import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.Date;

import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.ip.IpParameters;
import com.serotonin.modbus4j.msg.ModbusRequest;
import com.serotonin.modbus4j.msg.ModbusResponse;
import com.serotonin.modbus4j.msg.ReadHoldingRegistersRequest;
import com.serotonin.modbus4j.msg.WriteRegistersRequest;
import com.serotonin.modbus4j.msg.WriteRegistersResponse;
import com.serotonin.util.queue.ByteQueue;

public class ModbusUtil
{
	static ModbusFactory modbusFactory;
	static {
		if (modbusFactory == null) {
			modbusFactory = new ModbusFactory();
		}
	}

	/**
	 * �õ� WriteRegistersReques
	 * @param salveId ��Ӧ��վ�ı�ʾ
	 * @param start ��ʼλ��
	 * @param values ��������
	 * @return WriteRegistersRequest
	 */
	public static WriteRegistersRequest getWriteRegistersRequest(int slaveId, int start, short[] values) {
		WriteRegistersRequest request = null;
		try {
			request = new WriteRegistersRequest(slaveId, start, values);
		} catch (ModbusTransportException e) {
			e.printStackTrace();
		}
		return request;
	}

	/**
	 * �õ� WriteRegistersResponse
	 * @param tcpMaster
	 * @param request
	 * @return WriteRegistersResponse
	 */
	public static WriteRegistersResponse getWriteRegistersResponse(	ModbusMaster tcpMaster, WriteRegistersRequest request) {
		WriteRegistersResponse response = null;
		try {
			response = (WriteRegistersResponse) tcpMaster.send(request);
		} catch (ModbusTransportException e) {
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * ͨ��modbusд�����ݵ��豸��
	 * @param ip �豸��IP��ַ
	 * @param port �豸�Ķ˿ں�
	 * @param salveId ��Ӧ��վ�ı�ʾ
	 * @param start ��ʼλ��
	 * @param values ��������
	 * @return boolean �Ƿ�ɹ�
	 */
	public static boolean modbusWTCP(String ip, int port, int slaveId, int start, short[] values) {
		ModbusMaster tcpMaster = getTcpMaster(ip, port, slaveId);
		if (tcpMaster == null) {
			System.out.println("tcpMaster is null ");
			return false;
		}
		tcpMaster = initTcpMaster(tcpMaster);
		WriteRegistersRequest request = getWriteRegistersRequest(slaveId,start, values);
		WriteRegistersResponse response = getWriteRegistersResponse(tcpMaster,request);
		if (response.isException()) {
			return false;
		} else {
			return true;
		}
	}

	
	/**
	 * ��ʼ�� tcpMaster
	 * @param tcpMaster
	 * @return ModbusMaster
	 */
	public static ModbusMaster initTcpMaster(ModbusMaster tcpMaster) {
		if (tcpMaster == null)
			return null;
		try {
			tcpMaster.init();
			return tcpMaster;
		} catch (ModbusInitException e) {
			return null;
		}
	}

	/**
	 * �õ� ModbusRequest
	 * @param salveId ��Ӧ��վ�ı�ʾ
	 * @param start ��ʼλ��
	 * @param readLenth ���ݳ���
	 * @param tcpMaster
	 * @return ModbusRequest
	 */
	public static ModbusRequest getModbusRequest(int salveId, int start, int readLenth, ModbusMaster tcpMaster) {
		ModbusRequest modbusRequest = null;
		try {
			modbusRequest = new ReadHoldingRegistersRequest(salveId, start,readLenth); 
			return modbusRequest;
		} catch (ModbusTransportException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * �õ� ModbusRespons
	 * @param tcpMaster
	 * @param request
	 * @return ModbusResponse
	 */
	public static ModbusResponse getModbusResponse(ModbusMaster tcpMaster, ModbusRequest request) {
		ModbusResponse modbusResponse = null;
		try {
			modbusResponse = tcpMaster.send(request);
			return modbusResponse;
		} catch (ModbusTransportException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * ��ȡTcpMaster
	 * @param ip �豸��IP��ַ
	 * @param port �豸�Ķ˿ں�
	 * @param salveId ��Ӧ��վ�ı�ʾ
	 * @return ModbusMaster
	 */
	public static ModbusMaster getTcpMaster(String ip, int port, int salveId) {
		IpParameters params = new IpParameters();
		params.setHost(ip);// ����ip
		//�˿����ã��˿�Ĭ��ֵΪ502
		if (port == 0){
			params.setPort(502);
		}else{
			params.setPort(port);
		} 
		ModbusMaster tcpMaster = modbusFactory.createTcpMaster(params, true);// ��ȡModbusMaster����
		return tcpMaster;
	}

	/**
	 * ʹ��modbus��Ӳ���豸�ж�ȡָ��λ�õ���Ϣ
	 * @param ip �豸��IP��ַ
	 * @param port �豸�Ķ˿ں�
	 * @param salveId ��Ӧ��վ�ı�ʾ
	 * @param start ��ʼλ��
	 * @param readLenth ���ݳ���
	 * @return ByteQueue ��������
	 */
	public static ByteQueue modbusRTCP(String ip, int port, int salveId, int start, int readLenth) {
		ModbusMaster tcpMaster = getTcpMaster(ip, port, salveId);
		if (tcpMaster == null) {
			System.out.println("tcpMaster is null");
			return null;
		}
		// ��ʼ��tcpMaster
		tcpMaster = initTcpMaster(tcpMaster);
		if (tcpMaster == null) {
			System.out.println("tcpMaster is null");
			return null;
		}
		//��ȡ�������modbusRequest
		ModbusRequest modbusRequest = getModbusRequest(salveId, start, readLenth,tcpMaster);		
		if (modbusRequest == null) {
			System.out.println("request is null");
			return null;
		}
		//��ȡ����������Ӧ�������
		ModbusResponse modbusResponse = getModbusResponse(tcpMaster, modbusRequest);
		ByteQueue byteQueue = new ByteQueue(12);
		modbusResponse.write(byteQueue);
		System.out.println("����" + modbusRequest.getFunctionCode());
		System.out.println("��վ��ַ:" + modbusRequest.getSlaveId());
		System.out.println("�յ�����Ӧ��Ϣ��С" + byteQueue.size());
		System.out.println("�յ�����Ӧ��ϢС:" + byteQueue);
		return byteQueue;
	}

	/* *
	 * Convert byte[] to hex
	 * string.�������ǿ��Խ�byteת����int��Ȼ������Integer.toHexString(int)��ת����16�����ַ���
	 * @param src byte[] data
	 * @return hex string
	 */
	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}
	public static float ByteToFloat(byte[] bResponse)
    {
        if (bResponse.length < 4 || bResponse.length > 4)
        {
            //throw new NotEnoughDataInBufferException(data.length(), 8);
            return 0;
        }
        else
        {
            byte[] intBuffer = new byte[4];
            //��byte�����ǰ�������ֽڵĸߵ�λ������ DCBA
            intBuffer[0] = bResponse[2];
            intBuffer[1] = bResponse[3];
            intBuffer[2] = bResponse[0];
            intBuffer[3] = bResponse[1];
            int accum = 0;  
            for ( int shiftBy = 0; shiftBy < 4; shiftBy++ ) {  
                    accum |= (intBuffer[shiftBy] & 0xff) << shiftBy * 8;  
            }  
            return Float.intBitsToFloat(accum);  
        }
    }
	/**
	 * ***************************************************
	 * ��ʼλ��15,��Ӧ���ݣ���վ|data�����Ĵ���������|data length|data*
	 * ***************************************************
	 * 
	 * @param bq
	 */
	public static void ansisByteQueue(ByteQueue bq) {
		byte[] result = bq.peekAll();
		System.out.println("��վ��ַ===" + result[0]);
		System.out.println("data ����===" + result[1]);
		System.out.println("data ����===" + result[2]);
		for(int i=0;i<result.length;i++){
			System.out.println(i+"--->"+result[i]);
		}
		byte[] temp = null;
		ByteBuffer buffer = ByteBuffer.wrap(result, 3, result.length - 3);//ֱ�ӻ�ȡ data
		while (buffer.hasRemaining()) {
			temp = new byte[4];
			buffer.get(temp, 0, temp.length);
			System.out.print(ByteToFloat(temp));
		}

	}

	public static void main(String[] args) {
		ByteQueue result = ModbusUtil.modbusRTCP("127.0.0.1", 502, 1,0, 2);
		ansisByteQueue(result);
//		short[] shor = new short[1];
//		shor[0] = 0x12;
//		ModbusUtil.modbusWTCP("127.0.0.1", 502, 1, 0, shor);
		
	}
}
