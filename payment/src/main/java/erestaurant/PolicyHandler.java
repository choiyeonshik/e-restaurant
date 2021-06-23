package erestaurant;

import erestaurant.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        // Get Methods


        // Sample Logic //
        System.out.println("\n\n##### listener Pay : " + cooked.toJson() + "\n\n");
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrdered_RegisterPayInfo(@Payload Ordered ordered){

        if(!ordered.validate()) return;
        // Get Methods


        // Sample Logic //
        System.out.println("\n\n##### listener RegisterPayInfo : " + ordered.toJson() + "\n\n");
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
