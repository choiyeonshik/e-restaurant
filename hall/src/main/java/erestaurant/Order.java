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

        boolean result = HallApplication.applicationContext.getBean(erestaurant.external.CookService.class)
            .receive(cook);

            if (result) {
                this.orderid = cook.getOrderid();
                this.status = "주문완료";
                this.tagdate = new Date(System.currentTimeMillis());
            } else {
                this.status = "주문중 오류발생";
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
