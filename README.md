# e-restaurant


## 시나리오
>  사원은 구내식당에서 자신의 ID Card를 이용하여 점심 또는 저녁식사를 하고
>  비용은 급여에서 자동 차감되도록 서비스를 구현한다.

  ### 기능적 요구사항
    1. 사원은 사원증을 타각하여 음식을 주문한다.
    2. 주문한 음식을 주방에서 요리한다.
    3. 주문이 완료되면 급여에서 해당 금액을 차감한다.
    4. 요리가 완료된 음식은 사원에게 메시지를 발송한다.
    5. 사원은 주문상태를 조회할 수 있다.

  ### 비기능적 요구사항
    1. 트랜잭션
      - 급여 차감은 주문이 완료 상태이어야 한다.
    2. 장애격리
      - 급여 정산 서비스가 정상 동작하지 않더라도 주문은 계속할 수 있어야 한다.
      - 동시간대에 주문이 폭주하는 경우에 잠시 동안 주문을 받지 않고 중단한 후 다시 서비스 할 수 있다.

  ### 성능
    - Main Service의 성능에 영향이 없도록 사용자 주문상태를 조회할 수 있도록 View를 제공한다.


## 체크포인트

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

## 분석/설계

  MSAEZ 를 통하여 DDD(Domain Driven Desing)기반 설계를 완성하였습니다.

### Event Storming

* 결과: http://www.msaez.io/#/storming/prssBIL3V4WW7AwMrfdCgM5LT0e2/mine/d060c00f74a577af42b2ff4fdfd84c96


* Event 도출

![image](https://user-images.githubusercontent.com/82796103/122709738-d3ca1e80-d299-11eb-9e2d-4910082141bb.png)


* 부적격 Event 탈락

![image](https://user-images.githubusercontent.com/82796103/122709754-df1d4a00-d299-11eb-95ba-3417f791e64f.png)

* 완성된 모형

![image](https://user-images.githubusercontent.com/82796103/122709917-30c5d480-d29a-11eb-94ce-43be118641f3.png)

* 시나리오 검증

![image](https://user-images.githubusercontent.com/82796103/122719284-f4e63b80-d2a8-11eb-9d1b-4f662784e65d.png)


### Hexagonal Architecture 설계

![image](https://user-images.githubusercontent.com/82796103/122718453-ed726280-d2a7-11eb-9078-175c5b87092f.png)


