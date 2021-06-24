package erestaurant;

import erestaurant.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired OrderRepository orderRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCooked_SendMessage(@Payload Cooked cooked){

        if(!cooked.validate()) return;

        // Get Methods
        Order order = orderRepository.findByOrderid(Long.valueOf(cooked.getOrderid()));
        order.setStatus("문자발송");
        orderRepository.save(order);

        // Sample Logic //
        System.out.println("\n\n##### listener SendMessage : " + cooked.toJson() + "\n\n");
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
