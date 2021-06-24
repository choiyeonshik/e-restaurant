# e-restaurant

---------------------------------------

## 0. 체크포인트

* Saga
* CQRS
* Correlation
* Req/Resp
* Gateway
* Deploy/ Pipeline
* Circuit Breaker
* Autoscale (HPA)
* Zero-downtime deploy (Readiness Probe)
* Config Map/ Persistence Volume
* Polyglot
* Self-healing (Liveness Probe)

---------------------------------------

## 1. 시나리오
>  사원은 구내식당에서 자신의 ID Card를 이용하여 점심 또는 저녁식사를 하고
>
>  비용은 급여에서 자동 차감되도록 서비스를 구현한다.

### 1.1. 기능적 요구사항
1. 사원은 사원증을 타각하여 음식을 주문한다.
1. 주문한 음식을 주방에서 요리한다.
1. 주문이 완료되면 급여에서 해당 금액을 차감한다.
1. 요리가 완료된 음식은 사원에게 메시지를 발송한다.
1. 사원은 주문상태를 조회할 수 있다.

### 1.2. 비기능적 요구사항
* 트랜잭션
  - 급여 차감은 주문이 완료 상태이어야 한다.
* 장애격리
  - 급여 정산 서비스가 정상 동작하지 않더라도 주문은 계속할 수 있어야 한다.
  - 동시간대에 주문이 폭주하는 경우에 잠시 동안 주문을 받지 않고 중단한 후 다시 서비스 할 수 있다.

### 1.3. 성능
* Main Service의 성능에 영향이 없도록 사용자 주문상태를 조회할 수 있도록 View를 제공한다.

---------------------------------------

## 2. 분석/설계

>  MSAEZ 를 통하여 DDD(Domain Driven Desing)기반 설계를 완성하였습니다.

#### 2.1. Event Storming

* 결과: http://www.msaez.io/#/storming/prssBIL3V4WW7AwMrfdCgM5LT0e2/mine/d060c00f74a577af42b2ff4fdfd84c96


* Event 도출

