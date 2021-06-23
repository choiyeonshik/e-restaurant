package erestaurant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

 @RestController
 public class CookController {

        @Autowired
        CookRepository cookRepository;

        @RequestMapping(value = "/cooks/requestCooking",
                method = RequestMethod.POST,
                produces = "application/json;charset=UTF-8")

        // public boolean receive(HttpServletRequest request, HttpServletResponse response) throws Exception {
        public boolean receive(@RequestBody Cook cook) throws Exception {
                System.out.println("##### /cook/receive  called #####");
                // System.out.println("#"+ request);
                // System.out.println("#"+ request.getParameter("orderid"));

                //서킷브레이커 시간지연
                Thread.currentThread().sleep((long) (400 + Math.random() * 220));

                boolean result;
                try {
                        // Cook cook = new Cook();
                        // cook.setOrderid(Long.parseLong(request.getParameter("orderid")));
                        cook.setStatus("접수완료");

                        cookRepository.save(cook);

                        result = true;

                } catch (Exception e) {
                        result = false;
                        e.printStackTrace();
                }
                return result;
        }

 }
