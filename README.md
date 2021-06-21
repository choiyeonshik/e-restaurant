# e-restaurant

---------------------------------------

## 체크포인트
```
  1. Saga
  2. CQRS
  3. Correlation
  4. Req/Resp
  5. Gateway
  6. Deploy/ Pipeline
  7. Circuit Breaker
  8. Autoscale (HPA)
  9. Zero-downtime deploy (Readiness Probe)
  10. Config Map/ Persistence Volume
  11. Polyglot
  12. Self-healing (Liveness Probe)
```
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

> 각 서비스의 실행방법은 아래와 같다.
>
> (포트넘버 : 8081 ~ 8084, 8088)

* 실행
```sh
    cd hall
    mvn spring-boot:run  

    cd kitchen
    mvn spring-boot:run

    cd payment
    mvn spring-boot:run 

    cd workercenter
    mvn spring-boot:run  

    cd gateway
    mvn spring-boot:run
```

* 시나리오 검증
> 시나리오 검증을 위한 스크립트는 아래와 같다.

  - 초기데이터 구축
    + UserDeposit 등록
```
http POST http://20.194.44.70:8080/userDeposits userid=1 deposit=100000
http POST http://20.194.44.70:8080/userDeposits userid=2 deposit=200000
http POST http://20.194.44.70:8080/userDeposits userid=3 deposit=200000
``` 
  - 
    + UserDeposit 확인
