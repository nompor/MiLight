package com.nompor.milight;
import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class MiLightConroller implements Closeable {

	public static byte ZONE_ALL = 0;
	public static byte ZONE_1 = 1;
	public static byte ZONE_2 = 2;
	public static byte ZONE_3 = 3;
	public static byte ZONE_4 = 4;

	private byte sessionId1;
	private byte sessionId2;
	private byte iboxSeq=0;
	private DatagramSocket sock;
	private DatagramPacket pack;
	public MiLightConroller(String host) throws IOException {
		this(host,DEFAULT_PORT);
	}
	public MiLightConroller(String host, int port) throws IOException {
		this(host,port,(byte)0x10);
	}
	public MiLightConroller(String host, int port, byte seq) throws IOException {
		this.iboxSeq=seq;
		sock = new DatagramSocket();
		sock.setSoTimeout(30000);

		//接続先
		InetSocketAddress address = new InetSocketAddress(host, port);

		//セッション開始
		pack = new DatagramPacket(SESSION_START_CMD,SESSION_START_CMD.length,address);
		sock.send(pack);

		//セッション開始に対するレスポンス取得
		byte[] recv = new byte[32];
		pack.setData(recv, 0, recv.length);
		sock.receive(pack);

		sessionId1 = recv[19];
		sessionId2 = recv[20];

		//開始通知判定用
		byte[] RESPONSE_START_SESSION = {
				0x28, 0x00, 0x00, 0x00, 0x11, 0x00, 0x02
		};

		//開始されたかチェック
		for ( int i = 0;i < RESPONSE_START_SESSION.length;i++ ) {
			if ( RESPONSE_START_SESSION[i] != recv[i] ) {
				throw new IOException("session start fail.");
			}
		}
	}

	public static final int DEFAULT_PORT=5987;

	private static final byte[]

			//セッション開始命令コマンド
			SESSION_START_CMD = {
				  (byte)0x23, (byte)0x00 , (byte)0x00 , (byte)0x00 , (byte)0x16
				, (byte)0x02, (byte)0x62 , (byte)0x3a , (byte)0xd5 , (byte)0xed
				, (byte)0xa3, (byte)0x01 , (byte)0xae , (byte)0x08 , (byte)0x2d
				, (byte)0x46, (byte)0x61 , (byte)0x41 , (byte)0xa7 , (byte)0xf6
				, (byte)0xdc, (byte)0xaf , (byte)0xa6 , (byte)0xa1 , (byte)0x00
				, (byte)0x00, (byte)0x64
			}

			;

	public void send(byte[] cmd) throws IOException {

		int checkSumInt=0;
		for ( byte b : cmd ) {
			checkSumInt += b;
		}
		byte checkSum = (byte) (checkSumInt & 0xff);

		byte[] ctrlData = new byte[] {
				  (byte)0x80 , (byte)0x00 , (byte)0x00 , (byte)0x00 , (byte)0x11
				, sessionId1 ,  sessionId2 , (byte)0x00 ,    iboxSeq , (byte)0x00
				, cmd[0] , cmd[1] , cmd[2] , cmd[3] , cmd[4]
				, cmd[5] , cmd[6] , cmd[7] , cmd[8] , cmd[9]
				, (byte)0x00 , checkSum};

		pack.setData(ctrlData, 0, ctrlData.length);
		sock.send(pack);

		byte[] recv = new byte[8];
		pack.setData(recv, 0, recv.length);
		sock.receive(pack);



		//開始通知判定用
		byte[] RESPONSE_CHECK = {
				(byte) 0x88, 0x00, 0x00, 0x00, 0x03, 0x00
		};

		//処理されたかチェック
		for ( int i = 0;i < RESPONSE_CHECK.length;i++ ) {
			if ( RESPONSE_CHECK[i] != recv[i] ) {
				throw new IOException("control fail.");
			}
		}

		//シーケンスチェック
		if ( recv[6] != iboxSeq ) {
			throw new IOException("sequence fail.");
		}
	}
	@Override
	public void close() throws IOException {
		// TODO 自動生成されたメソッド・スタブ
		sock.close();
	}


	/**
	 * ライトを点灯します。
	 * @param zone ゾーン
	 * @throws IOException
	 */
	public void on(byte zone) throws IOException {

		byte[] cmd = new byte[] {
				  (byte)0x31 , (byte)0x00 , (byte)0x00 , (byte)0x08 , (byte)0x04
				, (byte)0x01 , (byte)0x00 , (byte)0x00 , (byte)0x00 , zone
		};

		send(cmd);
	}

	/**
	 * ライトを消灯します。
	 * @param zone ゾーン
	 * @throws IOException
	 */
	public void off(byte zone) throws IOException {

		byte[] cmd = new byte[] {
				  (byte)0x31 , (byte)0x00 , (byte)0x00 , (byte)0x08 , (byte)0x04
				, (byte)0x02 , (byte)0x00 , (byte)0x00 , (byte)0x00 , zone
		};
		send(cmd);
	}

	/**
	 * 明度の変更を行います。
	 * @param bri 0～100
	 * @param zone ゾーン
	 * @throws IOException
	 */
	public void brightness(byte bri,byte zone) throws IOException {


		//明度の変更
		byte[] cmd = new byte[] {
				  (byte)0x31 , (byte)0x00 , (byte)0x00 , (byte)0x08 , (byte)0x03
				, bri , (byte)0x00 , (byte)0x00 , (byte)0x00 , zone
		};
		send(cmd);
	}

	/**
	 * 彩度の変更を行います。
	 * @param sat 0～100
	 * @param zone ゾーン
	 * @throws IOException
	 */
	public void saturation(byte sat,byte zone) throws IOException {

		//彩度の変更
		byte[] cmd = new byte[] {
				  (byte)0x31 , (byte)0x00 , (byte)0x00 , (byte)0x08 , (byte)0x02
				, sat , (byte)0x00 , (byte)0x00 , (byte)0x00 , zone
		};
		send(cmd);
	}

	/**
	 * 色温度を変更します。
	 * @param tem 0～107
	 * @param zone ゾーン
	 * @throws IOException
	 */
	public void temperature(byte tem,byte zone) throws IOException {


		byte[] cmd = new byte[] {
				  (byte)0x31 , (byte)0x00 , (byte)0x00 , (byte)0x08 , (byte)0x05
				, tem , (byte)0x00 , (byte)0x00 , (byte)0x00 , zone
		};
		send(cmd);
	}

	/**
	 * 色を変更します。
	 * @param clr
	 * @param zone ゾーン
	 * @throws IOException
	 */
	public void color(byte clr,byte zone) throws IOException {


		//色変更
		byte[] cmd = new byte[] {
				  (byte)0x31 , (byte)0x00 , (byte)0x00 , (byte)0x08 , (byte)0x01
				, clr , clr , clr , clr , zone
		};
		send(cmd);
	}

	/**
	 * 特殊な点灯モードを実行します。
	 * @param mode モード番号
	 * @param zone ゾーン
	 * @throws IOException
	 */
	public void mode(byte mode,byte zone) throws IOException {

		byte[] cmd = new byte[] {
				  (byte)0x31 , (byte)0x00 , (byte)0x00 , (byte)0x08 , (byte)0x06
				, mode , (byte)0x00 , (byte)0x00 , (byte)0x00 , zone
		};

		send(cmd);
	}

	/**
	 * コントローラとペアリングします。
	 * @param zone ゾーン
	 * @throws IOException
	 */
	public void link(byte zone) throws IOException {

		byte[] cmd = new byte[] {
				0x3D, 0x00, 0x00, 0x08, 0x00
				, 0x00, 0x00, 0x00, 0x00, zone
		};

		send(cmd);
	}

	/**
	 * コントローラとのペアリングを解除します。
	 * @param zone ゾーン
	 * @throws IOException
	 */
	public void unlink(byte zone) throws IOException {

		byte[] cmd = new byte[] {
				0x3E, 0x00, 0x00, 0x08, 0x00
				, 0x00, 0x00, 0x00, 0x00, zone
		};

		send(cmd);
	}

	/**
	 * 特殊な点灯モード時の速度を上げます。
	 * @param zone ゾーン
	 * @throws IOException
	 */
	public void speedUp(byte zone) throws IOException {


		//モードスピードアップ
		byte[] cmd = new byte[] {
				  (byte)0x31 , (byte)0x00 , (byte)0x00 , (byte)0x08 , (byte)0x04
				, (byte)0x03 , (byte)0x00 , (byte)0x00 , (byte)0x00 , zone
		};
		send(cmd);
	}

	/**
	 * 特殊な点灯モード時の速度を下げます。
	 * @param zone ゾーン
	 * @throws IOException
	 */
	public void speedDown(byte zone) throws IOException {


		//モードスピードダウン
		byte[] cmd = new byte[] {
				  (byte)0x31 , (byte)0x00 , (byte)0x00 , (byte)0x08 , (byte)0x04
				, (byte)0x04 , (byte)0x00 , (byte)0x00 , (byte)0x00 , zone
		};
		send(cmd);
	}

}