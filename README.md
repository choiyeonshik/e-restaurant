# e-restaurant

---------------------------------------

## 0. 체크포인트

* Saga
* CQRS
* Correlation
* Req/Resp
* Gateway
* Polyglot
* Deploy/ Pipeline
* Config Map/ Persistence Volume
* Autoscale (HPA)
* Circuit Breaker
* Self-healing (Liveness Probe)
* Zero-downtime deploy (Readiness Probe)

---------------------------------------

## 1. 시나리오
>  사원은 구내식당에서 자신의 ID Card를 이용하여 식사를 하고
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

> MSAEZ 를 통하여 DDD(Domain Driven Desing)기반 Event Storming을 진행하였습니다.
> Event, Actor와 Command, Policy 순으로 item을 도출하고, Aggregate로 관련 Object를 하나로 묶었습니다.
> 이후 Bounded Context로 Micro Service를 구성하였으며 
> Service간 필요정보가 없는지 Attribute, Policy 중심으로 Inspection 하였습니다.
>
> 특히, Micro Service 간에는 Choreography SAGA, Orchestrator SAGA 패턴을 적용하였습니다.
>(Pub/Sub, FeignClient(Req/Res) 구조)

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

![image](https://user-images.githubusercontent.com/82796103/123294242-a4dcd280-d54f-11eb-8e14-e29deffd0b3a.png)

- Chris Richardson, MSA Patterns 참고하여 Inbound adaptor와 Outbound adaptor를 구분함
- Kafka를 이용하여 호출관계에서 Pub/Sub 구조로 설계하고  Feign CLient는 REST API를 이용한 Req/Resp 로 형식 구현함
- Hall Service는 Hsql DB 설계하였고 기타 Kitchen, Payment 서비스는 H2 DB로 설계 함

---------------------------------------

## 3. 구현

> Micro Service 구현결과는 다음과 같다.

#### 3.1. 시나리오 검증
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


#### 3.2. Gateway
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


#### 3.3. CQRS
> hall, kitchen, payment 서비스의 데이터를 수집하여 View service를 제공한다.
> 데이터 원본에 접근없이(Composite 서비스나 조인SQL 등 없이) 도 조회가 가능하도록 workercenter 서비스의 CQRS를 통하여  서비스를 구현하였다.
> 사원의 주문 정보와 payment 정보 뿐만아니라, 요리결과도 조회할 수 있다. 

  * workercenter 서비스
``` 
http GET http://52.231.200.152:8080/mypages
``` 
![image](https://user-images.githubusercontent.com/82796103/123216176-36beee00-d504-11eb-9f73-f064cec81746.png)


    
#### 3.4. Correlation, Req/Resp

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


#### 3.5. Polyglot 프로그래밍

> hall 서비스의 Hsql DB와 기타 kitchen, payment, workercenter 등 서비스의  H2 DB를 사용하여 폴리글랏을 구현하였다.

* hall service의 pom.xml DB 설정 코드

![image](https://user-images.githubusercontent.com/82796103/123209737-05dabb00-d4fc-11eb-88ef-32927f07779f.png)

* 기타 service의 pom.xml DB 설정 코드

![image](https://user-images.githubusercontent.com/82796103/123209807-1f7c0280-d4fc-11eb-84d9-47310e6074c1.png)



---------------------------------------

## 4. 운영

> 각 서비스에 필요한 Namespace를 별도로 생성 및 관리하며 소스는 github에서 clone한다.
> 서비스 구동에 필요한 S/W는 별도의 Namespace로 구분하여 설치하였으며
> 본 Micro Service를 위하여 choi namespace도 별도 생성하였다.

#### 4.1. namespace 생성
```sh
	  kubectl create ns choi
```

#### 4.2 Deploy / Pipeline

> gib hub에서 가져온 소스를 build 하여 Azure Repository에 등록한다.

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
    az acr build --registry skccuser23 --image skccuser23.azurecr.io/hall:v1 .
    
    #초기 1회만 실행 
    kubectl create deploy hall --image=skccuser23.azurecr.io/hall:v1 -n choi
    
    #이후
    kubectl create -f ./kubernetes/deployment.yml -n choi
    kubectl create -f ./kubernetes/service.yaml -n choi
    
    cd /home/project/e-restaurant/kitchen
    az acr build --registry skccuser23 --image skccuser23.azurecr.io/kitchen:v1 .
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
    
    kubectl expose deploy hall --type=ClusterIP --port=8080 -n choi
    kubectl expose deploy kitchen --type=ClusterIP --port=8080 -n choi
    kubectl expose deploy payment --type=ClusterIP --port=8080 -n choi
    kubectl expose deploy workercenter --type=ClusterIP --port=8080 -n choi
    kubectl expose deploy gateway --type=LoadBalancer --port=8080 -n choi
```

* deployment.yml 파일

![image](https://user-images.githubusercontent.com/82796103/123209013-fd35b500-d4fa-11eb-9c3c-70a07eec65b6.png)

* Deploy 완료

>  replicaset, deployment, pod 모두 정상 동작 확인

![image](https://user-images.githubusercontent.com/82796103/123214131-cc0cb300-d501-11eb-9840-2bed6a2e5871.png)


#### 4.3. ConfigMap 적용
> 시스템별로 변경 가능성이 있는 설정들을 ConfigMap을 사용하여 관리한다.

  * application.yml 파일에 ${app.feignclient.url.kitchen} 설정
![image](https://user-images.githubusercontent.com/82796103/123221282-bf8c5880-d509-11eb-89a2-aa08a02b73d5.png)

    - CookService.java 에 hall 서비스가 kitchen 서비스 호출시 호출할 URL 설정
```java
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
```

  * deployment.yml 설정

![image](https://user-images.githubusercontent.com/82796103/123225331-9372d680-d50d-11eb-8fa1-6bbec0e59faf.png)

  * configMap 생성
```
kubectl create configmap configmap-url --from-literal=CONFIGMAP_KITCHEN_URL=http://kitchen:8080 -n choi
```
![image](https://user-images.githubusercontent.com/82796103/123225514-c4530b80-d50d-11eb-889a-02a84b142acc.png)

#### 4.4. Autoscale (HPA)
> Autoscale 설정 후 siege를 통하여 pod 내부로 접속하여 부하를 발생시켜 검증하였습니다.

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
![image](https://user-images.githubusercontent.com/82796103/123234854-48a98c80-d516-11eb-8665-5fe7088ce0d6.png)

  - Auto Scale 설정
```sh
	kubectl autoscale deployment hall --cpu-percent=20 --min=1 --max=3 -n choi
```
![image](https://user-images.githubusercontent.com/82796103/123236409-bbffce00-d517-11eb-9ab6-41df92dd42d0.png)

* Auto Scale Out 확인

  - 부하 시작 (siege) : 동시접속 200명, 120초 동안 
```sh
kubectl exec -it pod/siege-d484db9c-6f6h2 -c siege -n choi -- /bin/bash
siege -c200 -t120S -v --content-type "application/json" 'http://hall:8080/orders POST {"employeeCardNo": "1", "menuname":"불고기덮밥", "menuname":"5000"}'
```
  - Scale out / in

![image](https://user-images.githubusercontent.com/82796103/123237201-67a91e00-d518-11eb-8682-c1d0f6fc1d82.png)
![image](https://user-images.githubusercontent.com/82796103/123244095-8dd1bc80-d51e-11eb-8b17-050377b65538.png)

#### 4.5. Circuit Breaker

> 서킷 브레이킹 프레임워크의 선택 : Spring FeignClient + Hystrix 옵션을 사용하여 구현함

* Hystrix를 설정
  - 요청처리 쓰레드에서 처리시간이 1200 밀리가 넘어서기 시작하여 어느정도 유지되면 CB가 닫히도록(요청을 빠르게 실패처리, 차단) 설정
  - 동기 호출 주체인 hall 서비스에 Hystrix 설정
  - hall 서비스 application.yml 파일 내용

```sh
	feign:
	  hystrix:
		enabled: true
	hystrix:
	  command:
		default:
		  execution.isolation.thread.timeoutInMilliseconds: 1200
```

* 부하에 대한 지연시간 발생코드 kitchen서비스의 CookController.java 지연 적용

![image](https://user-images.githubusercontent.com/82796103/123238642-9a074b00-d519-11eb-9508-0cc3c87949f6.png)

* 부하 테스터 siege툴을 통한 서킷 브레이커 동작확인 : 동시 사용자 10명, 120초 동안 실시
```
kubectl exec -it pod/siege-d484db9c-6f6h2 -c siege -n choi -- /bin/bash
siege -c10 -t120S -v --content-type "application/json" 'http://hall:8080/orders POST {"employeeCardNo": "2", "menuname":"된장찌개", "menuname":"4500"}'
```

* 결과

![image](https://user-images.githubusercontent.com/82796103/123244975-5dd6e900-d51f-11eb-856e-eef4c2d32cd4.png)


#### 4.6. Self-healing (Liveness Probe)
> 정상동작 중인 hall pod의 health check port를 변경하여 강제로 재실행하도록 한 후 검증한다.

* hall 서비스 정상 확인

![image](https://user-images.githubusercontent.com/82796103/123247294-b4ddbd80-d521-11eb-8c6c-03027c050ccc.png)

* deployment.yml 에 Liveness Probe 옵션
> 강제로 health check port를 8081변경하여 오동작을 유발한다.(정상포트는 8080 임)
```sh
          livenessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8081
            initialDelaySeconds: 10
            periodSeconds: 5
```
![image](https://user-images.githubusercontent.com/82796103/123248486-f327ac80-d522-11eb-894a-eacc60e1c069.png)


* hall pod에 liveness가 적용된 부분 확인
```sh
  kubectl describe deploy hall -n choi
```

![image](https://user-images.githubusercontent.com/82796103/123248322-beb3f080-d522-11eb-8958-1538968e7917.png)

* hall 서비스 지속적 restart 발생(이미지 우측의 hall pod restats 회수 확인)

![image](https://user-images.githubusercontent.com/82796103/123249323-caec7d80-d523-11eb-8b15-18bf40aac365.png)

#### 4.7. Zero-downtime deploy (readiness probe)

> hall 서비스 배포 중 서비스가 정상동작하는 지 확인한다.

* deployment.yml 에 Readiness Probe 옵션
```sh
          readinessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8080
            initialDelaySeconds: 10
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 10
```
![image](https://user-images.githubusercontent.com/82796103/123288864-e6b74a00-d54a-11eb-8d66-021b6b339c18.png)

* Pod Describe에 Readiness 설정 확인
```
kubectl apply -f kubernetes/deployment.yml -n choi
kubectl describe pod/nginx3-7cffc5cd4d-bdbhx -n choi
```
![image](https://user-images.githubusercontent.com/82796103/123289670-9f7d8900-d54b-11eb-8df6-65027f727e95.png)

* 기존 버전과 새 버전의 pod 공존
> 배포 중 서비스 중단없이 running 완료
```
kubectl exec -it pod/siege-d484db9c-6f6h2 -c siege -n choi -- /bin/bash
siege -c1 -t10S -v --content-type "application/json" 'http://hall:8080/orders POST {"employeeCardNo": "2", "menuname":"돈까스", "menuname":"5500"}'
```
![image](https://user-images.githubusercontent.com/82796103/123291609-36971080-d54d-11eb-9879-17d440c17669.png)