![image](https://user-images.githubusercontent.com/82796103/122709738-d3ca1e80-d299-11eb-9e2d-4910082141bb.png)


* 부적격 Event 탈락

![image](https://user-images.githubusercontent.com/82796103/122709754-df1d4a00-d299-11eb-95ba-3417f791e64f.png)

* 완성된 모형

![image](https://user-images.githubusercontent.com/82796103/122709917-30c5d480-d29a-11eb-94ce-43be118641f3.png)

* 시나리오 검증

![image](https://user-images.githubusercontent.com/82796103/122719284-f4e63b80-d2a8-11eb-9d1b-4f662784e65d.png)


#### 2.2. Hexagonal Architecture 설계

![image](https://user-images.githubusercontent.com/82796103/122718453-ed726280-d2a7-11eb-9078-175c5b87092f.png)

- Chris Richardson, MSA Patterns 참고하여 Inbound adaptor와 Outbound adaptor를 구분함
- Kafka를 이용하여 호출관계에서 Pub/Sub 구조로 설계하고  Feign CLient는 REST API를 이용한 Req/Resp 로 형식 구현함
- Hall Service는 Hsql DB 셜계하였고 기타 Kitchen, Payment 서비스는 H2 DB 설계(Polyglot)

---------------------------------------

## 3. 구현

> 각 서비스의 구현결과는 다음과 같다.

#### 3.1. Gateway
> API GateWay를 통하여 마이크로 서비스들의 집입점을 통일할 수 있다. 다음과 같이 Gateway를 적용하였다.
> (포트넘버 : 8081 ~ 8084, 8088(집입점, Cloud 환경은 8080))

```
server:
  port: 8088

---

spring:
  profiles: default
  cloud:
    gateway:
      routes:
        - id: hall
          uri: http://localhost:8081
          predicates:
            - Path=/orders/** 
        - id: kitchen
          uri: http://localhost:8082
          predicates:
            - Path=/cooks/** 
        - id: workercenter
          uri: http://localhost:8083
          predicates:
            - Path= /mypages/**
        - id: payment
          uri: http://localhost:8084
          predicates:
            - Path=/payments/** 
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "*"
            allowedMethods:
              - "*"
            allowedHeaders:
              - "*"
            allowCredentials: true


---

spring:
  profiles: docker
  cloud:
    gateway:
      routes:
        - id: hall
          uri: http://hall:8080
          predicates:
            - Path=/orders/** 
        - id: kitchen
          uri: http://kitchen:8080
          predicates:
            - Path=/cooks/** 
        - id: workercenter
          uri: http://workercenter:8080
          predicates:
            - Path= /mypages/**
        - id: payment
          uri: http://payment:8080
          predicates:
            - Path=/payments/** 
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "*"
            allowedMethods:
              - "*"
            allowedHeaders:
              - "*"
            allowCredentials: true

server:
  port: 8080

```

#### 3.2. 시나리오 검증
> 시나리오 검증을 위한 스크립트는 아래와 같다.

  * 주문
```
http POST http://52.231.200.152:8080/orders employeeCardNo=1 menuname="불고기덮밥" amount=5000
``` 
  *
    -  주문결과 Hall

![image](https://user-images.githubusercontent.com/82796103/123214584-5e14bb80-d502-11eb-885d-02385feaeef7.png)

  *
    -  주문결과 Kitchen

![image](https://user-images.githubusercontent.com/82796103/123215176-15113700-d503-11eb-9a1d-947694f8b6b6.png)


  * 요리완료
```
http PATCH http://52.231.200.152:8080/cooks/1 status="요리완료"
``` 
  *
    -  요리결과 Kitchen

![image](https://user-images.githubusercontent.com/82796103/123215262-3114d880-d503-11eb-9b43-afbf2fab8dfa.png)


  * 확인
    - hall 서비스
``` 
http GET http://52.231.200.152:8080/orders
``` 
![image](https://user-images.githubusercontent.com/82796103/123215719-c1531d80-d503-11eb-81a5-10a8ad4a467d.png)

  * 
    - kitchen 서비스
``` 
http GET http://52.231.200.152:8080/cooks
``` 
![image](https://user-images.githubusercontent.com/82796103/123215883-ec3d7180-d503-11eb-97f6-b6fda1a1c055.png)

  * 
    - payment 서비스
``` 
http GET http://52.231.200.152:8080/payments
``` 
![image](https://user-images.githubusercontent.com/82796103/123216048-1858f280-d504-11eb-84fc-be33bb4c0edd.png)


  * 주문 불가 화면 (Request / Response)
```
     양고기 주문시 에러 발생 
     http POST http://52.231.200.152:8080/orders employeeCardNo=4 menuname="양고기" amount=86000
```
![image](https://user-images.githubusercontent.com/82796103/123218578-dd0bf300-d506-11eb-91d6-f09305e65d1e.png)


#### 3.4. CQRS
> hall, kitchen, payment 서비스의 데이터를 수집하여 View service를 제공한다.
> 데이터 원본에 접근없이(Composite 서비스나 조인SQL 등 없이) 도 조회가 가능하도록 workercenter 서비스의 CQRS를 통하여  서비스를 구현하였다.
> 사원의 주문 정보와 payment 정보 뿐만아니라, 요리결과도 조회할 수 있다. 

  * workercenter 서비스
``` 
http GET http://52.231.200.152:8080/mypages
``` 
![image](https://user-images.githubusercontent.com/82796103/123216176-36beee00-d504-11eb-9f73-f064cec81746.png)


    

#### 3.5. Correlation, Req/Resp

> API 호출에 대한 식별자를 정의하고, 컴포넌트 간, 그 식별자를 공유하는 하도록 서비스 컴포넌트들은 각 비즈니스 모델에 맞는 Bounded Context 라는 도메인 모델의 경계를 이루며 동작하고 있다.
>
> 컴포넌트 간 API 호출 방식은 Req/Resp와 Pub/Sub 으로 구성되어 있으며 컴포넌트간 요청과 응답을 API 별로 구분이 가능하며
> 시스템 전반에 걸쳐 일관되게 추적할 수 있다.
>
> 또한, 본 과제에서는 MSAEZ.io를 통하여 도출된 Aggregate는 Entity로 선언하여 PRE/POST PERSIST/UPDATE/DELETE 반영하였으며, Repository Pattern을 적용하여 ACID를 구현하였다.

* hall 서비스의 Order.java
```java
package erestaurant;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Entity
@Table(name="Order_table")
public class Order {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long orderid;
    private Long employeeCardNo;
    private String menuname;
    private Date tagdate;
    private Long amount;
    private String status;

    @PostPersist
    public void onPostPersist(){
        Ordered ordered = new Ordered();
        BeanUtils.copyProperties(this, ordered);
        ordered.publishAfterCommit();
    }
    @PostUpdate
    public void onPostUpdate(){
        SentMessage sentMessage = new SentMessage();
        BeanUtils.copyProperties(this, sentMessage);
        sentMessage.publishAfterCommit();

    }
    @PrePersist
    public void onPrePersist(){
        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

        erestaurant.external.Cook cook = new erestaurant.external.Cook();
        
        cook.setMenuname(this.menuname);
        cook.setOrderid(System.currentTimeMillis());

        String result = HallApplication.applicationContext.getBean(erestaurant.external.CookService.class)
            .receive(cook);

            if ("".equals(result)) {
                this.orderid = cook.getOrderid();
                this.status = "주문완료";
                this.tagdate = new Date(System.currentTimeMillis());
            } else {
                this.status = result;
            }
    }
    @PreUpdate
    public void onPreUpdate(){
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderid() {
        return orderid;
    }

    public void setOrderid(Long orderid) {
        this.orderid = orderid;
    }
    public Long getEmployeeCardNo() {
        return employeeCardNo;
    }

    public void setEmployeeCardNo(Long employeeCardNo) {
        this.employeeCardNo = employeeCardNo;
    }
    public String getMenuname() {
        return menuname;
    }

    public void setMenuname(String menuname) {
        this.menuname = menuname;
    }
    public Date getTagdate() {
        return tagdate;
    }

    public void setTagdate(Date tagdate) {
        this.tagdate = tagdate;
    }
    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
```

* kitchen 서비스의 CookController.java
```java
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
```

* payment 서비스의 PolicyHandler.java
```java
package erestaurant;

import erestaurant.config.kafka.KafkaProcessor;

import java.util.Date;

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
        payement.setPaiddate(new Date(System.currentTimeMillis()));
        payement.setStatus("결제완료");

        paymentRepository.save(payement);

        // Sample Logic //
        System.out.println("\n\n##### listener Pay : " + cooked.toJson() + "\n\n");
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrdered_RegisterPayInfo(@Payload Ordered ordered){

        if(!ordered.validate()) return;
        
        Payment payment = new Payment();

        payment.setStatus("주문완료");
        payment.setMenuname(ordered.getMenuname());
        payment.setAmount(ordered.getAmount());
        payment.setOrderid(ordered.getOrderid());

        paymentRepository.save(payment);

        // Sample Logic //
        System.out.println("\n\n##### listener RegisterPayInfo : " + ordered.toJson() + "\n\n");
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}
}
```


#### 3.6. Polyglot 프로그래밍

> hall 서비스의 Hsql DB와 기타 kitchen, payment, workercenter 등 서비스의  H2 DB를 사용하여 폴리글랏을 구현하였다.

* hall service의 pom.xml DB 설정 코드

![image](https://user-images.githubusercontent.com/82796103/123209737-05dabb00-d4fc-11eb-88ef-32927f07779f.png)

* 기타 service의 pom.xml DB 설정 코드

![image](https://user-images.githubusercontent.com/82796103/123209807-1f7c0280-d4fc-11eb-84d9-47310e6074c1.png)



---------------------------------------

## 4. 운영
> 운영에 필요한 검증항목은 다음과 같다.

#### 4.1. namespace 생성
```sh
	  kubectl create ns choi
```

#### 4.2 Deploy / Pipeline

* git에서 소스 가져오기
```sh
	git clone https:/github.com/choiyeonshik/e-restaurant.git
```

* Build 하기
```sh
	cd /home/project/e-restaurant/hall
	mvn clean
	mvn compile
	mvn package

	cd /home/project/e-restaurant/kitchen
	mvn clean
	mvn compile
	mvn package

	cd /home/project/e-restaurant/payment
	mvn clean
	mvn compile
	mvn package

	cd /home/project/e-restaurant/workercenter
	mvn clean
	mvn compile
	mvn package

	cd /home/project/gbike/gateway
	mvn clean
	mvn compile
	mvn package
```

* yml파일 이용한 deploy
```sh
	cd /home/project/e-restaurant/hall
	az acr build --registry skccuser23 --image skccuser23.azurecr.io/hall:v2 .
	kubectl create -f ./kubernetes/deployment.yml -n choi
	kubectl create -f ./kubernetes/service.yaml -n choi

	cd /home/project/e-restaurant/kitchen
	az acr build --registry skccuser23 --image skccuser23.azurecr.io/kitchen:v2 .
	kubectl create -f ./kubernetes/deployment.yml -n choi
	kubectl create -f ./kubernetes/service.yaml -n choi

	cd /home/project/e-restaurant/payment
	az acr build --registry skccuser23 --image skccuser23.azurecr.io/payment:v1 .
	kubectl create -f ./kubernetes/deployment.yml -n choi
	kubectl create -f ./kubernetes/service.yaml -n choi

	cd /home/project/e-restaurant/workercenter
	az acr build --registry skccuser23 --image skccuser23.azurecr.io/workercenter:v1 .
	kubectl create -f ./kubernetes/deployment.yml -n choi
	kubectl create -f ./kubernetes/service.yaml -n choi

	cd /home/project/e-restaurant/gateway
	az acr build --registry skccuser23 --image skccuser23.azurecr.io/gateway:v1 .
	kubectl create -f ./kubernetes/deployment.yml -n choi
	kubectl create -f ./kubernetes/service.yaml -n choi

	kubectl expose deploy gateway --type=LoadBalancer --port=8080 -n choi
```

* deployment.yml 파일

![image](https://user-images.githubusercontent.com/82796103/123209013-fd35b500-d4fa-11eb-9c3c-70a07eec65b6.png)

* Deploy 완료

![image](https://user-images.githubusercontent.com/82796103/123214131-cc0cb300-d501-11eb-9840-2bed6a2e5871.png)


#### 4.3. ConfigMap 적용
>	시스템별로 변경 가능성이 있는 설정들을 ConfigMap을 사용하여 관리한다.
	
* application.yml 파일에 ${api.url.bikeservice} 설정
![image](https://user-images.githubusercontent.com/82796103/121114706-1c6fe980-c84f-11eb-8e86-024a6e33a3e8.png)

![image](https://user-images.githubusercontent.com/82796103/121021504-6cfa2f00-c7dc-11eb-9269-528765e63ab1.png)

* deployment-config.yaml
![image](https://user-images.githubusercontent.com/82796103/121037602-8a35fa00-c7ea-11eb-889e-d8a03ae445b6.png)

* configMap 
![image](https://user-images.githubusercontent.com/82796103/121039821-61166900-c7ec-11eb-9c88-a9bb5221f924.png)


#### 4.4. Autoscale (HPA)

* 부하 테스트 siege Pod 설치
```sh
	kubectl apply -f - <<EOF
	apiVersion: v1
	kind: Pod
	metadata:
	  name: siege
	spec:
	  containers:
	    - name: siege
	    image: apexacme/siege-nginx
	EOF
```

* Auto Scale-Out 설정
  - deployment.yml 파일 수정
```sh
        resources:
          limits:
            cpu: 500m
          requests:
            cpu: 200m
```

  - Auto Scale 설정
```sh
	kubectl autoscale deployment bike --cpu-percent=20 --min=1 --max=3 -n gbike
```

* Auto Scale Out 확인

  - 부하 시작 (siege) : 동시접속 100명, 120초 동안 
```sh
	siege -c100 -t120S -v http://20.194.44.70:8080/bikes
```
![autoscale1](https://user-images.githubusercontent.com/82795748/121107122-55559180-c842-11eb-8542-bbfef1463584.jpg)

  - Scale out 확인

![autoscale2](https://user-images.githubusercontent.com/82795748/121107303-a4032b80-c842-11eb-958c-a64e98bda3ce.jpg)

![autoscale3](https://user-images.githubusercontent.com/82795748/121107154-643c4400-c842-11eb-9033-69c1a3114eb2.jpg)


#### 4.5. Circuit Breaker

> 서킷 브레이킹 프레임워크의 선택 : Spring FeignClient + Hystrix 옵션을 사용하여 구현함

* Hystrix를 설정

  - 요청처리 쓰레드에서 처리시간이 1200 밀리가 넘어서기 시작하여 어느정도 유지되면 CB 회로가 닫히도록(요청을 빠르게 실패처리, 차단) 설정
  - 동기 호출 주체인 Rent 서비스에 Hystrix 설정
  - rent/src/main/resources/application.yml 파일

```sh
	feign:
	  hystrix:
		enabled: true
	hystrix:
	  command:
		default:
		  execution.isolation.thread.timeoutInMilliseconds: 1200
```

* 부하에 대한 지연시간 발생코드 BikeController.java 지연 적용

![circuit](https://user-images.githubusercontent.com/82795748/121125003-c9069700-c860-11eb-9a4f-1ffb5e20a550.jpg)

* 부하 테스터 siege툴을 통한 서킷 브레이커 동작확인 : 동시 사용자 5명, 10초 동안 실시

	siege -c5 -t10S -r10 -v --content-type "application/json" 'http://20.194.44.70:8080/rents POST {"bikeid": "1", "userid": "1"}'

* 결과

![image](https://user-images.githubusercontent.com/82796103/121124344-b9d31980-c85f-11eb-9d9b-2778f3fcb06a.png)

![image](https://user-images.githubusercontent.com/82796103/121125220-2995d400-c861-11eb-96ef-01f771097e2e.png)



#### 4.6. Self-healing (Liveness Probe)

* userdeposit 서비스 정상 확인

![liveness1](https://user-images.githubusercontent.com/84724396/121038124-fdd80700-c7ea-11eb-9063-ce9360b36278.PNG)


* deployment.yml 에 Liveness Probe 옵션 추가
```sh
cd ~/gbike/userDeposit
vi deployment.yml

(아래 설정 변경)
          livenessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8081
            initialDelaySeconds: 3
            periodSeconds: 5
```

![liveness43](https://user-images.githubusercontent.com/84724396/121042427-a471d700-c7ee-11eb-9140-3e59ac801fed.PNG)



* gbike pod에 liveness가 적용된 부분 확인
```sh
  kubectl describe deploy userdeposit -n gbike
```

![liveness42](https://user-images.githubusercontent.com/84724396/121044305-65448580-c7f0-11eb-9d1a-29b4b0118904.PNG)


* userdeposit 서비스의 liveness가 발동되어 2번 retry 시도 한 부분 확인

![image](https://user-images.githubusercontent.com/84724396/121130881-fa379500-c869-11eb-9921-b24701660a72.png)


#### 4.7. Zero-downtime deploy (readiness probe)

* readiness 옵션 제거 후 배포 - 신규 Pod 생성 시 downtime 발생

![image](https://user-images.githubusercontent.com/82795726/121106857-d06a7800-c841-11eb-85cd-d7ad08ff62db.png)

* readiness 옵션 추가하여 배포

![image](https://user-images.githubusercontent.com/82795726/121106445-fc392e00-c840-11eb-9b8c-b413ef06b95e.png)

![image](https://user-images.githubusercontent.com/82795726/121106524-225ece00-c841-11eb-9953-2febeab82108.png)

* Pod Describe에 Readiness 설정 확인

![image](https://user-images.githubusercontent.com/82795726/121110068-a61bb900-c847-11eb-9229-63701496846a.png)

* 기존 버전과 새 버전의  pod 공존

![image](https://user-images.githubusercontent.com/82795726/121109942-6e147600-c847-11eb-9dae-9dfce13e8c62.png)
