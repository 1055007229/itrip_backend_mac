package cn.itrip.auth.service.impl;

import cn.itrip.auth.exception.SmsException;
import cn.itrip.auth.service.SmsService;
import cn.itrip.common.SystemConfig;
import com.cloopen.rest.sdk.CCPRestSmsSDK;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Set;

@Service
public class SmsServiceImpl implements SmsService {

    @Autowired
    private SystemConfig systemConfig;

    @Override
    public void sendSms(String to, String templateId, String[] datas) throws SmsException {
        HashMap<String, Object> result = null;
        CCPRestSmsSDK sdk = new CCPRestSmsSDK();
        sdk.init(systemConfig.getSmsServerIP(),systemConfig.getSmsServerPort());
        sdk.setAccount(systemConfig.getSmsAccountSid(),systemConfig.getSmsAuthToken());
        sdk.setAppId(systemConfig.getSmsAppID());
        result = sdk.sendTemplateSMS(to,templateId,datas);
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
            throw new SmsException("错误码=" + result.get("statusCode") +" 错误信息= "+result.get("statusMsg"));
        }
    }
}
