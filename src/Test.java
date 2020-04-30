import java.io.IOException;

import com.nompor.milight.MiLightConroller;

public class Test {
	private static String host = "コントローラのIPアドレス";

	public static void main(String[] args) throws IOException, InterruptedException {
		try (MiLightConroller ctrl = new MiLightConroller(host)){

			//LED点灯
			ctrl.on(MiLightConroller.ZONE_2);
			Thread.sleep(1000);
			ctrl.on(MiLightConroller.ZONE_3);
			Thread.sleep(1000);
			ctrl.on(MiLightConroller.ZONE_4);
			Thread.sleep(1000);

			//LEDの明るさを変更
			ctrl.brightness((byte)10,MiLightConroller.ZONE_ALL);
			Thread.sleep(1000);
			ctrl.brightness((byte)40,MiLightConroller.ZONE_ALL);
			Thread.sleep(1000);
			ctrl.brightness((byte)80,MiLightConroller.ZONE_ALL);
			Thread.sleep(1000);
			ctrl.brightness((byte)50,MiLightConroller.ZONE_ALL);
			Thread.sleep(1000);

			//カラーLEDの色を変更
			ctrl.color((byte)0,MiLightConroller.ZONE_4);
			Thread.sleep(1000);
			ctrl.color((byte)50,MiLightConroller.ZONE_4);
			Thread.sleep(1000);
			ctrl.color((byte)100,MiLightConroller.ZONE_4);
			Thread.sleep(1000);
			ctrl.color((byte)150,MiLightConroller.ZONE_4);
			Thread.sleep(1000);
			ctrl.color((byte)200,MiLightConroller.ZONE_4);
			Thread.sleep(1000);

			//全LEDオフ
			ctrl.off(MiLightConroller.ZONE_ALL);
		}
	}
}
