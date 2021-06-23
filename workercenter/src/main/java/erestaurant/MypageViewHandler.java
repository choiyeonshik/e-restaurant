package erestaurant;

import erestaurant.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MypageViewHandler {


    @Autowired
    private MypageRepository mypageRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrdered_then_CREATE_1 (@Payload Ordered ordered) {
        try {

            if (!ordered.validate()) return;

            // view 객체 생성
            Mypage mypage = new Mypage();
            // view 객체에 이벤트의 Value 를 set 함
            mypage.setEmployeeCardNo(ordered.getEmployeeCardNo());
            mypage.setMenuname(ordered.getMenuname());
            mypage.setOrderid(ordered.getOrderid());
            mypage.setAmount(ordered.getAmount());
            // view 레파지 토리에 save
            mypageRepository.save(mypage);

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whenReceieved_then_UPDATE_1(@Payload Receieved receieved) {
        try {
            if (!receieved.validate()) return;
                // view 객체 조회

                    List<Mypage> mypageList = mypageRepository.findByCookid(receieved.getCookid());
                    for(Mypage mypage : mypageList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    mypage.setCookid(receieved.getCookid());
                    mypage.setCookingstatus(receieved.getStatus());
                // view 레파지 토리에 save
                mypageRepository.save(mypage);
                }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenRegisteredPayInfo_then_UPDATE_2(@Payload RegisteredPayInfo registeredPayInfo) {
        try {
            if (!registeredPayInfo.validate()) return;
                // view 객체 조회

                    List<Mypage> mypageList = mypageRepository.findByPaymentid(registeredPayInfo.getPaymentid());
                    for(Mypage mypage : mypageList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    mypage.setPaymentstatus(registeredPayInfo.getStatus());
                    mypage.setAmount(registeredPayInfo.getAmount());
                // view 레파지 토리에 save
                mypageRepository.save(mypage);
                }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenPaid_then_UPDATE_3(@Payload Paid paid) {
        try {
            if (!paid.validate()) return;
                // view 객체 조회

                    List<Mypage> mypageList = mypageRepository.findByCookid(paid.getCookid());
                    for(Mypage mypage : mypageList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    mypage.setPaymentstatus(paid.getStatus());
                    mypage.setPaiddate(paid.getPaiddate());
                // view 레파지 토리에 save
                mypageRepository.save(mypage);
                }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}

