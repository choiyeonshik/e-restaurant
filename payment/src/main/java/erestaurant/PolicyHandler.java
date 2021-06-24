package erestaurant;

import erestaurant.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired PaymentRepository paymentRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCooked_Pay(@Payload Cooked cooked){

        if(!cooked.validate()) return;

        Payment payement = paymentRepository.findByOrderid(Long.valueOf(cooked.getOrderid()));

        payement.setCookid(cooked.getCookid());
        payement.setStatus("요리완료");

        paymentRepository.save(payement);

        // Sample Logic //
        System.out.println("\n\n##### listener Pay : " + cooked.toJson() + "\n\n");
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrdered_RegisterPayInfo(@Payload Ordered ordered){

        if(!ordered.validate()) return;
        
        Payment payment = new Payment();

        payment.setStatus("주문완료");
        payment.setAmount(ordered.getAmount());
        payment.setOrderid(ordered.getOrderid());

        paymentRepository.save(payment);

        // Sample Logic //
        System.out.println("\n\n##### listener RegisterPayInfo : " + ordered.toJson() + "\n\n");
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
