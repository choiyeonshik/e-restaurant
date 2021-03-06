
package erestaurant.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name="kitchen", url="${app.feignclient.url.kitchen}")
public interface CookService {
    
    @RequestMapping(method= RequestMethod.GET, path="/cooks/requestCooking")
    public String receive(@RequestBody Cook cook);

}

