package erestaurant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

 @RestController
 public class CookController {

        @Autowired
        CookRepository cookRepository;

        @RequestMapping(value = "/cooks/requestCooking",
                method = RequestMethod.POST,
                produces = "application/json;charset=UTF-8")

        // public boolean receive(HttpServletRequest request, HttpServletResponse response) throws Exception {
        public String receive(@RequestBody Cook cook) throws Exception {
                System.out.println("##### /cook/receive  called #####");

                //서킷브레이커 시간지연
                Thread.currentThread().sleep((long) (400 + Math.random() * 220));

                String result;

                try {
                        if ("양고기".equals(cook.getMenuname())) {
                                result = "양고기는 메뉴에 없습니다.";
                        } else {
                                cook.setStatus("접수완료");

                                cookRepository.save(cook);
        
                                result = "";
                        }

                } catch (Exception e) {
                        result = e.getMessage();
                        e.printStackTrace();
                }
                return result;
        }

 }
