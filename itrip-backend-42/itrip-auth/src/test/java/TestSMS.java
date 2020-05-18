import com.cloopen.rest.sdk.CCPRestSmsSDK;
import org.junit.Test;

import java.util.HashMap;
import java.util.Set;

public class TestSMS {

    @Test
    public void test1(){
        HashMap<String, Object> result = null;
        CCPRestSmsSDK sdk = new CCPRestSmsSDK();
        sdk.init("app.cloopen.com","8883");
        sdk.setAccount("8a216da87051c90f01707cd0d9d21811","421256be45bc40ed8db899df07bd6d4a");
        sdk.setAppId("8a216da87051c90f01707cd0da391818");
        result = sdk.sendTemplateSMS("13055769579","1",new String[]{"1234","1"});
        System.out.println(result.get("statusCode"));
        if("000000".equals(result.get("statusCode"))){
            //正常返回输出data包体信息（map）
            HashMap<String,Object> data = (HashMap<String, Object>) result.get("data");
            Set<String> keySet = data.keySet();
            for(String key:keySet){
                Object object = data.get(key);
                System.out.println(key +" = "+object);
            }
        }else{
            //异常返回输出错误码和错误信息
            System.out.println("错误码=" + result.get("statusCode") +" 错误信息= "+result.get("statusMsg"));
        }
    }

}