```
http GET http://20.194.44.70:8080/userDeposits
```
![image](https://user-images.githubusercontent.com/84724396/121111293-9309e880-c849-11eb-8e74-9263cf46e734.png)

  - 
    + Bike 등록
```
http POST http://20.194.44.70:8080/bikes bikeid=1 status=사용가능 location=분당_정자역_1구역
http POST http://20.194.44.70:8080/bikes bikeid=2 status=사용중 location=분당_정자역_1구역
http POST http://20.194.44.70:8080/bikes bikeid=3 status=불량 location=분당_정자역_1구역
``` 
  - 
    + Bike 확인
``` 
http GET http://20.194.44.70:8080/bikes
``` 
![image](https://user-images.githubusercontent.com/84724396/121111431-d1070c80-c849-11eb-9de0-7e625c5a7c42.png)

  - 자전거 대여
    + 대여(rent) 화면
```
http POST http://20.194.44.70:8080/rents userid=1 bikeid=1
```
![image](https://user-images.githubusercontent.com/84724396/121114074-0e6d9900-c84e-11eb-970c-82c39fa6350d.png)

  - 
    + 대여(rent) 후 bikes 화면 : 자전거 상태가 '사용 가능' -> '사용중' 으로 변경된다.     
```
     http GET http://20.194.44.70:8080/bikes
```
![사용중](https://user-images.githubusercontent.com/84724396/121121127-fd2a8980-c859-11eb-9955-54988c8b331e.PNG)
  - 
    + 대여(rent) 후 billings 화면 : bill이 하나 생성된다.
```
http GET http://20.194.44.70:8080/billings
```
![image](https://user-images.githubusercontent.com/84724396/121126700-901bf180-c863-11eb-9b92-22cc6d227ff4.png)


- 자전거 대여 불가 화면 (Request / Response)

     1. rent 신청를 하면 bike에서 자전거 상태를 체크하고 '사용 가능'일 때만 rent 가 성공한다.    
     http POST http://20.194.44.70:8080/rents bikeid=2 userid=2

![image](https://user-images.githubusercontent.com/84724396/121115300-e2ebae00-c84f-11eb-9266-8c05b0a3f2d3.png)


![image](https://user-images.githubusercontent.com/84724396/121115456-10d0f280-c850-11eb-9377-c33ef6e31514.png)

      2. 자전거 생태 체크를 하는 bike 서비스를 내리고 rent 신청을 하면 자전거 생태 체크를 할 수 없어 rent를 할 수 없다.

![오류1](https://user-images.githubusercontent.com/84724396/121119465-a3749000-c856-11eb-8772-f00832f5c3fd.PNG)


    위와 같이 Rent -> Bike -> Return -> Billing -> userDeposit 순으로 Sequence Flow 가 정상동작하는 것을 확인할 수 있다.
    (대여불가 자전거는 예외)

    대여 후 Status가 "사용중"으로, 반납하면 Status가 "사용가능"으로 Update 되는 것을 볼 수 있으며 반납이후 사용자의 예치금은 정산 후 차감된다.

    또한 Correlation을 key를 활용하여 userid, rentid, bikeid, billid 등 원하는 값을 서비스간의 I/F를 통하여 서비스 간에 트랜잭션이 묶여 있음을 알 수 있다.

#### 3.1. CQRS
* 대여(rent) 후 rentAndBillingView 화면(CQRS) : rent한 정보를 조회할 수 있다.
    http GET http://20.194.44.70:8080/rentAndBillingViews

![image](https://user-images.githubusercontent.com/84724396/121114171-34933900-c84e-11eb-98b6-b02faf2e5b6b.png)

    타 마이크로서비스의 데이터 원본에 접근없이(Composite 서비스나 조인SQL 등 없이) 도 조회가 가능하도록 rentAndBillingView 서비스의 CQRS를 통하여 Costomer Center 서비스를 구현하였다.
    rentAndBillingView View를 통하여 사용자가 rental한 bike 정보와 billing 정보를 조회할 수 있으며 반납 후 billing 상태를 확인할 수 있다. 


#### 3.2. Gateway
> API GateWay를 통하여 마이크로 서비스들의 집입점을 통일할 수 있다. 다음과 같이 Gateay를 적용하였다.
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
				- Path=/halls/** 
			- id: kitchen
			uri: http://localhost:8082
			predicates:
				- Path=/kitchens/** 
			- id: payment
			uri: http://localhost:8083
			predicates:
				- Path=/payments/** 
			- id: workercenter
			uri: http://localhost:8084
			predicates:
				- Path=/workercenters/** 

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
				- Path=/halls/** 
			- id: kitchen
			uri: http://kitchen:8080
			predicates:
				- Path=/kitchens/** 
			- id: payment
			uri: http://payment:8080
			predicates:
				- Path=/payments/** 
			- id: workercenter
			uri: http://workercenter:8080
			predicates:
				- Path=/workercenters/** 

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

#### 3.3. Correlation, Req/Resp

> API 호출에 대한 식별자를 정의하고, 컴포넌트 간, 그 식별자를 공유하는 하도록 서비스 컴포넌트들은 각 비즈니스 모델에 맞는 Bounded Context 라는 도메인 모델의 경계를 이루며 동작하고 있다.
>
> 컴포넌트 간 API 호출 방식은 Req/Resp와 Pub/Sub 으로 구성되어 있으며 컴포넌트간 요청과 응답을 API 별로 구분이 가능하며
> 시스템 전반에 걸쳐 일관되게 추적할 수 있다.
>
> 또한, 본 과제에서는 MSAEZ.io를 통하여 도출된 Aggregate는 Entity로 선언하여 PRE/POST PERSIST/UPDATE/DELETE 반영하였으며, Repository Pattern을 적용하여 ACID를 구현하였다.

* hall 서비스의 Hall.java
```java
	package gbike;

	import javax.persistence.*;

	import org.springframework.beans.BeanUtils;
	import gbike.external.BikeService;

	import java.util.Date;

	@Entity
	@Table(name = "Rent_table")
	public class Rent {

	    @Id
	    @GeneratedValue(strategy = GenerationType.AUTO)
	    private Long rentid;
	    private Long userid;
	    private Long bikeid;
	    private String status;
	    private Date starttime;
	    private Date endtime;
	    private String endlocation;

	    private static final String STATUS_RENTED = "rented";
	    private static final String STATUS_RETURNED = "returned";

	    @PrePersist
	    public void onPrePersist() throws Exception {
		//bike가 사용가능 상태인지 확인한다.
		boolean result = RentApplication.applicationContext.getBean(gbike.external.BikeService.class)
			.chkAndUpdateStatus(this.getBikeid());
		System.out.println("bike.chkAndUpdateStatus --------  " + result);
		if (result) {
		    //bike가 사용가능 상태이므로, rent에 저장할 값을 set 한다. 
		    this.starttime = new Date(System.currentTimeMillis());
		    this.status = STATUS_RENTED;
		    System.out.println("onPrePersist .... ");
		} else {
		    throw new Exception(" 자전거는 대여할 수 없는 상태입니다. " + this.getBikeid());
		}
	    }

	    @PostPersist
	    public void onPostPersist() {
		//Rent를 저장했으므로, Rented 이벤트를 pub 한다. 
		System.out.println("onPostPersist ....  rentid :: " + this.rentid);
		Rented rented = new Rented();
		BeanUtils.copyProperties(this, rented);
		rented.publishAfterCommit();
	    }

	    @PreUpdate
	    public void onPreUpdate() {
		//Returned로 업데이트 할 때 저장할 값을 set 한다. 
		System.out.println("onPreUpdate .... ");
		this.endtime = new Date(System.currentTimeMillis());
		this.status = STATUS_RETURNED;
	    }

	    @PostUpdate
	    public void onPostUpdate() {
		//Rent를 returned 상태로 저장했으므로, Returned 이벤트를 pub 한다. 
		System.out.println("onPostUpdate .... ");
		Returned returned = new Returned();
		BeanUtils.copyProperties(this, returned);
		returned.publishAfterCommit();
	    }

	    public Long getRentid() {
		return rentid;
	    }

	    public void setRentid(Long rentid) {
		this.rentid = rentid;
	    }

	    public String getStatus() {
		return status;
	    }

	    public void setStatus(String status) {
		this.status = status;
	    }

	    public Date getStarttime() {
		return starttime;
	    }

	    public void setStarttime(Date starttime) {
		this.starttime = starttime;
	    }

	    public Date getEndtime() {
		return endtime;
	    }

	    public void setEndtime(Date endtime) {
		this.endtime = endtime;
	    }

	    public String getEndlocation() {
		return endlocation;
	    }

	    public void setEndlocation(String endlocation) {
		this.endlocation = endlocation;
	    }

	    public Long getUserid() {
		return userid;
	    }

	    public void setUserid(Long userid) {
		this.userid = userid;
	    }

	    public Long getBikeid() {
		return bikeid;
	    }

	    public void setBikeid(Long bikeid) {
		this.bikeid = bikeid;
	    }


	}
```

* kitchen 서비스의 PolicyHandler.java
```java
	package gbike;

	import gbike.config.kafka.KafkaProcessor;
	import com.fasterxml.jackson.databind.DeserializationFeature;
	import com.fasterxml.jackson.databind.ObjectMapper;
	import org.springframework.beans.factory.annotation.Autowired;
	import org.springframework.cloud.stream.annotation.StreamListener;
	import org.springframework.messaging.handler.annotation.Payload;
	import org.springframework.stereotype.Service;

	@Service
	public class PolicyHandler{
		@Autowired BikeRepository bikeRepository;

		@StreamListener(KafkaProcessor.INPUT)
		public void wheneverReturned_UpdateStatusAndLoc(@Payload Returned returned){

			if(returned.isMe()){
				
				Bike bike = bikeRepository.findByBikeid(Long.valueOf(returned.getBikeid()));
				
				//bike.setStatus(returned.getStatus());
				bike.setStatus("사용가능");
				bike.setLocation(returned.getEndlocation());
				
				bikeRepository.save(bike);
			}
				
		}

		@StreamListener(KafkaProcessor.INPUT)
		public void whatever(@Payload String eventString){}


	}
```

#### 3.4. Polyglot 프로그래밍

> hall 서비스의 Hsql DB와 기타 bike, billing, bikeDepository 등 서비스의  H2 DB를 사용하여 폴리글랏을 구현하였다.

* hall service의 pom.xml DB 설정 코드

  ![image](https://user-images.githubusercontent.com/82796103/120737666-73ad4b80-c529-11eb-828e-f3089b929ca9.png)

* 기타 service의 pom.xml DB 설정 코드

  ![image](https://user-images.githubusercontent.com/82796103/120737496-1dd8a380-c529-11eb-907a-7a8b1a3a8bcd.png)


---------------------------------------

## 4. 운영
> 운영에 필요한 검증항목은 다음과 같다.

#### 4.1. namespace 생성
```sh
	  kubectl create ns e-restaurant
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

* Docker Image Push/deploy/서비스생성
```sh
	cd /home/project/gbike/bike
	az acr build --registry skcc1team --image skcc1team.azurecr.io/bike:latest .
	kubectl create deploy bike --image=skcc1team.azurecr.io/bike:latest -n gbike
	kubectl expose deploy bike --type=ClusterIP --port=8080 -n gbike

	cd /home/project/gbike/billing
	az acr build --registry skcc1team --image skcc1team.azurecr.io/billing:latest .
	kubectl create deploy billing --image=skcc1team.azurecr.io/billing:latest -n gbike
	kubectl expose deploy billing --type=ClusterIP --port=8080 -n gbike

	cd /home/project/gbike/rent
	az acr build --registry skcc1team --image skcc1team.azurecr.io/rent:latest .
	kubectl create deploy rent --image=skcc1team.azurecr.io/rent:latest -n gbike
	kubectl expose deploy rent --type=ClusterIP --port=8080 -n gbike

	cd /home/project/gbike/rentAndBillingView
	az acr build --registry skcc1team --image skcc1team.azurecr.io/rentandbillingview:latest .
	kubectl create deploy rentandbillingview --image=skcc1team.azurecr.io/rentandbillingview:latest -n gbike
	kubectl expose deploy rentandbillingview --type=ClusterIP --port=8080 -n gbike

	cd /home/project/gbike/userDeposit
	az acr build --registry skcc1team --image skcc1team.azurecr.io/userdeposit:latest .
	kubectl create deploy userdeposit --image=skcc1team.azurecr.io/userdeposit:latest -n gbike
	kubectl expose deploy userdeposit --type=ClusterIP --port=8080 -n gbike

	cd /home/project/gbike/gateway
	az acr build --registry skcc1team --image skcc1team.azurecr.io/gateway:latest .
	kubectl create deploy gateway --image=skcc1team.azurecr.io/gateway:latest -n gbike
	kubectl expose deploy gateway --type=LoadBalancer --port=8080 -n gbike
```

* yml파일 이용한 deploy
```sh
	cd /home/project/gbike/rent
	kubectl apply -f ./kubernetes/deployment.yml -n gbike
```

* deployment.yml 파일

![image](https://user-images.githubusercontent.com/82796103/121019311-43d89f00-c7da-11eb-8744-7c42d81baca4.png)


* Deploy 완료

![image](https://user-images.githubusercontent.com/82796103/121105067-479e0d00-c83e-11eb-93a6-4a051d7eb45f.png)



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
