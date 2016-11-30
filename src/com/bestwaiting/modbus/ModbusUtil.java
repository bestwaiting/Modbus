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
	 * 得到 WriteRegistersReques
	 * @param salveId 对应从站的标示
	 * @param start 起始位置
	 * @param values 数据内容
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
	 * 得到 WriteRegistersResponse
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
	 * 通过modbus写入内容到设备中
	 * @param ip 设备的IP地址
	 * @param port 设备的端口号
	 * @param salveId 对应从站的标示
	 * @param start 起始位置
	 * @param values 数据内容
	 * @return boolean 是否成功
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
	 * 初始化 tcpMaster
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
	 * 得到 ModbusRequest
	 * @param salveId 对应从站的标示
	 * @param start 起始位置
	 * @param readLenth 数据长度
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
	 * 得到 ModbusRespons
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
	 * 获取TcpMaster
	 * @param ip 设备的IP地址
	 * @param port 设备的端口号
	 * @param salveId 对应从站的标示
	 * @return ModbusMaster
	 */
	public static ModbusMaster getTcpMaster(String ip, int port, int salveId) {
		IpParameters params = new IpParameters();
		params.setHost(ip);// 设置ip
		//端口设置，端口默认值为502
		if (port == 0){
			params.setPort(502);
		}else{
			params.setPort(port);
		} 
		ModbusMaster tcpMaster = modbusFactory.createTcpMaster(params, true);// 获取ModbusMaster对象
		return tcpMaster;
	}

	/**
	 * 使用modbus从硬件设备中读取指定位置的信息
	 * @param ip 设备的IP地址
	 * @param port 设备的端口号
	 * @param salveId 对应从站的标示
	 * @param start 起始位置
	 * @param readLenth 数据长度
	 * @return ByteQueue 数据内容
	 */
	public static ByteQueue modbusRTCP(String ip, int port, int salveId, int start, int readLenth) {
		ModbusMaster tcpMaster = getTcpMaster(ip, port, salveId);
		if (tcpMaster == null) {
			System.out.println("tcpMaster is null");
			return null;
		}
		// 初始化tcpMaster
		tcpMaster = initTcpMaster(tcpMaster);
		if (tcpMaster == null) {
			System.out.println("tcpMaster is null");
			return null;
		}
		//获取请求对象modbusRequest
		ModbusRequest modbusRequest = getModbusRequest(salveId, start, readLenth,tcpMaster);		
		if (modbusRequest == null) {
			System.out.println("request is null");
			return null;
		}
		//获取请求对象的相应结果对象
		ModbusResponse modbusResponse = getModbusResponse(tcpMaster, modbusRequest);
		ByteQueue byteQueue = new ByteQueue(12);
		modbusResponse.write(byteQueue);
		System.out.println("功能" + modbusRequest.getFunctionCode());
		System.out.println("从站地址:" + modbusRequest.getSlaveId());
		System.out.println("收到的响应信息大小" + byteQueue.size());
		System.out.println("收到的响应信息小:" + byteQueue);
		return byteQueue;
	}

	/* *
	 * Convert byte[] to hex
	 * string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串
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
            //将byte数组的前后两个字节的高低位换过来 DCBA
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
	 * 起始位置15,响应数据：从站|data包含的传感器个数|data length|data*
	 * ***************************************************
	 * 
	 * @param bq
	 */
	public static void ansisByteQueue(ByteQueue bq) {
		byte[] result = bq.peekAll();
		System.out.println("从站地址===" + result[0]);
		System.out.println("data 个数===" + result[1]);
		System.out.println("data 长度===" + result[2]);
		for(int i=0;i<result.length;i++){
			System.out.println(i+"--->"+result[i]);
		}
		byte[] temp = null;
		ByteBuffer buffer = ByteBuffer.wrap(result, 3, result.length - 3);//直接获取 data
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
