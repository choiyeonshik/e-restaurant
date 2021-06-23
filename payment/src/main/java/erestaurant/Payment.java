package erestaurant;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.Date;

@Entity
@Table(name="Payment_table")
public class Payment {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long paymentid;
    private Long orderid;
    private Long amount;
    private Long cookid;
    private Date paiddate;
    private String status;
    private String menuname;

    @PostPersist
    public void onPostPersist(){
        RegisteredPayInfo registeredPayInfo = new RegisteredPayInfo();
        BeanUtils.copyProperties(this, registeredPayInfo);
        registeredPayInfo.publishAfterCommit();

    }
    @PreUpdate
    public void onPreUpdate(){
        Paid paid = new Paid();
        BeanUtils.copyProperties(this, paid);
        paid.publishAfterCommit();

    }

    public Long getPaymentid() {
        return paymentid;
    }

    public void setPaymentid(Long paymentid) {
        this.paymentid = paymentid;
    }
    public Long getOrderid() {
        return orderid;
    }

    public void setOrderid(Long orderid) {
        this.orderid = orderid;
    }
    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }
    public Long getCookid() {
        return cookid;
    }

    public void setCookid(Long cookid) {
        this.cookid = cookid;
    }
    public Date getPaiddate() {
        return paiddate;
    }

    public void setPaiddate(Date paiddate) {
        this.paiddate = paiddate;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String getMenuname() {
        return menuname;
    }

    public void setMenuname(String menuname) {
        this.menuname = menuname;
    }




}
